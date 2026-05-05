#language: en
@allure.label.epic:Mail_Service @allure.label.suite:Mail_Service @allure.label.subSuite:Mailbox_Delete
Feature: DELETE /api/v1/mail/:token

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Delete mailbox returns 204 and subsequent GET returns 404
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Delete mailbox "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Get mailbox by token "token" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Delete mailbox — GET messages also returns 404
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Delete mailbox "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 404 from "messagesResp"
    And Convert error response "messagesResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Delete mailbox with messages — message retrieval also returns 404
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
    And Save field id from MessageResp "msg" as "msgId"
    When Delete mailbox "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Get message "msgId" from mailbox "token" and save response as "getMsg"
    Then Get and check status code 404 from "getMsg"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Delete non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Delete mailbox "fakeToken" and save response as "deleteResp"
    Then Get and check status code 404 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: Delete mailbox twice — second returns 404 MAILBOX_NOT_FOUND
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Delete mailbox "token" and save response as "firstDelete"
    Then Get and check status code 204 from "firstDelete"
    When Delete mailbox "token" and save response as "secondDelete"
    Then Get and check status code 404 from "secondDelete"
    And Convert error response "secondDelete" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"
