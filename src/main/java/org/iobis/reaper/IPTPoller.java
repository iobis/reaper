package org.iobis.reaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IPTPoller {

    private Logger logger = LoggerFactory.getLogger(IPTPoller.class);

    @Scheduled(fixedDelay = 1000)
    public void poll() {

        logger.info("Test");

    }

}
