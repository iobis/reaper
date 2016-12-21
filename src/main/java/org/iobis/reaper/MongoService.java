package org.iobis.reaper;

import com.mongodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
public class MongoService {

    @Autowired
    private MongoClient mongoClient;

    private DB db;

    @PostConstruct
    private void init() {
        db = mongoClient.getDB("reaper");
    }

    /**
     * Gets all sources.
     *
     * @return list of sources
     */
    public DBCursor getSources() {
         return db.getCollection("sources").find();
    }

    /**
     * Gets a single resource.
     *
     * @param url source URL
     * @return source
     */
    public DBObject getResource(String url) {

        BasicDBObject query = new BasicDBObject();
        query.append("url", url);
        return db.getCollection("resources").findOne(query);

    }

    public void saveResource(DBObject resource) {
        db.getCollection("resources").save(resource);
    }

    public void saveLog(String message) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("date", new Date());
        db.getCollection("log").save(log);
    }

    public void saveLog(String message, String url) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection("log").save(log);
    }

}
