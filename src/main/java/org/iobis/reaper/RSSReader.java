package org.iobis.reaper;

import org.iobis.reaper.model.Feed;
import org.iobis.reaper.model.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSSReader {

    private Logger logger = LoggerFactory.getLogger(RSSReader.class);

    private Feed feed;

    public RSSReader(Feed feed) {
        this.feed = feed;
    }

    /**
     * Reads the IPT RSS feed.
     *
     * @return a list of IPT resources
     */
    public List<Dataset> read() {
        List<Dataset> datasets = new ArrayList<Dataset>();

        try {
            URL url = new URL(feed.getUrl());
            InputStream is = url.openStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList items = doc.getElementsByTagName("item");
            logger.debug("Found " + items.getLength() + " datasets in feed " + url);

            for (int i = 0; i < items.getLength(); i++) {

                Dataset dataset = new Dataset();
                dataset.setFeed(feed);
                Node item = items.item(i);

                for (int j = 0; j < item.getChildNodes().getLength(); j++) {

                    Node child = item.getChildNodes().item(j);
                    String name = child.getNodeName();

                    if (name == "title") {
                        dataset.setTitle(child.getTextContent());
                    } else if (name == "description") {
                        dataset.setDescription(child.getTextContent());
                    } else if (name == "link") {
                        String datasetUrl = child.getTextContent();
                        dataset.setUrl(datasetUrl);

                        // parse dataset short name from url
                        Pattern pattern = Pattern.compile("r=(.*)");
                        Matcher matcher = pattern.matcher(datasetUrl);
                        if (matcher.find()) {
                            dataset.setName(matcher.group(1));
                        }

                    } else if (name == "ipt:eml") {
                        dataset.setEml(child.getTextContent());
                    } else if (name == "ipt:dwca") {
                        dataset.setDwca(child.getTextContent());
                    } else if (name == "pubDate") {
                        String dateString = child.getTextContent();
                        String pattern = "EEE, dd MMM yyyy HH:mm:ss Z"; // RFC 2822
                        SimpleDateFormat format = new SimpleDateFormat(pattern);
                        dataset.setPublished(format.parse(dateString));
                    }

                }

                // IPT bug
                if (dataset.getPublished() == null) {
                    continue;
                }
                datasets.add(dataset);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return datasets;
    }

}
