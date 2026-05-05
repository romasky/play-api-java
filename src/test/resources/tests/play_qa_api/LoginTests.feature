#language: en
@allure.label.epic:Authentication @allure.label.suite:Authentication @allure.label.subSuite:Login
Feature: Login

  # ─────────────────── POSITIVE ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Login with valid credentials returns 200 and token
    Given Create minimal user and save response as "createResp"
    And Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Login response contains all required fields
    Given Create minimal user and save response as "createResp"
    And Get and check status code 201 from "createResp"
    And Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Convert login response "loginResp" to LoginResp and save as "login"
    And Assert login response "login" has all required fields

  # ─────────────────── FLOW ───────────────────

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Login invalidates previous token — old token returns 401 on PATCH
    Given Create minimal user and save response as "createResp"
    And Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "oldToken"
    When Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "oldToken" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Login after logout succeeds and issues new token
    Given Create minimal user and save response as "createResp"
    And Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Convert login response "loginResp" to LoginResp and save as "login"
    And Assert login response "login" has all required fields

  # ─────────────────── NEGATIVE ───────────────────

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login with wrong password returns 401 INVALID_CREDENTIALS
    Given Create minimal user and save response as "createResp"
    And Get and check status code 201 from "createResp"
    And Generate password and save as "wrongPass"
    When Login with email "generatedEmail" password "wrongPass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_CREDENTIALS" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login with non-existent email returns 401 INVALID_CREDENTIALS
    Given Generate sender email and save as "fakeEmail"
    And Generate password and save as "fakePass"
    When Login with email "fakeEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_CREDENTIALS" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login with invalid email format returns 400 VALIDATION_ERROR
    Given Generate invalid email and save as "badEmail"
    And Generate password and save as "fakePass"
    When Login with email "badEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 400 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login with password shorter than 8 chars returns 400 VALIDATION_ERROR
    Given Generate sender email and save as "email"
    And Generate short password and save as "shortPass"
    When Login with email "email" password "shortPass" and save raw response as "loginResp"
    Then Get and check status code 400 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login with empty body returns 400 VALIDATION_ERROR
    When Login with no body and save response as "loginResp"
    Then Get and check status code 400 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login error response contains request_id in body
    Given Generate sender email and save as "fakeEmail"
    And Generate password and save as "fakePass"
    When Login with email "fakeEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Convert error response "loginResp" to ErrorResp and save as "error"
    And Assert error response has request_id in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Login X-Request-ID is echoed in response header
    Given Generate sender email and save as "fakeEmail"
    And Generate password and save as "fakePass"
    When Login with email "fakeEmail" password "fakePass" and save raw response as "loginResp"
    Then Get and check status code 401 from "loginResp"
    And Assert response header "X-Request-ID" is present in "loginResp"
