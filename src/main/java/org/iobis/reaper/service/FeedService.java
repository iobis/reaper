package org.iobis.reaper.service;

import com.mongodb.*;
import org.iobis.reaper.Util;
import org.iobis.reaper.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService {

    private Logger logger = LoggerFactory.getLogger(FeedService.class);

    private static String FEEDS_COLLECTION = "feeds";

    @Autowired
    private MongoClient mongoClient;

    private DB db;

    @PostConstruct
    private void init() {
        db = mongoClient.getDB("reaper");

        // check if feeds are present and populate if not

        List<Feed> feeds = getFeeds();
        if (feeds == null || feeds.size() == 0) {
            String query = Util.getQuery("feeds.js");
            db.eval(query);
            logger.warn("Populated feeds collection");
        }

    }

    public List<Feed> getFeeds() {
        List<Feed> feeds = new ArrayList<Feed>();
        DBCursor cursor = db.getCollection(FEEDS_COLLECTION).find();
        while(cursor.hasNext()) {
            DBObject object = cursor.next();
            String id = (String) object.get("_id");
            String url = (String) object.get("url");
            feeds.add(new Feed(id, url));
        }
        return feeds;
    }

}
