package org.iobis.reaper.service;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.iobis.reaper.model.Archive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;

@Service
public class ArchiveService {

    private static String ARCHIVE_COLLECTION = "archive";

    private Logger logger = LoggerFactory.getLogger(ArchiveService.class);

    @Autowired
    private MongoClient mongoClient;

    private DB db;

    @PostConstruct
    private void init() {
        db = mongoClient.getDB("reaper");
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
            file.put("_id", archive.getId());
            file.save();
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
