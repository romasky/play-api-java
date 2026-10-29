package com.testFramework.core;

import java.util.concurrent.ThreadLocalRandom;

public class Generator {

    private static final String LATIN = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    public static String latinString(int length) {
        return randomFrom(LATIN, length);
    }

    public static String numericString(int length) {
        return randomFrom(NUMERIC, length);
    }

    public static String alphanumericString(int length) {
        return randomFrom(LATIN + NUMERIC, length);
    }

    public static String email() {
        return alphanumericString(10).toLowerCase() + "@play-qa.com";
    }

    public static String username() {
        return "user_" + alphanumericString(8).toLowerCase();
    }

    public static String password() {
        return "Pass_" + alphanumericString(10) + "!1";
    }

    public static String firstName() {
        return "Test" + latinString(6);
    }

    public static String lastName() {
        return "User" + latinString(6);
    }

    public static String phoneNumber() {
        return "+1" + numericString(10);
    }

    public static String senderEmail() {
        return alphanumericString(8).toLowerCase() + "@example.com";
    }

    public static String messageSubject() {
        return "Subject " + alphanumericString(6);
    }

    public static String messageBody() {
        return "Body " + alphanumericString(10);
    }

    public static String invalidEmail() {
        return "notanemail_" + alphanumericString(4);
    }

    public static String shortPassword() {
        return alphanumericString(4);
    }

    public static String fakeMongoId() {
        return numericString(24);
    }

    public static String fakeUuid() {
        return "00000000-0000-0000-0000-" + numericString(12);
    }

    public static String string(int length, boolean cyrillic, boolean latin, boolean numeric,
                                boolean spaces, boolean special) {
        StringBuilder pool = new StringBuilder();
        if (latin) pool.append(LATIN);
        if (numeric) pool.append(NUMERIC);
        if (special) pool.append(SPECIAL);
        if (spaces) pool.append(" ");
        if (cyrillic) pool.append("абвгдеёжзийклмнопрстуфхцчшщъыьэюя");
        if (pool.isEmpty()) pool.append(LATIN);
        return randomFrom(pool.toString(), length);
    }

    /** A bounded stream rather than a counted loop: a mutated loop bound can only hang, never be killed. */
    private static String randomFrom(String chars, int length) {
        return ThreadLocalRandom.current().ints(length, 0, chars.length())
                .collect(StringBuilder::new, (sb, i) -> sb.append(chars.charAt(i)), StringBuilder::append)
                .toString();
    }
}
