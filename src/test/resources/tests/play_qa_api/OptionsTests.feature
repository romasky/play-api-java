#language: en
@AllTests @Options
Feature: OPTIONS /api/v1/users/options

  @Run @Smoke
  Scenario: OPTIONS returns 204 and Allow header
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"
    And Assert response header "Allow" is present in "response"

  @Run
  Scenario: OPTIONS Allow header contains all required methods
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"
    And Assert response header "Allow" contains "GET" in "response"
    And Assert response header "Allow" contains "POST" in "response"
    And Assert response header "Allow" contains "PUT" in "response"
    And Assert response header "Allow" contains "DELETE" in "response"
    And Assert response header "Allow" contains "PATCH" in "response"
    And Assert response header "Allow" contains "HEAD" in "response"
    And Assert response header "Allow" contains "OPTIONS" in "response"

  @Run
  Scenario: OPTIONS does not require Authorization header
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"
