package com.testFramework.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A JDK HttpServer on a free localhost port, published as the {@code baseUrl} system property
 * (which wins over config.properties), so every RestHandler created while it runs talks to it.
 * {@link #close()} stops it and restores the previous {@code baseUrl}.
 */
public final class LocalBaseUrlServer implements AutoCloseable {

    private final HttpServer server;
    private final String previousBaseUrl;

    private LocalBaseUrlServer(HttpServer server, String previousBaseUrl) {
        this.server = server;
        this.previousBaseUrl = previousBaseUrl;
    }

    public static LocalBaseUrlServer start(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", handler);
        server.start();
        String previous = System.getProperty("baseUrl");
        System.setProperty("baseUrl", "http://localhost:" + server.getAddress().getPort());
        return new LocalBaseUrlServer(server, previous);
    }

    @Override
    public void close() {
        server.stop(0);
        if (previousBaseUrl == null) System.clearProperty("baseUrl");
        else System.setProperty("baseUrl", previousBaseUrl);
    }

    /** 200 with a JSON body; a HEAD reply carries the headers but no body. */
    public static void replyJson(HttpExchange exchange, String json) throws IOException {
        reply(exchange, 200, json);
    }

    /** {@code status} with a JSON body; a HEAD reply carries the headers but no body. */
    public static void reply(HttpExchange exchange, int status, String json) throws IOException {
        byte[] reply = json.getBytes(StandardCharsets.UTF_8);
        boolean head = "HEAD".equals(exchange.getRequestMethod());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, head ? -1 : reply.length);
        if (!head) exchange.getResponseBody().write(reply);
        exchange.close();
    }
}
