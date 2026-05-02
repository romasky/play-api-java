#language: en
@AllTests @Auth @Login
Feature: POST /api/v1/login — Login scenarios

  Background:
    Given Create minimal user and save response as "setupResp"
    And Convert create user response "setupResp" to CreateUserResp and save as "setupUser"
    And Save field id from CreateUserResp "setupUser" as "userId_g"
    And Save field accessToken from CreateUserResp "setupUser" as "userToken_g"

  # ─────────────────── POSITIVE ───────────────────

  @Run
  Scenario: Login with valid credentials returns 200 and token
    Given Generate email and save as "email_g"
    And Generate username and save as "username"
    And Generate password and save as "password_g"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email_g" username "username" password "password_g" firstName "firstName" lastName "lastName" and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    When Login with email "email_g" password "password_g" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Convert login response "loginResp" to LoginResp and save as "login"
    And Assert "login" not null

  # ─────────────────── NEGATIVE ───────────────────

  @Run
  Scenario: Login with wrong password returns 401 INVALID_CREDENTIALS
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Save string "wrongpassword123" as "wrongPass"
    When Login with email "email" password "wrongPass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_CREDENTIALS" in "error"

  @Run
  Scenario: Login with non-existent email returns 401 INVALID_CREDENTIALS
    Given Save string "nonexistent@play-qa.com" as "fakeEmail"
    And Save string "SomePassword123!" as "fakePass"
    When Login with email "fakeEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_CREDENTIALS" in "error"

  @Run
  Scenario: Login with invalid email format returns 400 VALIDATION_ERROR
    Given Save string "notanemail" as "badEmail"
    And Save string "SomePassword123!" as "fakePass"
    When Login with email "badEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 400 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"
