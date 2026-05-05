#language: en
@allure.label.epic:Mail_Service @allure.label.suite:Mail_Service @allure.label.subSuite:Mailbox_Create
Feature: POST /api/v1/mail/create

  # ─────────────────── POSITIVE ───────────────────

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Create mailbox with empty body returns 201 with all required fields
    When Create mailbox with empty body and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create mailbox response "response" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" has all required fields
    And Assert CreateMailboxResp "mailbox" token is UUID format

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Create mailbox with custom local_part returns 201 with matching email
    Given Generate string of length 10 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"
    And Convert create mailbox response "response" to CreateMailboxResp and save as "mailbox"
    And Assert CreateMailboxResp "mailbox" email starts with "localPart"

  @Run @Positive @allure.label.story:Positive_Scenario
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

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Create mailbox with local_part at minimum boundary 3 chars returns 201
    Given Generate string of length 3 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Create mailbox with local_part at maximum boundary 30 chars returns 201
    Given Generate string of length 30 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Create mailbox with allowed special chars underscore and hyphen returns 201
    Given Generate string of length 10 and save as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 201 from "response"

  # ─────────────────── NEGATIVE ───────────────────

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create duplicate mailbox local_part and domain returns 409 ADDRESS_TAKEN
    Given Generate string of length 12 and save as "localPart"
    And Save string "play-qa.com" as "domain"
    When Create mailbox with domain "domain" local part "localPart" and save response as "firstResp"
    Then Get and check status code 201 from "firstResp"
    When Create mailbox with domain "domain" local part "localPart" and save response as "secondResp"
    Then Get and check status code 409 from "secondResp"
    And Convert error response "secondResp" to ErrorResp and save as "error"
    And Assert error code is "ADDRESS_TAKEN" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create mailbox with invalid domain returns 400 INVALID_DOMAIN
    Given Save string "notavaliddomain.com" as "domain"
    When Create mailbox with domain "domain" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_DOMAIN" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create mailbox with local_part shorter than 3 chars returns 400 INVALID_LOCAL_PART
    Given Save string "ab" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create mailbox with local_part longer than 30 chars returns 400 INVALID_LOCAL_PART
    Given Save string "this_local_part_is_way_too_longgg" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create mailbox with uppercase in local_part returns 400 INVALID_LOCAL_PART
    Given Save string "UpperCase123" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario: Create mailbox with @ in local_part returns 400 INVALID_LOCAL_PART
    Given Save string "bad@local" as "localPart"
    When Create mailbox with local part "localPart" and save response as "response"
    Then Get and check status code 400 from "response"
    And Convert error response "response" to ErrorResp and save as "error"
    And Assert error code is "INVALID_LOCAL_PART" in "error"
