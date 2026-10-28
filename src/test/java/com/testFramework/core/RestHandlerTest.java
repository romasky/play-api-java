package com.testFramework.core;

import com.sun.net.httpserver.HttpExchange;
import com.testFramework.support.LocalBaseUrlServer;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.AllureResultsWriter;
import io.qameta.allure.aspects.StepsAspects;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;
import io.qameta.allure.model.WithAttachments;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/** Drives RestHandler against a local recording server — no live API. */
class RestHandlerTest {

    record Recorded(String method, String path, String query, String authorization, String contentType, String body) { }

    private static final String EMPTY_REPLY_PATH = "/no-content";

    private static LocalBaseUrlServer server;
    private static volatile Recorded last;

    private RestHandler rest;

    @BeforeAll
    static void startServer() throws IOException {
        server = LocalBaseUrlServer.start(RestHandlerTest::record);
    }

    @AfterAll
    static void stopServer() {
        server.close();
    }

    @BeforeEach
    void newHandler() {
        last = null;
        rest = new RestHandler();
    }

    // ── bearerHeader ────────────────────────────────────────────────

    @Test
    void bearerHeaderPrefixesBareToken() {
        assertEquals("Bearer abc", RestHandler.bearerHeader("abc"));
        assertEquals("Bearer ", RestHandler.bearerHeader(""));
    }

    @Test
    void bearerHeaderKeepsAlreadyPrefixedTokenOnlyOnExactPrefix() {
        assertEquals("Bearer abc", RestHandler.bearerHeader("Bearer abc"));
        assertEquals("Bearer bearer abc", RestHandler.bearerHeader("bearer abc"));
        assertEquals("Bearer Bearer", RestHandler.bearerHeader("Bearer"));
    }

    // ── authorization ───────────────────────────────────────────────

    @Test
    void authorizationIsOneVerbatimNameValuePair() {
        assertEquals("Authorization", RestHandler.AUTHORIZATION);
        assertArrayEquals(new String[]{"Authorization", "Bearer "}, RestHandler.authorization("Bearer "));
        assertArrayEquals(new String[]{"Authorization", ""}, RestHandler.authorization(""));
        assertArrayEquals(new String[]{"Authorization", " x "}, RestHandler.authorization(" x "));
    }

    @Test
    void authorizationPairIsSentAsTheAuthorizationHeader() {
        rest.delete("/u/1", RestHandler.authorization("Bearer t"));

        assertEquals("Bearer t", last.authorization());
    }

    // ── verbs ───────────────────────────────────────────────────────

    @Test
    void everyVerbReturnsTheServerResponse() {
        assertReply(rest.post("/p", Map.of("a", 1)));
        assertReply(rest.postNoBody("/p"));
        assertReply(rest.get("/g"));
        assertReply(rest.get("/g", queryOf("q", "1")));
        assertReply(rest.put("/u", Map.of("a", 1)));
        assertReply(rest.patch("/u", Map.of("a", 1)));
        assertReply(rest.delete("/u"));
        assertReply(rest.options("/o"));
        assertEquals(200, rest.head("/h").getStatusCode());
    }

    @Test
    void everyVerbExceptHeadAttachesTheResponseToAllure() {
        List<Supplier<Response>> calls = List.of(
                () -> rest.post("/p", Map.of("a", 1)),
                () -> rest.postNoBody("/p"),
                () -> rest.get("/g"),
                () -> rest.get("/g", queryOf("q", "1")),
                () -> rest.put("/u", Map.of("a", 1)),
                () -> rest.patch("/u", Map.of("a", 1)),
                () -> rest.delete("/u"),
                () -> rest.options("/o"));

        for (Supplier<Response> call : calls) {
            assertEquals(List.of("Response 200"), attachmentsDuring(call));
        }
        assertEquals(List.of(), attachmentsDuring(() -> rest.head("/h")));
    }

    /** Proves the AspectJ agent actually weaves {@code @Step}; without it the report has no request steps. */
    @Test
    void verbsAreRecordedAsAllureSteps() {
        assertEquals(List.of("GET /g"), stepNamesDuring(() -> rest.get("/g")));
    }

    @Test
    void blankResponseBodyIsNotAttached() {
        assertEquals(List.of(), attachmentsDuring(() -> rest.get(EMPTY_REPLY_PATH)));
    }

    @Test
    void postSendsJsonBodyAndHeaders() {
        rest.post("/api/v1/login", Map.of("email", "a@b.c"), "Authorization", "Bearer t");

        assertEquals(new Recorded("POST", "/api/v1/login", null, "Bearer t", "application/json", "{\"email\":\"a@b.c\"}"), last);
    }

    @Test
    void postWithNullOrEmptyBodySendsNoBody() {
        rest.post("/p", null);
        assertEquals("", last.body());

        rest.post("/p", "");
        assertEquals("", last.body());
        assertTrue(last.contentType().startsWith("application/json"));
    }

