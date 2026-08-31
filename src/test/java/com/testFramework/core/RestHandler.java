package com.testFramework.core;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RestHandler {

    // Apache HttpClient parameter keys (see org.apache.http.params.CoreConnectionPNames).
    // Used as string literals so we don't depend on the Apache class being on the classpath.
    private static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    private static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";

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
        Response r = buildPostSpec(path, body, headers);
        attachResponse(r);
        return r;
    }

    private Response buildPostSpec(String path, Object body, String... headers) {
        RequestSpecification spec = buildSpec(headers).contentType(ContentType.JSON);
        if (body != null && !body.toString().isEmpty()) spec.body(body);
        return spec.post(path).then().extract().response();
    }

    @Step("POST {path} (no body)")
    public Response postNoBody(String path, String... headers) {
        Response r = buildSpec(headers).post(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    @Step("GET {path}")
    public Response get(String path, String... headers) {
        Response r = buildSpec(headers).get(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    @Step("GET {path}")
    public Response get(String path, String[] queryParams, String... headers) {
        Response r = buildGetWithParams(path, queryParams, headers);
        attachResponse(r);
        return r;
    }

    private Response buildGetWithParams(String path, String[] queryParams, String... headers) {
        RequestSpecification spec = buildSpec(headers);
        for (int i = 0; i < queryParams.length - 1; i += 2) {
            spec.queryParam(queryParams[i], queryParams[i + 1]);
        }
        return spec.get(path).then().extract().response();
    }

    @Step("PUT {path}")
    public Response put(String path, Object body, String... headers) {
        Response r = buildSpec(headers).contentType(ContentType.JSON).body(body).put(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    @Step("PATCH {path}")
    public Response patch(String path, Object body, String... headers) {
        Response r = buildSpec(headers).contentType(ContentType.JSON).body(body).patch(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    @Step("DELETE {path}")
    public Response delete(String path, String... headers) {
        Response r = buildSpec(headers).delete(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    @Step("HEAD {path}")
    public Response head(String path) {
        return buildSpec().head(path).then().extract().response();
    }

    @Step("OPTIONS {path}")
    public Response options(String path) {
        Response r = buildSpec().options(path).then().extract().response();
        attachResponse(r);
        return r;
    }

    private static void attachResponse(Response r) {
        int status = r.getStatusCode();
        String body = r.getBody().asPrettyString();
        if (!body.isBlank()) {
            Allure.addAttachment("Response " + status, "application/json", body, ".json");
        }
    }

    private RequestSpecification buildSpec(String... headers) {
        RequestSpecification spec = given().config(config).baseUri(baseUrl);
        if (headers != null) {
            for (int i = 0; i < headers.length - 1; i += 2) {
                spec.header(headers[i], headers[i + 1]);
            }
        }
        return spec;
    }

    public static String bearerHeader(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
