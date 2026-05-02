package com.testFramework.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyHandler {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/config.properties"))) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public String getProperty(String key) {
        int systemPriority = Integer.parseInt(props.getProperty("systemVariablesPropertiesPriority", "2"));
        int filePriority = Integer.parseInt(props.getProperty("configFilePropertiesPriority", "1"));

        String fromSystem = System.getProperty(key);
        String fromFile = props.getProperty(key);

        String result;
        if (systemPriority < filePriority) {
            result = fromSystem != null ? fromSystem : fromFile;
        } else {
            result = fromFile != null ? fromFile : fromSystem;
        }

        if (result == null) {
            throw new IllegalArgumentException("Property '" + key + "' not found in config.properties or system properties");
        }
        return result;
    }

    public static String get(String key) {
        return new PropertyHandler().getProperty(key);
    }
}
