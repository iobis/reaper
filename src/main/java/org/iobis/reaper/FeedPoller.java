package org.iobis.reaper;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

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
     * Polls feeds in sources collection.
     */
    @Scheduled(fixedDelayString = "${feedpoller.delay}")
    private void poll() {

        logger.debug("Polling feeds");

        DBCollection sources = db.getCollection("sources");
        DBCursor cursor = sources.find();
        while(cursor.hasNext()) {
            DBObject object = cursor.next();
            String url = (String) object.get("url");
            try {
                checkFeed(url);
            } catch (Exception e) {
                logger.error("Failed to read feed " + url);
            }
        }

    }

    /**
     * Checks feed for updated datasets.
     *
     * @param url feed URL
     */
    @Async
    private void checkFeed(String url) {

        logger.debug("Checking feed " + url);
        List<IPTResource> resources = new RSSReader(url).read();

    }

}


