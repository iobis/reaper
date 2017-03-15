package org.iobis.reaper.web;

import com.mongodb.DBObject;
import org.iobis.reaper.service.FeedService;
import org.iobis.reaper.service.LogService;
import org.iobis.reaper.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReaperController {

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private LogService logService;

    @RequestMapping("/errors")
    public List<DBObject> errors() {
        return logService.getErrors(100);
    }

    @RequestMapping("/log")
    public List<DBObject> log() {
        return logService.getLog(100);
    }

    @RequestMapping("/datasets")
    public List<DBObject> datasets() {
        return datasetService.getDatasets();
    }

    @RequestMapping("/feeds")
    public Object feeds() {
        return feedService.getFeeds().toArray();
    }

}
