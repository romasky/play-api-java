package com.testFramework.core;

import java.util.UUID;
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

    public static String email(String domain) {
        return alphanumericString(10).toLowerCase() + "@" + domain;
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

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String phoneNumber() {
        return "+1" + numericString(10);
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

    private static String randomFrom(String chars, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }
}
