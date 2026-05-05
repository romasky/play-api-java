#language: en
@allure.label.epic:User_Lifecycle @allure.label.suite:User_Management @allure.label.subSuite:Options
Feature: Options

  @Run @Smoke @Positive @allure.label.severity:critical @allure.label.story:Positive_Scenario
  Scenario: OPTIONS returns 204
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"

  @Run @Positive @allure.label.story:Positive_Scenario
  Scenario: OPTIONS does not require Authorization header
    When Send OPTIONS request to users and save response as "response"
    Then Get and check status code 204 from "response"
