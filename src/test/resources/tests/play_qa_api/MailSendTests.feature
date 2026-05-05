#language: en
@allure.label.epic:Mail_Service @allure.label.suite:Mail_Service @allure.label.subSuite:Mail_Send
Feature: POST /api/v1/mail/:token/send

  # ─────────────────── POSITIVE ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Send message returns 201 with full message object
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "msg"
    And Assert message "msg" has subject "subject"
    And Assert message "msg" has from "from"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Send message without html_body returns 201
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"

  # ─────────────────── FLOW ───────────────────

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Sent message appears in GET messages list
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response body contains "subject" in "listResp"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Sent message is retrievable via GET messages ID
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "sent"
    And Save field id from MessageResp "sent" as "msgId"
    When Get message "msgId" from mailbox "token" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Assert response body contains "subject" in "getResp"

  # ─────────────────── NEGATIVE ───────────────────

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario Outline: Send message missing required field <field> returns 400 VALIDATION_ERROR
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Send message with missing field "<field>" to mailbox "token" and save response as "sendResp"
    Then Get and check status code 400 from "sendResp"
    And Convert error response "sendResp" to ErrorResp and save as "error"
    And Assert error code is "VALIDATION_ERROR" in "error"
    Examples:
      | field   |
      | from    |
      | subject |
      | body    |

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Send message to non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "fakeToken" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 404 from "sendResp"
    And Convert error response "sendResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"
