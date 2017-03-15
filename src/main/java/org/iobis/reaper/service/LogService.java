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
import java.util.Date;
import java.util.List;

@Service
public class LogService {

    private static String LOG_COLLECTION = "log";
    private static String ERRORS_COLLECTION = "errors";

    private Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private MongoClient mongoClient;

    private DB db;

    @PostConstruct
    private void init() {
        db = mongoClient.getDB("reaper");
    }

    public List<DBObject> getErrors(Integer limit) {
        DBCursor cursor = db.getCollection(ERRORS_COLLECTION).find();
        cursor.sort(new BasicDBObject("date", -1)).limit(limit);
        return cursor.toArray();
    }

    public List<DBObject> getLog(Integer limit) {
        DBCursor cursor = db.getCollection(LOG_COLLECTION).find();
        cursor.sort(new BasicDBObject("date", -1)).limit(limit);
        return cursor.toArray();
    }

    public void saveLog(String message, String feed, String url) {
        DBObject log = new BasicDBObject();
        log.put("_id", Util.generateId());
        log.put("feed", feed);
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(LOG_COLLECTION).save(log);
        logger.debug(message + " - " + url);
    }

    public void saveError(String message, String feed, String url) {
        DBObject log = new BasicDBObject();
        log.put("_id", Util.generateId());
        log.put("feed", feed);
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(ERRORS_COLLECTION).save(log);
        logger.error(message + " - " + url);
    }

}