package com.testFramework.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Drives RestHandler against a local recording server — no live API. */
class RestHandlerTest {

    record Recorded(String method, String path, String query, String authorization, String contentType, String body) { }

    private static HttpServer server;
    private static volatile Recorded last;
    private static String previousBaseUrl;

    private RestHandler rest;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", RestHandlerTest::record);
        server.start();
        previousBaseUrl = System.getProperty("baseUrl");
        System.setProperty("baseUrl", "http://localhost:" + server.getAddress().getPort());
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
        if (previousBaseUrl == null) System.clearProperty("baseUrl");
        else System.setProperty("baseUrl", previousBaseUrl);
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

    // ── verbs ───────────────────────────────────────────────────────

    @Test
    void postSendsJsonBodyAndHeaders() {
        Response r = rest.post("/api/v1/login", Map.of("email", "a@b.c"), "Authorization", "Bearer t");

        assertEquals(200, r.getStatusCode());
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
        rest.get("/api/v1/users/list", new String[]{"page", "2", "limit", "5"}, "Authorization", "Bearer t");

        assertEquals("GET", last.method());
        assertEquals("page=2&limit=5", last.query());
        assertEquals("Bearer t", last.authorization());
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

    @Test
    void unpairedQueryParamIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> rest.get("/api/v1/users/list", new String[]{"page", "2", "limit"}));
        assertNull(last);
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
        byte[] reply = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
        boolean head = "HEAD".equals(exchange.getRequestMethod());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, head ? -1 : reply.length);
        if (!head) exchange.getResponseBody().write(reply);
        exchange.close();
    }
}
