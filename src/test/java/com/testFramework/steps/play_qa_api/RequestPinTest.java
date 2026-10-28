package com.testFramework.steps.play_qa_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.testFramework.core.RestHandler;
import com.testFramework.support.LocalBaseUrlServer;
import com.testFramework.play_qa_api.models.createUser.response.CreateUserResp;
import com.testFramework.play_qa_api.models.createUser.response.UserResp;
import com.testFramework.play_qa_api.models.createUser.response.UsersListResp;
import com.testFramework.play_qa_api.models.error.ErrorResp;
import com.testFramework.play_qa_api.models.health.HealthResp;
import com.testFramework.play_qa_api.models.login.LoginResp;
import com.testFramework.play_qa_api.models.mail.CreateMailboxResp;
import com.testFramework.play_qa_api.models.mail.MessageResp;
import com.testFramework.play_qa_api.models.mail.MessagesListResp;
import com.testFramework.play_qa_api.models.options.UserOptionsResp;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Golden-master pin of the HTTP traffic every request-producing step sends, recorded by a
 * local server: method, raw path, raw query, Authorization (absent vs value), Content-Type,
 * X-Request-ID and the JSON body (keys sorted, random Generator output normalized).
 * Also records which scenario-context keys existed when the request left, and what the step saved.
 */
class RequestPinTest {

    record Recorded(String method, String path, String query, String authorization,
                    String contentType, String requestId, String body, String contextAtRequest) { }

    private static final String SCENARIO = "request pin";
    private static final String OUT = "out";
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    /**
     * Generator shapes → stable placeholders. Only whole quoted string VALUES (after ": " or inside an
     * array) are replaced — never keys — so literals and field names stay exact.
     */
    private static final String VALUE = "(?<=: |\\[ |, )";
    private static final Map<Pattern, String> RANDOM_SHAPES = Map.of(
            Pattern.compile(VALUE + "\"[a-z0-9]{10}@play-qa\\.com\""), "\"<email>\"",
            Pattern.compile(VALUE + "\"user_[a-z0-9]{8}\""), "\"<username>\"",
            Pattern.compile(VALUE + "\"Pass_[a-z0-9]{10}!1\""), "\"<password>\"",
            Pattern.compile(VALUE + "\"Test[a-z]{6}\""), "\"<firstName>\"",
            Pattern.compile(VALUE + "\"User[a-z]{6}\""), "\"<lastName>\"",
            Pattern.compile(VALUE + "\"\\+1[0-9]{10}\""), "\"<phone>\"",
            // alphanumericString(n) for the lengths this test asks for (7, 13, 21)
            Pattern.compile(VALUE + "\"[a-z0-9]{7}\""), "\"<alnum7>\"",
            Pattern.compile(VALUE + "\"[a-z0-9]{13}\""), "\"<alnum13>\"",
            Pattern.compile(VALUE + "\"[a-z0-9]{21}\""), "\"<alnum21>\"");

    /** Keys the fixture pre-saves; the golden shows only keys a step added before its request left. */
    private static Set<String> fixtureKeys;

    // CPD-OFF: canned-reply and fixture tables are data, not logic
    private static final Map<String, String> CANNED = Map.ofEntries(
            Map.entry("/api/v1/login", "{\"access_token\":\"tok-from-login\"}"),
            Map.entry("/canned/login", "{\"access_token\":\"tok-1\"}"),
            Map.entry("/canned/createUser", "{\"id\":\"id-1\"}"),
            Map.entry("/canned/user", "{\"id\":\"id-2\"}"),
            Map.entry("/canned/usersList", "{\"page\":3}"),
            Map.entry("/canned/error", "{\"request_id\":\"rq-1\"}"),
            Map.entry("/canned/mailbox", "{\"token\":\"mt-1\"}"),
            Map.entry("/canned/messages", "{\"count\":4}"),
            Map.entry("/canned/message", "{\"id\":\"msg-1\"}"),
            Map.entry("/canned/health", "{\"status\":\"ok\"}"),
            Map.entry("/canned/options", "{\"allowed_methods\":[\"GET\"]}"));
    // CPD-ON

    private static LocalBaseUrlServer server;
    private static final List<Recorded> recorded = new CopyOnWriteArrayList<>();

    private final BaseSteps ctx = new BaseSteps();
    private AccountsSteps accounts;
    private MailSteps mail;
    private HealthSteps health;
    private OptionsSteps options;
    private RestHandler rest;
    private StringBuilder out;

    @BeforeAll
    static void startServer() throws IOException {
        server = LocalBaseUrlServer.start(RequestPinTest::record);
    }

    @AfterAll
    static void stopServer() {
        server.close();
    }

