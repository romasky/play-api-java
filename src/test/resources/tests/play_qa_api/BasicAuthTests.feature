#language: en
@AllTests @BasicAuth
Feature: GET /api/v1/auth/basic — HTTP Basic Authentication

  @Run @Smoke
  Scenario: Valid credentials admin/admin return 200 success
    When Send basic auth request with username "admin" password "admin" and save response as "response"
    Then Get and check status code 200 from "response"
    And Convert basic auth response "response" to BasicAuthResp and save as "auth"
    And Assert basic auth success in "auth"

  @Run
  Scenario: No Authorization header returns 401 with WWW-Authenticate header and correct message
    When Send basic auth request with no Authorization header and save response as "response"
    Then Get and check status code 401 from "response"
    And Assert response header "WWW-Authenticate" contains "Basic realm=" in "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Basic authentication required" in "error"

  @Run
  Scenario: Bearer scheme instead of Basic returns 401 invalid format
    Given Save string "Bearer" as "scheme"
    And Save string "YWRtaW46YWRtaW4=" as "creds"
    When Send basic auth request with scheme "scheme" credentials "creds" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid authentication format" in "error"

  @Run
  Scenario: Invalid base64 encoding returns 401 with correct message
    When Send basic auth request with raw Authorization "Basic !!!notbase64!!!" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid base64 encoding" in "error"

  @Run
  Scenario: Valid base64 but no colon separator returns 401 invalid credentials format
    # base64("adminadmin") — no colon, so no user:pass split
    When Send basic auth request with raw Authorization "Basic YWRtaW5hZG1pbg==" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid credentials format" in "error"

  @Run
  Scenario: Wrong username returns 401 invalid username or password
    When Send basic auth request with username "wronguser" password "admin" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid username or password" in "error"

  @Run
  Scenario: Wrong password returns 401 invalid username or password
    When Send basic auth request with username "admin" password "wrongpassword" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid username or password" in "error"

  @Run
  Scenario: Error response is non-standard — no success timestamp or request_id fields
    When Send basic auth request with no Authorization header and save response as "response"
    Then Get and check status code 401 from "response"
    And Assert basic auth error response has no field "success" in "response"
    And Assert basic auth error response has no field "timestamp" in "response"
    And Assert basic auth error response has no field "request_id" in "response"

  @Run
  Scenario: Empty password (admin with colon but empty pass) returns 401
    # base64("admin:") = YWRtaW46
    When Send basic auth request with raw Authorization "Basic YWRtaW46" and save response as "response"
    Then Get and check status code 401 from "response"
    And Convert basic auth error response "response" to BasicAuthErrorResp and save as "error"
    And Assert basic auth error message is "Invalid username or password" in "error"
