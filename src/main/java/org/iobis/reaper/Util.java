package org.iobis.reaper;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static String generateId() {
        return (new ObjectId()).toHexString();
    }

    public static Date parseDate(String input) {
        Date result = null;
        if (input != null && !input.isEmpty()) {
            try {
                result = new SimpleDateFormat("yyyy-MM-ddThh:mm:sssZ").parse(input);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    public static String getQuery(String fileName) {
        StringBuilder result = new StringBuilder("");
        ClassLoader classLoader = Util.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("queries/" + fileName);
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is, writer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

}

