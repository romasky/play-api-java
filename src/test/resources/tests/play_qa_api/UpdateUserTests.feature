#language: en
@allure.label.epic:User_Lifecycle @allure.label.suite:User_Management @allure.label.subSuite:Update_User
Feature: PUT /api/v1/users/update/:id

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Full update user with valid token returns 200
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    When Update user "userId" token "token" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 200 from "updateResp"
    And Assert response body does not contain "\"access_token\"" in "updateResp"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: PUT update persists changed email in subsequent GET
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    When Update user "userId" token "token" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 200 from "updateResp"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Assert response body contains "newEmail" in "getResp"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: PUT update with no Authorization header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Update user "userId" with no auth header and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: PUT update with another user token returns 401 INVALID_TOKEN
    When Create minimal user and save response as "userAResp"
    Then Get and check status code 201 from "userAResp"
    And Convert create user response "userAResp" to CreateUserResp and save as "userA"
    And Save field id from CreateUserResp "userA" as "userAId"
    When Create minimal user and save response as "userBResp"
    Then Get and check status code 201 from "userBResp"
    And Convert create user response "userBResp" to CreateUserResp and save as "userB"
    And Save field id from CreateUserResp "userB" as "userBId"
    And Save field accessToken from CreateUserResp "userB" as "userBToken"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    When Update user "userAId" token "userBToken" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: PUT update non-existent user with another user token returns 401 INVALID_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate fake mongo id and save as "fakeId"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    When Update user "fakeId" token "token" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: PUT update with invalid email returns 400 VALIDATION_ERROR
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Update user "userId" with token "token" and invalid email and save response as "updateResp"
    Then Get and check status code 400 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: PUT update after logout returns 401 INVALID_TOKEN
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
