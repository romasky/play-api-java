#language: en
@allure.label.epic:User_Lifecycle @allure.label.suite:User_Management @allure.label.subSuite:List_Users
Feature: List Users

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: Get users list returns 200 with pagination fields
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" has pagination fields
    And Assert users list "list" page is 1
    And Assert users list "list" per page is 10

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Get users list with per_page 10 returns at most 10 results
    When Get users list page "1" perPage "10" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" count is at most 10

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Get users list with per_page 100 returns 200
    When Get users list page "1" perPage "100" and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Convert users list response "listResp" to UsersListResp and save as "list"
    And Assert users list "list" count is at most 100

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Users list response does not contain access_token or password
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response body does not contain "\"access_token\"" in "listResp"
    And Assert response body does not contain "\"password\"" in "listResp"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: Users list has Cache-Control no-store header
    When Get users list and save response as "listResp"
    Then Get and check status code 200 from "listResp"
    And Assert response header "Cache-Control" contains "no-store" in "listResp"

  # ─────────────────── NEGATIVE / BOUNDARY PAGINATION ───────────────────
  # Spec does not state how invalid pagination params are handled, so these
  # assert the weaker-but-true property (no 5xx) and record actual behavior.
  # Tighten to an exact code once the contract is confirmed.

  @Run @Negative @allure.label.story:Negative_Scenario
  Scenario Outline: Users list with invalid pagination <label> does not 5xx
    When Get users list page "<page>" perPage "<perPage>" and save response as "listResp"
    Then Assert response is not a server error in "listResp"
    And Assert status code is one of "200,400" in "listResp"
    Examples:
      | label            | page | perPage |
      | per_page zero    | 1    | 0       |
      | per_page negative| 1    | -5      |
      | page zero        | 0    | 10      |
      | page negative    | -1   | 10      |
      | per_page over max| 1    | 100000  |
      | page non-numeric | abc  | 10      |
      | perPage non-num  | 1    | xyz     |
