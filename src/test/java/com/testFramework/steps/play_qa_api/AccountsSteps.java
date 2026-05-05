package com.testFramework.steps.play_qa_api;

import com.testFramework.core.Generator;
import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.createUser.*;
import com.testFramework.play_qa_api.models.createUser.response.CreateUserResp;
import com.testFramework.play_qa_api.models.createUser.response.UserResp;
import com.testFramework.play_qa_api.models.createUser.response.UsersListResp;
import com.testFramework.play_qa_api.models.error.ErrorResp;
import com.testFramework.play_qa_api.models.login.LoginReq;
import com.testFramework.play_qa_api.models.login.LoginResp;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class AccountsSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    @Before
    public void before(Scenario scenario) { ctx.before(scenario); }

    // Nginx create_user zone: 30r/min, burst=10. Pace to stay within the window.
    @Before("@Users")
    public void paceCreateUserScenarios() throws InterruptedException {
        Thread.sleep(2_000);
    }

    // Go LoginRateLimiter: 5r/min. Pace login scenarios to avoid 429.
    @Before("@Login")
    public void paceLoginScenarios() throws InterruptedException {
        Thread.sleep(13_000);
    }

    // ── Login ────────────────────────────────────────────────────────

    @Given("Login with email {string} password {string} and save token as {string}")
    public void loginAndSaveToken(String emailVar, String passwordVar, String tokenVar) {
        LoginReq req = LoginReq.builder()
                .email(ctx.str(emailVar))
                .password(ctx.str(passwordVar))
                .build();
        Response response = rest.post(ApiPaths.LOGIN, req);
        Assertions.assertEquals(200, response.getStatusCode(),
                "Login failed: " + response.asString());
        ctx.save(tokenVar, response.as(LoginResp.class).getAccessToken());
    }

    @Given("Login with email {string} password {string} and save raw response as {string}")
    public void loginAndSaveResponse(String emailVar, String passwordVar, String varName) {
        LoginReq req = LoginReq.builder()
                .email(ctx.str(emailVar))
                .password(ctx.str(passwordVar))
                .build();
        ctx.save(varName, rest.post(ApiPaths.LOGIN, req));
    }

    @Given("Login with no body and save response as {string}")
    public void loginEmptyBody(String varName) {
        ctx.save(varName, rest.post(ApiPaths.LOGIN, "{}"));
    }

    @Then("Convert login response {string} to LoginResp and save as {string}")
    public void convertLoginResponse(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(LoginResp.class));
    }

    @Then("Assert login response {string} has all required fields")
    public void assertLoginResponseFields(String varName) {
        LoginResp resp = (LoginResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getAccessToken(), "access_token is null");
        Assertions.assertFalse(resp.getAccessToken().isBlank());
        Assertions.assertNotNull(resp.getUserId(), "user_id is null");
        Assertions.assertNotNull(resp.getEmail(), "email is null");
        Assertions.assertNotNull(resp.getUsername(), "username is null");
        Assertions.assertNotNull(resp.getExpiresAt(), "expires_at is null");
    }

    @Then("Save field accessToken from LoginResp {string} as {string}")
    public void saveTokenFromLoginResp(String varName, String saveAs) {
        ctx.save(saveAs, ((LoginResp) ctx.get(varName, true)).getAccessToken());
    }

    @Then("Save field userId from LoginResp {string} as {string}")
    public void saveUserIdFromLoginResp(String varName, String saveAs) {
        ctx.save(saveAs, ((LoginResp) ctx.get(varName, true)).getUserId());
    }

    // ── Create user ──────────────────────────────────────────────────

    @When("Create user with email {string} username {string} password {string} firstName {string} lastName {string} and save response as {string}")
    public void createUser(String emailVar, String usernameVar, String passwordVar,
                           String firstNameVar, String lastNameVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(ctx.str(emailVar))
                .username(ctx.str(usernameVar))
                .password(ctx.str(passwordVar))
                .profile(ProfileReq.builder()
                        .firstName(ctx.str(firstNameVar))
                        .lastName(ctx.str(lastNameVar))
                        .build())
                .build()));
    }

    @When("Create user with email {string} username {string} password {string} firstName {string} lastName {string} gender {string} and save response as {string}")
    public void createUserWithGender(String emailVar, String usernameVar, String passwordVar,
                                     String firstNameVar, String lastNameVar, String genderVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(ctx.str(emailVar))
                .username(ctx.str(usernameVar))
                .password(ctx.str(passwordVar))
                .profile(ProfileReq.builder()
                        .firstName(ctx.str(firstNameVar))
                        .lastName(ctx.str(lastNameVar))
                        .gender(ctx.str(genderVar))
                        .build())
                .build()));
    }

    @When("Create user with employment status {string} and save response as {string}")
    public void createUserWithEmploymentStatus(String statusVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .build())
                .employment(EmploymentReq.builder()
                        .status(ctx.str(statusVar))
                        .build())
                .build()));
    }

    @When("Create user with theme {string} and save response as {string}")
    public void createUserWithTheme(String themeVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .build())
                .settings(SettingsReq.builder()
                        .theme(ctx.str(themeVar))
                        .build())
                .build()));
    }

    @When("Create user with username length {int} and save response as {string}")
    public void createUserWithUsernameLength(int length, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.alphanumericString(length).toLowerCase())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .build())
                .build()));
    }

    @When("Create user with interests {string} and save response as {string}")
    public void createUserWithInterests(String interestsVar, String varName) {
        String raw = ctx.str(interestsVar);
        List<String> interests = raw.isBlank() ? List.of() : List.of(raw.split(","));
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .interests(interests)
                        .build())
                .build()));
    }

    @When("Create user with bio of length {int} and save response as {string}")
    public void createUserWithBioLength(int length, String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .bio(Generator.alphanumericString(length))
                        .build())
                .build()));
    }

    @When("Create user with firstName length {int} and save response as {string}")
    public void createUserWithFirstNameLength(int length, String varName) {
        String name = length > 0 ? Generator.alphanumericString(length) : "";
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(name)
                        .lastName(Generator.lastName())
                        .build())
                .build()));
    }

    @When("Create minimal user and save response as {string}")
    public void createMinimalUser(String varName) {
        String email = Generator.email();
        String password = Generator.password();
        ctx.save("generatedEmail", email);
        ctx.save("generatedPassword", password);
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(email)
                .username(Generator.username())
                .password(password)
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
                        .build())
                .build()));
    }

    @When("Create full user with all fields and save response as {string}")
    public void createFullUser(String varName) {
        ctx.save(varName, rest.post(ApiPaths.USERS_CREATE, CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(ProfileReq.builder()
                        .firstName(Generator.firstName())
                        .lastName(Generator.lastName())
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
                .build()));
    }

    @Then("Convert create user response {string} to CreateUserResp and save as {string}")
    public void convertCreateUserResponse(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(CreateUserResp.class));
    }

    @Then("Assert CreateUserResp {string} has email {string}")
    public void assertCreateUserRespEmail(String varName, String expected) {
        Assertions.assertEquals(ctx.str(expected),
                ((CreateUserResp) ctx.get(varName, true)).getEmail());
    }

    @Then("Assert CreateUserResp {string} has username {string}")
    public void assertCreateUserRespUsername(String varName, String expected) {
        Assertions.assertEquals(ctx.str(expected),
                ((CreateUserResp) ctx.get(varName, true)).getUsername());
    }

    @Then("Assert CreateUserResp {string} has access token not null")
    public void assertCreateUserRespHasToken(String varName) {
        String token = ((CreateUserResp) ctx.get(varName, true)).getAccessToken();
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());
    }

    @Then("Assert CreateUserResp {string} metadata is_active is true")
    public void assertMetadataIsActive(String varName) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getMetadata());
        Assertions.assertTrue(resp.getMetadata().getIsActive());
    }

    @Then("Assert CreateUserResp {string} metadata is_verified is false")
    public void assertMetadataIsVerified(String varName) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertEquals(Boolean.FALSE, resp.getMetadata().getIsVerified());
    }

    @Then("Assert CreateUserResp {string} metadata login_count is 0")
    public void assertMetadataLoginCount(String varName) {
        CreateUserResp resp = (CreateUserResp) ctx.get(varName, true);
        Assertions.assertEquals(0, resp.getMetadata().getLoginCount());
    }

    @Then("Save field id from CreateUserResp {string} as {string}")
    public void saveIdFromCreateUserResp(String varName, String saveAs) {
        ctx.save(saveAs, ((CreateUserResp) ctx.get(varName, true)).getId());
    }

    @Then("Save field accessToken from CreateUserResp {string} as {string}")
    public void saveTokenFromCreateUserResp(String varName, String saveAs) {
        ctx.save(saveAs, ((CreateUserResp) ctx.get(varName, true)).getAccessToken());
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
        ctx.save(varName, rest.get(ApiPaths.USERS_LIST,
                new String[]{"page", ctx.str(pageVar), "per_page", ctx.str(perPageVar)}));
    }

    @Then("Convert get user response {string} to UserResp and save as {string}")
    public void convertUserResponse(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(UserResp.class));
    }

    @Then("Convert users list response {string} to UsersListResp and save as {string}")
    public void convertUsersListResponse(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(UsersListResp.class));
    }

    @Then("Assert users list {string} has pagination fields")
    public void assertUserListPaginationFields(String varName) {
        UsersListResp resp = (UsersListResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getPage(), "page is null");
        Assertions.assertNotNull(resp.getPerPage(), "per_page is null");
        Assertions.assertNotNull(resp.getTotalPages(), "total_pages is null");
    }

    @Then("Assert users list {string} per page is {int}")
    public void assertUsersListPerPage(String varName, int expected) {
        Assertions.assertEquals(expected,
                ((UsersListResp) ctx.get(varName, true)).getPerPage());
    }

    @Then("Assert users list {string} page is {int}")
    public void assertUsersListPage(String varName, int expected) {
        Assertions.assertEquals(expected,
                ((UsersListResp) ctx.get(varName, true)).getPage());
    }

    @Then("Assert users list {string} count is at most {int}")
    public void assertUsersListCountAtMost(String varName, int max) {
        UsersListResp resp = (UsersListResp) ctx.get(varName, true);
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
        String header = ((Response) ctx.get(varName, true)).getHeader("X-User-Exists");
        Assertions.assertEquals(expected, header, "X-User-Exists header mismatch");
    }

    // ── Update / Patch ───────────────────────────────────────────────

    @When("Update user {string} token {string} firstName {string} lastName {string} email {string} username {string} and save response as {string}")
    public void updateUser(String idVar, String tokenVar,
                           String firstNameVar, String lastNameVar,
                           String emailVar, String usernameVar, String varName) {
        ctx.save(varName, rest.put(ApiPaths.usersUpdate(ctx.str(idVar)),
                CreateUserReq.builder()
                        .email(ctx.str(emailVar))
                        .username(ctx.str(usernameVar))
                        .profile(ProfileReq.builder()
                                .firstName(ctx.str(firstNameVar))
                                .lastName(ctx.str(lastNameVar))
                                .build())
                        .build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Update user {string} with no auth header and save response as {string}")
    public void updateUserNoAuth(String idVar, String varName) {
        ctx.save(varName, rest.put(ApiPaths.usersUpdate(ctx.str(idVar)),
                CreateUserReq.builder()
                        .email(Generator.email())
                        .username(Generator.username())
                        .profile(ProfileReq.builder()
                                .firstName(Generator.firstName())
                                .lastName(Generator.lastName())
                                .build())
                        .build()));
    }

    @When("Update user {string} with token {string} and invalid email and save response as {string}")
    public void updateUserInvalidEmail(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.put(ApiPaths.usersUpdate(ctx.str(idVar)),
                CreateUserReq.builder()
                        .email("notanemail")
                        .username(Generator.username())
                        .profile(ProfileReq.builder()
                                .firstName(Generator.firstName())
                                .lastName(Generator.lastName())
                                .build())
                        .build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Patch user {string} token {string} firstName {string} and save response as {string}")
    public void patchUser(String idVar, String tokenVar, String firstNameVar, String varName) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)),
                CreateUserReq.builder()
                        .profile(ProfileReq.builder()
                                .firstName(ctx.str(firstNameVar))
                                .lastName("PatchedLastName")
                                .build())
                        .build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Patch user {string} token {string} email {string} and save response as {string}")
    public void patchUserEmail(String idVar, String tokenVar, String emailVar, String varName) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)),
                CreateUserReq.builder().email(ctx.str(emailVar)).build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Patch user {string} token {string} username {string} and save response as {string}")
    public void patchUserUsername(String idVar, String tokenVar, String usernameVar, String varName) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)),
                CreateUserReq.builder().username(ctx.str(usernameVar)).build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Patch user {string} with no auth header and save response as {string}")
    public void patchUserNoAuth(String idVar, String varName) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)),
                CreateUserReq.builder()
                        .profile(ProfileReq.builder()
                                .firstName(Generator.firstName())
                                .lastName(Generator.lastName())
                                .build())
                        .build()));
    }

    @When("Patch user {string} with empty body token {string} and save response as {string}")
    public void patchUserEmptyBody(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.patch(ApiPaths.usersPatch(ctx.str(idVar)),
                CreateUserReq.builder().build(),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    // ── Delete / Logout ──────────────────────────────────────────────

    @When("Delete user {string} with token {string} and save response as {string}")
    public void deleteUser(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.delete(ApiPaths.usersDelete(ctx.str(idVar)),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Delete user {string} with no auth header and save response as {string}")
    public void deleteUserNoAuth(String idVar, String varName) {
        ctx.save(varName, rest.delete(ApiPaths.usersDelete(ctx.str(idVar))));
    }

    @When("Logout user {string} with token {string} and save response as {string}")
    public void logoutUser(String idVar, String tokenVar, String varName) {
        ctx.save(varName, rest.postNoBody(ApiPaths.usersLogout(ctx.str(idVar)),
                "Authorization", RestHandler.bearerHeader(ctx.str(tokenVar))));
    }

    @When("Logout user {string} with no auth header and save response as {string}")
    public void logoutUserNoAuth(String idVar, String varName) {
        ctx.save(varName, rest.postNoBody(ApiPaths.usersLogout(ctx.str(idVar))));
    }

    // ── Error assertions ─────────────────────────────────────────────

    @Then("Convert error response {string} to ErrorResp and save as {string}")
    public void convertErrorResponse(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(ErrorResp.class));
    }

    @Then("Assert error code is {string} in {string}")
    public void assertErrorCode(String expected, String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertEquals(expected, error.getError().getCode(),
                "Error code mismatch. Full: " + error);
    }

    @Then("Assert error message contains {string} in {string}")
    public void assertErrorMessageContains(String expected, String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertTrue(error.getError().getMessage().contains(expected),
                "Error message '" + error.getError().getMessage() + "' does not contain '" + expected + "'");
    }

    @Then("Assert error response has request_id in {string}")
    public void assertErrorHasRequestId(String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertNotNull(error.getRequestId(), "request_id should not be null");
        Assertions.assertFalse(error.getRequestId().isBlank());
    }

    @Then("Assert error validation array is not empty in {string}")
    public void assertValidationArrayNotEmpty(String varName) {
        ErrorResp error = (ErrorResp) ctx.get(varName, true);
        Assertions.assertNotNull(error.getError().getValidation(), "validation array is null");
        Assertions.assertFalse(error.getError().getValidation().isEmpty(), "validation array is empty");
    }
}
