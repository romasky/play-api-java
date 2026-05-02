package com.testFramework.core;

import com.testFramework.play_qa_api.models.mail.CreateMailboxReq;
import com.testFramework.play_qa_api.models.mail.CreateMailboxResp;
import com.testFramework.play_qa_api.models.mail.MessageResp;
import com.testFramework.play_qa_api.models.mail.MessagesListResp;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Handles temp mailbox creation and message polling using the play-qa.com mail API.
 * No browser/Selenium needed — the API provides full mail capabilities.
 */
public class TempMailHandler {

    private static final String BASE_URL = PropertyHandler.get("baseUrl");
    private static final String MAIL_CREATE = "/api/v1/mail/create";
    private static final String MAIL_MESSAGES = "/api/v1/mail/{token}/messages";
    private static final long POLL_INTERVAL_MS = 3000;

    @Step("Create temp mailbox")
    public CreateMailboxResp createMailbox() {
        Response response = given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .body(new CreateMailboxReq())
                .post(MAIL_CREATE);

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create mailbox: " + response.asString());
        }
        return response.as(CreateMailboxResp.class);
    }

    @Step("Create temp mailbox with local part: {localPart}")
    public CreateMailboxResp createMailbox(String localPart) {
        CreateMailboxReq req = CreateMailboxReq.builder().localPart(localPart).build();
        Response response = given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .body(req)
                .post(MAIL_CREATE);

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create mailbox: " + response.asString());
        }
        return response.as(CreateMailboxResp.class);
    }

    @Step("Wait for message in mailbox {token} (up to {timeoutSec}s)")
    public MessageResp waitForMessage(String token, long timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000;

        while (System.currentTimeMillis() < deadline) {
            Response response = given()
                    .baseUri(BASE_URL)
                    .pathParam("token", token)
                    .get(MAIL_MESSAGES);

            if (response.statusCode() == 200) {
                MessagesListResp list = response.as(MessagesListResp.class);
                if (list.getCount() != null && list.getCount() > 0) {
                    String msgId = list.getMessages().get(0).getId();
                    return getFullMessage(token, msgId);
                }
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for email", e);
            }
        }
        throw new RuntimeException("No email received in mailbox " + token + " within " + timeoutSec + " seconds");
    }

    @Step("Get full message {messageId} from mailbox {token}")
    public MessageResp getFullMessage(String token, String messageId) {
        Response response = given()
                .baseUri(BASE_URL)
                .get("/api/v1/mail/" + token + "/messages/" + messageId);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get message: " + response.asString());
        }
        return response.as(MessageResp.class);
    }
}
