package com.testFramework.support;

import com.testFramework.steps.play_qa_api.ScenarioContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * For offline step tests: each test runs inside scenario {@value #SCENARIO} with an empty
 * scenario context, which is cleared again afterwards ({@code @ExtendWith(StepContext.class)}).
 */
public final class StepContext implements BeforeEachCallback, AfterEachCallback {

    public static final String SCENARIO = "unit";

    @Override
    public void beforeEach(ExtensionContext context) {
        clear();
        ScenarioContext.scenarioName = SCENARIO;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        clear();
    }

    private static void clear() {
        ScenarioContext.global.clear();
        ScenarioContext.local.clear();
        ScenarioContext.scenarioName = null;
    }

    /** The step fails as an assertion (not as an NPE or cast error) with a message starting {@code prefix}. */
    public static void failsWith(String prefix, Executable step) {
        AssertionFailedError e = assertThrows(AssertionFailedError.class, step);
        assertTrue(e.getMessage().startsWith(prefix), () -> "message was: " + e.getMessage());
    }
}
