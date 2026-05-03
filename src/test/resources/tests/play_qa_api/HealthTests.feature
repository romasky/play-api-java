#language: en
@AllTests @Health
Feature: GET /api/v1/health

  @Run @Smoke
  Scenario: Health endpoint returns 200 with status ok and timestamp
    When Get health and save response as "response"
    Then Get and check status code 200 from "response"
    And Convert health response "response" to HealthResp and save as "health"
    And Assert health status is ok in "health"

  @Run @Smoke
  Scenario: Health response includes X-Request-ID header auto-generated
    When Get health and save response as "response"
    Then Get and check status code 200 from "response"
    And Assert response header "X-Request-ID" is present in "response"

  @Run
  Scenario: Health response echoes provided X-Request-ID header
    Given Save string "my-custom-request-id-123" as "myId"
    When Get health with request id "myId" and save response as "response"
    Then Get and check status code 200 from "response"
    And Assert response header "X-Request-ID" equals "my-custom-request-id-123" in "response"
