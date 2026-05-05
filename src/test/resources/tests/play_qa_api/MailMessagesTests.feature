#language: en
@allure.label.epic:Mail_Service @allure.label.suite:Mail_Service @allure.label.subSuite:Mail_Messages
Feature: Mail Messages

  # ─────────────────── GET MESSAGES LIST ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Get messages for new mailbox returns 200 with count 0
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Convert messages list response "messagesResp" to MessagesListResp and save as "messages"
    And Assert messages list "messages" count is 0

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Messages list does not contain html_body field
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Assert messages list "messagesResp" items have no body field

  @Run @Flow @allure.label.story:End_to_End_Flow
  Scenario: After sending message count is 1
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Convert messages list response "messagesResp" to MessagesListResp and save as "messages"
    And Assert messages list "messages" count is 1

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Get messages for non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Get messages for mailbox "fakeToken" and save response as "messagesResp"
    Then Get and check status code 404 from "messagesResp"
    And Convert error response "messagesResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  # ─────────────────── GET SINGLE MESSAGE ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Get message by ID returns 200 with full body and html_body
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    And Save string "<p>HTML body</p>" as "htmlBody"
    When Send message with html to mailbox "token" from "from" subject "subject" body "body" htmlBody "htmlBody" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "sent"
    And Save field id from MessageResp "sent" as "msgId"
    When Get message "msgId" from mailbox "token" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Convert message response "getResp" to MessageResp and save as "msg"
    And Assert message "msg" has full body not null
    And Assert message "msg" has subject "subject"
    And Assert message "msg" has from "from"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Get message with non-existent message ID returns 404 MESSAGE_NOT_FOUND
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "000000000000000000000000" as "fakeId"
    When Get message "fakeId" from mailbox "token" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MESSAGE_NOT_FOUND" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Get message with non-existent mailbox token returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    And Save string "000000000000000000000000" as "fakeId"
    When Get message "fakeId" from mailbox "fakeToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Cross-mailbox message access returns 404 MESSAGE_NOT_FOUND
    When Create mailbox with empty body and save response as "mailboxAResp"
    Then Get and check status code 201 from "mailboxAResp"
    And Convert create mailbox response "mailboxAResp" to CreateMailboxResp and save as "mailboxA"
    And Save field token from CreateMailboxResp "mailboxA" as "tokenA"
    When Create mailbox with empty body and save response as "mailboxBResp"
    Then Get and check status code 201 from "mailboxBResp"
    And Convert create mailbox response "mailboxBResp" to CreateMailboxResp and save as "mailboxB"
    And Save field token from CreateMailboxResp "mailboxB" as "tokenB"
    And Generate sender email and save as "from"
    And Generate message subject and save as "subject"
    And Generate message body and save as "body"
    When Send message to mailbox "tokenA" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "sent"
    And Save field id from MessageResp "sent" as "msgId"
    When Get message "msgId" from mailbox "tokenB" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MESSAGE_NOT_FOUND" in "error"
