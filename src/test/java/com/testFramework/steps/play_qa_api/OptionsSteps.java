package com.testFramework.steps.play_qa_api;

import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.options.UserOptionsResp;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

public class OptionsSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    @Before
    public void before(Scenario scenario) { ctx.before(scenario); }

    @When("Send OPTIONS request to users and save response as {string}")
    public void sendOptions(String varName) {
        ctx.save(varName, rest.options(ApiPaths.USERS_OPTIONS));
    }

    @Then("Convert options response {string} to UserOptionsResp and save as {string}")
    public void convertOptionsResp(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(UserOptionsResp.class));
    }

    @Then("Assert options allowed methods contains {string} in {string}")
    public void assertAllowedMethodsContains(String method, String varName) {
        UserOptionsResp resp = (UserOptionsResp) ctx.get(varName, true);
        Assertions.assertTrue(resp.getAllowedMethods().contains(method),
                "allowed_methods should contain '" + method + "'. Actual: " + resp.getAllowedMethods());
    }

    @Then("Assert options allowed methods has {int} items in {string}")
    public void assertAllowedMethodsCount(int count, String varName) {
        UserOptionsResp resp = (UserOptionsResp) ctx.get(varName, true);
        Assertions.assertEquals(count, resp.getAllowedMethods().size());
    }

    @Then("Assert options endpoints map is not empty in {string}")
    public void assertEndpointsNotEmpty(String varName) {
        UserOptionsResp resp = (UserOptionsResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getEndpoints());
        Assertions.assertFalse(resp.getEndpoints().isEmpty());
    }

    @Then("Assert options authentication type is {string} in {string}")
    public void assertAuthenticationType(String expected, String varName) {
        UserOptionsResp resp = (UserOptionsResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getAuthentication());
        Assertions.assertEquals(expected, resp.getAuthentication().getType());
    }
}
