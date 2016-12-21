package org.iobis.reaper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

            for (int i = 0; i < items.getLength(); i++) {

                IPTResource resource = new IPTResource();
                Node item = items.item(i);

                for (int j = 0; j < item.getChildNodes().getLength(); j++) {

                    Node child = item.getChildNodes().item(j);
                    String name = child.getNodeName();

                    if (name == "title") {
                        resource.setTitle(child.getTextContent());
                    } else if (name == "description") {
                        resource.setDescription(child.getTextContent());
                    } else if (name == "link") {
                        resource.setUrl(child.getTextContent());
                    } else if (name == "ipt:eml") {
                        resource.setEml(child.getTextContent());
                    } else if (name == "ipt:dwca") {
                        resource.setDwca(child.getTextContent());
                    } else if (name == "pubDate") {
                        String dateString = child.getTextContent();
                        String pattern = "EEE, dd MMM yyyy HH:mm:ss Z"; // RFC 2822
                        SimpleDateFormat format = new SimpleDateFormat(pattern);
                        resource.setDate(format.parse(dateString));
                    }

                }

                // IPT bug
                if (resource.getDate() == null) {
                    continue;
                }
                resources.add(resource);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return resources;
    }

}
