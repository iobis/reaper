package org.iobis.reaper;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
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
        resource.put("updated", new Date());
        db.getCollection("resources").save(resource);
    }

    public void saveLog(String message, String url) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection("log").save(log);
    }

    public void saveError(String message, String url) {
        DBObject log = new BasicDBObject();
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection("error").save(log);
    }

    public void deleteArchive(String dwca) {
        GridFS fs = new GridFS(db, "archive");
        GridFSDBFile file = fs.findOne(dwca);
        if (file != null) {
            fs.remove(file);
        }
    }

    public void saveArchive(String dwca) {
        try {
            GridFS fs = new GridFS(db, "archive");
            InputStream is = new URL(dwca).openStream();
            GridFSInputFile file = fs.createFile(is);
            file.setFilename(dwca);
            file.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
