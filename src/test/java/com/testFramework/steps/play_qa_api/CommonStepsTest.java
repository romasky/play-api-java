package com.testFramework.steps.play_qa_api;

import com.testFramework.support.AllureFixture;
import com.testFramework.support.StepContext;
import io.cucumber.java.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.testFramework.support.Fake.response;
import static com.testFramework.support.StepContext.failsWith;
import static org.junit.jupiter.api.Assertions.*;

/** Offline: the generic steps — data generation, context ops and assertions on a stored response. */
@ExtendWith(StepContext.class)
class CommonStepsTest {

    private final CommonSteps common = new CommonSteps();
    private final BaseSteps ctx = new BaseSteps();

    // ── hook ─────────────────────────────────────────────────────────

    @Test
    void beforeHookMakesTheScenarioTheScopeOfLocalVariables() throws Exception {
        Scenario scenario = scenarioNamed("Login works");

        String hookName = AllureFixture.nameAfter(() -> common.before(scenario));

        assertEquals("Setup: init scenario context", hookName);
        assertEquals("Login works", ScenarioContext.scenarioName);
        ctx.save("k", "v");
        assertEquals("v", ScenarioContext.local.get("Login works").get("k"));
    }

    /** Cucumber builds Scenario from a TestCaseState behind a package-private constructor. */
    private static Scenario scenarioNamed(String name) throws ReflectiveOperationException {
        Class<?> state = Class.forName("io.cucumber.core.backend.TestCaseState");
        Object proxy = Proxy.newProxyInstance(state.getClassLoader(), new Class<?>[]{state},
                (p, method, args) -> "getName".equals(method.getName()) ? name : null);
        Constructor<Scenario> constructor = Scenario.class.getDeclaredConstructor(state);
        constructor.setAccessible(true);
        return constructor.newInstance(proxy);
    }

    // ── data generation ──────────────────────────────────────────────

    @Test
    void saveStringStoresTheLiteral() {
        common.saveString("hello", "greeting_g");

        assertEquals("hello", ScenarioContext.global.get("greeting"));
    }

    @Test
    void everyGenerateStepSavesAValueOfTheGeneratorsShape() {
        Map<String, Consumer<String>> shapes = new LinkedHashMap<>();
        shapes.put("[a-z0-9]{10}@play-qa\\.com", common::generateEmail);
        shapes.put("user_[a-z0-9]{8}", common::generateUsername);
        shapes.put("Pass_[a-z0-9]{10}!1", common::generatePassword);
        shapes.put("Test[a-z]{6}", common::generateFirstName);
        shapes.put("User[a-z]{6}", common::generateLastName);
        shapes.put("[a-z0-9]{8}@example\\.com", common::generateSenderEmail);
        shapes.put("Subject [a-z0-9]{6}", common::generateMessageSubject);
        shapes.put("Body [a-z0-9]{10}", common::generateMessageBody);
        shapes.put("notanemail_[a-z0-9]{4}", common::generateInvalidEmail);
        shapes.put("[a-z0-9]{4}", common::generateShortPassword);
        shapes.put("[0-9]{24}", common::generateFakeMongoId);
        shapes.put("00000000-0000-0000-0000-[0-9]{12}", common::generateFakeUuid);
        shapes.put("\\+1[0-9]{10}", common::generatePhoneNumber);
        shapes.put("[a-z]{9}", key -> common.generateString(9, "true", "false", key));
        shapes.put("[0-9]{9}", key -> common.generateString(9, "false", "true", key));
        shapes.put("[a-z0-9]{6}", key -> common.generateStringOfLength(6, key));

        shapes.forEach((shape, step) -> {
            step.accept("generated");
            assertTrue(ctx.str("generated").matches(shape), shape + " vs " + ctx.str("generated"));
        });
    }

    @Test
    void generateStringWithBothPoolsMixesLettersAndDigits() {
        common.generateString(200, "true", "true", "mixed");

        assertTrue(ctx.str("mixed").matches("(?=.*[a-z])(?=.*[0-9])[a-z0-9]{200}"), ctx.str("mixed"));
    }

    @Test
    void saveCurrentDateSavesTodayAsIsoDate() {
        common.saveCurrentDate("today");

        assertEquals(LocalDate.now().toString(), ctx.str("today"));
    }

    // ── status code ──────────────────────────────────────────────────

    @Test
    void statusCodeStepPassesOnMatchAndFailsOtherwise() {
        ctx.save("resp", response(201, "made"));

        assertDoesNotThrow(() -> common.assertStatusCode(201, "resp"));
        failsWith("Unexpected status code. Body: made", () -> common.assertStatusCode(200, "resp"));
    }

    @Test
    void statusCodeOneOfAcceptsAnyListedCodeIgnoringSpaces() {
        ctx.save("resp", response(201, "made"));

        assertDoesNotThrow(() -> common.assertStatusCodeOneOf("200, 201", "resp"));
        failsWith("Status 201 not in allowed [200, 204]. Body: made",
                () -> common.assertStatusCodeOneOf("200 ,204", "resp"));
    }

