package com.testFramework.steps.play_qa_api;

import com.testFramework.play_qa_api.models.createUser.response.CreateUserResp;
import com.testFramework.play_qa_api.models.createUser.response.UserResp;
import com.testFramework.play_qa_api.models.createUser.response.UsersListResp;
import com.testFramework.play_qa_api.models.error.ErrorResp;
import com.testFramework.play_qa_api.models.login.LoginResp;
import com.testFramework.support.LocalBaseUrlServer;
import com.testFramework.support.StepContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static com.testFramework.support.Fake.*;
import static com.testFramework.support.StepContext.failsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline: the AccountsSteps assertions and field-savers against DTOs parsed from canned JSON.
 * The requests these steps send are pinned by {@link RequestPinTest}.
 */
@ExtendWith(StepContext.class)
class AccountsStepsTest {

    private static final String LOGIN =
            "{'access_token':'tok','user_id':'u1','email':'a@b.c','username':'neo','expires_at':'2030'}";
    private static final String CREATED = "{'id':'u1','email':'a@b.c','username':'neo','access_token':'tok',"
            + "'metadata':{'is_active':true,'is_verified':false,'login_count':0}}";
    private static final String USER = "{'id':'u1','email':'a@b.c','username':'neo','profile':{},'metadata':{}}";
    private static final String LIST = "{'page':2,'per_page':3,'total_pages':4,'users':[{},{}]}";
    private static final String ERROR =
            "{'request_id':'rq-1','error':{'code':'NOT_FOUND','message':'user not found','validation':[{}]}}";

    private final AccountsSteps accounts = new AccountsSteps();
    private final BaseSteps ctx = new BaseSteps();

    /** One way a stored DTO breaks a step: the JSON it was parsed from, and the failure message the step gives. */
    record Broken(Class<?> type, String json, Consumer<String> step, String message) { }

    @Test
    void everyRequiredFieldCheckFailsNamingTheBrokenField() {
        // CPD-OFF: one row per broken field (data)
        List<Broken> cases = List.of(
                new Broken(LoginResp.class, without(LOGIN, "access_token"), accounts::assertLoginResponseFields, "access_token is null"),
                new Broken(LoginResp.class, with(LOGIN, "access_token", "' '"), accounts::assertLoginResponseFields, "expected: <false>"),
                new Broken(LoginResp.class, without(LOGIN, "user_id"), accounts::assertLoginResponseFields, "user_id is null"),
                new Broken(LoginResp.class, without(LOGIN, "email"), accounts::assertLoginResponseFields, "email is null"),
                new Broken(LoginResp.class, without(LOGIN, "username"), accounts::assertLoginResponseFields, "username is null"),
                new Broken(LoginResp.class, without(LOGIN, "expires_at"), accounts::assertLoginResponseFields, "expires_at is null"),
                new Broken(CreateUserResp.class, without(CREATED, "access_token"), accounts::assertCreateUserRespHasToken, "expected: not <null>"),
                new Broken(CreateUserResp.class, with(CREATED, "access_token", "''"), accounts::assertCreateUserRespHasToken, "expected: <false>"),
                new Broken(CreateUserResp.class, without(CREATED, "metadata"), accounts::assertMetadataIsActive, "expected: not <null>"),
                new Broken(CreateUserResp.class, with(CREATED, "metadata.is_active", "false"), accounts::assertMetadataIsActive, "expected: <true>"),
                new Broken(CreateUserResp.class, with(CREATED, "metadata.is_verified", "true"), accounts::assertMetadataIsVerified, "expected: <false> but was: <true>"),
                new Broken(CreateUserResp.class, with(CREATED, "metadata.login_count", "1"), accounts::assertMetadataLoginCount, "expected: <0> but was: <1>"),
                new Broken(UserResp.class, without(USER, "id"), accounts::assertUserRespCoreFields, "id is null"),
                new Broken(UserResp.class, without(USER, "email"), accounts::assertUserRespCoreFields, "email is null"),
                new Broken(UserResp.class, without(USER, "username"), accounts::assertUserRespCoreFields, "username is null"),
                new Broken(UserResp.class, without(USER, "profile"), accounts::assertUserRespCoreFields, "profile is null"),
                new Broken(UserResp.class, without(USER, "metadata"), accounts::assertUserRespCoreFields, "metadata is null"),
                new Broken(UsersListResp.class, without(LIST, "page"), accounts::assertUserListPaginationFields, "page is null"),
                new Broken(UsersListResp.class, without(LIST, "per_page"), accounts::assertUserListPaginationFields, "per_page is null"),
                new Broken(UsersListResp.class, without(LIST, "total_pages"), accounts::assertUserListPaginationFields, "total_pages is null"),
                new Broken(ErrorResp.class, without(ERROR, "request_id"), accounts::assertErrorHasRequestId, "request_id should not be null"),
                new Broken(ErrorResp.class, with(ERROR, "request_id", "' '"), accounts::assertErrorHasRequestId, "expected: <false>"),
                new Broken(ErrorResp.class, without(ERROR, "error.validation"), accounts::assertValidationArrayNotEmpty, "validation array is null"),
                new Broken(ErrorResp.class, with(ERROR, "error.validation", "[]"), accounts::assertValidationArrayNotEmpty, "validation array is empty"));
        // CPD-ON

        for (Broken c : cases) {
            ctx.save("dto", dto(c.type(), c.json()));
            failsWith(c.message(), () -> c.step().accept("dto"));
        }
    }

