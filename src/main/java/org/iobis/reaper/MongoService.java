package org.iobis.reaper;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
public class MongoService {

    private static String ERRORS_COLLECTION = "errors";
    private static String SOURCES_COLLECTION = "sources";
    private static String RESOURCES_COLLECTION = "resources";
    private static String LOG_COLLECTION = "resources";
    private static String ARCHIVE_COLLECTION = "archive";

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
        return db.getCollection(SOURCES_COLLECTION).find();
    }

    public DBCursor getErrors() {
        return db.getCollection(ERRORS_COLLECTION).find();
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
        return db.getCollection(RESOURCES_COLLECTION).findOne(query);
    }

    public void saveResource(DBObject resource) {
        resource.put("updated", new Date());
        db.getCollection(RESOURCES_COLLECTION).save(resource);
    }

    public void saveLog(String message, String url) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(LOG_COLLECTION).save(log);
    }

    public void saveError(String message, String url) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(ERRORS_COLLECTION).save(log);
    }

    public void deleteArchive(String dwca) {
        GridFS fs = new GridFS(db, ARCHIVE_COLLECTION);
        GridFSDBFile file = fs.findOne(dwca);
        if (file != null) {
            fs.remove(file);
        }
    }

    public void saveArchive(String dwca) {
        try {
            GridFS fs = new GridFS(db, ARCHIVE_COLLECTION);
            InputStream is = new URL(dwca).openStream();
            GridFSInputFile file = fs.createFile(is);
            file.setFilename(dwca);
            file.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
