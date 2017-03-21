package org.iobis.reaper.service;

import com.mongodb.*;
import org.iobis.reaper.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
public class DatasetService {

    private Logger logger = LoggerFactory.getLogger(DatasetService.class);

    @Value("${mongodb.collection.datasets}")
    private String DATASETS_COLLECTION;

    @Autowired
    private DB db;

    public List<DBObject> getDatasets() {
        DBCursor cursor = db.getCollection(DATASETS_COLLECTION).find();
        cursor.sort(new BasicDBObject("url", 1));
        return cursor.toArray();
    }

    /**
     * Find dataset by feed id and dataset short name.
     */
    public Dataset getDataset(Dataset dataset) {

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
            if (o.containsField("file")) result.setFile((String) o.get("file"));
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
        o.put("file", dataset.getFile());
        db.getCollection(DATASETS_COLLECTION).save(o);
    }

}
