#language: en
@AllTests @Options
Feature: OPTIONS /api/v1/users/options

  @Run @Smoke
  Scenario: OPTIONS returns 204
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"

  @Run
  Scenario: OPTIONS does not require Authorization header
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"
