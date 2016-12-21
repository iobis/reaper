package org.iobis.reaper;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FeedPoller {

    private Logger logger = LoggerFactory.getLogger(FeedPoller.class);

    @Autowired
    private MongoService mongoService;

    /**
     * Continuously polls all feeds in sources collection. Waits for all feeds to be processed
     * before continuing.
     */
    @Scheduled(fixedDelayString = "${feedpoller.delay}")
    private void poll() {

        logger.debug("Polling feeds");

        List<CompletableFuture> futures = new ArrayList<CompletableFuture>();

        DBCursor cursor = mongoService.getSources();
        while(cursor.hasNext()) {
            DBObject object = cursor.next();
            String url = (String) object.get("url");
            try {
                CompletableFuture future = checkFeed(url);
                futures.add(future);
            } catch (Exception e) {
                mongoService.saveError("Failed to read feed", url);
                logger.error("Failed to read feed " + url + " - " + e.getMessage());
            }
        }

        // wait for all threads to complete
        CompletableFuture all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        all.join();

        logger.debug("All feeds processed");

    }

    /**
     * Checks feed for updated datasets.
     *
     * @param url feed URL
     */
    @Async
    private CompletableFuture checkFeed(String url) {

        logger.debug("Checking feed " + url);
        List<IPTResource> resources = new RSSReader(url).read();

        for (IPTResource resource : resources) {
            processResource(resource);
        }

        return CompletableFuture.completedFuture(url);
    }

    /**
     * Processes a feed item.
     *
     * @param resource feed item
     */
    private void processResource(IPTResource resource) {

        // todo: restrict fields?
        DBObject dbResource = mongoService.getResource(resource.getUrl());

        if (dbResource == null) {
            dbResource = new BasicDBObject();
        }

        if (!dbResource.containsField("date") || ((Date) dbResource.get("date")).before(resource.getDate())) {
            try {
                mongoService.deleteArchive(resource.getDwca());
                dbResource.put("url", resource.getUrl());
                dbResource.put("title", resource.getTitle());
                dbResource.put("description", resource.getDescription());
                dbResource.put("dwca", resource.getDwca());
                dbResource.put("eml", resource.getEml());
                dbResource.put("date", resource.getDate());
                mongoService.saveArchive(resource.getDwca());
                mongoService.saveResource(dbResource);
                mongoService.saveLog("Updated resource", resource.getUrl());
                logger.debug("Updated resource " + resource.getUrl());
            } catch (Exception e) {
                mongoService.saveError("Error updating resource", resource.getUrl());
                logger.error("Error updating resource " + resource.getUrl());
            }
        }

    }

}


