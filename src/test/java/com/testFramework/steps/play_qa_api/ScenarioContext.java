package com.testFramework.steps.play_qa_api;

import io.cucumber.java.Scenario;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

    // Current scenario — set once per scenario by the single @Before in BaseSteps
    public static Scenario current;

    // Shared across all scenarios in a test run (suffix _g)
    public static final Map<String, Object> global = new HashMap<>();

    // Isolated per scenario (suffix _l)
    public static final Map<String, Map<String, Object>> local = new HashMap<>();
}