    @BeforeEach
    void setUp() {
        ScenarioContext.scenarioName = SCENARIO;
        recorded.clear();
        accounts = new AccountsSteps();
        mail = new MailSteps();
        health = new HealthSteps();
        options = new OptionsSteps();
        rest = new RestHandler();
        out = new StringBuilder();
        // CPD-OFF: fixture table (data)
        Map.ofEntries(
                Map.entry("uid", "u-123"), Map.entry("tok", "t-abc"), Map.entry("tokPrefixed", "Bearer t-pre"),
                Map.entry("tokEmpty", ""), Map.entry("email", "E-mail@x.io"), Map.entry("uname", "Uname-1"),
                Map.entry("pass", "P@ss-1"), Map.entry("first", "First-N"), Map.entry("last", "Last-N"),
                Map.entry("gender", "female"), Map.entry("status", "employed"), Map.entry("theme", "Dark-T"),
                Map.entry("interests", "go,java"), Map.entry("interestsSpaced", " go , java "),
                Map.entry("interestsBlank", "  "), Map.entry("page", "2"), Map.entry("perPage", "5"),
                Map.entry("mboxTok", "mb-tok"), Map.entry("localPart", "Local-Part"), Map.entry("domain", "dom.example"),
                Map.entry("msgId", "m-1"), Map.entry("from", "From@x.io"), Map.entry("subject", "Subj-1"),
                Map.entry("body", "Body-1"), Map.entry("html", "<b>Hi</b>"), Map.entry("reqId", "req-1"))
                .forEach(ctx::save);
        // CPD-ON
        fixtureKeys = Set.copyOf(ScenarioContext.local.get(SCENARIO).keySet());
    }

    @AfterEach
    void clearContext() {
        ScenarioContext.global.clear();
        ScenarioContext.local.clear();
        ScenarioContext.scenarioName = null;
    }

    // ── request pins ───────────────────────────────────────────────

