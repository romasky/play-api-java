package com.testFramework.core;

import io.qameta.allure.Allure;
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
        return given().head(path).then().extract().response();
    }

    @Step("OPTIONS {path}")
    public Response options(String path) {
        Response r = given().options(path).then().extract().response();
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
}
