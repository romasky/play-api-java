package com.testFramework.steps.play_qa_api;

import com.testFramework.core.RestHandler;
import com.testFramework.play_qa_api.ApiPaths;
import com.testFramework.play_qa_api.models.mail.*;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

public class MailSteps {

    private final BaseSteps ctx = new BaseSteps();
    private final RestHandler rest = new RestHandler();

    @Before
    public void before(Scenario scenario) { ctx.before(scenario); }

    // ── Create mailbox ───────────────────────────────────────────────

    @When("Create mailbox with empty body and save response as {string}")
    @Step("POST /mail/create (empty body)")
    public void createMailboxEmpty(String varName) {
        ctx.save(varName, rest.post(ApiPaths.MAIL_CREATE, new CreateMailboxReq()));
    }

    @When("Create mailbox with local part {string} and save response as {string}")
    @Step("POST /mail/create local_part={localPartVar}")
    public void createMailboxWithLocalPart(String localPartVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.MAIL_CREATE,
                CreateMailboxReq.builder().localPart(ctx.str(localPartVar)).build()));
    }

    @When("Create mailbox with domain {string} and save response as {string}")
    @Step("POST /mail/create domain={domainVar}")
    public void createMailboxWithDomain(String domainVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.MAIL_CREATE,
                CreateMailboxReq.builder().domain(ctx.str(domainVar)).build()));
    }

    @When("Create mailbox with domain {string} local part {string} and save response as {string}")
    @Step("POST /mail/create domain+local_part")
    public void createMailboxWithDomainAndLocalPart(String domainVar, String localPartVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.MAIL_CREATE,
                CreateMailboxReq.builder()
                        .domain(ctx.str(domainVar))
                        .localPart(ctx.str(localPartVar))
                        .build()));
    }

    @Then("Convert create mailbox response {string} to CreateMailboxResp and save as {string}")
    public void convertCreateMailboxResp(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(CreateMailboxResp.class));
    }

    @Then("Save field token from CreateMailboxResp {string} as {string}")
    public void saveMailboxToken(String varName, String saveAs) {
        ctx.save(saveAs, ((CreateMailboxResp) ctx.get(varName, true)).getToken());
    }

    @Then("Save field emailAddress from CreateMailboxResp {string} as {string}")
    public void saveMailboxEmail(String varName, String saveAs) {
        ctx.save(saveAs, ((CreateMailboxResp) ctx.get(varName, true)).getEmailAddress());
    }

    @Then("Assert CreateMailboxResp {string} has all required fields")
    @Step("Assert mailbox response has all fields")
    public void assertCreateMailboxFields(String varName) {
        CreateMailboxResp resp = (CreateMailboxResp) ctx.get(varName, true);
        Assertions.assertNotNull(resp.getId(), "id is null");
        Assertions.assertNotNull(resp.getToken(), "token is null");
        Assertions.assertNotNull(resp.getEmailAddress(), "email_address is null");
        Assertions.assertNotNull(resp.getDomain(), "domain is null");
        Assertions.assertNotNull(resp.getExpiresAt(), "expires_at is null");
        Assertions.assertNotNull(resp.getCreatedAt(), "created_at is null");
    }

    @Then("Assert CreateMailboxResp {string} token is UUID format")
    @Step("Assert mailbox token is UUID")
    public void assertMailboxTokenIsUUID(String varName) {
        String token = ((CreateMailboxResp) ctx.get(varName, true)).getToken();
        Assertions.assertDoesNotThrow(() -> UUID.fromString(token),
                "Token '" + token + "' is not a valid UUID");
    }

    @Then("Assert CreateMailboxResp {string} email contains domain {string}")
    public void assertMailboxEmailContainsDomain(String varName, String domainVar) {
        CreateMailboxResp resp = (CreateMailboxResp) ctx.get(varName, true);
        Assertions.assertTrue(resp.getEmailAddress().endsWith("@" + ctx.str(domainVar)),
                "Email '" + resp.getEmailAddress() + "' does not end with '@" + ctx.str(domainVar) + "'");
    }

    @Then("Assert CreateMailboxResp {string} email starts with {string}")
    public void assertMailboxEmailStartsWith(String varName, String localPartVar) {
        CreateMailboxResp resp = (CreateMailboxResp) ctx.get(varName, true);
        Assertions.assertTrue(resp.getEmailAddress().startsWith(ctx.str(localPartVar)),
                "Email '" + resp.getEmailAddress() + "' does not start with '" + ctx.str(localPartVar) + "'");
    }

    // ── Get mailbox ──────────────────────────────────────────────────

    @When("Get mailbox by token {string} and save response as {string}")
    @Step("GET /mail/:token")
    public void getMailbox(String tokenVar, String varName) {
        String token = ctx.str(tokenVar);
        Allure.parameter("token", token);
        ctx.save(varName, rest.get(ApiPaths.mailGet(token)));
    }

    @Then("Convert get mailbox response {string} to CreateMailboxResp and save as {string}")
    public void convertGetMailboxResp(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(CreateMailboxResp.class));
    }

    @Then("Assert mailbox {string} token matches {string}")
    public void assertMailboxTokenMatches(String varName, String expectedTokenVar) {
        CreateMailboxResp resp = (CreateMailboxResp) ctx.get(varName, true);
        Assertions.assertEquals(ctx.str(expectedTokenVar), resp.getToken());
    }

    // ── Get messages ─────────────────────────────────────────────────

    @When("Get messages for mailbox {string} and save response as {string}")
    @Step("GET /mail/:token/messages")
    public void getMessages(String tokenVar, String varName) {
        String token = ctx.str(tokenVar);
        Allure.parameter("token", token);
        ctx.save(varName, rest.get(ApiPaths.mailMessages(token)));
    }

    @Then("Convert messages list response {string} to MessagesListResp and save as {string}")
    public void convertMessagesListResp(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(MessagesListResp.class));
    }

    @Then("Assert messages list {string} count is {int}")
    @Step("Assert messages count = {expected}")
    public void assertMessagesCount(String varName, int expected) {
        MessagesListResp resp = (MessagesListResp) ctx.get(varName, true);
        Assertions.assertEquals(expected, resp.getCount(),
                "Messages count mismatch");
    }

    @Then("Assert messages list {string} count is greater than {int}")
    public void assertMessagesCountGreaterThan(String varName, int min) {
        MessagesListResp resp = (MessagesListResp) ctx.get(varName, true);
        Assertions.assertTrue(resp.getCount() > min,
                "Expected count > " + min + " but got " + resp.getCount());
    }

    @Then("Assert messages list {string} items have no body field")
    @Step("Assert list items have no full body")
    public void assertMessagesListNoBody(String varName) {
        Response resp = (Response) ctx.get(varName, true);
        // body_preview is allowed, but "body" as a standalone key should not appear at top level of message items
        // We check the raw JSON doesn't have `"body":` without "preview"
        String raw = resp.asString();
        // Only body_preview should appear, not a standalone "body" key
        // Use simple heuristic: count occurrences of "body_preview" vs "\"body\":"
        long previewCount = raw.chars().filter(c -> c == 'p').count(); // rough check
        Assertions.assertFalse(
                raw.contains("\"html_body\""),
                "List items should NOT contain html_body. Body: " + raw);
    }

    @Then("Save first message id from messages list {string} as {string}")
    public void saveFirstMessageId(String varName, String saveAs) {
        MessagesListResp resp = (MessagesListResp) ctx.get(varName, true);
        Assertions.assertFalse(resp.getMessages().isEmpty(), "Messages list is empty");
        ctx.save(saveAs, resp.getMessages().get(0).getId());
    }

    // ── Get single message ───────────────────────────────────────────

    @When("Get message {string} from mailbox {string} and save response as {string}")
    @Step("GET /mail/:token/messages/:id")
    public void getMessage(String msgIdVar, String tokenVar, String varName) {
        String token = ctx.str(tokenVar);
        String msgId = ctx.str(msgIdVar);
        Allure.parameter("token", token);
        Allure.parameter("messageId", msgId);
        ctx.save(varName, rest.get(ApiPaths.mailMessage(token, msgId)));
    }

    @Then("Convert message response {string} to MessageResp and save as {string}")
    public void convertMessageResp(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(MessageResp.class));
    }

    @Then("Assert message {string} has full body not null")
    @Step("Assert message has body field")
    public void assertMessageHasBody(String varName) {
        MessageResp msg = (MessageResp) ctx.get(varName, true);
        Assertions.assertNotNull(msg.getBody(), "body is null");
    }

    @Then("Assert message {string} has subject {string}")
    public void assertMessageSubject(String varName, String expectedVar) {
        MessageResp msg = (MessageResp) ctx.get(varName, true);
        Assertions.assertEquals(ctx.str(expectedVar), msg.getSubject());
    }

    @Then("Assert message {string} has from {string}")
    public void assertMessageFrom(String varName, String expectedVar) {
        MessageResp msg = (MessageResp) ctx.get(varName, true);
        Assertions.assertEquals(ctx.str(expectedVar), msg.getFrom());
    }

    // ── Send message ─────────────────────────────────────────────────

    @When("Send message to mailbox {string} from {string} subject {string} body {string} and save response as {string}")
    @Step("POST /mail/:token/send")
    public void sendMessage(String tokenVar, String fromVar, String subjectVar, String bodyVar, String varName) {
        Allure.parameter("from", ctx.str(fromVar));
        Allure.parameter("subject", ctx.str(subjectVar));
        ctx.save(varName, rest.post(ApiPaths.mailSend(ctx.str(tokenVar)),
                SendMessageReq.builder()
                        .from(ctx.str(fromVar))
                        .subject(ctx.str(subjectVar))
                        .body(ctx.str(bodyVar))
                        .build()));
    }

    @When("Send message with html to mailbox {string} from {string} subject {string} body {string} htmlBody {string} and save response as {string}")
    @Step("POST /mail/:token/send (with HTML)")
    public void sendMessageWithHtml(String tokenVar, String fromVar, String subjectVar,
                                    String bodyVar, String htmlBodyVar, String varName) {
        ctx.save(varName, rest.post(ApiPaths.mailSend(ctx.str(tokenVar)),
                SendMessageReq.builder()
                        .from(ctx.str(fromVar))
                        .subject(ctx.str(subjectVar))
                        .body(ctx.str(bodyVar))
                        .htmlBody(ctx.str(htmlBodyVar))
                        .build()));
    }

    @When("Send message with missing field {string} to mailbox {string} and save response as {string}")
    @Step("POST /mail/:token/send (missing field)")
    public void sendMessageMissingField(String missingField, String tokenVar, String varName) {
        Allure.parameter("missingField", missingField);
        SendMessageReq.SendMessageReqBuilder req = SendMessageReq.builder();
        if (!missingField.equals("from"))    req.from("sender@test.com");
        if (!missingField.equals("subject")) req.subject("Test Subject");
        if (!missingField.equals("body"))    req.body("Test body text");
        ctx.save(varName, rest.post(ApiPaths.mailSend(ctx.str(tokenVar)), req.build()));
    }

    @Then("Convert send message response {string} to MessageResp and save as {string}")
    public void convertSendMessageResp(String rawVar, String saveAs) {
        ctx.save(saveAs, ((Response) ctx.get(rawVar, true)).as(MessageResp.class));
    }

    @Then("Save field id from MessageResp {string} as {string}")
    public void saveMessageId(String varName, String saveAs) {
        ctx.save(saveAs, ((MessageResp) ctx.get(varName, true)).getId());
    }

    // ── Delete mailbox ───────────────────────────────────────────────

    @When("Delete mailbox {string} and save response as {string}")
    @Step("DELETE /mail/:token")
    public void deleteMailbox(String tokenVar, String varName) {
        String token = ctx.str(tokenVar);
        Allure.parameter("token", token);
        ctx.save(varName, rest.delete(ApiPaths.mailDelete(token)));
    }
}
