package com.testFramework.steps.play_qa_api;

import com.testFramework.play_qa_api.models.mail.CreateMailboxResp;
import com.testFramework.play_qa_api.models.mail.MessageResp;
import com.testFramework.play_qa_api.models.mail.MessagesListResp;
import com.testFramework.support.StepContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.testFramework.support.Fake.*;
import static com.testFramework.support.StepContext.failsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline: the MailSteps assertions and field-savers against DTOs parsed from canned JSON.
 * The requests these steps send are pinned by {@link RequestPinTest}.
 */
@ExtendWith(StepContext.class)
class MailStepsTest {

    private static final String TOKEN = "3f2b8c1e-9a4d-4e6f-8b7a-1c2d3e4f5a6b";
    private static final String MAILBOX = "{'id':'mb-1','token':'" + TOKEN + "','email_address':'neo@mail.test',"
            + "'domain':'mail.test','expires_at':'2030','created_at':'2029'}";
    private static final String MESSAGE = "{'id':'m-1','from':'a@b.c','subject':'Hi','body':'Hello'}";
    private static final String LIST = "{'count':2,'messages':[{'id':'m-1','body_preview':'He'},{'id':'m-2'}]}";

    private final MailSteps mail = new MailSteps();
    private final BaseSteps ctx = new BaseSteps();

    @Test
    void createMailboxFieldsCheckNamesEachMissingField() {
        ctx.save("box", dto(CreateMailboxResp.class, MAILBOX));
        assertDoesNotThrow(() -> mail.assertCreateMailboxFields("box"));

        for (String field : List.of("id", "token", "email_address", "domain", "expires_at", "created_at")) {
            ctx.save("box", dto(CreateMailboxResp.class, without(MAILBOX, field)));
            failsWith(field + " is null", () -> mail.assertCreateMailboxFields("box"));
        }
    }

    @Test
    void mailboxTokenMustBeAUuidAndMatchTheExpectedToken() {
        ctx.save("box", dto(CreateMailboxResp.class, MAILBOX));
        ctx.save("bad", dto(CreateMailboxResp.class, with(MAILBOX, "token", "'not-a-uuid'")));
        ctx.save("myToken", TOKEN);

        assertAll(
                () -> mail.assertMailboxTokenIsUUID("box"),
                () -> mail.assertMailboxTokenMatches("box", "myToken"));
        failsWith("Token 'not-a-uuid' is not a valid UUID", () -> mail.assertMailboxTokenIsUUID("bad"));
        failsWith("expected: <other> but was: <" + TOKEN + ">", () -> mail.assertMailboxTokenMatches("box", "other"));
    }

    // CPD-OFF: store-DTO / pass / fail tests, each for a different step. With identifiers and literals
    // ignored CPD sees only their Arrange-Act-Assert shape, which they share with the other step tests.
    @Test
    void mailboxEmailChecksResolveDomainAndLocalPartFromContext() {
        ctx.save("box", dto(CreateMailboxResp.class, MAILBOX));
        ctx.save("myDomain", "mail.test");
        ctx.save("myLocal", "neo");

        assertAll(
                () -> mail.assertMailboxEmailContainsDomain("box", "myDomain"),
                () -> mail.assertMailboxEmailStartsWith("box", "myLocal"));
        failsWith("Email 'neo@mail.test' does not end with '@test'",
                () -> mail.assertMailboxEmailContainsDomain("box", "test"));
        failsWith("Email 'neo@mail.test' does not start with 'trinity'",
                () -> mail.assertMailboxEmailStartsWith("box", "trinity"));
    }

    @Test
    void messagesCountChecks() {
        ctx.save("list", dto(MessagesListResp.class, LIST));

        assertAll(
                () -> mail.assertMessagesCount("list", 2),
                () -> mail.assertMessagesCountGreaterThan("list", 1));
        failsWith("Messages count mismatch ==> expected: <3> but was: <2>", () -> mail.assertMessagesCount("list", 3));
        failsWith("Expected count > 2 but got 2", () -> mail.assertMessagesCountGreaterThan("list", 2));
    }

    @Test
    void listItemsMustCarryNeitherBodyNorHtmlBody() {
        ctx.save("clean", jsonResponse(LIST));
        ctx.save("noList", jsonResponse("{'count':0}"));
        ctx.save("withBody", jsonResponse(with(LIST, "messages", "[{'id':'m-1'},{'id':'m-2','body':'full'}]")));
        ctx.save("withHtml", jsonResponse(with(LIST, "messages", "[{'id':'m-3','html_body':'<b>x</b>'}]")));

        assertDoesNotThrow(() -> mail.assertMessagesListNoBody("clean"));
        failsWith("messages list is null", () -> mail.assertMessagesListNoBody("noList"));
        failsWith("List item m-2 should NOT contain full body: full", () -> mail.assertMessagesListNoBody("withBody"));
        failsWith("List item m-3 should NOT contain html_body: <b>x</b>", () -> mail.assertMessagesListNoBody("withHtml"));
    }

    @Test
    void saveFirstMessageIdTakesTheFirstAndFailsOnAnEmptyList() {
        ctx.save("list", dto(MessagesListResp.class, LIST));
        ctx.save("empty", dto(MessagesListResp.class, "{'count':0,'messages':[]}"));

        mail.saveFirstMessageId("list", "first");

        assertEquals("m-1", ctx.str("first"));
        failsWith("Messages list is empty", () -> mail.saveFirstMessageId("empty", "none"));
        assertEquals("none", ctx.str("none"));
    }

    @Test
    void singleMessageChecks() {
        ctx.save("msg", dto(MessageResp.class, MESSAGE));
        ctx.save("noBody", dto(MessageResp.class, without(MESSAGE, "body")));
        ctx.save("mySubject", "Hi");
        ctx.save("myFrom", "a@b.c");

        assertAll(
                () -> mail.assertMessageHasBody("msg"),
                () -> mail.assertMessageSubject("msg", "mySubject"),
                () -> mail.assertMessageFrom("msg", "myFrom"));
        failsWith("body is null", () -> mail.assertMessageHasBody("noBody"));
        failsWith("expected: <Bye> but was: <Hi>", () -> mail.assertMessageSubject("msg", "Bye"));
        failsWith("expected: <x@y.z> but was: <a@b.c>", () -> mail.assertMessageFrom("msg", "x@y.z"));
    }

    // CPD-ON

    @Test
    void fieldSaversCopyTheFieldUnderTheNewName() {
        ctx.save("box", dto(CreateMailboxResp.class, MAILBOX));
        ctx.save("msg", dto(MessageResp.class, MESSAGE));

        mail.saveMailboxToken("box", "a");
        mail.saveMailboxEmail("box", "b");
        mail.saveMessageId("msg", "c");

        assertEquals(List.of(TOKEN, "neo@mail.test", "m-1"), List.of(ctx.str("a"), ctx.str("b"), ctx.str("c")));
    }
}
