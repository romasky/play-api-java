package com.testFramework.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins how a config value is resolved: the source with the HIGHER priority number wins,
 * falling back to the other source; defaults are system=2, file=1.
 */
class PropertyHandlerTest {

    private static final String KEY = "propertyHandlerTest.key";

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty(KEY);
    }

    @Test
    void systemPropertyWinsByDefault() {
        System.setProperty(KEY, "from-system");

        assertEquals("from-system", PropertyHandler.resolve(file("from-file"), KEY));
    }

    @Test
    void filePropertyWinsWhenFilePriorityIsHigher() {
        System.setProperty(KEY, "from-system");
        Properties file = file("from-file");
        file.setProperty("configFilePropertiesPriority", "3");

        assertEquals("from-file", PropertyHandler.resolve(file, KEY));
    }

    @Test
    void filePropertyWinsOnEqualPriority() {
        System.setProperty(KEY, "from-system");
        Properties file = file("from-file");
        file.setProperty("systemVariablesPropertiesPriority", "1");

        assertEquals("from-file", PropertyHandler.resolve(file, KEY));
    }

    @Test
    void fallsBackToFileWhenSystemPropertyMissing() {
        assertEquals("from-file", PropertyHandler.resolve(file("from-file"), KEY));
    }

    @Test
    void fallsBackToSystemWhenFilePropertyMissingAndFileWins() {
        System.setProperty(KEY, "from-system");
        Properties file = new Properties();
        file.setProperty("configFilePropertiesPriority", "3");

        assertEquals("from-system", PropertyHandler.resolve(file, KEY));
    }

    @Test
    void missingEverywhereNamesTheKey() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> PropertyHandler.resolve(new Properties(), KEY));

        assertEquals("Property '" + KEY + "' not found in config.properties or system properties", e.getMessage());
    }

    @Test
    void realConfigFileIsLoaded() {
        assertEquals("20000", PropertyHandler.get("connectionTimeout"));
    }

    private static Properties file(String value) {
        Properties file = new Properties();
        file.setProperty(KEY, value);
        return file;
    }
}
