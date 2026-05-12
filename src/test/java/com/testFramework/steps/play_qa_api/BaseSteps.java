package com.testFramework.steps.play_qa_api;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

/**
 * Pure utility base — no Cucumber annotations here.
 * Cucumber 7 forbids step class inheritance, so this holds only the context helpers.
 */
public class BaseSteps {

    protected Scenario scenario;

    @Before
    public void before(Scenario scenario) {
        if (this.scenario == null) {
            this.scenario = scenario;
        }
        renameCurrentHook("Setup: init scenario context");
    }

    protected static void renameCurrentHook(String name) {
        Allure.getLifecycle().updateFixture(r -> r.setName(name));
    }

    protected void save(String key, Object value) {
        if (key.endsWith("_g")) {
            String cleanKey = key.substring(0, key.length() - 2);
            ScenarioContext.global.put(cleanKey, value);
        } else {
            String cleanKey = key.endsWith("_l") ? key.substring(0, key.length() - 2) : key;
            String scenarioName = scenario.getName();
            ScenarioContext.local
                    .computeIfAbsent(scenarioName, k -> new HashMap<>())
                    .put(cleanKey, value);
        }
    }

    protected Object get(String key, boolean failIfMissing) {
        boolean isGlobal = key.endsWith("_g");
        boolean isLocal = key.endsWith("_l");
        String cleanKey = (isGlobal || isLocal) ? key.substring(0, key.length() - 2) : key;

        Object value = null;
        if (isGlobal) {
            value = ScenarioContext.global.get(cleanKey);
        } else {
            var localMap = ScenarioContext.local.get(scenario.getName());
            if (localMap != null) {
                value = localMap.get(cleanKey);
            }
            if (value == null) {
                value = ScenarioContext.global.get(cleanKey);
            }
        }

        if (value == null && failIfMissing) {
            Assertions.fail("Context variable '" + cleanKey + "' (" + (isGlobal ? "global" : "local") + ") not found.");
        }
        return value != null ? value : key;
    }

    protected Object get(String key) {
        return get(key, false);
    }

    protected String str(String key) {
        return String.valueOf(get(key));
    }

    protected void assertStatusCode(int expected, String varName) {
        Response response = (Response) get(varName, true);
        int actual = response.getStatusCode();
        Assertions.assertEquals(expected, actual,
                "Unexpected status code. Body: " + response.asString());
    }
}
