package com.testFramework.core;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RestHandler {

    private final String baseUrl;

    public RestHandler() {
        this.baseUrl = PropertyHandler.get("baseUrl");
        RestAssured.baseURI = this.baseUrl;
    }

    @Step("POST {path}")
    public Response post(String path, Object body, String... headers) {
        if (path.contains("/users/create")) throttleCreateUser();
        if (path.contains("/login")) throttleLogin();
        Response r = buildPostSpec(path, body, headers);
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildPostSpec(path, body, headers);
        }
        return r;
    }

    private Response buildPostSpec(String path, Object body, String... headers) {
        RequestSpecification spec = buildSpec(headers).contentType(ContentType.JSON);
        if (body != null && !body.toString().isEmpty()) spec.body(body);
        return spec.post(path).then().extract().response();
    }

    private boolean isNginx404(Response r) {
        return r.getStatusCode() == 404 && r.asString().contains("nginx");
    }

    private void sleepForRetry(int attemptNumber) {
        long waitMs = 10000L * attemptNumber;
        System.out.printf("[RestHandler] Rate limit 404 hit (attempt %d) — waiting %ds and retrying...%n",
                attemptNumber, waitMs / 1000);
        try { Thread.sleep(waitMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Step("POST {path} (no body)")
    public Response postNoBody(String path, String... headers) {
        Response r = buildSpec(headers).post(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildSpec(headers).post(path).then().extract().response();
        }
        return r;
    }

    @Step("GET {path}")
    public Response get(String path, String... headers) {
        Response r = buildSpec(headers).get(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildSpec(headers).get(path).then().extract().response();
        }
        return r;
    }

    @Step("GET {path} with query params")
    public Response get(String path, String[] queryParams, String... headers) {
        Response r = buildGetWithParams(path, queryParams, headers);
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildGetWithParams(path, queryParams, headers);
        }
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
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildSpec(headers).contentType(ContentType.JSON).body(body).put(path).then().extract().response();
        }
        return r;
    }

    @Step("PATCH {path}")
    public Response patch(String path, Object body, String... headers) {
        Response r = buildSpec(headers).contentType(ContentType.JSON).body(body).patch(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildSpec(headers).contentType(ContentType.JSON).body(body).patch(path).then().extract().response();
        }
        return r;
    }

    @Step("DELETE {path}")
    public Response delete(String path, String... headers) {
        Response r = buildSpec(headers).delete(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = buildSpec(headers).delete(path).then().extract().response();
        }
        return r;
    }

    @Step("HEAD {path}")
    public Response head(String path) {
        Response r = given().head(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = given().head(path).then().extract().response();
        }
        return r;
    }

    @Step("OPTIONS {path}")
    public Response options(String path) {
        Response r = given().options(path).then().extract().response();
        for (int attempt = 1; isNginx404(r) && attempt <= 3; attempt++) {
            sleepForRetry(attempt);
            r = given().options(path).then().extract().response();
        }
        return r;
    }

    private RequestSpecification buildSpec(String... headers) {
        RequestSpecification spec = given();
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

    /** Respect the server rate limit of ~100 user creations/min (1 per 600ms). */
    private static volatile long lastCreateRequestMs = 0;
    private static final long CREATE_USER_DELAY_MS = 700;

    /** Respect login rate limit of 5 req/min (1 per 12s). */
    private static volatile long lastLoginRequestMs = 0;
    private static final long LOGIN_DELAY_MS = 13000;

    private void throttleLogin() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastLoginRequestMs;
        if (elapsed < LOGIN_DELAY_MS) {
            try {
                Thread.sleep(LOGIN_DELAY_MS - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastLoginRequestMs = System.currentTimeMillis();
    }

    private void throttleCreateUser() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastCreateRequestMs;
        if (elapsed < CREATE_USER_DELAY_MS) {
            try {
                Thread.sleep(CREATE_USER_DELAY_MS - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastCreateRequestMs = System.currentTimeMillis();
    }
}
