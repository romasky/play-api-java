#language: en
@allure.label.suite:Authentication @allure.label.feature:Auth @allure.label.story:Logout
Feature: POST /api/v1/users/logout/:id

  @Run @Smoke @Positive @allure.label.severity:critical
  Scenario: Logout returns 200 with success true message
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Assert response body contains "true" in "logoutResp"
    And Assert response body contains "revoked" in "logoutResp"

  # ─────────────────── FLOW ───────────────────

  @Run @Flow
  Scenario: Logout revoked token — PATCH returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "token" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Flow
  Scenario: Logout revoked token — PUT also returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    When Update user "userId" token "token" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Flow
  Scenario: Logout revoked token — DELETE also returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Delete user "userId" with token "token" and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Flow
  Scenario: Double logout — second attempt returns 401 INVALID_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "firstLogout"
    Then Get and check status code 200 from "firstLogout"
    When Logout user "userId" with token "token" and save response as "secondLogout"
    Then Get and check status code 401 from "secondLogout"
    And Convert error response "secondLogout" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  # ─────────────────── NEGATIVE ───────────────────

  @Run @Negative
  Scenario: Logout with no auth header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Logout user "userId" with no auth header and save response as "logoutResp"
    Then Get and check status code 401 from "logoutResp"
    And Convert error response "logoutResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run @Flow
  Scenario: User can re-login after logout and receives new valid token
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Convert login response "loginResp" to LoginResp and save as "login"
    And Assert login response "login" has all required fields
