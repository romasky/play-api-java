package com.testFramework.core;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.restassured.RestAssured.given;

public class RestHandler {

    // Apache HttpClient parameter keys (see org.apache.http.params.CoreConnectionPNames).
    // Used as string literals so we don't depend on the Apache class being on the classpath.
    private static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    private static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";

    public static final String AUTHORIZATION = "Authorization";

    private final String baseUrl;
    private final RestAssuredConfig config;

    public RestHandler() {
        this.baseUrl = PropertyHandler.get("baseUrl");
        int connectionTimeout = Integer.parseInt(PropertyHandler.get("connectionTimeout"));
        int socketTimeout = Integer.parseInt(PropertyHandler.get("socketTimeout"));
        this.config = RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                        .setParam(HTTP_CONNECTION_TIMEOUT, connectionTimeout)
                        .setParam(HTTP_SOCKET_TIMEOUT, socketTimeout));
    }

    @Step("POST {path}")
    public Response post(String path, Object body, String... headers) {
        RequestSpecification spec = buildSpec(headers).contentType(ContentType.JSON);
        if (body != null) spec.body(body);
        return attachResponse(spec.post(path));
    }

    @Step("POST {path} (no body)")
    public Response postNoBody(String path, String... headers) {
        return attachResponse(buildSpec(headers).post(path));
    }

    @Step("GET {path}")
    public Response get(String path, String... headers) {
        return attachResponse(buildSpec(headers).get(path));
    }

    /** Query params are a Map so they can never be mistaken for the {@code String...} headers of the overload above. */
    @Step("GET {path}")
    public Response get(String path, Map<String, String> queryParams, String... headers) {
        return attachResponse(buildSpec(headers).queryParams(queryParams).get(path));
    }

    @Step("PUT {path}")
    public Response put(String path, Object body, String... headers) {
        return attachResponse(buildSpec(headers).contentType(ContentType.JSON).body(body).put(path));
    }

    @Step("PATCH {path}")
    public Response patch(String path, Object body, String... headers) {
        return attachResponse(buildSpec(headers).contentType(ContentType.JSON).body(body).patch(path));
    }

    @Step("DELETE {path}")
    public Response delete(String path, String... headers) {
        return attachResponse(buildSpec(headers).delete(path));
    }

    /** Not attached: a HEAD response has no body, so there is nothing to show. */
    @Step("HEAD {path}")
    public Response head(String path) {
        return buildSpec().head(path);
    }

    @Step("OPTIONS {path}")
    public Response options(String path) {
        return attachResponse(buildSpec().options(path));
    }

    /** Attaches a non-blank body to the current Allure step and hands the response back. */
    private static Response attachResponse(Response r) {
        int status = r.getStatusCode();
        String body = r.getBody().asPrettyString();
        if (!body.isBlank()) {
            Allure.addAttachment("Response " + status, "application/json", body, ".json");
        }
        return r;
    }

    private RequestSpecification buildSpec(String... headers) {
        RequestSpecification spec = given().config(config).baseUri(baseUrl);
        forEachPair(headers, spec::header);
        return spec;
    }

    /** Walks a {name, value, name, value…} array; an unpaired name is a caller bug, never silently dropped. */
    private static void forEachPair(String[] nameValues, BiConsumer<String, String> action) {
        if (nameValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected name-value pairs but got " + nameValues.length
                    + " element(s): " + Arrays.toString(nameValues));
        }
        for (int i = 0; i < nameValues.length; i += 2) {
            action.accept(nameValues[i], nameValues[i + 1]);
        }
    }

    /** The {@code Authorization} header as a name-value pair for the {@code String...} headers; the value is sent verbatim. */
    public static String[] authorization(String headerValue) {
        return new String[]{AUTHORIZATION, headerValue};
    }

    public static String bearerHeader(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
