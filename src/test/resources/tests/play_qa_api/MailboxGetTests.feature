#language: en
@allure.label.suite:Mail_Service @allure.label.feature:Mail @allure.label.story:Mailbox_Get
Feature: GET /api/v1/mail/:token

  @Run @Smoke @Positive @allure.label.severity:critical
  Scenario: Get mailbox by token returns 200 with all expected fields
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "created"
    And Save field token from CreateMailboxResp "created" as "token"
    When Get mailbox by token "token" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Convert get mailbox response "getResp" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" has all required fields
    And Assert mailbox "mailbox" token matches "token"

  @Run @Negative
  Scenario: Get mailbox with non-existent token returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Get mailbox by token "fakeToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run @Negative
  Scenario: Get mailbox with malformed token returns 404 MAILBOX_NOT_FOUND
    Given Save string "not-a-uuid-at-all" as "badToken"
    When Get mailbox by token "badToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"