    @Test
    void everyRequestProducingStepSendsThePinnedRequest() {
        // Login
        pin("login token", () -> accounts.loginAndSaveToken("email", "pass", OUT));
        pin("login raw", () -> accounts.loginAndSaveResponse("email", "pass", OUT));
        pin("login no body", () -> accounts.loginEmptyBody(OUT));

        // Create
        pin("create", () -> accounts.createUser("email", "uname", "pass", "first", "last", OUT));
        pin("create literals", () -> accounts.createUser("Lit-E", "Lit-U", "Lit-P", "Lit-F", "Lit-L", OUT));
        pin("create gender", () -> accounts.createUserWithGender("email", "uname", "pass", "first", "last", "gender", OUT));
        pin("create employment", () -> accounts.createUserWithEmploymentStatus("status", OUT));
        pin("create theme", () -> accounts.createUserWithTheme("theme", OUT));
        pin("create username length 13", () -> accounts.createUserWithUsernameLength(13, OUT));
        pin("create interests csv", () -> accounts.createUserWithInterests("interests", OUT));
        pin("create interests spaced", () -> accounts.createUserWithInterests("interestsSpaced", OUT));
        pin("create interests blank", () -> accounts.createUserWithInterests("interestsBlank", OUT));
        pin("create interests empty literal", () -> accounts.createUserWithInterests("", OUT));
        pin("create bio length 21", () -> accounts.createUserWithBioLength(21, OUT));
        pin("create firstName length 7", () -> accounts.createUserWithFirstNameLength(7, OUT));
        pin("create firstName length 0", () -> accounts.createUserWithFirstNameLength(0, OUT));
        pin("create firstName length -1", () -> accounts.createUserWithFirstNameLength(-1, OUT));
        pin("create minimal", () -> accounts.createMinimalUser(OUT));
        pinSaved("generatedEmail");
        pinSaved("generatedPassword");
        pin("create full", () -> accounts.createFullUser(OUT));

        // Get / list / exists
        pin("get by id", () -> accounts.getUserById("uid", OUT));
        pin("get by literal id", () -> accounts.getUserById("Lit-Id", OUT));
        pin("list", () -> accounts.getUsersList(OUT));
        pin("list paged", () -> accounts.getUsersListPaged("page", "perPage", OUT));
        pin("exists HEAD", () -> accounts.checkUserExists("uid", OUT));
        pin("exists GET", () -> accounts.checkUserExistsGet("uid", OUT));

        // Update / patch (bearer, no-auth)
        pin("update", () -> accounts.updateUser("uid", "tok", "first", "last", "email", "uname", OUT));
        pin("update prefixed token", () -> accounts.updateUser("uid", "tokPrefixed", "first", "last", "email", "uname", OUT));
        pin("update empty token", () -> accounts.updateUser("uid", "tokEmpty", "first", "last", "email", "uname", OUT));
        pin("update no auth", () -> accounts.updateUserNoAuth("uid", OUT));
        pin("update invalid email", () -> accounts.updateUserInvalidEmail("uid", "tok", OUT));
        pin("patch firstName", () -> accounts.patchUser("uid", "tok", "first", OUT));
        pin("patch email", () -> accounts.patchUserEmail("uid", "tok", "email", OUT));
        pin("patch username", () -> accounts.patchUserUsername("uid", "tok", "uname", OUT));
        pin("patch no auth", () -> accounts.patchUserNoAuth("uid", OUT));
        pin("patch empty body", () -> accounts.patchUserEmptyBody("uid", "tok", OUT));

        // Delete / logout (bearer, no-auth)
        pin("delete", () -> accounts.deleteUser("uid", "tok", OUT));
        pin("delete prefixed token", () -> accounts.deleteUser("uid", "tokPrefixed", OUT));
        pin("delete no auth", () -> accounts.deleteUserNoAuth("uid", OUT));
        pin("logout", () -> accounts.logoutUser("uid", "tok", OUT));
        pin("logout empty token", () -> accounts.logoutUser("uid", "tokEmpty", OUT));
        pin("logout no auth", () -> accounts.logoutUserNoAuth("uid", OUT));

        // Raw Authorization header — passed verbatim, never resolved from context
        for (String raw : List.of("", "Bearer ", "Bearer abc", "tok")) {
            String q = "[" + raw + "]";
            pin("raw delete " + q, () -> accounts.deleteUserRawAuth("uid", raw, OUT));
            pin("raw patch " + q, () -> accounts.patchUserRawAuth("uid", raw, "first", OUT));
            pin("raw update " + q, () -> accounts.updateUserRawAuth("uid", raw, OUT));
            pin("raw logout " + q, () -> accounts.logoutUserRawAuth("uid", raw, OUT));
        }

        // Mail
        pin("mailbox empty", () -> mail.createMailboxEmpty(OUT));
        pin("mailbox local part", () -> mail.createMailboxWithLocalPart("localPart", OUT));
        pin("mailbox domain", () -> mail.createMailboxWithDomain("domain", OUT));
        pin("mailbox domain + local part", () -> mail.createMailboxWithDomainAndLocalPart("domain", "localPart", OUT));
        pin("mailbox get", () -> mail.getMailbox("mboxTok", OUT));
        pin("messages", () -> mail.getMessages("mboxTok", OUT));
        pin("message", () -> mail.getMessage("msgId", "mboxTok", OUT));
        pin("send", () -> mail.sendMessage("mboxTok", "from", "subject", "body", OUT));
        pin("send html", () -> mail.sendMessageWithHtml("mboxTok", "from", "subject", "body", "html", OUT));
        for (String missing : List.of("from", "subject", "body", "none")) {
            pin("send missing " + missing, () -> mail.sendMessageMissingField(missing, "mboxTok", OUT));
        }
        pin("mailbox delete", () -> mail.deleteMailbox("mboxTok", OUT));

        // Health / options
        pin("health", () -> health.getHealth(OUT));
        pin("health request id", () -> health.getHealthWithRequestId("reqId", OUT));
        pin("options", () -> options.sendOptions(OUT));

        GoldenFile.assertMatches("requests.txt", out.toString());
    }

    @Test
    void createMinimalUserSavesCredentialsBeforeThePostLeaves() {
        accounts.createMinimalUser(OUT);

        assertEquals(1, recorded.size());
        String keys = recorded.get(0).contextAtRequest();
        assertTrue(keys.contains("generatedEmail") && keys.contains("generatedPassword"), keys);
        assertTrue(recorded.get(0).body().contains("\"email\":\"" + ctx.str("generatedEmail") + "\""));
        assertTrue(recorded.get(0).body().contains("\"password\":\"" + ctx.str("generatedPassword") + "\""));
    }

    // ── convert pins ───────────────────────────────────────────────

    record Convert(String path, BiConsumer<String, String> step, Class<?> type, Function<Object, Object> field, Object expected) { }

