#language: en
@allure.label.suite:User_Management @allure.label.feature:Users @allure.label.story:Create_User
Feature: POST /api/v1/users/create

  # ─────────────────── POSITIVE ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical
  Scenario: Create user with minimal required fields returns 201 with token
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "created"
    And Assert CreateUserResp "created" has email "email"
    And Assert CreateUserResp "created" has username "username"
    And Assert CreateUserResp "created" has access token not null

  @Run @Positive
  Scenario: Create user with all optional fields returns 201
    When Create full user with all fields and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "created"
    And Assert CreateUserResp "created" has access token not null

  @Run @Positive
  Scenario: Response body never contains password field
    When Create minimal user and save response as "response"
    Then Get and check status code 201 from "response"
    And Assert response body does not contain "\"password\"" in "response"

  @Run @Positive
  Scenario: access_token format matches usr_{timestamp}_{hex} pattern
    When Create minimal user and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "created"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Assert "token" matches regex "usr_\d+_[a-f0-9]{32}"

  @Run @Positive
  Scenario: Create user metadata fields are correct on creation
    When Create minimal user and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create user response "response" to CreateUserResp and save as "created"
    And Assert CreateUserResp "created" metadata is_active is true
    And Assert CreateUserResp "created" metadata is_verified is false
    And Assert CreateUserResp "created" metadata login_count is 0

  @Run @Positive
  Scenario: Create user with username at minimum length 3 chars returns 201
    When Create user with username length 3 and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Positive
  Scenario: Create user with username at maximum length 30 chars returns 201
    When Create user with username length 30 and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Positive
  Scenario: Create user with empty interests array returns 201
    Given Save string "" as "emptyInterests"
    When Create user with interests "emptyInterests" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Positive
  Scenario: Create user with multiple interests returns 201
    Given Save string "coding,travel,music" as "interests"
    When Create user with interests "interests" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Smoke @Positive @allure.label.severity:critical
  Scenario Outline: Create user with valid gender enum returns 201
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Save string "<gender>" as "gender"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" gender "gender" and save response as "response"
    Then Get and check status code 201 from "response"
    Examples:
      | gender            |
      | male              |
      | female            |
      | other             |
      | prefer_not_to_say |

  @Run @Positive
  Scenario Outline: Create user with valid employment status returns 201
    Given Save string "<status>" as "status"
    When Create user with employment status "status" and save response as "response"
    Then Get and check status code 201 from "response"
    Examples:
      | status     |
      | employed   |
      | unemployed |
      | student    |
      | retired    |
      | freelancer |

  @Run @Positive
  Scenario Outline: Create user with valid theme returns 201
    Given Save string "<theme>" as "theme"
    When Create user with theme "theme" and save response as "response"
    Then Get and check status code 201 from "response"
    Examples:
      | theme  |
      | light  |
      | dark   |
      | system |

  # ─────────────────── NEGATIVE ───────────────────

  @Run @Negative
  Scenario: Create user with duplicate email returns 409 DUPLICATE_USER
    Given Generate email and save as "email_g"
    And Generate username and save as "username"
    And Generate username and save as "username2"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email_g" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "firstResp"
    Then Get and check status code 201 from "firstResp"
    When Create user with email "email_g" username "username2" password "password" firstName "firstName" lastName "lastName" and save response as "secondResp"
    Then Get and check status code 409 from "secondResp"
    And Convert error response "secondResp" to ErrorResp and save as "error"
    And Assert error code is "DUPLICATE_USER" in "error"

  @Run @Negative
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

  @Run @Negative
  Scenario: Create user with invalid email returns 400 VALIDATION_ERROR
    Given Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Generate invalid email and save as "badEmail"
    When Create user with email "badEmail" username "username" password "password" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: Create user with password shorter than 8 chars returns 400 VALIDATION_ERROR
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate short password and save as "shortPass"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    When Create user with email "email" username "username" password "shortPass" firstName "firstName" lastName "lastName" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: Create user with username shorter than 3 chars returns 400 VALIDATION_ERROR
    When Create user with username length 2 and save response as "response"
    Then Get and check status code 400 from "response"

  @Run @Negative
  Scenario: Create user with username longer than 30 chars returns 400 VALIDATION_ERROR
    When Create user with username length 31 and save response as "response"
    Then Get and check status code 400 from "response"

  @Run @Negative
  Scenario: Create user missing profile.first_name returns 400 VALIDATION_ERROR
    When Create user with firstName length 0 and save response as "response"
    Then Get and check status code 400 from "response"

  @Run @Negative
  Scenario: Create user with first_name shorter than 2 chars returns 400 VALIDATION_ERROR
    When Create user with firstName length 1 and save response as "response"
    Then Get and check status code 400 from "response"

  @Run @Negative
  Scenario: Create user with invalid gender enum returns 400 VALIDATION_ERROR
    Given Generate email and save as "email"
    And Generate username and save as "username"
    And Generate password and save as "password"
    And Generate first name and save as "firstName"
    And Generate last name and save as "lastName"
    And Save string "unknown_gender" as "gender"
    When Create user with email "email" username "username" password "password" firstName "firstName" lastName "lastName" gender "gender" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: Create user with invalid employment status enum returns 400 VALIDATION_ERROR
    Given Save string "king" as "status"
    When Create user with employment status "status" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: Create user with invalid theme enum returns 400 VALIDATION_ERROR
    Given Save string "pink" as "theme"
    When Create user with theme "theme" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: Create user with bio longer than 500 chars returns 400 VALIDATION_ERROR
    When Create user with bio of length 501 and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"

  @Run @Negative
  Scenario: VALIDATION_ERROR response contains request_id and error details
    When Create user with bio of length 501 and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error response has request_id in "error"
    And Assert error code is "VALIDATION_ERROR" in "error"
    And Assert response body contains "bio" in "response"
