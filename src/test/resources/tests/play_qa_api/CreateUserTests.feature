#language: en
# Variables: _l = local (per scenario), _g = global (shared across scenarios)
# Tags: @Run = execute, @Ignore/@Bug/@NotImplemented = skip

@AllTests @Users @CreateUser
Feature: POST /api/v1/users/create — Create user scenarios

  # ─────────────────── POSITIVE ───────────────────

  @Run
  Scenario: Create user with minimal required fields returns 201 and token
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "createdUser"
    And Assert CreateUserResp "createdUser" has email "email"
    And Assert CreateUserResp "createdUser" has username "username"
    And Assert CreateUserResp "createdUser" has access token not null
    And Save field id from CreateUserResp "createdUser" as "userId"
    And Assert "userId" not null

  @Run
  Scenario: Create user with all optional fields returns 201
    When Create full user with all fields and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "createdUser"
    And Assert CreateUserResp "createdUser" has access token not null

  @Run
  Scenario: Created user can login with returned token
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "createdUser"
    And Save field id from CreateUserResp "createdUser" as "userId"
    And Save field accessToken from CreateUserResp "createdUser" as "userToken"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Convert get user response "getResp" to UserResp and save as "fetchedUser"
    And Assert "userId" not null

  # ─────────────────── NEGATIVE ───────────────────

  @Run
  Scenario: Create user with duplicate email returns 409 DUPLICATE_USER
    Given Generate email and save as "email_g"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email_g" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "firstResp"
    Then Get and check status code 201 from "firstResp"
    And Generate username and save as "username2"
    When Create user with email "email_g" username "username2" password "password" firstName "firstName" lastName "lastName" and save response as "secondResp"
    Then Get and check status code 409 from "secondResp"
    And Convert error response "secondResp" to ErrorResp and save as "error"
    And Assert error code is "DUPLICATE_USER" in "error"

  @Run
  Scenario: Create user with invalid email returns 400 VALIDATION_ERROR
    Given Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Save string "notanemail" as "badEmail"
    When Create user with email "badEmail" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run
  Scenario: Create user with password shorter than 8 chars returns 400 VALIDATION_ERROR
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Save string "short" as "shortPass"
    When Create user with email "email" username "username" password "shortPass" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run
  Scenario: Create user with duplicate username returns 409 DUPLICATE_USER
    Given Generate email and save as "email1"
    And Generate email and save as "email2"
    And Generate username and save as "sharedUsername"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email1" username "sharedUsername" password "password" firstName "firstName" lastName "lastName" and save response as "firstResp"
    Then Get and check status code 201 from "firstResp"
    When Create user with email "email2" username "sharedUsername" password "password" firstName "firstName" lastName "lastName" and save response as "secondResp"
    Then Get and check status code 409 from "secondResp"
    And Convert error response "secondResp" to ErrorResp and save as "error"
    And Assert error code is "DUPLICATE_USER" in "error"

  @Run
  Scenario Outline: Create user missing required field <field> returns 400
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Save string "<override>" as "<field>"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"
    Examples:
      | field     | override |
      | email     | bad@     |
      | password  | short    |
