package org.iobis.reaper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private Producer<String,String> producer;

    @Value("${producer.topic}")
    private String producerTopic;

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

        GridFSInputFile newArchive = null;
        GridFSDBFile dbArchive = null;

        try {

            // get existing dataset and archive if present

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
                    newArchive = archiveService.saveArchive(new Archive(newArchiveId, dataset.getDwca()));
                    dbDataset.setFile(newArchiveId);

                    // check if dataset is new or has been updated

                    if (isArchiveUpdated(dbArchive, newArchive)) {
                        logger.debug("Dataset " + dataset.getUrl() + " is new or archive has been updated");
                        dbDataset.setUpdated(dataset.getPublished());
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

                // kafka

                if (isArchiveUpdated(dbArchive, newArchive)) {
                    producer.send(createDatesetMessage(dbDataset));
                }

            }

        } catch (Exception e) {

            logService.saveError("Error updating dataset", dataset.getFeed().getId(), dataset.getUrl());
            e.printStackTrace();

        }

    }

    private boolean isArchiveUpdated(GridFSDBFile dbArchive, GridFSInputFile newArchive) {
        return dbArchive == null || dbArchive.getMD5() != newArchive.getMD5();
    }

    private ProducerRecord<String,String> createDatesetMessage(Dataset dataset) {
        ObjectMapper objectMapper = new ObjectMapper();

        String eventJson = "";
        try {
            eventJson = objectMapper.writeValueAsString(dataset);
        } catch (JsonProcessingException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return new ProducerRecord<String, String>(producerTopic, eventJson);
    }

}


