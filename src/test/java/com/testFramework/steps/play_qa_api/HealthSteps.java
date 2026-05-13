package com.testFramework.steps.play_qa_api;

import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.health.HealthResp;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

public class HealthSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    @When("Get health and save response as {string}")
    public void getHealth(String varName) {
        ctx.save(varName, rest.get(ApiPaths.HEALTH));
    }

    @When("Get health with request id {string} and save response as {string}")
    public void getHealthWithRequestId(String requestId, String varName) {
        ctx.save(varName, rest.get(ApiPaths.HEALTH, "X-Request-ID", ctx.str(requestId)));
    }

    @Then("Convert health response {string} to HealthResp and save as {string}")
    public void convertHealthResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(HealthResp.class));
    }

    @Then("Assert health status is ok in {string}")
    public void assertHealthStatusOk(String varName) {
        HealthResp resp = (HealthResp) ctx.get(varName, true);
        Assertions.assertEquals("ok", resp.getStatus());
        Assertions.assertNotNull(resp.getTime(), "time field should not be null");
        Assertions.assertFalse(resp.getTime().isBlank(), "time field should not be blank");
    }
}
