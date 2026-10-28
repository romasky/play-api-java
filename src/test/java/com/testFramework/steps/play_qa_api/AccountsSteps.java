package com.testFramework.steps.play_qa_api;

import com.testFramework.core.Generator;
import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.UserFixtures;
import com.testFramework.play_qa_api.models.createUser.*;
import com.testFramework.play_qa_api.models.createUser.response.CreateUserResp;
import com.testFramework.play_qa_api.models.createUser.response.UserResp;
import com.testFramework.play_qa_api.models.createUser.response.UsersListResp;
import com.testFramework.play_qa_api.models.error.ErrorResp;
import com.testFramework.play_qa_api.models.login.LoginReq;
import com.testFramework.play_qa_api.models.login.LoginResp;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountsSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    // Nginx create_user zone: 30r/min, burst=10. Pace to stay within the window.
    @Before("@allure.label.suite:User_Management")
    public void paceCreateUserScenarios() throws InterruptedException {
        BaseSteps.pause("User Management", 2_000);
    }

    // Go LoginRateLimiter: 5r/min. Pace login scenarios to avoid 429.
    @Before("@allure.label.subSuite:Login")
    public void paceLoginScenarios() throws InterruptedException {
        BaseSteps.pause("Login", 13_000);
    }

    // ── Login ────────────────────────────────────────────────────────

    @Given("Login with email {string} password {string} and save token as {string}")
    public void loginAndSaveToken(String emailVar, String passwordVar, String tokenVar) {
        Response response = login(emailVar, passwordVar);
        Assertions.assertEquals(200, response.getStatusCode(),
                "Login failed: " + response.asString());
        ctx.save(tokenVar, response.as(LoginResp.class).getAccessToken());
    }

    @Given("Login with email {string} password {string} and save raw response as {string}")
    public void loginAndSaveResponse(String emailVar, String passwordVar, String varName) {
        ctx.save(varName, login(emailVar, passwordVar));
    }

    private Response login(String emailVar, String passwordVar) {
        return rest.post(ApiPaths.LOGIN, LoginReq.builder()
                .email(ctx.str(emailVar))
                .password(ctx.str(passwordVar))
                .build());
    }

    @Given("Login with no body and save response as {string}")
    public void loginEmptyBody(String varName) {
        ctx.save(varName, rest.post(ApiPaths.LOGIN, "{}"));
    }

    @Then("Convert login response {string} to LoginResp and save as {string}")
    public void convertLoginResponse(String rawVar, String saveAs) {
        ctx.convertAndSave(rawVar, LoginResp.class, saveAs);
    }

    @Then("Assert login response {string} has all required fields")
    public void assertLoginResponseFields(String varName) {
        LoginResp resp = ctx.required(varName, LoginResp.class);
        Assertions.assertNotNull(resp.getAccessToken(), "access_token is null");
        Assertions.assertFalse(resp.getAccessToken().isBlank());
        Assertions.assertNotNull(resp.getUserId(), "user_id is null");
        Assertions.assertNotNull(resp.getEmail(), "email is null");
        Assertions.assertNotNull(resp.getUsername(), "username is null");
        Assertions.assertNotNull(resp.getExpiresAt(), "expires_at is null");
    }

    @Then("Save field accessToken from LoginResp {string} as {string}")
    public void saveTokenFromLoginResp(String varName, String saveAs) {
        ctx.save(saveAs, ctx.required(varName, LoginResp.class).getAccessToken());
    }

    @Then("Save field userId from LoginResp {string} as {string}")
    public void saveUserIdFromLoginResp(String varName, String saveAs) {
        ctx.save(saveAs, ctx.required(varName, LoginResp.class).getUserId());
    }

    // ── Create user ──────────────────────────────────────────────────

    @When("Create user with email {string} username {string} password {string} firstName {string} lastName {string} and save response as {string}")
    public void createUser(String emailVar, String usernameVar, String passwordVar,
                           String firstNameVar, String lastNameVar, String varName) {
        postCreateUser(varName, credentials(emailVar, usernameVar, passwordVar)
                .profile(namedProfile(firstNameVar, lastNameVar).build())
                .build());
    }

    @When("Create user with email {string} username {string} password {string} firstName {string} lastName {string} gender {string} and save response as {string}")
    public void createUserWithGender(String emailVar, String usernameVar, String passwordVar,
                                     String firstNameVar, String lastNameVar, String genderVar, String varName) {
        postCreateUser(varName, credentials(emailVar, usernameVar, passwordVar)
                .profile(namedProfile(firstNameVar, lastNameVar).gender(ctx.str(genderVar)).build())
                .build());
    }

    @When("Create user with employment status {string} and save response as {string}")
    public void createUserWithEmploymentStatus(String statusVar, String varName) {
        postCreateUser(varName, UserFixtures.randomUser()
                .employment(EmploymentReq.builder().status(ctx.str(statusVar)).build())
                .build());
    }

    @When("Create user with theme {string} and save response as {string}")
    public void createUserWithTheme(String themeVar, String varName) {
        postCreateUser(varName, UserFixtures.randomUser()
                .settings(SettingsReq.builder().theme(ctx.str(themeVar)).build())
                .build());
    }

    @When("Create user with username length {int} and save response as {string}")
    public void createUserWithUsernameLength(int length, String varName) {
        postCreateUser(varName, UserFixtures.randomUser()
                .username(Generator.alphanumericString(length).toLowerCase())
                .build());
    }

    @When("Create user with interests {string} and save response as {string}")
    public void createUserWithInterests(String interestsVar, String varName) {
        String raw = ctx.str(interestsVar);
        List<String> interests = raw.isBlank() ? List.of() : List.of(raw.split(","));
        postCreateUser(varName, UserFixtures.randomUser()
                .profile(UserFixtures.randomProfile().interests(interests).build())
                .build());
    }

    @When("Create user with bio of length {int} and save response as {string}")
    public void createUserWithBioLength(int length, String varName) {
        postCreateUser(varName, UserFixtures.randomUser()
                .profile(UserFixtures.randomProfile().bio(Generator.alphanumericString(length)).build())
                .build());
    }

    @When("Create user with firstName length {int} and save response as {string}")
    public void createUserWithFirstNameLength(int length, String varName) {
        String name = length > 0 ? Generator.alphanumericString(length) : "";
        postCreateUser(varName, UserFixtures.randomUser()
                .profile(UserFixtures.randomProfile().firstName(name).build())
                .build());
    }

    @When("Create minimal user and save response as {string}")
    public void createMinimalUser(String varName) {
        String email = Generator.email();
        String password = Generator.password();
        ctx.save("generatedEmail", email);
        ctx.save("generatedPassword", password);
        postCreateUser(varName, UserFixtures.randomUser().email(email).password(password).build());
    }

    @When("Create full user with all fields and save response as {string}")
    public void createFullUser(String varName) {
        postCreateUser(varName, UserFixtures.randomUser()
                .profile(UserFixtures.randomProfile()
                        .gender("male")
                        .bio("Test user bio")
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
                        .build())
                .settings(SettingsReq.builder()
                        .language("en")
                        .timezone("America/Los_Angeles")
                        .theme("dark")
                        .notificationsEnabled(true)
                        .build())
                .build());
    }

    private void postCreateUser(String varName, CreateUserReq body) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, body));
    }

    /** email / username / password resolved from context (literal fallback). */
    private CreateUserReq.CreateUserReqBuilder credentials(String emailVar, String usernameVar, String passwordVar) {
        return CreateUserReq.builder()
                .email(ctx.str(emailVar))
                .username(ctx.str(usernameVar))
                .password(ctx.str(passwordVar));
    }

    /** first / last name resolved from context (literal fallback). */
    private ProfileReq.ProfileReqBuilder namedProfile(String firstNameVar, String lastNameVar) {
        return ProfileReq.builder()
                .firstName(ctx.str(firstNameVar))
                .lastName(ctx.str(lastNameVar));
    }

    @Then("Convert create user response {string} to CreateUserResp and save as {string}")
    public void convertCreateUserResponse(String rawVar, String saveAs) {
        ctx.convertAndSave(rawVar, CreateUserResp.class, saveAs);
    }

    @Then("Assert CreateUserResp {string} has email {string}")
    public void assertCreateUserRespEmail(String varName, String expected) {
        Assertions.assertEquals(ctx.str(expected),
                ctx.required(varName, CreateUserResp.class).getEmail());
    }

    @Then("Assert CreateUserResp {string} has username {string}")
    public void assertCreateUserRespUsername(String varName, String expected) {
        Assertions.assertEquals(ctx.str(expected),
                ctx.required(varName, CreateUserResp.class).getUsername());
    }

    @Then("Assert CreateUserResp {string} has access token not null")
    public void assertCreateUserRespHasToken(String varName) {
        String token = ctx.required(varName, CreateUserResp.class).getAccessToken();
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());
    }

    @Then("Assert CreateUserResp {string} metadata is_active is true")
    public void assertMetadataIsActive(String varName) {
        CreateUserResp resp = ctx.required(varName, CreateUserResp.class);
        Assertions.assertNotNull(resp.getMetadata());
        Assertions.assertTrue(resp.getMetadata().getIsActive());
    }

    @Then("Assert CreateUserResp {string} metadata is_verified is false")
    public void assertMetadataIsVerified(String varName) {
        CreateUserResp resp = ctx.required(varName, CreateUserResp.class);
        Assertions.assertEquals(Boolean.FALSE, resp.getMetadata().getIsVerified());
    }

    @Then("Assert CreateUserResp {string} metadata login_count is 0")
    public void assertMetadataLoginCount(String varName) {
        CreateUserResp resp = ctx.required(varName, CreateUserResp.class);
        Assertions.assertEquals(0, resp.getMetadata().getLoginCount());
    }

    @Then("Save field id from CreateUserResp {string} as {string}")
    public void saveIdFromCreateUserResp(String varName, String saveAs) {
        ctx.save(saveAs, ctx.required(varName, CreateUserResp.class).getId());
    }

    @Then("Save field accessToken from CreateUserResp {string} as {string}")
    public void saveTokenFromCreateUserResp(String varName, String saveAs) {
        ctx.save(saveAs, ctx.required(varName, CreateUserResp.class).getAccessToken());
    }

    // ── Get / List users ─────────────────────────────────────────────

    @When("Get user by id {string} and save response as {string}")
    public void getUserById(String idVar, String varName) {
        ctx.save(varName, rest.get(ApiPaths.usersGet(ctx.str(idVar))));
    }

    @When("Get users list and save response as {string}")
    public void getUsersList(String varName) {
        ctx.save(varName, rest.get(ApiPaths.USERS_LIST));
    }

    @When("Get users list page {string} perPage {string} and save response as {string}")
    public void getUsersListPaged(String pageVar, String perPageVar, String varName) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("page", ctx.str(pageVar));
        query.put("per_page", ctx.str(perPageVar));
        ctx.save(varName, rest.get(ApiPaths.USERS_LIST, query));
    }

    @Then("Convert get user response {string} to UserResp and save as {string}")
    public void convertUserResponse(String rawVar, String saveAs) {
        ctx.convertAndSave(rawVar, UserResp.class, saveAs);
    }

    @Then("Assert UserResp {string} id equals {string}")
    public void assertUserRespId(String varName, String expectedVar) {
        Assertions.assertEquals(ctx.str(expectedVar),
                ctx.required(varName, UserResp.class).getId(), "UserResp id mismatch");
    }

    @Then("Assert UserResp {string} has all core fields")
    public void assertUserRespCoreFields(String varName) {
        UserResp resp = ctx.required(varName, UserResp.class);
        Assertions.assertNotNull(resp.getId(), "id is null");
        Assertions.assertNotNull(resp.getEmail(), "email is null");
        Assertions.assertNotNull(resp.getUsername(), "username is null");
        Assertions.assertNotNull(resp.getProfile(), "profile is null");
        Assertions.assertNotNull(resp.getMetadata(), "metadata is null");
    }

    @Then("Convert users list response {string} to UsersListResp and save as {string}")
    public void convertUsersListResponse(String rawVar, String saveAs) {
        ctx.convertAndSave(rawVar, UsersListResp.class, saveAs);
    }

    @Then("Assert users list {string} has pagination fields")
    public void assertUserListPaginationFields(String varName) {
        UsersListResp resp = ctx.required(varName, UsersListResp.class);
        Assertions.assertNotNull(resp.getPage(), "page is null");
        Assertions.assertNotNull(resp.getPerPage(), "per_page is null");
        Assertions.assertNotNull(resp.getTotalPages(), "total_pages is null");
    }

    @Then("Assert users list {string} per page is {int}")
    public void assertUsersListPerPage(String varName, int expected) {
        Assertions.assertEquals(expected,
                ctx.required(varName, UsersListResp.class).getPerPage());
    }

    @Then("Assert users list {string} page is {int}")
    public void assertUsersListPage(String varName, int expected) {
        Assertions.assertEquals(expected,
                ctx.required(varName, UsersListResp.class).getPage());
    }

    @Then("Assert users list {string} count is at most {int}")
    public void assertUsersListCountAtMost(String varName, int max) {
        UsersListResp resp = ctx.required(varName, UsersListResp.class);
        Assertions.assertTrue(resp.getUsers().size() <= max,
                "Expected at most " + max + " users, got " + resp.getUsers().size());
    }

    // ── Check exists ─────────────────────────────────────────────────

    @When("Check user exists by id {string} and save response as {string}")
    public void checkUserExists(String idVar, String varName) {
        ctx.save(varName, rest.head(ApiPaths.usersExists(ctx.str(idVar))));
    }

    @When("Check user exists GET by id {string} and save response as {string}")
    public void checkUserExistsGet(String idVar, String varName) {
        ctx.save(varName, rest.get(ApiPaths.usersExists(ctx.str(idVar))));
    }

    @Then("Assert user exists header is {string} in response {string}")
    public void assertUserExistsHeader(String expected, String varName) {
        String header = ctx.response(varName).getHeader("X-User-Exists");
        Assertions.assertEquals(expected, header, "X-User-Exists header mismatch");
    }

    // ── Update / Patch ───────────────────────────────────────────────

    @When("Update user {string} token {string} firstName {string} lastName {string} email {string} username {string} and save response as {string}")
    public void updateUser(String idVar, String tokenVar,
                           String firstNameVar, String lastNameVar,
                           String emailVar, String usernameVar, String varName) {
        sendPut(idVar, varName, CreateUserReq.builder()
                        .email(ctx.str(emailVar))
                        .username(ctx.str(usernameVar))
                        .profile(namedProfile(firstNameVar, lastNameVar).build())
                        .build(),
                ctx.bearerAuth(tokenVar));
    }

    @When("Update user {string} with no auth header and save response as {string}")
    public void updateUserNoAuth(String idVar, String varName) {
        sendPut(idVar, varName, UserFixtures.randomUpdate().build());
    }

    @When("Update user {string} with token {string} and invalid email and save response as {string}")
    public void updateUserInvalidEmail(String idVar, String tokenVar, String varName) {
        sendPut(idVar, varName, UserFixtures.randomUpdate().email("notanemail").build(), ctx.bearerAuth(tokenVar));
    }

    @When("Patch user {string} token {string} firstName {string} and save response as {string}")
    public void patchUser(String idVar, String tokenVar, String firstNameVar, String varName) {
        sendPatch(idVar, varName, profilePatch(firstNameVar, "PatchedLastName"), ctx.bearerAuth(tokenVar));
    }

    @When("Patch user {string} token {string} email {string} and save response as {string}")
    public void patchUserEmail(String idVar, String tokenVar, String emailVar, String varName) {
        sendPatch(idVar, varName, CreateUserReq.builder().email(ctx.str(emailVar)).build(),
                ctx.bearerAuth(tokenVar));
    }

    @When("Patch user {string} token {string} username {string} and save response as {string}")
    public void patchUserUsername(String idVar, String tokenVar, String usernameVar, String varName) {
        sendPatch(idVar, varName, CreateUserReq.builder().username(ctx.str(usernameVar)).build(),
                ctx.bearerAuth(tokenVar));
    }

    @When("Patch user {string} with no auth header and save response as {string}")
    public void patchUserNoAuth(String idVar, String varName) {
        sendPatch(idVar, varName, CreateUserReq.builder().profile(UserFixtures.randomProfile().build()).build());
    }

    @When("Patch user {string} with empty body token {string} and save response as {string}")
    public void patchUserEmptyBody(String idVar, String tokenVar, String varName) {
        sendPatch(idVar, varName, CreateUserReq.builder().build(), ctx.bearerAuth(tokenVar));
    }

    /** PATCH body touching only the profile name; the caller picks the last name. */
    private CreateUserReq profilePatch(String firstNameVar, String lastName) {
        return CreateUserReq.builder()
                .profile(ProfileReq.builder().firstName(ctx.str(firstNameVar)).lastName(lastName).build())
                .build();
    }

    // ── Delete / Logout ──────────────────────────────────────────────

    @When("Delete user {string} with token {string} and save response as {string}")
    public void deleteUser(String idVar, String tokenVar, String varName) {
        sendDelete(idVar, varName, ctx.bearerAuth(tokenVar));
    }

    @When("Delete user {string} with no auth header and save response as {string}")
    public void deleteUserNoAuth(String idVar, String varName) {
        sendDelete(idVar, varName);
    }

    @When("Logout user {string} with token {string} and save response as {string}")
    public void logoutUser(String idVar, String tokenVar, String varName) {
        sendLogout(idVar, varName, ctx.bearerAuth(tokenVar));
    }

    @When("Logout user {string} with no auth header and save response as {string}")
    public void logoutUserNoAuth(String idVar, String varName) {
        sendLogout(idVar, varName);
    }

    // ── Raw Authorization header (security tests) ─────────────────────
    // These steps send a LITERAL Authorization header value verbatim (no bearerHeader()
    // normalization), so we can exercise empty / whitespace / malformed tokens exactly
    // as a real client would send them.

    @When("Delete user {string} with raw auth header {string} and save response as {string}")
    public void deleteUserRawAuth(String idVar, String rawHeader, String varName) {
        sendDelete(idVar, varName, RestHandler.authorization(rawHeader));
    }

    @When("Patch user {string} with raw auth header {string} firstName {string} and save response as {string}")
    public void patchUserRawAuth(String idVar, String rawHeader, String firstNameVar, String varName) {
        sendPatch(idVar, varName, profilePatch(firstNameVar, "RawAuthLast"), RestHandler.authorization(rawHeader));
    }

    @When("Update user {string} with raw auth header {string} and save response as {string}")
    public void updateUserRawAuth(String idVar, String rawHeader, String varName) {
        sendPut(idVar, varName, UserFixtures.randomUpdate().build(), RestHandler.authorization(rawHeader));
    }

    @When("Logout user {string} with raw auth header {string} and save response as {string}")
    public void logoutUserRawAuth(String idVar, String rawHeader, String varName) {
        sendLogout(idVar, varName, RestHandler.authorization(rawHeader));
    }

    // ── Per-user requests: resolve the id var, send, save the raw response ──

    private void sendPut(String idVar, String varName, CreateUserReq body, String... headers) {
        ctx.save(varName, rest.put(ApiPaths.usersUpdate(ctx.str(idVar)), body, headers));
    }

    private void sendPatch(String idVar, String varName, CreateUserReq body, String... headers) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)), body, headers));
    }

    private void sendDelete(String idVar, String varName, String... headers) {
        ctx.save(varName, rest.delete(ApiPaths.usersDelete(ctx.str(idVar)), headers));
    }

    private void sendLogout(String idVar, String varName, String... headers) {
        ctx.save(varName, rest.postNoBody(ApiPaths.usersLogout(ctx.str(idVar)), headers));
    }

    // ── Error assertions ─────────────────────────────────────────────

    @Then("Convert error response {string} to ErrorResp and save as {string}")
    public void convertErrorResponse(String rawVar, String saveAs) {
        ctx.convertAndSave(rawVar, ErrorResp.class, saveAs);
    }

    @Then("Assert error code is {string} in {string}")
    public void assertErrorCode(String expected, String varName) {
        ErrorResp error = ctx.required(varName, ErrorResp.class);
        Assertions.assertEquals(expected, error.getError().getCode(),
                "Error code mismatch. Full: " + error);
    }

    @Then("Assert error message contains {string} in {string}")
    public void assertErrorMessageContains(String expected, String varName) {
        ErrorResp error = ctx.required(varName, ErrorResp.class);
        Assertions.assertTrue(error.getError().getMessage().contains(expected),
                "Error message '" + error.getError().getMessage() + "' does not contain '" + expected + "'");
    }

    @Then("Assert error response has request_id in {string}")
    public void assertErrorHasRequestId(String varName) {
        ErrorResp error = ctx.required(varName, ErrorResp.class);
        Assertions.assertNotNull(error.getRequestId(), "request_id should not be null");
        Assertions.assertFalse(error.getRequestId().isBlank());
    }

    @Then("Assert error validation array is not empty in {string}")
    public void assertValidationArrayNotEmpty(String varName) {
        ErrorResp error = ctx.required(varName, ErrorResp.class);
        Assertions.assertNotNull(error.getError().getValidation(), "validation array is null");
        Assertions.assertFalse(error.getError().getValidation().isEmpty(), "validation array is empty");
    }
}
