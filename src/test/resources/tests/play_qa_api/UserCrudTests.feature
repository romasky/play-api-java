#language: en
@AllTests @Users @CRUD
Feature: User CRUD — Get, Update, Patch, Delete, Logout

  # ─────────────────── GET USER ───────────────────

  @Run
  Scenario: Get user by ID returns 200 with full user object
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Get user by id "userId" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Convert get user response "getResp" to UserResp and save as "user"
    And Assert "userId" not null

  @Run
  Scenario: Get user by non-existent ID returns 404 USER_NOT_FOUND
    Given Save string "000000000000000000000000" as "fakeId"
    When Get user by id "fakeId" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "USER_NOT_FOUND" in "error"

  # ─────────────────── GET LIST ───────────────────

  @Run
  Scenario: Get users list returns 200 with pagination
    When Get users list page "1" perPage "10" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert "list" not null

  # ─────────────────── CHECK EXISTS ───────────────────

  @Run
  Scenario: Check existing user returns 200 with X-User-Exists: true
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Check user exists by id "userId" and save response as "existsResp"
    Then Get and check status code 200 from "existsResp"
    And Assert user exists header is "true" in response "existsResp"

  @Run
  Scenario: Check non-existent user returns 404 with X-User-Exists: false
    Given Save string "000000000000000000000000" as "fakeId"
    When Check user exists by id "fakeId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  # ─────────────────── UPDATE (PUT) ───────────────────

  @Run
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
    And Convert get user response "updateResp" to UserResp and save as "updated"
    And Assert "updated" not null

  @Run
  Scenario: Update user without auth token returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Generate email and save as "newEmail"
    And Generate username and save as "newUsername"
    And Generate first name and save as "newFirst"
    And Generate last name and save as "newLast"
    And Save string "invalid_token_value" as "badToken"
    When Update user "userId" token "badToken" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  # ─────────────────── PATCH ───────────────────

  @Run
  Scenario: Partial update (patch) user firstName returns 200
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "token" firstName "newFirst" and save response as "patchResp"
    Then Get and check status code 200 from "patchResp"
    And Convert get user response "patchResp" to UserResp and save as "patched"
    And Assert "patched" not null

  # ─────────────────── LOGOUT ───────────────────

  @Run
  Scenario: Logout user revokes token — subsequent request returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "token" firstName "newFirst" and save response as "afterLogoutResp"
    Then Get and check status code 401 from "afterLogoutResp"
    And Convert error response "afterLogoutResp" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  # ─────────────────── DELETE ───────────────────

  @Run
  Scenario: Delete user with valid token returns 204
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
