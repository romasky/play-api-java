#language: en
@allure.label.epic:Authentication @allure.label.suite:User_Management @allure.label.subSuite:Token_Security
Feature: Token Security

  # Guards against authentication-bypass and cross-account (IDOR) defects on the
  # mutating user endpoints (PUT / PATCH / DELETE / logout). Every scenario asserts
  # BOTH the status code AND the error code so a silent 200/204 would be caught.
  #
  # NOTE ON THE "empty Bearer" report: an "Authorization: Bearer " header (empty
  # token, trailing space) was suspected of bypassing auth. Verified against
  # https://www.play-qa.com — the server correctly REJECTS it with:
  #   HTTP 401  { error.code: "INVALID_TOKEN_FORMAT",
  #               message: "Invalid authorization header format",
  #               details: "Expected format: Bearer <token>" }
  # No bypass and no IDOR. The scenarios below lock in that correct behavior as
  # green regression tests, so any future regression to the reported bug fails CI.

  # ─────────────────── EMPTY / WHITESPACE TOKEN (NEGATIVE) ───────────────────

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: PATCH with empty Bearer token is rejected 401 INVALID_TOKEN_FORMAT
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Generate first name and save as "newFirst"
    When Patch user "userId" with raw auth header "Bearer " firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: PUT with empty Bearer token is rejected 401 INVALID_TOKEN_FORMAT
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Update user "userId" with raw auth header "Bearer " and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: DELETE with empty Bearer token is rejected 401 INVALID_TOKEN_FORMAT
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Delete user "userId" with raw auth header "Bearer " and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: Logout with empty Bearer token is rejected 401 INVALID_TOKEN_FORMAT
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Logout user "userId" with raw auth header "Bearer " and save response as "logoutResp"
    Then Get and check status code 401 from "logoutResp"
    And Convert error response "logoutResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  # ─────────────────── CORE IDOR: revoked-token holder attacks ANOTHER account ───────────────────

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: Logged-out user with empty Bearer token cannot DELETE another account
    When Create minimal user and save response as "victimResp"
    Then Get and check status code 201 from "victimResp"
    And Convert create user response "victimResp" to CreateUserResp and save as "victim"
    And Save field id from CreateUserResp "victim" as "victimId"
    When Create minimal user and save response as "attackerResp"
    Then Get and check status code 201 from "attackerResp"
    And Convert create user response "attackerResp" to CreateUserResp and save as "attacker"
    And Save field id from CreateUserResp "attacker" as "attackerId"
    And Save field accessToken from CreateUserResp "attacker" as "attackerToken"
    When Logout user "attackerId" with token "attackerToken" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Delete user "victimId" with raw auth header "Bearer " and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  @Run @Negative @allure.label.severity:critical @allure.label.story:Negative_Scenario
  Scenario: Logged-out user with empty Bearer token cannot PATCH another account
    When Create minimal user and save response as "victimResp"
    Then Get and check status code 201 from "victimResp"
    And Convert create user response "victimResp" to CreateUserResp and save as "victim"
    And Save field id from CreateUserResp "victim" as "victimId"
    When Create minimal user and save response as "attackerResp"
    Then Get and check status code 201 from "attackerResp"
    And Convert create user response "attackerResp" to CreateUserResp and save as "attacker"
    And Save field id from CreateUserResp "attacker" as "attackerId"
    And Save field accessToken from CreateUserResp "attacker" as "attackerToken"
    When Logout user "attackerId" with token "attackerToken" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Generate first name and save as "newFirst"
    When Patch user "victimId" with raw auth header "Bearer " firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN_FORMAT" in "error"

  # ─────────────────── MALFORMED AUTHORIZATION HEADERS (NEGATIVE) ───────────────────

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario Outline: PATCH with malformed Authorization header <label> is rejected 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Generate first name and save as "newFirst"
    When Patch user "userId" with raw auth header "<header>" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    Examples:
      | label                | header                    |
      | garbage token        | Bearer notarealtoken      |
      | missing Bearer prefix| deadbeefdeadbeef          |
      | wrong scheme Basic   | Basic dXNlcjpwYXNz        |
      | Bearer only no space | Bearer                    |
      | numeric junk         | Bearer 000000000000       |

  # ─────────────────── CROSS-ACCOUNT WITH VALID TOKEN (regression guard) ───────────────────

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Valid token of user B cannot logout user A
    When Create minimal user and save response as "userAResp"
    Then Get and check status code 201 from "userAResp"
    And Convert create user response "userAResp" to CreateUserResp and save as "userA"
    And Save field id from CreateUserResp "userA" as "userAId"
    When Create minimal user and save response as "userBResp"
    Then Get and check status code 201 from "userBResp"
    And Convert create user response "userBResp" to CreateUserResp and save as "userB"
    And Save field accessToken from CreateUserResp "userB" as "userBToken"
    When Logout user "userAId" with token "userBToken" and save response as "logoutResp"
    Then Get and check status code 401 from "logoutResp"
    And Convert error response "logoutResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"
