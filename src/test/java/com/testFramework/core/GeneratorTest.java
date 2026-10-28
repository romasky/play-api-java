package com.testFramework.core;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    private static final String CYRILLIC = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    @RepeatedTest(20)
    void generatedValuesMatchTheShapesTheApiAccepts() {
        assertShape("[a-z0-9]{10}@play-qa\\.com", Generator::email);
        assertShape("user_[a-z0-9]{8}", Generator::username);
        assertShape("Pass_[a-z0-9]{10}!1", Generator::password);
        assertShape("Test[a-z]{6}", Generator::firstName);
        assertShape("User[a-z]{6}", Generator::lastName);
        assertShape("\\+1[0-9]{10}", Generator::phoneNumber);
        assertShape("[a-z0-9]{8}@example\\.com", Generator::senderEmail);
        assertShape("Subject [a-z0-9]{6}", Generator::messageSubject);
        assertShape("Body [a-z0-9]{10}", Generator::messageBody);
        assertShape("notanemail_[a-z0-9]{4}", Generator::invalidEmail);
        assertShape("[a-z0-9]{4}", Generator::shortPassword);
        assertShape("[0-9]{24}", Generator::fakeMongoId);
        assertShape("00000000-0000-0000-0000-[0-9]{12}", Generator::fakeUuid);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 255})
    void plainStringsHaveExactLength(int length) {
        assertTrue(Pattern.matches("[a-z]{" + length + "}", Generator.latinString(length)));
        assertTrue(Pattern.matches("[0-9]{" + length + "}", Generator.numericString(length)));
        assertTrue(Pattern.matches("[a-z0-9]{" + length + "}", Generator.alphanumericString(length)));
    }

    @Test
    void stringWithNoCharacterClassFallsBackToLatin() {
        assertTrue(Pattern.matches("[a-z]{50}", Generator.string(50, false, false, false, false, false)));
    }

    @Test
    void stringDrawsOnlyFromEachSelectedClass() {
        assertOnly("[a-z]", Generator.string(300, false, true, false, false, false));
        assertOnly("[0-9]", Generator.string(300, false, false, true, false, false));
        assertOnly(" ", Generator.string(30, false, false, false, true, false));
        assertOnly("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]", Generator.string(300, false, false, false, false, true));
        assertOnly("[" + CYRILLIC + "]", Generator.string(300, true, false, false, false, false));
    }

    @Test
    void stringCombinesAllSelectedClasses() {
        String s = Generator.string(3000, true, true, true, true, true);

        assertEquals(3000, s.length());
        assertTrue(s.matches(".*[a-z].*"), "latin");
        assertTrue(s.matches(".*[0-9].*"), "numeric");
        assertTrue(s.contains(" "), "spaces");
        assertTrue(s.matches(".*[!@#$%^&*].*"), "special");
        assertTrue(s.matches(".*[" + CYRILLIC + "].*"), "cyrillic");
    }

    @Test
    void cyrillicPoolIncludesYo() {
        assertTrue(Generator.string(3000, true, false, false, false, false).contains("ё"));
    }

    private static void assertShape(String regex, Supplier<String> generator) {
        String value = generator.get();
        assertTrue(Pattern.matches(regex, value), value + " !~ " + regex);
    }

    private static void assertOnly(String charClass, String value) {
        assertTrue(Pattern.matches(charClass + "+", value), value);
    }
}
