package com.testFramework.steps.play_qa_api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden-master comparison for pin tests. A missing golden is written from the current
 * behavior and the test fails once, so a fresh golden is always reviewed before it is trusted.
 * On mismatch the actual output is dumped under target/pins/ for diffing.
 */
final class GoldenFile {

    private static final Path GOLDEN_DIR = Path.of("src", "test", "resources", "pins");
    private static final Path ACTUAL_DIR = Path.of("target", "pins");

    private GoldenFile() { }

    static void assertMatches(String fileName, String actual) {
        Path golden = GOLDEN_DIR.resolve(fileName);
        try {
            if (Files.notExists(golden)) {
                Files.createDirectories(GOLDEN_DIR);
                Files.writeString(golden, actual, StandardCharsets.UTF_8);
                fail("Golden " + golden + " was missing; wrote it from current behavior. Review it and re-run.");
            }
            String expected = Files.readString(golden, StandardCharsets.UTF_8);
            if (!expected.equals(actual)) {
                Files.createDirectories(ACTUAL_DIR);
                Files.writeString(ACTUAL_DIR.resolve(fileName), actual, StandardCharsets.UTF_8);
            }
            assertEquals(expected, actual, "Pinned behavior changed; actual written to " + ACTUAL_DIR.resolve(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
