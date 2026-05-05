#language: en
@allure.label.epic:User_Lifecycle @allure.label.suite:User_Management @allure.label.subSuite:Get_User
Feature: GET /api/v1/users/get/:id

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Get user by ID returns 200 with expected fields
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Assert response body does not contain "\"access_token\"" in "getResp"
    And Assert response body does not contain "\"password\"" in "getResp"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Get user by non-existent ID returns 404 USER_NOT_FOUND
    Given Generate fake mongo id and save as "fakeId"
    When Get user by id "fakeId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario Outline: Get user with malformed ID returns 404 USER_NOT_FOUND
    Given Save string "<id>" as "badId"
    When Get user by id "badId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"
    Examples:
      | id                       |
      | abc                      |
      | zzzzzzzzzzzzzzzzzzzzzzzz |
      | 123                      |
