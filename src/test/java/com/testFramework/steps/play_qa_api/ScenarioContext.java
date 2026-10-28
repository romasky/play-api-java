package com.testFramework.steps.play_qa_api;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

    // Name of the running scenario — set once per scenario by the single @Before in CommonSteps
    public static String scenarioName;

    // Shared across all scenarios in a test run (suffix _g)
    public static final Map<String, Object> global = new HashMap<>();

    // Isolated per scenario (suffix _l)
    public static final Map<String, Map<String, Object>> local = new HashMap<>();
}
