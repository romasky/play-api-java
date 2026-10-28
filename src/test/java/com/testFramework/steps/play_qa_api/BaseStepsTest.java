package com.testFramework.steps.play_qa_api;

import com.testFramework.play_qa_api.models.health.HealthResp;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the scenario-context contract every step relies on:
 * suffix "_g" → global (whole run); "_l" or no suffix → current scenario, falling back to global.
 */
class BaseStepsTest {

    private final BaseSteps ctx = new BaseSteps();

    @BeforeEach
    void enterScenario() {
        ScenarioContext.scenarioName = "scenario A";
    }

    @AfterEach
    void clearContext() {
        ScenarioContext.global.clear();
        ScenarioContext.local.clear();
        ScenarioContext.scenarioName = null;
    }

    @Test
    void globalSuffixSavesToGlobalUnderCleanKey() {
        ctx.save("token_g", "t1");

        assertEquals("t1", ScenarioContext.global.get("token"));
        assertTrue(ScenarioContext.local.isEmpty());
    }

    @Test
    void localSuffixAndNoSuffixSaveToCurrentScenarioUnderCleanKey() {
        ctx.save("a_l", 1);
        ctx.save("b", 2);

        assertEquals(1, ScenarioContext.local.get("scenario A").get("a"));
        assertEquals(2, ScenarioContext.local.get("scenario A").get("b"));
        assertTrue(ScenarioContext.global.isEmpty());
    }

    @Test
    void globalSuffixReadsGlobalOnly() {
        ctx.save("k", "local");

        assertEquals("k_g", ctx.get("k_g"));
        ctx.save("k_g", "global");
        assertEquals("global", ctx.get("k_g"));
    }

    @Test
    void localReadPrefersScenarioValueOverGlobal() {
        ctx.save("k_g", "global");
        ctx.save("k", "local");

        assertEquals("local", ctx.get("k"));
        assertEquals("local", ctx.get("k_l"));
    }

    @Test
    void localReadFallsBackToGlobal() {
        ctx.save("k_g", "global");

        assertEquals("global", ctx.get("k"));
        assertEquals("global", ctx.get("k_l"));
    }

    @Test
    void scenariosDoNotSeeEachOthersLocals() {
        ctx.save("k", "from A");
        ScenarioContext.scenarioName = "scenario B";

        assertEquals("k", ctx.get("k"));
        ctx.save("k", "from B");
        assertEquals("from B", ctx.get("k"));
        ScenarioContext.scenarioName = "scenario A";
        assertEquals("from A", ctx.get("k"));
    }

    @Test
    void missingKeyReturnsTheRawKeyItselfSoLiteralsPassThrough() {
        assertEquals("plain literal", ctx.get("plain literal"));
        assertEquals("missing_g", ctx.get("missing_g"));
        assertEquals("missing_l", ctx.str("missing_l"));
    }

    @Test
    void nullValueIsTreatedAsMissing() {
        ctx.save("k_g", "global");
        ctx.save("k", null);

        assertEquals("global", ctx.get("k"));
    }

    @Test
    void failIfMissingNamesTheCleanKeyAndScope() {
        AssertionFailedError local = assertThrows(AssertionFailedError.class, () -> ctx.get("user_l", true));
        AssertionFailedError global = assertThrows(AssertionFailedError.class, () -> ctx.get("user_g", true));

        assertEquals("Context variable 'user' (local) not found.", local.getMessage());
        assertEquals("Context variable 'user' (global) not found.", global.getMessage());
    }

    @Test
    void failIfMissingReturnsValueWhenPresent() {
        ctx.save("k", 42);

        assertEquals(42, ctx.get("k", true));
    }

    @Test
    void requiredReturnsTheStoredValueAsTheGivenType() {
        HealthResp stored = new HealthResp();
        ctx.save("dto_g", stored);

        HealthResp actual = ctx.required("dto_g", HealthResp.class);

        assertSame(stored, actual);
    }

    @Test
    void requiredOfMissingVariableFailsWithContextMessage() {
        AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> ctx.required("nope_g", HealthResp.class));

        assertEquals("Context variable 'nope' (global) not found.", e.getMessage());
    }

    @Test
    void requiredOfWrongTypeFailsNamingBothTypes() {
        ctx.save("k", "a string");

        ClassCastException e = assertThrows(ClassCastException.class, () -> ctx.required("k", HealthResp.class));

        assertEquals("Cannot cast java.lang.String to " + HealthResp.class.getName(), e.getMessage());
    }

    @Test
    void strStringifiesStoredValue() {
        ctx.save("n", 7);

        assertEquals("7", ctx.str("n"));
    }

    @Test
    void assertStatusCodePassesOnMatch() {
        ctx.save("resp", response(201, "created"));

        assertDoesNotThrow(() -> ctx.assertStatusCode(201, "resp"));
    }

    @Test
    void assertStatusCodeFailureShowsStatusesAndBody() {
        ctx.save("resp", response(403, "{\"code\":\"FORBIDDEN\"}"));

        AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> ctx.assertStatusCode(401, "resp"));
        assertEquals("Unexpected status code. Body: {\"code\":\"FORBIDDEN\"} ==> expected: <401> but was: <403>", e.getMessage());
    }

    @Test
    void responseReturnsTheStoredResponse() {
        Response stored = response(200, "{}");
        ctx.save("resp", stored);

        assertSame(stored, ctx.response("resp"));
    }

    @Test
    void responseOfMissingVariableFailsWithContextMessage() {
        AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> ctx.response("nope"));

        assertEquals("Context variable 'nope' (local) not found.", e.getMessage());
    }

    @Test
    void convertAndSaveDeserializesTheStoredResponseIntoTheGivenType() {
        ctx.save("raw", jsonResponse("{\"status\":\"ok\",\"time\":\"t1\"}"));

        ctx.convertAndSave("raw", HealthResp.class, "dto_g");

        HealthResp dto = (HealthResp) ScenarioContext.global.get("dto");
        assertEquals("ok", dto.getStatus());
        assertEquals("t1", dto.getTime());
    }

    @Test
    void bearerAuthResolvesTheTokenVariableAndPrefixesBearer() {
        ctx.save("tok", "abc");
        ctx.save("empty", "");
        ctx.save("prefixed", "Bearer xyz");

        assertArrayEquals(new String[]{"Authorization", "Bearer abc"}, ctx.bearerAuth("tok"));
        assertArrayEquals(new String[]{"Authorization", "Bearer "}, ctx.bearerAuth("empty"));
        assertArrayEquals(new String[]{"Authorization", "Bearer xyz"}, ctx.bearerAuth("prefixed"));
        assertArrayEquals(new String[]{"Authorization", "Bearer literal"}, ctx.bearerAuth("literal"));
    }

    @Test
    void pauseNameShowsScopeAndWholeSeconds() {
        assertEquals("Rate limit pause: Login (13s)", BaseSteps.pauseName("Login", 13_000));
        assertEquals("Rate limit pause: User Management (2s)", BaseSteps.pauseName("User Management", 2_000));
    }

    @Test
    void pauseWaitsAtLeastTheGivenTime() throws InterruptedException {
        long start = System.nanoTime();

        BaseSteps.pause("Unit", 50);

        assertTrue(System.nanoTime() - start >= 50_000_000L, "pause returned before 50 ms");
    }

    private static Response jsonResponse(String body) {
        return new ResponseBuilder().setStatusCode(200).setContentType("application/json").setBody(body).build();
    }

    private static Response response(int status, String body) {
        return new ResponseBuilder().setStatusCode(status).setBody(body).build();
    }
}
