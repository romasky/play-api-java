package com.testFramework.steps.play_qa_api;

import com.testFramework.core.Generator;
import com.testFramework.core.RestHandler;
import com.testFramework.core.TempMailHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.createUser.*;
import com.testFramework.play_qa_api.models.createUser.response.CreateUserResp;
import com.testFramework.play_qa_api.models.createUser.response.UserResp;
import com.testFramework.play_qa_api.models.createUser.response.UsersListResp;
import com.testFramework.play_qa_api.models.error.ErrorResp;
import com.testFramework.play_qa_api.models.login.LoginReq;
import com.testFramework.play_qa_api.models.login.LoginResp;
import com.testFramework.play_qa_api.models.mail.CreateMailboxResp;
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
 * Step definitions for User / Auth / Mail flows.
 * Composition over inheritance — BaseSteps is used as a helper, not a parent.
 */
public class AccountsSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();
    private final TempMailHandler mailHandler = new TempMailHandler();

    @Before
    public void before(Scenario scenario) {
        ctx.before(scenario);
    }

    // ────────────────────── Common utility steps ──────────────────────

    @Given("Save string {string} as {string}")
    public void saveString(String value, String varName) {
        ctx.save(varName, value);
    }

    @Given("Generate email and save as {string}")
    public void generateEmail(String varName) {
        ctx.save(varName, Generator.email());
    }

    @Given("Generate email with domain {string} and save as {string}")
    public void generateEmailWithDomain(String domain, String varName) {
        ctx.save(varName, Generator.email(domain));
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
                Boolean.parseBoolean(latin),
                Boolean.parseBoolean(numeric),
                false, false));
    }

    @Given("Get current date and save as {string}")
    public void saveCurrentDate(String varName) {
        ctx.save(varName, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    @When("Get and check status code {int} from {string}")
    @Step("Assert status code {expectedCode} from '{varName}'")
    public void assertStatusCode(int expectedCode, String varName) {
        ctx.assertStatusCode(expectedCode, varName);
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

    @Then("Assert {string} contains {string}")
    public void assertContains(String varName, String expected) {
        String actual = ctx.str(varName);
        Assertions.assertTrue(actual.contains(expected),
                "Expected '" + actual + "' to contain '" + expected + "'");
    }

    @Then("Assert {string} equals {string}")
    public void assertEquals(String varName, String expected) {
        Assertions.assertEquals(expected, ctx.str(varName));
    }

    @Then("Assert {string} not null")
    public void assertNotNull(String varName) {
        Object value = ctx.get(varName, true);
        Assertions.assertNotNull(value, varName + " should not be null");
        Assertions.assertNotEquals("null", String.valueOf(value), varName + " should not be null");
    }

    // ────────────────────── Auth ──────────────────────

    @Given("Login with email {string} password {string} and save token as {string}")
    @Step("Login and save token as '{tokenVar}'")
    public void loginAndSaveToken(String emailVar, String passwordVar, String tokenVar) {
        LoginReq req = LoginReq.builder()
                .email(ctx.str(emailVar))
                .password(ctx.str(passwordVar))
                .build();

        Response response = rest.post(ApiPaths.LOGIN, req);
        Assertions.assertEquals(200, response.getStatusCode(),
                "Login failed: " + response.asString());

        LoginResp resp = response.as(LoginResp.class);
        ctx.save(tokenVar, resp.getAccessToken());
    }

    @Given("Login with email {string} password {string} and save raw response as {string}")
    @Step("Login raw to '{varName}'")
    public void loginAndSaveResponse(String emailVar, String passwordVar, String varName) {
        LoginReq req = LoginReq.builder()
                .email(ctx.str(emailVar))
                .password(ctx.str(passwordVar))
                .build();
        ctx.save(varName, rest.post(ApiPaths.LOGIN, req));
    }

    @Then("Convert login response {string} to LoginResp and save as {string}")
    public void convertLoginResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(LoginResp.class));
    }

    // ────────────────────── Create user ──────────────────────

    @When("Create user with email {string} username {string} password {string} firstName {string} lastName {string} and save response as {string}")
    @Step("POST /users/create")
    public void createUser(String emailVar, String usernameVar, String passwordVar,
                           String firstNameVar, String lastNameVar, String varName) {
        CreateUserReq req = CreateUserReq.builder()
                .email(ctx.str(emailVar))
                .username(ctx.str(usernameVar))
                .password(ctx.str(passwordVar))
                .profile(ProfileReq.builder()
                        .firstName(ctx.str(firstNameVar))
                        .lastName(ctx.str(lastNameVar))
                        .build())
                .build();
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, req));
    }

    @When("Create minimal user and save response as {string}")
    @Step("Create minimal user (generated data)")
    public void createMinimalUser(String varName) {
        String email = Generator.email();
        String username = Generator.username();
        String password = Generator.password();

        CreateUserReq req = CreateUserReq.builder()
                .email(email)
                .username(username)
                .password(password)
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .build())
                .build();

        ctx.save("generatedEmail", email);
        ctx.save("generatedPassword", password);
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, req));
    }

    @When("Create full user with all fields and save response as {string}")
    @Step("Create full user with all optional fields")
    public void createFullUser(String varName) {
        CreateUserReq req = CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .gender("male")
                        .bio("Test automation framework user")
                        .build())
                .contacts(ContactsReq.builder()
                        .phone(Generator.phoneNumber())
                        .telegram("@testuser")
                        .build())
                .address(AddressReq.builder()
                        .country("US")
                        .city("San Francisco")
                        .build())
                .employment(EmploymentReq.builder()
                        .status("employed")
                        .company("Test Corp")
                        .position("QA Engineer")
                        .build())
                .settings(SettingsReq.builder()
                        .language("en")
                        .timezone("America/Los_Angeles")
                        .theme("dark")
                        .notificationsEnabled(true)
                        .build())
                .build();

        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, req));
    }

    @Then("Convert create user response {string} to CreateUserResp and save as {string}")
    public void convertCreateUserResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(CreateUserResp.class));
    }

    // ────────────────────── Get user ──────────────────────

    @When("Get user by id {string} and save response as {string}")
    @Step("GET /users/get/{idVar}")
    public void getUserById(String idVar, String varName) {
        ctx.save(varName, rest.get(ApiPaths.usersGet(ctx.str(idVar))));
    }

    @When("Get users list page {string} perPage {string} and save response as {string}")
    @Step("GET /users/list")
    public void getUsersList(String pageVar, String perPageVar, String varName) {
        ctx.save(varName, rest.get(ApiPaths.USERS_LIST,
                new String[]{"page", ctx.str(pageVar), "per_page", ctx.str(perPageVar)}));
    }

    @Then("Convert get user response {string} to UserResp and save as {string}")
    public void convertUserResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(UserResp.class));
    }

    @Then("Convert users list response {string} to UsersListResp and save as {string}")
    public void convertUsersListResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(UsersListResp.class));
    }

    // ────────────────────── Check exists ──────────────────────

    @When("Check user exists by id {string} and save response as {string}")
    @Step("HEAD /users/exists/{idVar}")
    public void checkUserExists(String idVar, String varName) {
        ctx.save(varName, rest.head(ApiPaths.usersExists(ctx.str(idVar))));
    }

    @Then("Assert user exists header is {string} in response {string}")
    @Step("Assert X-User-Exists = '{expected}'")
    public void assertUserExistsHeader(String expected, String varName) {
        Response response = (Response) ctx.get(varName, true);
        String header = response.getHeader("X-User-Exists");
        Assertions.assertEquals(expected, header, "X-User-Exists header mismatch");
    }

    // ────────────────────── Update ──────────────────────

    @When("Update user {string} token {string} firstName {string} lastName {string} email {string} username {string} and save response as {string}")
    @Step("PUT /users/update/{idVar}")
    public void updateUser(String idVar, String tokenVar,
                           String firstNameVar, String lastNameVar,
                           String emailVar, String usernameVar, String varName) {
        CreateUserReq req = CreateUserReq.builder()
                .email(ctx.str(emailVar))
                .username(ctx.str(usernameVar))
                .profile(ProfileReq.builder()
                        .firstName(ctx.str(firstNameVar))
                        .lastName(ctx.str(lastNameVar))
                        .build())
                .build();

        ctx.save(varName, rest.put(ApiPaths.usersUpdate(ctx.str(idVar)), req,
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Patch user {string} token {string} firstName {string} and save response as {string}")
    @Step("PATCH /users/patch/{idVar}")
    public void patchUser(String idVar, String tokenVar, String firstNameVar, String varName) {
        // API requires at minimum first_name + last_name when profile object is present
        CreateUserReq req = CreateUserReq.builder()
                .profile(ProfileReq.builder()
                        .firstName(ctx.str(firstNameVar))
                        .lastName("PatchedLastName")
                        .build())
                .build();

        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)), req,
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    // ────────────────────── Delete / logout ──────────────────────

    @When("Delete user {string} with token {string} and save response as {string}")
    @Step("DELETE /users/delete/{idVar}")
    public void deleteUser(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.delete(ApiPaths.usersDelete(ctx.str(idVar)),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Logout user {string} with token {string} and save response as {string}")
    @Step("POST /users/logout/{idVar}")
    public void logoutUser(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.postNoBody(ApiPaths.usersLogout(ctx.str(idVar)),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    // ────────────────────── Error assertions ──────────────────────

    @Then("Convert error response {string} to ErrorResp and save as {string}")
    public void convertErrorResponse(String rawVar, String saveAs) {
        Response resp = (Response) ctx.get(rawVar, true);
        ctx.save(saveAs, resp.as(ErrorResp.class));
    }

    @Then("Assert error code is {string} in {string}")
    @Step("Assert error code = '{expected}'")
    public void assertErrorCode(String expected, String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertEquals(expected, error.getError().getCode(),
                "Error code mismatch. Full: " + error);
    }

    @Then("Assert error message contains {string} in {string}")
    @Step("Assert error message contains '{expected}'")
    public void assertErrorMessageContains(String expected, String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertTrue(error.getError().getMessage().contains(expected),
                "Error message '" + error.getError().getMessage() + "' does not contain '" + expected + "'");
    }

    // ────────────────────── DTO field extraction ──────────────────────

    @Then("Assert CreateUserResp {string} has email {string}")
    public void assertCreateUserRespEmail(String varName, String expected) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertEquals(ctx.str(expected), resp.getEmail());
    }

    @Then("Assert CreateUserResp {string} has username {string}")
    public void assertCreateUserRespUsername(String varName, String expected) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertEquals(ctx.str(expected), resp.getUsername());
    }

    @Then("Assert CreateUserResp {string} has access token not null")
    public void assertCreateUserRespHasToken(String varName) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getAccessToken(), "access_token should not be null");
        Assertions.assertFalse(resp.getAccessToken().isBlank(), "access_token should not be blank");
    }

    @Then("Save field id from CreateUserResp {string} as {string}")
    public void saveIdFromCreateUserResp(String varName, String saveAs) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        ctx.save(saveAs, resp.getId());
    }

    @Then("Save field accessToken from CreateUserResp {string} as {string}")
    public void saveTokenFromCreateUserResp(String varName, String saveAs) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        ctx.save(saveAs, resp.getAccessToken());
    }

    @Then("Save field userId from LoginResp {string} as {string}")
    public void saveUserIdFromLoginResp(String varName, String saveAs) {
        LoginResp resp = (LoginResp) ctx.get(varName, true);
        ctx.save(saveAs, resp.getUserId());
    }

    // ────────────────────── Temp mail ──────────────────────

    @Given("Create temp mailbox and save token as {string} email as {string}")
    @Step("Create temp mailbox")
    public void createMailboxAndSave(String tokenVar, String emailVar) {
        CreateMailboxResp mailbox = mailHandler.createMailbox();
        ctx.save(tokenVar, mailbox.getToken());
        ctx.save(emailVar, mailbox.getEmailAddress());
    }

    @Then("Assert mail received in mailbox {string} within {int} seconds and save body as {string}")
    @Step("Wait for email in mailbox '{mailTokenVar}'")
    public void waitForMailAndSaveBody(String mailTokenVar, int timeoutSec, String bodyVar) {
        String token = ctx.str(mailTokenVar);
        var message = mailHandler.waitForMessage(token, timeoutSec);
        ctx.save(bodyVar, message.getBody() != null ? message.getBody() : message.getBodyPreview());
    }
}
