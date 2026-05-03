#language: en
@AllTests @Users @CRUD
Feature: User CRUD — Get, List, Exists, Update, Patch, Delete, Logout

  # ════════════════════ GET USER ════════════════════

  @Run @Smoke
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

  # ════════════════════ LIST USERS ════════════════════

  @Run @Smoke
  Scenario: Get users list returns 200 with pagination fields
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" has pagination fields
    And Assert users list "list" page is 1
    And Assert users list "list" per page is 10

  @Run
  Scenario: Get users list with per_page 10 returns at most 10 results
    When Get users list page "1" perPage "10" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" per page is 10
    And Assert users list "list" count is at most 10

  @Run
  Scenario: Get users list with per_page 100 returns 200
    When Get users list page "1" perPage "100" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" count is at most 100

  @Run
  Scenario: Users list response does not contain access_token or password
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response body does not contain "\"access_token\"" in "listResp"
    And Assert response body does not contain "\"password\"" in "listResp"

  @Run
  Scenario: Users list has Cache-Control no-store header
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response header "Cache-Control" contains "no-store" in "listResp"

  # ════════════════════ EXISTS ════════════════════

  @Run @Smoke
  Scenario: HEAD existing user returns 200 with X-User-Exists true
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Check user exists by id "userId" and save response as "existsResp"
    Then Get and check status code 200 from "existsResp"
    And Assert user exists header is "true" in response "existsResp"

  @Run
  Scenario: HEAD non-existent user returns 404 with X-User-Exists false
    Given Save string "000000000000000000000000" as "fakeId"
    When Check user exists by id "fakeId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run
  Scenario: GET alias for exists with existing user returns 200 with X-User-Exists true
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Check user exists GET by id "userId" and save response as "existsResp"
    Then Get and check status code 200 from "existsResp"
    And Assert user exists header is "true" in response "existsResp"

  @Run
  Scenario: GET alias for exists with non-existent user returns 404
    Given Save string "000000000000000000000000" as "fakeId"
    When Check user exists GET by id "fakeId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run
  Scenario: Check exists with malformed ID returns 404 with X-User-Exists false
    Given Save string "not-a-mongo-id" as "badId"
    When Check user exists by id "badId" and save response as "existsResp"
    Then Get and check status code 404 from "existsResp"
    And Assert user exists header is "false" in response "existsResp"

  @Run
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

  # ════════════════════ PUT UPDATE ════════════════════

  @Run @Smoke
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

  @Run
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

  @Run
  Scenario: PUT update with no Authorization header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Update user "userId" with no auth header and save response as "updateResp"
    Then Get and check status code 401 from "updateResp"
    And Convert error response "updateResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run
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

  @Run
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

  @Run
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

  @Run
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

  # ════════════════════ PATCH ════════════════════

  @Run @Smoke
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

  # ════════════════════ LOGOUT ════════════════════

  @Run @Smoke
  Scenario: Logout returns 200 with success true message
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Assert response body contains "true" in "logoutResp"
    And Assert response body contains "revoked" in "logoutResp"

  @Run
  Scenario: Logout revoked token — PATCH returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    And Generate first name and save as "newFirst"
    When Patch user "userId" token "token" firstName "newFirst" and save response as "afterLogout"
    Then Get and check status code 401 from "afterLogout"
    And Convert error response "afterLogout" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Logout revoked token — PUT also returns 401
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
    When Update user "userId" token "token" firstName "newFirst" lastName "newLast" email "newEmail" username "newUsername" and save response as "afterLogout"
    Then Get and check status code 401 from "afterLogout"
    And Convert error response "afterLogout" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Logout revoked token — DELETE also returns 401
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Delete user "userId" with token "token" and save response as "deleteAfterLogout"
    Then Get and check status code 401 from "deleteAfterLogout"
    And Convert error response "deleteAfterLogout" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Double logout — second attempt returns 401 INVALID_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "firstLogout"
    Then Get and check status code 200 from "firstLogout"
    When Logout user "userId" with token "token" and save response as "secondLogout"
    Then Get and check status code 401 from "secondLogout"
    And Convert error response "secondLogout" to ErrorResp and save as "error"
    And Assert error code is "INVALID_TOKEN" in "error"

  @Run
  Scenario: Logout with no auth header returns 401 MISSING_TOKEN
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    When Logout user "userId" with no auth header and save response as "logoutResp"
    Then Get and check status code 401 from "logoutResp"
    And Convert error response "logoutResp" to ErrorResp and save as "error"
    And Assert error code is "MISSING_TOKEN" in "error"

  @Run
  Scenario: User can re-login after logout and receives new valid token
    When Create minimal user and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create user response "createResp" to CreateUserResp and save as "created"
    And Save field id from CreateUserResp "created" as "userId"
    And Save field accessToken from CreateUserResp "created" as "token"
    When Logout user "userId" with token "token" and save response as "logoutResp"
    Then Get and check status code 200 from "logoutResp"
    When Login with email "generatedEmail" password "generatedPassword" and save raw response as "loginResp"
    Then Get and check status code 200 from "loginResp"
    And Convert login response "loginResp" to LoginResp and save as "login"
    And Assert login response "login" has all required fields

  # ════════════════════ DELETE ════════════════════

  @Run @Smoke
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
