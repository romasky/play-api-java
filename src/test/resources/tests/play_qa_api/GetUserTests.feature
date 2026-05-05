#language: en
@AllTests @Users @CRUD @GetUser @allure.label.suite:User Management @allure.label.feature:Users @allure.label.story:Get_User
Feature: GET /api/v1/users/get/:id

  @Run @Smoke @allure.label.severity:critical
  Scenario: Get user by ID returns 200 with expected fields
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Convert get user response "getResp" to UserResp and save as "user"
    And Assert "user" not null
    And Assert response body does not contain "\"access_token\"" in "getResp"
    And Assert response body does not contain "\"password\"" in "getResp"

  @Run
  Scenario: Get user by non-existent ID returns 404 USER_NOT_FOUND
    Given Save string "000000000000000000000000" as "fakeId"
    When Get user by id "fakeId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"

  @Run
  Scenario Outline: Get user with malformed ID returns 404 USER_NOT_FOUND
    Given Save string "<badId>" as "badId"
    When Get user by id "badId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"
    Examples:
      | badId                    |
      | abc                      |
      | zzzzzzzzzzzzzzzzzzzzzzzz |
      | 123                      |
