package com.testFramework.steps.play_qa_api;

import com.testFramework.core.Generator;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
        ScenarioContext.current = scenario;
        BaseSteps.renameCurrentHook("Setup: init scenario context");
    }

    // ── Data generation ──────────────────────────────────────────────

    @Given("Save string {string} as {string}")
    public void saveString(String value, String contextKey) {
        ctx.save(contextKey, value);
    }

    @Given("Generate email and save as {string}")
    public void generateEmail(String generatedEmail) {
        ctx.save(generatedEmail, Generator.email());
    }

    @Given("Generate username and save as {string}")
    public void generateUsername(String generatedUsername) {
        ctx.save(generatedUsername, Generator.username());
    }

    @Given("Generate password and save as {string}")
    public void generatePassword(String generatedPassword) {
        ctx.save(generatedPassword, Generator.password());
    }

    @Given("Generate first name and save as {string}")
    public void generateFirstName(String generatedFirstName) {
        ctx.save(generatedFirstName, Generator.firstName());
    }

    @Given("Generate last name and save as {string}")
    public void generateLastName(String generatedLastName) {
        ctx.save(generatedLastName, Generator.lastName());
    }

    @Given("Generate sender email and save as {string}")
    public void generateSenderEmail(String generatedSenderEmail) {
        ctx.save(generatedSenderEmail, Generator.senderEmail());
    }

    @Given("Generate message subject and save as {string}")
    public void generateMessageSubject(String generatedSubject) {
        ctx.save(generatedSubject, Generator.messageSubject());
    }

    @Given("Generate message body and save as {string}")
    public void generateMessageBody(String generatedBody) {
        ctx.save(generatedBody, Generator.messageBody());
    }

    @Given("Generate invalid email and save as {string}")
    public void generateInvalidEmail(String generatedInvalidEmail) {
        ctx.save(generatedInvalidEmail, Generator.invalidEmail());
    }

    @Given("Generate short password and save as {string}")
    public void generateShortPassword(String generatedShortPassword) {
        ctx.save(generatedShortPassword, Generator.shortPassword());
    }

    @Given("Generate fake mongo id and save as {string}")
    public void generateFakeMongoId(String generatedMongoId) {
        ctx.save(generatedMongoId, Generator.fakeMongoId());
    }

    @Given("Generate fake uuid and save as {string}")
    public void generateFakeUuid(String generatedUuid) {
        ctx.save(generatedUuid, Generator.fakeUuid());
    }

    @Given("Generate phone number and save as {string}")
    public void generatePhoneNumber(String generatedPhone) {
        ctx.save(generatedPhone, Generator.phoneNumber());
    }

    @Given("Generate string length {int} latin {string} numeric {string} and save as {string}")
    public void generateString(int length, String latin, String numeric, String generatedString) {
        ctx.save(generatedString, Generator.string(length, false,
                Boolean.parseBoolean(latin), Boolean.parseBoolean(numeric), false, false));
    }

    @Given("Generate string of length {int} and save as {string}")
    public void generateStringOfLength(int length, String generatedString) {
        ctx.save(generatedString, Generator.alphanumericString(length));
    }

    @Given("Get current date and save as {string}")
    public void saveCurrentDate(String currentDate) {
        ctx.save(currentDate, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    // ── Status code assertions ────────────────────────────────────────

    @When("Get and check status code {int} from {string}")
    public void assertStatusCode(int expectedCode, String responseVar) {
        ctx.assertStatusCode(expectedCode, responseVar);
    }

    // ── Response header assertions ────────────────────────────────────

    @Then("Assert response header {string} equals {string} in {string}")
    public void assertHeaderEquals(String header, String expected, String responseVar) {
        Response response = (Response) ctx.get(responseVar, true);
        String actual = response.getHeader(header);
        Assertions.assertEquals(expected, actual,
                "Header '" + header + "' mismatch. Response: " + response.asString());
    }

    @Then("Assert response header {string} contains {string} in {string}")
    public void assertHeaderContains(String header, String expected, String responseVar) {
        Response response = (Response) ctx.get(responseVar, true);
        String actual = response.getHeader(header);
        Assertions.assertNotNull(actual, "Header '" + header + "' is missing");
        Assertions.assertTrue(actual.contains(expected),
                "Header '" + header + "' value '" + actual + "' does not contain '" + expected + "'");
    }

    @Then("Assert response header {string} is present in {string}")
    public void assertHeaderPresent(String header, String responseVar) {
        Response response = (Response) ctx.get(responseVar, true);
        String actual = response.getHeader(header);
        Assertions.assertNotNull(actual, "Header '" + header + "' is missing in response");
        Assertions.assertFalse(actual.isBlank(), "Header '" + header + "' is blank");
    }

    // ── Response body assertions ──────────────────────────────────────

    @Then("Assert response body contains {string} in {string}")
    public void assertBodyContains(String expected, String responseVar) {
        String resolved = ctx.str(expected);
        Response response = (Response) ctx.get(responseVar, true);
        Assertions.assertTrue(response.asString().contains(resolved),
                "Body does not contain '" + resolved + "'. Body: " + response.asString());
    }

    @Then("Assert response body does not contain {string} in {string}")
    public void assertBodyNotContains(String unexpected, String responseVar) {
        Response response = (Response) ctx.get(responseVar, true);
        Assertions.assertFalse(response.asString().contains(unexpected),
                "Body should NOT contain '" + unexpected + "'. Body: " + response.asString());
    }

    @Then("Assert {string} not null")
    public void assertNotNull(String contextKey) {
        Object value = ctx.get(contextKey, true);
        Assertions.assertNotNull(value, contextKey + " should not be null");
        Assertions.assertNotEquals("null", String.valueOf(value));
    }

    @Then("Assert {string} equals {string}")
    public void assertEquals(String contextKey, String expected) {
        Assertions.assertEquals(expected, ctx.str(contextKey));
    }

    @Then("Assert {string} contains {string}")
    public void assertContains(String contextKey, String expected) {
        Assertions.assertTrue(ctx.str(contextKey).contains(expected),
                "'" + ctx.str(contextKey) + "' does not contain '" + expected + "'");
    }

    @Then("Assert {string} matches regex {string}")
    public void assertMatchesRegex(String contextKey, String regex) {
        String value = ctx.str(contextKey);
        Assertions.assertTrue(value.matches(regex),
                "'" + value + "' does not match regex '" + regex + "'");
    }

    @Then("Print response {string}")
    public void printResponse(String responseVar) {
        Object value = ctx.get(responseVar, true);
        if (value instanceof Response r) {
            System.out.printf("%n[%s] HTTP %d%n%s%n%n", responseVar, r.getStatusCode(), r.asString());
        } else {
            System.out.printf("%n[%s] = %s%n%n", responseVar, value);
        }
    }
}
