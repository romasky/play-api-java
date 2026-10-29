package com.testFramework.play_qa_api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pins every endpoint path against docs/API_REFERENCE.md so a typo cannot reach the live suite. */
class ApiPathsTest {

    @Test
    void fixedPaths() {
        assertEquals("/api/v1/health", ApiPaths.HEALTH);
        assertEquals("/api/v1/login", ApiPaths.LOGIN);
        assertEquals("/api/v1/users/create", ApiPaths.USERS_CREATE);
        assertEquals("/api/v1/users/list", ApiPaths.USERS_LIST);
        assertEquals("/api/v1/users/options", ApiPaths.USERS_OPTIONS);
        assertEquals("/api/v1/mail/create", ApiPaths.MAIL_CREATE);
    }

    @Test
    void userPathsAppendTheId() {
        assertEquals("/api/v1/users/get/42", ApiPaths.usersGet("42"));
        assertEquals("/api/v1/users/exists/42", ApiPaths.usersExists("42"));
        assertEquals("/api/v1/users/update/42", ApiPaths.usersUpdate("42"));
        assertEquals("/api/v1/users/patch/42", ApiPaths.usersPatch("42"));
        assertEquals("/api/v1/users/delete/42", ApiPaths.usersDelete("42"));
        assertEquals("/api/v1/users/logout/42", ApiPaths.usersLogout("42"));
    }

    @Test
    void mailPathsEmbedTheToken() {
        assertEquals("/api/v1/mail/tok", ApiPaths.mailGet("tok"));
        assertEquals("/api/v1/mail/tok/messages", ApiPaths.mailMessages("tok"));
        assertEquals("/api/v1/mail/tok/messages/7", ApiPaths.mailMessage("tok", "7"));
        assertEquals("/api/v1/mail/tok/send", ApiPaths.mailSend("tok"));
        assertEquals("/api/v1/mail/tok", ApiPaths.mailDelete("tok"));
    }

    @Test
    void emptyIdLeavesTrailingSlash() {
        assertEquals("/api/v1/users/get/", ApiPaths.usersGet(""));
    }
}