    @Test
    void requiredFieldChecksPassOnACompleteDto() {
        ctx.save("login", dto(LoginResp.class, LOGIN));
        ctx.save("created", dto(CreateUserResp.class, CREATED));
        ctx.save("user", dto(UserResp.class, USER));
        ctx.save("list", dto(UsersListResp.class, LIST));
        ctx.save("error", dto(ErrorResp.class, ERROR));

        assertAll(
                () -> accounts.assertLoginResponseFields("login"),
                () -> accounts.assertCreateUserRespHasToken("created"),
                () -> accounts.assertMetadataIsActive("created"),
                () -> accounts.assertMetadataIsVerified("created"),
                () -> accounts.assertMetadataLoginCount("created"),
                () -> accounts.assertUserRespCoreFields("user"),
                () -> accounts.assertUserListPaginationFields("list"),
                () -> accounts.assertErrorHasRequestId("error"),
                () -> accounts.assertValidationArrayNotEmpty("error"));
    }

    @Test
    void valueChecksCompareAgainstTheResolvedExpectation() {
        ctx.save("created", dto(CreateUserResp.class, CREATED));
        ctx.save("user", dto(UserResp.class, USER));
        ctx.save("myEmail", "a@b.c");
        ctx.save("myName", "neo");
        ctx.save("myId", "u1");

        assertAll(
                () -> accounts.assertCreateUserRespEmail("created", "myEmail"),
                () -> accounts.assertCreateUserRespEmail("created", "a@b.c"),
                () -> accounts.assertCreateUserRespUsername("created", "myName"),
                () -> accounts.assertUserRespId("user", "myId"));
        failsWith("expected: <x@y.z> but was: <a@b.c>", () -> accounts.assertCreateUserRespEmail("created", "x@y.z"));
        failsWith("expected: <trinity> but was: <neo>", () -> accounts.assertCreateUserRespUsername("created", "trinity"));
        failsWith("UserResp id mismatch ==> expected: <u2> but was: <u1>", () -> accounts.assertUserRespId("user", "u2"));
    }

    @Test
    void usersListPageChecks() {
        ctx.save("list", dto(UsersListResp.class, LIST));

        assertAll(
                () -> accounts.assertUsersListPage("list", 2),
                () -> accounts.assertUsersListPerPage("list", 3),
                () -> accounts.assertUsersListCountAtMost("list", 2),
                () -> accounts.assertUsersListCountAtMost("list", 3));
        failsWith("expected: <1> but was: <2>", () -> accounts.assertUsersListPage("list", 1));
        failsWith("expected: <5> but was: <3>", () -> accounts.assertUsersListPerPage("list", 5));
        failsWith("Expected at most 1 users, got 2", () -> accounts.assertUsersListCountAtMost("list", 1));
    }

    @Test
    void errorCodeAndMessageChecks() {
        ctx.save("error", dto(ErrorResp.class, ERROR));

        assertAll(
                () -> accounts.assertErrorCode("NOT_FOUND", "error"),
                () -> accounts.assertErrorMessageContains("not found", "error"));
        failsWith("Error code mismatch. Full: ErrorResp(", () -> accounts.assertErrorCode("FORBIDDEN", "error"));
        failsWith("Error message 'user not found' does not contain 'expired'",
                () -> accounts.assertErrorMessageContains("expired", "error"));
    }

    @Test
    void userExistsHeaderCheckReadsTheRawResponse() {
        ctx.save("resp", response(200, "", "X-User-Exists", "true"));

        assertDoesNotThrow(() -> accounts.assertUserExistsHeader("true", "resp"));
        failsWith("X-User-Exists header mismatch ==> expected: <false> but was: <true>",
                () -> accounts.assertUserExistsHeader("false", "resp"));
    }

    @Test
    void fieldSaversCopyTheFieldUnderTheNewName() {
        ctx.save("login", dto(LoginResp.class, LOGIN));
        ctx.save("created", dto(CreateUserResp.class, with(CREATED, "access_token", "'tok-c'")));

        accounts.saveTokenFromLoginResp("login", "a");
        accounts.saveUserIdFromLoginResp("login", "b");
        accounts.saveIdFromCreateUserResp("created", "c");
        accounts.saveTokenFromCreateUserResp("created", "d");

        assertEquals(List.of("tok", "u1", "u1", "tok-c"), List.of(ctx.str("a"), ctx.str("b"), ctx.str("c"), ctx.str("d")));
    }

    @Test
    void loginAndSaveTokenFailsOnANon200AndSavesNoToken() throws IOException {
        try (LocalBaseUrlServer ignored = LocalBaseUrlServer.start(
                exchange -> LocalBaseUrlServer.reply(exchange, 401, "{\"code\":\"BAD_CREDENTIALS\"}"))) {
            AccountsSteps steps = new AccountsSteps();

            failsWith("Login failed: {\"code\":\"BAD_CREDENTIALS\"} ==> expected: <200> but was: <401>",
                    () -> steps.loginAndSaveToken("a@b.c", "pw", "token"));
        }
        assertEquals("token", ctx.str("token"));
    }
}
