package com.testFramework.steps.play_qa_api;

import com.testFramework.core.RestHandler;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * Pure utility base — no Cucumber annotations here.
 * Cucumber 7 forbids step class inheritance, so this holds only the context helpers.
 * The single @Before that sets ScenarioContext.scenarioName lives in CommonSteps.
 */
public class BaseSteps {

    /** A context variable name with its scope suffix parsed off: "x_g" → global, "x_l" / "x" → scenario. */
    record ContextKey(String name, boolean global) {
        static ContextKey parse(String key) {
            if (key.endsWith("_g")) return new ContextKey(stripSuffix(key), true);
            if (key.endsWith("_l")) return new ContextKey(stripSuffix(key), false);
            return new ContextKey(key, false);
        }

        private static String stripSuffix(String key) {
            return key.substring(0, key.length() - 2);
        }
    }

    protected static void renameCurrentHook(String name) {
        Allure.getLifecycle().updateFixture(r -> r.setName(name));
    }

    protected void save(String key, Object value) {
        ContextKey k = ContextKey.parse(key);
        Map<String, Object> scope = k.global()
                ? ScenarioContext.global
                : ScenarioContext.local.computeIfAbsent(ScenarioContext.scenarioName, n -> new HashMap<>());
        scope.put(k.name(), value);
    }

    /** Resolves a context variable; a missing one resolves to the key itself, so literals pass through. */
    protected Object get(String key, boolean failIfMissing) {
        ContextKey k = ContextKey.parse(key);
        Object value = lookup(k);
        if (value == null && failIfMissing) {
            Assertions.fail("Context variable '" + k.name() + "' (" + (k.global() ? "global" : "local") + ") not found.");
        }
        return value != null ? value : key;
    }

    /** Global keys read global only; scenario keys read the current scenario, then fall back to global. */
    private static Object lookup(ContextKey k) {
        Object value = k.global()
                ? null
                : ScenarioContext.local.getOrDefault(ScenarioContext.scenarioName, Map.of()).get(k.name());
        return value != null ? value : ScenarioContext.global.get(k.name());
    }

    protected Object get(String key) {
        return get(key, false);
    }

    protected String str(String key) {
        return String.valueOf(get(key));
    }

    /** The value stored under {@code key} as {@code type}; fails if it is missing, throws if it is another type. */
    protected <T> T required(String key, Class<T> type) {
        return type.cast(get(key, true));
    }

    /** The raw HTTP response stored under {@code varName}; fails if it is missing. */
    protected Response response(String varName) {
        return required(varName, Response.class);
    }

    /** Deserializes the response stored under {@code rawVar} into {@code type} and saves it as {@code saveAs}. */
    protected <T> void convertAndSave(String rawVar, Class<T> type, String saveAs) {
        save(saveAs, response(rawVar).as(type));
    }

    /** {@code Authorization: Bearer <token>} where the token is resolved from context (literal fallback). */
    protected String[] bearerAuth(String tokenVar) {
        return RestHandler.authorization(RestHandler.bearerHeader(str(tokenVar)));
    }

    protected void assertStatusCode(int expected, String varName) {
        Response response = response(varName);
        int actual = response.getStatusCode();
        Assertions.assertEquals(expected, actual,
                "Unexpected status code. Body: " + response.asString());
    }
}
