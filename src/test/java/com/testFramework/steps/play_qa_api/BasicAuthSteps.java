package com.testFramework.steps.play_qa_api;

import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.basicauth.BasicAuthErrorResp;
import com.testFramework.play_qa_api.models.basicauth.BasicAuthResp;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.Base64;

public class BasicAuthSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    @Before
    public void before(Scenario scenario) { ctx.before(scenario); }

    @When("Send basic auth request with username {string} password {string} and save response as {string}")
    @Step("GET /auth/basic username={username}")
    public void sendBasicAuth(String username, String password, String varName) {
        String encoded = Base64.getEncoder().encodeToString(
                (ctx.str(username) + ":" + ctx.str(password)).getBytes());
        ctx.save(varName, rest.get(ApiPaths.AUTH_BASIC,
                "Authorization", "Basic " + encoded));
    }

    @When("Send basic auth request with no Authorization header and save response as {string}")
    @Step("GET /auth/basic (no auth header)")
    public void sendBasicAuthNoHeader(String varName) {
        ctx.save(varName, rest.get(ApiPaths.AUTH_BASIC));
    }

    @When("Send basic auth request with scheme {string} credentials {string} and save response as {string}")
    @Step("GET /auth/basic scheme={scheme}")
    public void sendBasicAuthWithScheme(String scheme, String credentials, String varName) {
        ctx.save(varName, rest.get(ApiPaths.AUTH_BASIC,
                "Authorization", ctx.str(scheme) + " " + ctx.str(credentials)));
    }

    @When("Send basic auth request with raw Authorization {string} and save response as {string}")
    @Step("GET /auth/basic raw header")
    public void sendBasicAuthRaw(String headerValue, String varName) {
        ctx.save(varName, rest.get(ApiPaths.AUTH_BASIC,
                "Authorization", ctx.str(headerValue)));
    }

    @Then("Convert basic auth response {string} to BasicAuthResp and save as {string}")
    public void convertBasicAuthResp(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(BasicAuthResp.class));
    }

    @Then("Convert basic auth error response {string} to BasicAuthErrorResp and save as {string}")
    public void convertBasicAuthErrorResp(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(BasicAuthErrorResp.class));
    }

    @Then("Assert basic auth success in {string}")
    @Step("Assert basic auth success=true, user=admin")
    public void assertBasicAuthSuccess(String varName) {
        BasicAuthResp resp = (BasicAuthResp) ctx.get(varName, true);
        Assertions.assertEquals(Boolean.TRUE, resp.getSuccess());
        Assertions.assertEquals("admin", resp.getUser());
        Assertions.assertNotNull(resp.getMessage());
    }

    @Then("Assert basic auth error message is {string} in {string}")
    @Step("Assert basic auth error message")
    public void assertBasicAuthErrorMessage(String expected, String varName) {
        BasicAuthErrorResp resp = (BasicAuthErrorResp) ctx.get(varName, true);
        Assertions.assertEquals(expected, resp.getMessage(),
                "Unexpected error message. Full: " + resp);
    }

    @Then("Assert basic auth error response has no field {string} in {string}")
    @Step("Assert non-standard error has no '{field}' field")
    public void assertErrorHasNoField(String field, String varName) {
        Response resp = (Response) ctx.get(varName, true);
        Assertions.assertFalse(resp.asString().contains("\"" + field + "\""),
                "Response should NOT contain field '" + field + "'. Body: " + resp.asString());
    }
}
