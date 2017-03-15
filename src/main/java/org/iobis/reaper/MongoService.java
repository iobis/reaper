package org.iobis.reaper;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.iobis.reaper.model.Archive;
import org.iobis.reaper.model.Dataset;
import org.iobis.reaper.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MongoService {

    private Logger logger = LoggerFactory.getLogger(MongoService.class);

    private static String ERRORS_COLLECTION = "errors";
    private static String FEEDS_COLLECTION = "feeds";
    private static String DATASETS_COLLECTION = "datasets";
    private static String LOG_COLLECTION = "log";
    private static String ARCHIVE_COLLECTION = "archive";

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

    public List<DBObject> getErrors(Integer limit) {
        DBCursor cursor = db.getCollection(ERRORS_COLLECTION).find();
        cursor.sort(new BasicDBObject("date", -1)).limit(limit);
        return cursor.toArray();
    }

    public List<DBObject> getDatasets() {
        DBCursor cursor = db.getCollection(DATASETS_COLLECTION).find();
        cursor.sort(new BasicDBObject("url", 1));
        return cursor.toArray();
    }

    public Dataset getDataset(Dataset dataset) {

        // find dataset by feed id and dataset short name
        BasicDBObject query = new BasicDBObject();
        query.append("feed", dataset.getFeed().getId());
        query.append("name", dataset.getName());

        Dataset result = null;
        DBObject o = db.getCollection(DATASETS_COLLECTION).findOne(query);

        if (o != null) {
            result = new Dataset();
            result.setFeed(dataset.getFeed());
            if (o.containsField("_id")) result.setId((String) o.get("id"));
            if (o.containsField("url")) result.setUrl((String) o.get("url"));
            if (o.containsField("title")) result.setTitle((String) o.get("title"));
            if (o.containsField("name")) result.setName((String) o.get("name"));
            if (o.containsField("description")) result.setDescription((String) o.get("description"));
            if (o.containsField("eml")) result.setEml((String) o.get("eml"));
            if (o.containsField("dwca")) result.setDwca((String) o.get("dwca"));
            if (o.containsField("published")) result.setPublished((Date) o.get("published"));
            if (o.containsField("updated")) result.setUpdated((Date) o.get("updated"));
        }

        return result;
    }

    public void saveDataset(Dataset dataset) {
        DBObject o = new BasicDBObject();
        o.put("_id", dataset.getId());
        o.put("url", dataset.getUrl());
        o.put("title", dataset.getTitle());
        o.put("name", dataset.getName());
        o.put("description", dataset.getDescription());
        o.put("eml", dataset.getEml());
        o.put("dwca", dataset.getDwca());
        o.put("published", dataset.getPublished());
        o.put("updated", dataset.getUpdated());
        o.put("feed", dataset.getFeed().getId());
        db.getCollection(DATASETS_COLLECTION).save(o);
    }

    public void saveLog(String message, String feed, String url) {
        DBObject log = new BasicDBObject();
        log.put("_id", Util.generateId());
        log.put("feed", feed);
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(LOG_COLLECTION).save(log);
    }

    public void saveError(String message, String feed, String url) {
        DBObject log = new BasicDBObject();
        log.put("_id", Util.generateId());
        log.put("feed", feed);
        log.put("message", message);
        log.put("url", url);
        log.put("date", new Date());
        db.getCollection(ERRORS_COLLECTION).save(log);
    }

    public GridFSDBFile getArchive(String id) {
        GridFS fs = new GridFS(db, ARCHIVE_COLLECTION);
        return fs.findOne(new BasicDBObject("_id", id));
    }

    public void deleteArchive(String id) {
        GridFS fs = new GridFS(db, ARCHIVE_COLLECTION);
        GridFSDBFile file = fs.findOne(new BasicDBObject("_id", id));
        if (file != null) {
            fs.remove(file);
        }
    }

    public GridFSInputFile saveArchive(Archive archive) {
        logger.debug("Saving archive " + archive.getId() + " " + archive.getUrl());
        try {
            GridFS fs = new GridFS(db, ARCHIVE_COLLECTION);
            InputStream is = new URL(archive.getUrl()).openStream();
            GridFSInputFile file = fs.createFile(is);
            file.setFilename(archive.getId());
            file.save();
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
