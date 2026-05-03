#language: en
@AllTests @Mail
Feature: Mail API — /api/v1/mail/*

  # ════════════════════ CREATE MAILBOX ════════════════════

  @Run @Smoke
  Scenario: Create mailbox with empty body returns 201 with all required fields
    When Create mailbox with empty body and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create mailbox response "response" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" has all required fields
    And Assert CreateMailboxResp "mailbox" token is UUID format

  @Run
  Scenario: Create mailbox with custom local_part returns 201 with matching email
    Given Generate string of length 10 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create mailbox response "response" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" email starts with "localPart"

  @Run
  Scenario Outline: Create mailbox with valid domain returns 201
    Given Save string "<domain>" as "domain"
    When Create mailbox with domain "domain" and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create mailbox response "response" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" email contains domain "<domain>"
    Examples:
      | domain              |
      | play-qa.com         |
      | mail.play-qa.com    |
      | temp.play-qa.com    |
      | inbox.play-qa.com   |

  @Run
  Scenario: Create mailbox with local_part at minimum boundary 3 chars returns 201
    Given Generate string of length 3 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run
  Scenario: Create mailbox with local_part at maximum boundary 30 chars returns 201
    Given Generate string of length 30 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run
  Scenario: Create mailbox with allowed special chars underscore and hyphen returns 201
    Given Generate string of length 10 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run
  Scenario: Create duplicate mailbox local_part and domain returns 409 ADDRESS_TAKEN
    Given Generate string of length 12 and save as "localPart"
    And Save string "play-qa.com" as "domain"
    When Create mailbox with domain "domain" local part "localPart" and save response as "firstResp"
    Then Get and check status code 201 from "firstResp"
    When Create mailbox with domain "domain" local part "localPart" and save response as "secondResp"
    Then Get and check status code 409 from "secondResp"
    And Convert error response "secondResp" to ErrorResp and save as "error"
    And Assert error code is "ADDRESS_TAKEN" in "error"

  @Run
  Scenario: Create mailbox with invalid domain returns 400 INVALID_DOMAIN
    Given Save string "notavaliddomain.com" as "domain"
    When Create mailbox with domain "domain" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_DOMAIN" in "error"

  @Run
  Scenario: Create mailbox with local_part shorter than 3 chars returns 400 INVALID_LOCAL_PART
    Given Save string "ab" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run
  Scenario: Create mailbox with local_part longer than 30 chars returns 400 INVALID_LOCAL_PART
    Given Save string "this_local_part_is_way_too_longgg" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run
  Scenario: Create mailbox with uppercase in local_part returns 400 INVALID_LOCAL_PART
    Given Save string "UpperCase123" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run
  Scenario: Create mailbox with @ in local_part returns 400 INVALID_LOCAL_PART
    Given Save string "bad@local" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  # ════════════════════ GET MAILBOX ════════════════════

  @Run @Smoke
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

  @Run
  Scenario: Get mailbox with non-existent token returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Get mailbox by token "fakeToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run
  Scenario: Get mailbox with malformed token returns 404 MAILBOX_NOT_FOUND
    Given Save string "not-a-uuid-at-all" as "badToken"
    When Get mailbox by token "badToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  # ════════════════════ GET MESSAGES ════════════════════

  @Run @Smoke
  Scenario: Get messages for new mailbox returns 200 with count 0
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Convert messages list response "messagesResp" to MessagesListResp and save as "messages"
    And Assert messages list "messages" count is 0

  @Run
  Scenario: Messages list does not contain html_body field
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "Test Subject" as "subject"
    And Save string "Test body content" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Assert messages list "messagesResp" items have no body field

  @Run
  Scenario: After sending message count is 1
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "Hello" as "subject"
    And Save string "Body text" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "messagesResp"
    Then Get and check status code 200 from "messagesResp"
    And Convert messages list response "messagesResp" to MessagesListResp and save as "messages"
    And Assert messages list "messages" count is 1

  @Run
  Scenario: Get messages for non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Get messages for mailbox "fakeToken" and save response as "messagesResp"
    Then Get and check status code 404 from "messagesResp"
    And Convert error response "messagesResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  # ════════════════════ GET SINGLE MESSAGE ════════════════════

  @Run @Smoke
  Scenario: Get message by ID returns 200 with full body and html_body
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "Test Subject" as "subject"
    And Save string "Plain text body" as "body"
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

  @Run
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

  @Run
  Scenario: Get message with non-existent mailbox token returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    And Save string "000000000000000000000000" as "fakeId"
    When Get message "fakeId" from mailbox "fakeToken" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run
  Scenario: Cross-mailbox message access returns 404 MESSAGE_NOT_FOUND
    When Create mailbox with empty body and save response as "mailboxAResp"
    Then Get and check status code 201 from "mailboxAResp"
    And Convert create mailbox response "mailboxAResp" to CreateMailboxResp and save as "mailboxA"
    And Save field token from CreateMailboxResp "mailboxA" as "tokenA"
    When Create mailbox with empty body and save response as "mailboxBResp"
    Then Get and check status code 201 from "mailboxBResp"
    And Convert create mailbox response "mailboxBResp" to CreateMailboxResp and save as "mailboxB"
    And Save field token from CreateMailboxResp "mailboxB" as "tokenB"
    And Save string "sender@example.com" as "from"
    And Save string "Subject" as "subject"
    And Save string "Body" as "body"
    When Send message to mailbox "tokenA" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "sent"
    And Save field id from MessageResp "sent" as "msgId"
    When Get message "msgId" from mailbox "tokenB" and save response as "getResp"
    Then Get and check status code 404 from "getResp"
    And Convert error response "getResp" to ErrorResp and save as "error"
    And Assert error code is "MESSAGE_NOT_FOUND" in "error"

  # ════════════════════ SEND MESSAGE ════════════════════

  @Run @Smoke
  Scenario: Send message returns 201 with full message object
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "Hello World" as "subject"
    And Save string "This is the body" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "msg"
    And Assert message "msg" has subject "subject"
    And Assert message "msg" has from "from"

  @Run
  Scenario: Send message without html_body returns 201
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "No HTML" as "subject"
    And Save string "Plain text only" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"

  @Run
  Scenario: Sent message appears in GET messages list
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "ListCheck" as "subject"
    And Save string "Body" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    When Get messages for mailbox "token" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response body contains "ListCheck" in "listResp"

  @Run
  Scenario: Sent message is retrievable via GET messages ID
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "RetrievalTest" as "subject"
    And Save string "Body content" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "sent"
    And Save field id from MessageResp "sent" as "msgId"
    When Get message "msgId" from mailbox "token" and save response as "getResp"
    Then Get and check status code 200 from "getResp"
    And Assert response body contains "RetrievalTest" in "getResp"

  @Run
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

  @Run
  Scenario: Send message to non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    And Save string "sender@example.com" as "from"
    And Save string "Subject" as "subject"
    And Save string "Body" as "body"
    When Send message to mailbox "fakeToken" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 404 from "sendResp"
    And Convert error response "sendResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  # ════════════════════ DELETE MAILBOX ════════════════════

  @Run @Smoke
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

  @Run
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

  @Run
  Scenario: Delete mailbox with messages — message retrieval also returns 404
    When Create mailbox with empty body and save response as "createResp"
    Then Get and check status code 201 from "createResp"
    And Convert create mailbox response "createResp" to CreateMailboxResp and save as "mailbox"
    And Save field token from CreateMailboxResp "mailbox" as "token"
    And Save string "sender@example.com" as "from"
    And Save string "Subject" as "subject"
    And Save string "Body" as "body"
    When Send message to mailbox "token" from "from" subject "subject" body "body" and save response as "sendResp"
    Then Get and check status code 201 from "sendResp"
    And Convert send message response "sendResp" to MessageResp and save as "msg"
    And Save field id from MessageResp "msg" as "msgId"
    When Delete mailbox "token" and save response as "deleteResp"
    Then Get and check status code 204 from "deleteResp"
    When Get message "msgId" from mailbox "token" and save response as "getMsg"
    Then Get and check status code 404 from "getMsg"

  @Run
  Scenario: Delete non-existent mailbox returns 404 MAILBOX_NOT_FOUND
    Given Save string "00000000-0000-0000-0000-000000000000" as "fakeToken"
    When Delete mailbox "fakeToken" and save response as "deleteResp"
    Then Get and check status code 404 from "deleteResp"
    And Convert error response "deleteResp" to ErrorResp and save as "error"
    And Assert error code is "MAILBOX_NOT_FOUND" in "error"

  @Run
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
