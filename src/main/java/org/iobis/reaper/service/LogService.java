package org.iobis.reaper.service;

import com.mongodb.*;
import org.iobis.reaper.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LogService {

    @Value("${mongodb.collection.log}")
    private String logCollection;

    @Value("${mongodb.collection.errors}")
    private String errorsCollection;

    private Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private DB db;

    public List<DBObject> getErrors(Integer limit) {
        DBCursor cursor = db.getCollection(errorsCollection).find();
        cursor.sort(new BasicDBObject("date", -1)).limit(limit);
        return cursor.toArray();
    }

    public List<DBObject> getLog(Integer limit) {
        DBCursor cursor = db.getCollection(logCollection).find();
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
        db.getCollection(logCollection).save(log);
        logger.debug(message + " - " + url);
    }

    public void saveError(String message, String feed, String url) {
        DBObject log = new BasicDBObject();
        log.put("_id", Util.generateId());
        log.put("feed", feed);
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(errorsCollection).save(log);
        logger.error(message + " - " + url);
    }

}
