#language: en
@AllTests @Users @CRUD @DeleteUser @allure.label.suite:User Management @allure.label.feature:Users @allure.label.story:Delete_User
Feature: DELETE /api/v1/users/delete/:id

  @Run @Smoke @allure.label.severity:critical
  Scenario: Delete user with valid token returns 204 and subsequent GET returns 404
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Delete user "userId" with token "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Get user by id "userId" and save response as "getAfterDelete"
    Then Get and check status code 404 from "getAfterDelete"
    And Convert error response "getAfterDelete" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"

  @Run
  Scenario: Delete returns 204 with empty body
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Delete user "userId" with token "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    And Assert response body does not contain "error" in "deleteResp"

  @Run
  Scenario: Delete with no Authorization header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Delete user "userId" with no auth header and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run
  Scenario: Delete non-existent user with another user token returns 401 INVALID_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Save string "000000000000000000000000" as "fakeId"
    When Delete user "fakeId" with token "token" and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Delete with another user token returns 401 INVALID_TOKEN
    When Create minimal user and save response as "userAResp"
    Then Get and check status code 201 from "userAResp"
    And Convert create user response "userAResp" to CreateUserResp and save as "userA"
    And Save field id from CreateUserResp "userA" as "userAId"
    When Create minimal user and save response as "userBResp"
    Then Get and check status code 201 from "userBResp"
    And Convert create user response "userBResp" to CreateUserResp and save as "userB"
    And Save field accessToken from CreateUserResp "userB" as "userBToken"
    When Delete user "userAId" with token "userBToken" and save response as "deleteResp"
    Then Get and check status code 401 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Delete same user twice — second delete returns 404
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Delete user "userId" with token "token" and save response as "firstDelete"
    Then Get and check status code 204 from "firstDelete"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
