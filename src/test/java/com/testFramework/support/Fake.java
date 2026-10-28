package com.testFramework.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;

import java.io.UncheckedIOException;

/**
 * Offline stand-ins for API responses, so assertion and convert steps can be unit-tested without
 * the network. JSON is written single-quoted for readability ({@code {'a':1}}) and becomes real
 * JSON here; a DTO is parsed through a Rest Assured {@link Response}, the same path the steps use.
 */
public final class Fake {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Fake() { }

    /** {@code {'a':'b'}} → {@code {"a":"b"}}. */
    public static String json(String singleQuoted) {
        return singleQuoted.replace('\'', '"');
    }

    /** A response with this status and raw body, plus header name/value pairs. */
    public static Response response(int status, String body, String... headerPairs) {
        ResponseBuilder builder = new ResponseBuilder().setStatusCode(status).setBody(body);
        for (int i = 0; i < headerPairs.length; i += 2) builder.setHeader(headerPairs[i], headerPairs[i + 1]);
        return builder.build();
    }

    /** A 200 {@code application/json} response carrying {@code singleQuoted} as real JSON. */
    public static Response jsonResponse(String singleQuoted) {
        return new ResponseBuilder().setStatusCode(200).setContentType("application/json")
                .setBody(json(singleQuoted)).build();
    }

    /** The DTO a step would get from a response with this JSON body. */
    public static <T> T dto(Class<T> type, String singleQuoted) {
        return jsonResponse(singleQuoted).as(type);
    }

    /** The JSON with the field at the dot-separated {@code path} removed. */
    public static String without(String singleQuoted, String path) {
        return edit(singleQuoted, path, null);
    }

    /** The JSON with the field at the dot-separated {@code path} set to {@code singleQuotedValue}. */
    public static String with(String singleQuoted, String path, String singleQuotedValue) {
        return edit(singleQuoted, path, singleQuotedValue);
    }

    private static String edit(String singleQuoted, String path, String singleQuotedValue) {
        try {
            JsonNode root = JSON.readTree(json(singleQuoted));
            String[] keys = path.split("\\.");
            JsonNode parent = root;
            for (int i = 0; i < keys.length - 1; i++) parent = parent.get(keys[i]);
            String last = keys[keys.length - 1];
            if (singleQuotedValue == null) ((ObjectNode) parent).remove(last);
            else ((ObjectNode) parent).set(last, JSON.readTree(json(singleQuotedValue)));
            return JSON.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
