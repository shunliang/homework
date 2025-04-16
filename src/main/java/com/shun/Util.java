package com.shun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class Util {

    private static final Logger logger = LogManager.getLogger(Util.class);

    public static Map<String, String> readPropertiesToMap(String propertiesFileName) {
        Map<String, String> resultMap = new HashMap<>();

        try (InputStream input = App.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                logger.error("Unable to read properties file: {}", propertiesFileName);
                return resultMap;
            }

            Properties properties = new Properties();
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                resultMap.put(key, value);

                logger.debug("properties: key={}, value={}", key, value);
            }
        }
        catch (IOException e) {
            logger.debug("Exception: {}", e.getMessage());
        }

        return resultMap;
    }

    public static void printTreeMap(TreeMap<Float, Float> map) {
        /*
        for (Map.Entry<Float, Float> entry : map.entrySet()) {
            logger.debug("[ key = {}, value = {} ]", entry.getKey(), entry.getValue());
        }
        */

        Iterator<Map.Entry<Float, Float> > iterator = map.entrySet().iterator();
        Map.Entry<Float, Float> entry = null;

        while (iterator.hasNext()) {
            entry = iterator.next();

            logger.debug("entry [ {}, {} ]", entry.getKey(), entry.getValue());
            logger.debug("floorKey={}, higherKey={}, ceilingKey={}",
                    map.floorKey(entry.getKey()),
                    map.higherKey(entry.getKey()),
                    map.ceilingKey(entry.getKey()));
        }
    }
}
