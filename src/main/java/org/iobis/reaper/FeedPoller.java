package org.iobis.reaper;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.iobis.reaper.model.Archive;
import org.iobis.reaper.model.Feed;
import org.iobis.reaper.model.Dataset;
import org.iobis.reaper.service.ArchiveService;
import org.iobis.reaper.service.FeedService;
import org.iobis.reaper.service.LogService;
import org.iobis.reaper.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FeedPoller {

    private Logger logger = LoggerFactory.getLogger(FeedPoller.class);

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private LogService logService;

    /**
     * Continuously polls all feeds in feeds collection. Waits for all feeds to be processed
     * before continuing.
     */
    @Scheduled(fixedDelayString = "${feedpoller.delay}")
    private void poll() {

        logger.info("Polling feeds");

        List<CompletableFuture> futures = new ArrayList<CompletableFuture>();

        List<Feed> feeds = feedService.getFeeds();

        for (Feed feed : feeds) {
            try {
                CompletableFuture future = processFeed(feed);
                futures.add(future);
            } catch (Exception e) {
                logService.saveError("Failed to read feed", feed.getId(), feed.getUrl());
            }
        }

        // wait for all threads to complete
        CompletableFuture all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        all.join();

        logger.info("All feeds processed");

    }

    /**
     * Checks feed for updated datasets.
     *
     * @param feed feed
     */
    @Async
    private CompletableFuture processFeed(Feed feed) {

        logger.debug("Checking feed " + feed.getUrl());

        List<Dataset> datasets = new RSSReader(feed).read();
        for (Dataset dataset : datasets) {
            processDataset(dataset);
        }

        return CompletableFuture.completedFuture(feed.getUrl());
    }

    /**
     * Processes a dataset.
     *
     * @param dataset dataset
     */
    private void processDataset(Dataset dataset) {

        try {

            // get existing dataset and archive if present

            GridFSDBFile dbArchive = null;
            Dataset dbDataset = datasetService.getDataset(dataset);

            if (dbDataset == null) {
                dbDataset = new Dataset();
                dbDataset.setId(Util.generateId());
            } else {
                dbArchive = archiveService.getArchive(dbDataset.getFile());
            }

            // only update dataset if new or if published date later than previous one

            if (dbDataset.getPublished() == null || (dbDataset.getPublished() != null && dbDataset.getPublished().before(dataset.getPublished()))) {

                // save new archive if archive available

                if (dataset.getDwca() != null) {
                    String newArchiveId = Util.generateId();
                    GridFSInputFile newArchive = archiveService.saveArchive(new Archive(newArchiveId, dataset.getDwca()));
                    dbDataset.setFile(newArchiveId);

                    // check if dataset is new or has been updated

                    if (dbArchive == null || dbArchive.getMD5() != newArchive.getMD5()) {
                        logger.debug("Dataset " + dataset.getUrl() + " is new or archive has been updated");
                        dbDataset.setUpdated(dataset.getPublished());
                    } else {
                        //logger.debug("Dataset " + dataset.getUrl() + " has not changed");
                    }

                }

                // populate dataset

                dbDataset.setName(dataset.getName());
                dbDataset.setDwca(dataset.getDwca());
                dbDataset.setEml(dataset.getEml());
                dbDataset.setDescription(dataset.getDescription());
                dbDataset.setUrl(dataset.getUrl());
                dbDataset.setTitle(dataset.getTitle());
                dbDataset.setPublished(dataset.getPublished());
                dbDataset.setFeed(dataset.getFeed());

                // clean up old archive if present

                if (dbArchive != null) {
                    archiveService.deleteArchive(dbArchive.getFilename());
                }

                // save

                datasetService.saveDataset(dbDataset);

                logService.saveLog("Updated dataset", dataset.getFeed().getId(), dataset.getUrl());

            } else {

                //logger.debug("Dataset " + dataset.getUrl() + " has not changed");

            }

        } catch (Exception e) {

            logService.saveError("Error updating dataset", dataset.getFeed().getId(), dataset.getUrl());
            e.printStackTrace();

        }

    }

}


