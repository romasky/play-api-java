#language: en
@AllTests @Users @CRUD @ListUsers @allure.label.suite:User Management @allure.label.feature:Users @allure.label.story:List_Users
Feature: GET /api/v1/users/list

  @Run @Smoke @allure.label.severity:critical
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
