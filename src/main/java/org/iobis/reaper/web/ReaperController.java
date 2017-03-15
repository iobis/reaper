package org.iobis.reaper.web;

import com.mongodb.DBObject;
import org.iobis.reaper.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReaperController {

    @Autowired
    private MongoService mongoService;

    @RequestMapping("/errors")
    public List<DBObject> errors() {
        return mongoService.getErrors(100);
    }

    @RequestMapping("/datasets")
    public List<DBObject> datasets() {
        return mongoService.getDatasets();
    }

    @RequestMapping("/feeds")
    public Object feeds() {
        return mongoService.getFeeds().toArray();
    }

}
