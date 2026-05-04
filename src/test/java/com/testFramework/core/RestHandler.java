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
        return buildPostSpec(path, body, headers);
    }

    private Response buildPostSpec(String path, Object body, String... headers) {
        RequestSpecification spec = buildSpec(headers).contentType(ContentType.JSON);
        if (body != null && !body.toString().isEmpty()) spec.body(body);
        return spec.post(path).then().extract().response();
    }

    @Step("POST {path} (no body)")
    public Response postNoBody(String path, String... headers) {
        return buildSpec(headers).post(path).then().extract().response();
    }

    @Step("GET {path}")
    public Response get(String path, String... headers) {
        return buildSpec(headers).get(path).then().extract().response();
    }

    @Step("GET {path} with query params")
    public Response get(String path, String[] queryParams, String... headers) {
        return buildGetWithParams(path, queryParams, headers);
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
        return buildSpec(headers).contentType(ContentType.JSON).body(body).put(path).then().extract().response();
    }

    @Step("PATCH {path}")
    public Response patch(String path, Object body, String... headers) {
        return buildSpec(headers).contentType(ContentType.JSON).body(body).patch(path).then().extract().response();
    }

    @Step("DELETE {path}")
    public Response delete(String path, String... headers) {
        return buildSpec(headers).delete(path).then().extract().response();
    }

    @Step("HEAD {path}")
    public Response head(String path) {
        return given().head(path).then().extract().response();
    }

    @Step("OPTIONS {path}")
    public Response options(String path) {
        return given().options(path).then().extract().response();
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
