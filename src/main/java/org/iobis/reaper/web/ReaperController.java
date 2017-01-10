package org.iobis.reaper.web;

import org.iobis.reaper.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReaperController {

    @Autowired
    private MongoService mongoService;

    @RequestMapping("/errors")
    public Object errors() {
        return mongoService.getErrors().toArray();
    }

}
