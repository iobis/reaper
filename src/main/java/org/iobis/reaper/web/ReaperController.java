package org.iobis.reaper.web;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.iobis.reaper.service.ArchiveService;
import org.iobis.reaper.service.FeedService;
import org.iobis.reaper.service.LogService;
import org.iobis.reaper.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class ReaperController {

    private Logger logger = LoggerFactory.getLogger(ReaperController.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private LogService logService;

    @Autowired
    private ArchiveService archiveService;

    @RequestMapping("/errors")
    public List<DBObject> errors() {
        return logService.getErrors(100);
    }

    @RequestMapping("/log")
    public List<DBObject> log() {
        return logService.getLog(100);
    }

    @RequestMapping("/archive/{id}")
    public void archive(HttpServletResponse response, @PathVariable String id) {
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + id + ".zip");
            GridFSDBFile file = archiveService.getArchive(id);
            file.writeTo(response.getOutputStream());
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
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
