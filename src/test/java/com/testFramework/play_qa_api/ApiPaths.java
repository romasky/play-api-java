package com.testFramework.play_qa_api;

public class ApiPaths {

    private static final String BASE = "/api/v1";

    public static final String HEALTH         = BASE + "/health";
    public static final String LOGIN          = BASE + "/login";

    public static final String USERS_CREATE   = BASE + "/users/create";
    public static final String USERS_LIST     = BASE + "/users/list";
    public static final String USERS_OPTIONS  = BASE + "/users/options";

    public static final String MAIL_CREATE    = BASE + "/mail/create";

    public static String usersGet(String id)    { return BASE + "/users/get/" + id; }
    public static String usersExists(String id) { return BASE + "/users/exists/" + id; }
    public static String usersUpdate(String id) { return BASE + "/users/update/" + id; }
    public static String usersPatch(String id)  { return BASE + "/users/patch/" + id; }
    public static String usersDelete(String id) { return BASE + "/users/delete/" + id; }
    public static String usersLogout(String id) { return BASE + "/users/logout/" + id; }
    public static String mailGet(String token)  { return BASE + "/mail/" + token; }
    public static String mailMessages(String token) { return BASE + "/mail/" + token + "/messages"; }
    public static String mailMessage(String token, String id) { return BASE + "/mail/" + token + "/messages/" + id; }
    public static String mailSend(String token) { return BASE + "/mail/" + token + "/send"; }
    public static String mailDelete(String token) { return BASE + "/mail/" + token; }
}
