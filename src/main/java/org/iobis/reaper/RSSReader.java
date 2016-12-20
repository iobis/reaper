package org.iobis.reaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSReader {

    private Logger logger = LoggerFactory.getLogger(RSSReader.class);

    private URL url;

    public RSSReader(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the IPT RSS feed.
     *
     * @return a list of IPT resources
     */
    public List<IPTResource> read() {
        List<IPTResource> resources = new ArrayList<IPTResource>();

        try {
            InputStream is = url.openStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList items = doc.getElementsByTagName("item");
            logger.debug("Found " + items.getLength() + " datasets in feed " + url);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resources;
    }

}
