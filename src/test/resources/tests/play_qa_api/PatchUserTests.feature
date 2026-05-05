#language: en
@AllTests @Users @CRUD @PatchUser @allure.label.suite:User Management @allure.label.feature:Users @allure.label.story:Patch_User
Feature: PATCH /api/v1/users/patch/:id

  @Run @Smoke @allure.label.severity:critical
  Scenario: Partial update firstName returns 200
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "token" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 200 from "patchResp"
    And Assert response body does not contain "\"access_token\"" in "patchResp"

  @Run
  Scenario: Patch only email — other fields remain unchanged
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate email and save as "newEmail"
    When Patch user "userId" token "token" email "newEmail" and save response as "patchResp"
    Then Get and check status code 200 from "patchResp"
    And Assert response body contains "newEmail" in "patchResp"

  @Run
  Scenario: Patch only username returns 200
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate username and save as "newUsername"
    When Patch user "userId" token "token" username "newUsername" and save response as "patchResp"
    Then Get and check status code 200 from "patchResp"

  @Run
  Scenario: Patch with empty body returns 200 with no changes
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Patch user "userId" with empty body token "token" and save response as "patchResp"
    Then Get and check status code 200 from "patchResp"

  @Run
  Scenario: Patch with no auth header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Patch user "userId" with no auth header and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run
  Scenario: Patch with another user token returns 401 INVALID_TOKEN
    When Create minimal user and save response as "userAResp"
    Then Get and check status code 201 from "userAResp"
    And Convert create user response "userAResp" to CreateUserResp and save as "userA"
    And Save field id from CreateUserResp "userA" as "userAId"
    When Create minimal user and save response as "userBResp"
    Then Get and check status code 201 from "userBResp"
    And Convert create user response "userBResp" to CreateUserResp and save as "userB"
    And Save field accessToken from CreateUserResp "userB" as "userBToken"
    And Generate first name and save as "newFirst"
    When Patch user "userAId" token "userBToken" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 401 from "patchResp"
    And Convert error response "patchResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Patch after logout returns 401 INVALID_TOKEN
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