    @Test
    void everyConvertStepStoresTheTypedDto() {
        // CPD-OFF: one row per Convert step (data)
        List<Convert> converts = List.of(
                new Convert("/canned/login", accounts::convertLoginResponse, LoginResp.class, o -> ((LoginResp) o).getAccessToken(), "tok-1"),
                new Convert("/canned/createUser", accounts::convertCreateUserResponse, CreateUserResp.class, o -> ((CreateUserResp) o).getId(), "id-1"),
                new Convert("/canned/user", accounts::convertUserResponse, UserResp.class, o -> ((UserResp) o).getId(), "id-2"),
                new Convert("/canned/usersList", accounts::convertUsersListResponse, UsersListResp.class, o -> ((UsersListResp) o).getPage(), 3),
                new Convert("/canned/error", accounts::convertErrorResponse, ErrorResp.class, o -> ((ErrorResp) o).getRequestId(), "rq-1"),
                new Convert("/canned/mailbox", mail::convertCreateMailboxResp, CreateMailboxResp.class, o -> ((CreateMailboxResp) o).getToken(), "mt-1"),
                new Convert("/canned/mailbox", mail::convertGetMailboxResp, CreateMailboxResp.class, o -> ((CreateMailboxResp) o).getToken(), "mt-1"),
                new Convert("/canned/messages", mail::convertMessagesListResp, MessagesListResp.class, o -> ((MessagesListResp) o).getCount(), 4),
                new Convert("/canned/message", mail::convertMessageResp, MessageResp.class, o -> ((MessageResp) o).getId(), "msg-1"),
                new Convert("/canned/message", mail::convertSendMessageResp, MessageResp.class, o -> ((MessageResp) o).getId(), "msg-1"),
                new Convert("/canned/health", health::convertHealthResponse, HealthResp.class, o -> ((HealthResp) o).getStatus(), "ok"),
                new Convert("/canned/options", options::convertOptionsResp, UserOptionsResp.class, o -> ((UserOptionsResp) o).getAllowedMethods(), List.of("GET")));

        // CPD-ON

        for (Convert c : converts) {
            ctx.save("raw", rest.get(c.path()));
            c.step().accept("raw", "dto");
            Object dto = ctx.get("dto", true);
            assertEquals(c.type(), dto.getClass(), c.path());
            assertEquals(c.expected(), c.field().apply(dto), c.path());
            // rows share a canned reply; a stale "dto" would let a no-op Convert step pass
            ScenarioContext.local.get(SCENARIO).remove("dto");
        }
        assertEquals(12, converts.size(), "every Convert step is pinned");
    }

    @Test
    void convertOfMissingRawResponseFailsWithContextMessage() {
        AssertionFailedError e = assertThrows(AssertionFailedError.class,
                () -> accounts.convertLoginResponse("missing", "dto"));
        assertEquals("Context variable 'missing' (local) not found.", e.getMessage());
    }

    // ── plumbing ───────────────────────────────────────────────────

    private void pin(String label, Runnable step) {
        recorded.clear();
        step.run();
        out.append("### ").append(label).append('\n');
        for (Recorded r : recorded) out.append(format(r));
        appendSaved(OUT);
        ScenarioContext.local.get(SCENARIO).remove(OUT);
        out.append('\n');
    }

    private void pinSaved(String key) {
        out.append("### saved ").append(key).append('\n');
        appendSaved(key);
        ScenarioContext.local.get(SCENARIO).remove(key);
        out.append('\n');
    }

    private void appendSaved(String key) {
        Object saved = ScenarioContext.local.get(SCENARIO).get(key);
        String shown = saved instanceof Response r ? "Response " + r.getStatusCode()
                : saved == null ? "<absent>" : "\"" + saved + "\"";
        out.append(normalize("  saved " + key + ": " + shown)).append('\n');
    }

    private static String format(Recorded r) {
        return r.method() + " " + r.path() + '\n'
                + "  query: " + quoted(r.query()) + '\n'
                + "  authorization: " + quoted(r.authorization()) + '\n'
                + "  content-type: " + quoted(r.contentType()) + '\n'
                + "  x-request-id: " + quoted(r.requestId()) + '\n'
                + "  context keys added before request: " + r.contextAtRequest() + '\n'
                + "  body: " + body(r.body()) + '\n';
    }

    private static String quoted(String v) {
        return v == null ? "<absent>" : "\"" + v + "\"";
    }

    private static String body(String raw) {
        if (raw.isEmpty()) return "<empty>";
        try {
            return normalize(JSON.writeValueAsString(JSON.readValue(raw, Object.class)));
        } catch (JsonProcessingException e) {
            return "<non-json> " + raw;
        }
    }

    private static String normalize(String text) {
        // the shapes match disjoint whole quoted strings, so application order does not matter
        String result = text;
        for (Map.Entry<Pattern, String> shape : RANDOM_SHAPES.entrySet()) {
            result = shape.getKey().matcher(result).replaceAll(shape.getValue());
        }
        return result;
    }

    private static String addedKeys(Set<String> keys) {
        TreeSet<String> added = new TreeSet<>(keys);
        added.removeAll(fixtureKeys);
        return added.toString();
    }

    private static void record(HttpExchange exchange) throws IOException {
        Map<String, Object> scenario = ScenarioContext.local.getOrDefault(SCENARIO, Map.of());
        recorded.add(new Recorded(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getRawPath(),
                exchange.getRequestURI().getRawQuery(),
                exchange.getRequestHeaders().getFirst("Authorization"),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                exchange.getRequestHeaders().getFirst("X-Request-ID"),
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
                addedKeys(scenario.keySet())));
        LocalBaseUrlServer.replyJson(exchange, CANNED.getOrDefault(exchange.getRequestURI().getPath(), "{}"));
    }
}
