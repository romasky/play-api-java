package com.testFramework.steps.play_qa_api;

import com.testFramework.core.Generator;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generic reusable steps: data generation, context ops, common assertions.
 */
public class CommonSteps {

    private final BaseSteps ctx = new BaseSteps();

    @Before
    public void before(Scenario scenario) {
        ctx.before(scenario);
    }

    // ── Data generation ──────────────────────────────────────────────

    @Given("Save string {string} as {string}")
    public void saveString(String value, String varName) {
        ctx.save(varName, value);
    }

    @Given("Generate email and save as {string}")
    public void generateEmail(String varName) {
        ctx.save(varName, Generator.email());
    }

    @Given("Generate username and save as {string}")
    public void generateUsername(String varName) {
        ctx.save(varName, Generator.username());
    }

    @Given("Generate password and save as {string}")
    public void generatePassword(String varName) {
        ctx.save(varName, Generator.password());
    }

    @Given("Generate first name and save as {string}")
    public void generateFirstName(String varName) {
        ctx.save(varName, Generator.firstName());
    }

    @Given("Generate last name and save as {string}")
    public void generateLastName(String varName) {
        ctx.save(varName, Generator.lastName());
    }

    @Given("Generate string length {int} latin {string} numeric {string} and save as {string}")
    public void generateString(int length, String latin, String numeric, String varName) {
        ctx.save(varName, Generator.string(length, false,
                Boolean.parseBoolean(latin), Boolean.parseBoolean(numeric), false, false));
    }

    @Given("Generate string of length {int} and save as {string}")
    public void generateStringOfLength(int length, String varName) {
        ctx.save(varName, Generator.alphanumericString(length));
    }

    @Given("Get current date and save as {string}")
    public void saveCurrentDate(String varName) {
        ctx.save(varName, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    // ── Status code assertions ────────────────────────────────────────

    @When("Get and check status code {int} from {string}")
    @Step("Assert status {expectedCode} from '{varName}'")
    public void assertStatusCode(int expectedCode, String varName) {
        ctx.assertStatusCode(expectedCode, varName);
    }

    // ── Response header assertions ────────────────────────────────────

    @Then("Assert response header {string} equals {string} in {string}")
    @Step("Assert header '{header}' = '{expected}'")
    public void assertHeaderEquals(String header, String expected, String varName) {
        Response response = (Response) ctx.get(varName, true);
        String actual = response.getHeader(header);
        Assertions.assertEquals(expected, actual,
                "Header '" + header + "' mismatch. Response: " + response.asString());
    }

    @Then("Assert response header {string} contains {string} in {string}")
    @Step("Assert header '{header}' contains '{expected}'")
    public void assertHeaderContains(String header, String expected, String varName) {
        Response response = (Response) ctx.get(varName, true);
        String actual = response.getHeader(header);
        Assertions.assertNotNull(actual, "Header '" + header + "' is missing");
        Assertions.assertTrue(actual.contains(expected),
                "Header '" + header + "' value '" + actual + "' does not contain '" + expected + "'");
    }

    @Then("Assert response header {string} is present in {string}")
    @Step("Assert header '{header}' is present")
    public void assertHeaderPresent(String header, String varName) {
        Response response = (Response) ctx.get(varName, true);
        String actual = response.getHeader(header);
        Assertions.assertNotNull(actual, "Header '" + header + "' is missing in response");
        Assertions.assertFalse(actual.isBlank(), "Header '" + header + "' is blank");
    }

    // ── Response body assertions ──────────────────────────────────────

    @Then("Assert response body contains {string} in {string}")
    @Step("Assert body contains '{expected}'")
    public void assertBodyContains(String expected, String varName) {
        // Resolve the expected value from context if it was saved as a variable
        String resolved = ctx.str(expected);
        Response response = (Response) ctx.get(varName, true);
        Assertions.assertTrue(response.asString().contains(resolved),
                "Body does not contain '" + resolved + "'. Body: " + response.asString());
    }

    @Then("Assert response body does not contain {string} in {string}")
    @Step("Assert body does NOT contain '{unexpected}'")
    public void assertBodyNotContains(String unexpected, String varName) {
        Response response = (Response) ctx.get(varName, true);
        Assertions.assertFalse(response.asString().contains(unexpected),
                "Body should NOT contain '" + unexpected + "'. Body: " + response.asString());
    }

    @Then("Assert {string} not null")
    public void assertNotNull(String varName) {
        Object value = ctx.get(varName, true);
        Assertions.assertNotNull(value, varName + " should not be null");
        Assertions.assertNotEquals("null", String.valueOf(value));
    }

    @Then("Assert {string} equals {string}")
    public void assertEquals(String varName, String expected) {
        Assertions.assertEquals(expected, ctx.str(varName));
    }

    @Then("Assert {string} contains {string}")
    public void assertContains(String varName, String expected) {
        Assertions.assertTrue(ctx.str(varName).contains(expected),
                "'" + ctx.str(varName) + "' does not contain '" + expected + "'");
    }

    @Then("Assert {string} matches regex {string}")
    @Step("Assert '{varName}' matches regex")
    public void assertMatchesRegex(String varName, String regex) {
        String value = ctx.str(varName);
        Assertions.assertTrue(value.matches(regex),
                "'" + value + "' does not match regex '" + regex + "'");
    }

    @Then("Print response {string}")
    public void printResponse(String varName) {
        Object value = ctx.get(varName, true);
        if (value instanceof Response r) {
            System.out.printf("%n[%s] HTTP %d%n%s%n%n", varName, r.getStatusCode(), r.asString());
        } else {
            System.out.printf("%n[%s] = %s%n%n", varName, value);
        }
    }
}
