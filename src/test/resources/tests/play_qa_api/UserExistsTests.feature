#language: en
@allure.label.suite:User_Management @allure.label.feature:Users @allure.label.story:User_Exists
Feature: HEAD /api/v1/users/exists/:id

  @Run @Smoke @Positive @allure.label.severity:critical
  Scenario: HEAD existing user returns 200 with X-User-Exists true
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Check user exists by id "userId" and save response as "existsResp"
    Then Get and check status code 200 from "existsResp"
    And Assert user exists header is "true" in response "existsResp"

  @Run @Negative
  Scenario: HEAD non-existent user returns 404 with X-User-Exists false
    Given Generate fake mongo id and save as "fakeId"
    When Check user exists by id "fakeId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run @Positive
  Scenario: GET alias for exists with existing user returns 200 with X-User-Exists true
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Check user exists GET by id "userId" and save response as "existsResp"
    Then Get and check status code 200 from "existsResp"
    And Assert user exists header is "true" in response "existsResp"

  @Run @Negative
  Scenario: GET alias for exists with non-existent user returns 404
    Given Generate fake mongo id and save as "fakeId"
    When Check user exists GET by id "fakeId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run @Negative
  Scenario: Check exists with malformed ID returns 404 with X-User-Exists false
    Given Save string "bad-id" as "badId"
    When Check user exists by id "badId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run @Flow
  Scenario: After delete user exists returns false
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Delete user "userId" with token "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Check user exists by id "userId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"