    @Test
    void notServerErrorPassesBelow500AndFailsFrom500() {
        ctx.save("ok", response(499, "client"));
        ctx.save("bad", response(500, "boom"));

        assertDoesNotThrow(() -> common.assertNotServerError("ok"));
        failsWith("Expected non-5xx but got 500. Body: boom", () -> common.assertNotServerError("bad"));
    }

    // ── headers ──────────────────────────────────────────────────────

    @Test
    void headerEqualsComparesTheExactValue() {
        ctx.save("resp", response(200, "b", "X-Mode", "fast"));

        assertDoesNotThrow(() -> common.assertHeaderEquals("X-Mode", "fast", "resp"));
        failsWith("Header 'X-Mode' mismatch. Response: b ==> expected: <fas>",
                () -> common.assertHeaderEquals("X-Mode", "fas", "resp"));
    }

    @Test
    void headerContainsNeedsThePresentHeaderToContainTheText() {
        ctx.save("resp", response(200, "b", "Content-Type", "application/json; charset=utf-8"));

        assertDoesNotThrow(() -> common.assertHeaderContains("Content-Type", "json", "resp"));
        failsWith("Header 'Content-Type' value 'application/json; charset=utf-8' does not contain 'xml'",
                () -> common.assertHeaderContains("Content-Type", "xml", "resp"));
        failsWith("Header 'X-None' is missing", () -> common.assertHeaderContains("X-None", "json", "resp"));
    }

    @Test
    void headerPresentNeedsANonBlankValue() {
        ctx.save("resp", response(200, "b", "X-Id", "42", "X-Blank", " "));

        assertDoesNotThrow(() -> common.assertHeaderPresent("X-Id", "resp"));
        failsWith("Header 'X-None' is missing in response", () -> common.assertHeaderPresent("X-None", "resp"));
        failsWith("Header 'X-Blank' is blank", () -> common.assertHeaderPresent("X-Blank", "resp"));
    }

    // ── body ─────────────────────────────────────────────────────────

    @Test
    void bodyContainsResolvesTheExpectedTextFromContext() {
        ctx.save("resp", response(200, "{\"email\":\"a@b.c\"}"));
        ctx.save("email", "a@b.c");

        assertDoesNotThrow(() -> common.assertBodyContains("email", "resp"));
        assertDoesNotThrow(() -> common.assertBodyContains("\"email\"", "resp"));
        failsWith("Body does not contain 'x@y.z'. Body: {\"email\":\"a@b.c\"}",
                () -> common.assertBodyContains("x@y.z", "resp"));
    }

    @Test
    void bodyNotContainsTakesTheTextLiterally() {
        ctx.save("resp", response(200, "secret-hash"));
        ctx.save("word", "nothing-like-it");

        assertDoesNotThrow(() -> common.assertBodyNotContains("word", "resp"));
        failsWith("Body should NOT contain 'hash'. Body: secret-hash",
                () -> common.assertBodyNotContains("hash", "resp"));
    }

    // ── context values ───────────────────────────────────────────────

    @Test
    void notNullFailsOnMissingVariableAndOnTheTextNull() {
        ctx.save("id", "u1");
        ctx.save("nullish", "null");

        assertDoesNotThrow(() -> common.assertNotNull("id"));
        failsWith("Context variable 'missing' (local) not found.", () -> common.assertNotNull("missing"));
        failsWith("expected: not equal but was: <null>", () -> common.assertNotNull("nullish"));
    }

    @Test
    void equalsContainsAndRegexReadTheVariable() {
        ctx.save("name", "neo-42");

        assertDoesNotThrow(() -> common.assertEquals("name", "neo-42"));
        failsWith("expected: <neo> but was: <neo-42>", () -> common.assertEquals("name", "neo"));
        assertDoesNotThrow(() -> common.assertContains("name", "o-4"));
        failsWith("'neo-42' does not contain 'trinity'", () -> common.assertContains("name", "trinity"));
        assertDoesNotThrow(() -> common.assertMatchesRegex("name", "[a-z]+-\\d+"));
        failsWith("'neo-42' does not match regex '\\d+'", () -> common.assertMatchesRegex("name", "\\d+"));
    }

    // ── print ────────────────────────────────────────────────────────

    @Test
    void printResponseShowsStatusAndBodyOfAResponseOrTheValueOtherwise() {
        ctx.save("resp", response(404, "nope"));
        ctx.save("plain", "text");

        assertEquals(String.format("%n[resp] HTTP 404%nnope%n%n"), printed(() -> common.printResponse("resp")));
        assertEquals(String.format("%n[plain] = text%n%n"), printed(() -> common.printResponse("plain")));
        failsWith("Context variable 'gone' (local) not found.", () -> common.printResponse("gone"));
    }

    private static String printed(Runnable step) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            step.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