    @Test
    void postNoBodySendsNoJsonContentType() {
        rest.postNoBody("/api/v1/mail/create", "Authorization", "x");

        assertEquals("POST", last.method());
        assertEquals("x", last.authorization());
        assertEquals("", last.body());
        assertFalse(String.valueOf(last.contentType()).startsWith("application/json"));
    }

    @Test
    void getWithoutHeadersSendsNoAuthorization() {
        rest.get("/api/v1/health");

        assertEquals(new Recorded("GET", "/api/v1/health", null, null, null, ""), last);
    }

    @Test
    void getWithQueryParams() {
        rest.get("/api/v1/users/list", queryOf("page", "2", "per_page", "5"), "Authorization", "Bearer t");

        assertEquals("GET", last.method());
        assertEquals("page=2&per_page=5", last.query());
        assertEquals("Bearer t", last.authorization());
    }

    @Test
    void getWithQueryParamsAndNoHeadersStillSendsAQueryString() {
        rest.get("/api/v1/users/list", queryOf("page", "2"));

        assertEquals("page=2", last.query());
    }

    @Test
    void putAndPatchSendJsonBody() {
        rest.put("/u/1", Map.of("a", 1));
        assertEquals("PUT", last.method());
        assertEquals("{\"a\":1}", last.body());

        rest.patch("/u/1", Map.of("b", 2));
        assertEquals("PATCH", last.method());
        assertEquals("{\"b\":2}", last.body());
    }

    @Test
    void deleteHeadAndOptionsHitThePath() {
        rest.delete("/u/1", "Authorization", "Bearer t");
        assertEquals(new Recorded("DELETE", "/u/1", null, "Bearer t", null, ""), last);

        assertEquals(200, rest.head("/h").getStatusCode());
        assertEquals("HEAD", last.method());

        rest.options("/o");
        assertEquals("OPTIONS", last.method());
    }

    @Test
    void emptyAuthorizationValueIsSentAsEmptyHeaderNotDropped() {
        rest.delete("/u/1", "Authorization", "");

        assertEquals("", last.authorization());
    }

    // ── boundary: header / query arrays come in name-value pairs ───

    @Test
    void unpairedHeaderIsRejectedInsteadOfSilentlyDropped() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> rest.delete("/u/1", "Authorization"));

        assertEquals("Expected name-value pairs but got 1 element(s): [Authorization]", e.getMessage());
        assertNull(last, "no request may be sent");
    }

    private static Map<String, String> queryOf(String... nameValues) {
        Map<String, String> query = new LinkedHashMap<>();
        for (int i = 0; i < nameValues.length; i += 2) query.put(nameValues[i], nameValues[i + 1]);
        return query;
    }

    private static void assertReply(Response r) {
        assertEquals(200, r.getStatusCode());
        assertEquals("{\"ok\":true}", r.asString());
    }

    /** Names of all attachments the call made, at any step depth. */
    private static List<String> attachmentsDuring(Supplier<Response> call) {
        TestResult result = resultOf(call);
        List<String> names = new ArrayList<>();
        collectAttachments(result, result.getSteps(), names);
        return names;
    }

    private static List<String> stepNamesDuring(Supplier<Response> call) {
        return resultOf(call).getSteps().stream().map(StepResult::getName).toList();
    }

    /** Runs the call inside a throwaway Allure test case and returns what Allure recorded. */
    private static TestResult resultOf(Supplier<Response> call) {
        AllureLifecycle original = Allure.getLifecycle();
        AllureLifecycle originalSteps = StepsAspects.getLifecycle();
        List<TestResult> written = new ArrayList<>();
        AllureLifecycle capture = new AllureLifecycle(new AllureResultsWriter() {
            @Override public void write(TestResult result) { written.add(result); }
            @Override public void write(TestResultContainer container) { }
            @Override public void write(String source, InputStream attachment) { }
        });
        String uuid = UUID.randomUUID().toString();
        Allure.setLifecycle(capture);
        StepsAspects.setLifecycle(capture); // the @Step aspect keeps its own lifecycle holder
        try {
            capture.scheduleTestCase(new TestResult().setUuid(uuid));
            capture.startTestCase(uuid);
            call.get();
            capture.stopTestCase(uuid);
            capture.writeTestCase(uuid);
        } finally {
            Allure.setLifecycle(original);
            StepsAspects.setLifecycle(originalSteps);
        }
        return written.get(0);
    }

    private static void collectAttachments(WithAttachments node, List<StepResult> steps, List<String> names) {
        node.getAttachments().forEach(a -> names.add(a.getName()));
        steps.forEach(step -> collectAttachments(step, step.getSteps(), names));
    }

    private static void record(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        last = new Recorded(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                exchange.getRequestURI().getRawQuery(),
                exchange.getRequestHeaders().getFirst("Authorization"),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                body);
        if (EMPTY_REPLY_PATH.equals(exchange.getRequestURI().getPath())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        LocalBaseUrlServer.replyJson(exchange, "{\"ok\":true}");
    }
}
