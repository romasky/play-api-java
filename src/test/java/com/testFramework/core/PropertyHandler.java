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

    public static String get(String key) {
        return resolve(props, key);
    }

    /** The source with the higher priority number wins; the other one is the fallback. */
    static String resolve(Properties file, String key) {
        int systemPriority = Integer.parseInt(file.getProperty("systemVariablesPropertiesPriority", "2"));
        int filePriority = Integer.parseInt(file.getProperty("configFilePropertiesPriority", "1"));

        String fromSystem = System.getProperty(key);
        String fromFile = file.getProperty(key);

        String result = systemPriority > filePriority ? firstPresent(fromSystem, fromFile) : firstPresent(fromFile, fromSystem);
        if (result == null) {
            throw new IllegalArgumentException("Property '" + key + "' not found in config.properties or system properties");
        }
        return result;
    }

    private static String firstPresent(String preferred, String fallback) {
        return preferred != null ? preferred : fallback;
    }
}
