package org.iobis.reaper;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FeedPoller {

    private Logger logger = LoggerFactory.getLogger(FeedPoller.class);

    @Autowired
    private MongoClient mongoClient;

    private DB db;

    @PostConstruct
    private void init() {
        db = mongoClient.getDB("reaper");
    }

    /**
     * Continuously polls all feeds in sources collection. Waits for all feeds to be processed
     * before continuing.
     */
    @Scheduled(fixedDelayString = "${feedpoller.delay}")
    private void poll() {

        logger.debug("Polling feeds");

        List<CompletableFuture> futures = new ArrayList<CompletableFuture>();

        DBCollection sources = db.getCollection("sources");
        DBCursor cursor = sources.find();
        while(cursor.hasNext()) {
            DBObject object = cursor.next();
            String url = (String) object.get("url");
            try {
                CompletableFuture future = checkFeed(url);
                futures.add(future);
            } catch (Exception e) {
                logger.error("Failed to read feed " + url);
            }
        }

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

        return CompletableFuture.completedFuture(url);
    }

}


