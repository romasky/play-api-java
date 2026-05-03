# play-api-java

> **API Test Automation Framework** for [play-qa.com](https://www.play-qa.com) — built with Java 17, Rest Assured, Cucumber 7 (BDD), JUnit 5, Allure Reporting, and Lombok.

---

## Features

- **Strict Object Pattern** — every request/response is a typed DTO. No raw JSON, no `Map<String, Object>`.
- **BDD Scenarios** — human-readable Cucumber `.feature` files covering positive and negative paths.
- **Allure Reports** — rich HTML reports with step-level detail, published automatically to GitHub Pages.
- **Built-in Temp Mail** — uses the `play-qa.com` mail API for end-to-end email flow tests (no Selenium needed).
- **CI/CD** — GitHub Actions runs tests on every push, PR, and daily at 08:00 UTC.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Maven | 3.9+ | Build & dependency management |
| Rest Assured | 5.4.0 | HTTP client for API testing |
| JUnit 5 | 5.10.2 | Test runner |
| Cucumber | 7.15.0 | BDD framework |
| Allure | 2.27.0 | Test reporting |
| Lombok | 1.18.38 | Boilerplate reduction |
| Jackson | 2.17.0 | JSON serialization |

---

## Project Structure

```
src/test/
├── java/com/testFramework/
│   ├── RunnerForTest.java              # JUnit 5 Suite entry point
│   ├── core/
│   │   ├── Generator.java              # Random data generation
│   │   ├── PropertyHandler.java        # Config loading (config.properties)
│   │   ├── RestHandler.java            # Rest Assured wrapper (all HTTP methods)
│   │   ├── RunMode.java                # PROD/DEV enum
│   │   └── TempMailHandler.java        # Temp mailbox via play-qa.com mail API
│   ├── play_qa_api/
│   │   ├── ApiPaths.java               # All API endpoint paths
│   │   ├── Constants.java              # Shared constants (enums, timeouts)
│   │   └── models/
│   │       ├── basicauth/              # BasicAuthResp, BasicAuthErrorResp
│   │       ├── createUser/             # Request DTOs: CreateUserReq, ProfileReq, ...
│   │       │   └── response/           # Response DTOs: CreateUserResp, UserResp, ...
│   │       ├── error/                  # ErrorResp, ErrorDetailResp, ValidationErrorResp
│   │       ├── health/                 # HealthResp
│   │       ├── login/                  # LoginReq, LoginResp
│   │       ├── mail/                   # CreateMailboxReq/Resp, MessageResp, ...
│   │       └── options/                # UserOptionsResp
│   └── steps/play_qa_api/
│       ├── ScenarioContext.java        # Global and local scenario variable storage
│       ├── BaseSteps.java              # Shared context helpers (save/get/assert)
│       ├── AccountsSteps.java          # User & auth step definitions
│       ├── BasicAuthSteps.java         # Basic auth step definitions
│       ├── CommonSteps.java            # Generic reusable steps (generate data, assert)
│       ├── HealthSteps.java            # Health endpoint step definitions
│       ├── MailSteps.java              # Mail API step definitions
│       └── OptionsSteps.java           # User options step definitions
└── resources/
    ├── config.properties               # Base URL, timeouts, run mode
    ├── cucumber.properties             # Cucumber publish settings
    ├── junit-platform.properties       # Cucumber plugin and glue config
    └── tests/play_qa_api/
        ├── BasicAuthTests.feature      # GET /auth/basic scenarios
        ├── CreateUserTests.feature     # POST /users/create scenarios
        ├── HealthTests.feature         # GET /health scenarios
        ├── LoginTests.feature          # POST /login scenarios
        ├── MailTests.feature           # Mail API scenarios
        ├── OptionsTests.feature        # GET /users/options scenarios
        └── UserCrudTests.feature       # GET/PUT/PATCH/DELETE/HEAD scenarios
```

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+

### Run all tests

```bash
mvn test
```

### Run specific tags

```bash
mvn test -Dcucumber.filter.tags="@CreateUser"
mvn test -Dcucumber.filter.tags="@Smoke"
mvn test -Dcucumber.filter.tags="@Run and not @Bug"
```

### Run against a different environment

```bash
mvn test -DbaseUrl="https://staging.play-qa.com"
```

### Generate and view Allure report

```bash
mvn allure:serve
```

---

## Architecture

### Object Pattern (strict)

All HTTP interactions use typed DTOs:

```java
// Request — built with Lombok @Builder
CreateUserReq req = CreateUserReq.builder()
    .email("user@play-qa.com")
    .username("johndoe")
    .password("SecurePass123!")
    .profile(ProfileReq.builder()
        .firstName("John")
        .lastName("Doe")
        .build())
    .build();

// Response — deserialized by Rest Assured
CreateUserResp resp = response.as(CreateUserResp.class);
String token = resp.getAccessToken();
```

### Context Variables

Scenario variables use suffix conventions for scope:
- `varName_g` — **global** (shared across all scenarios in a run)
- `varName_l` — **local** (isolated to current scenario)
- `varName` — local by default

```gherkin
Given Generate email and save as "email_g"    # global — reused across scenarios
When Create user with email "email_g" ...
```

### Layer Separation

```
Feature files (BDD scenarios)
    ↓ uses
Step Definitions (AccountsSteps, BaseSteps)
    ↓ uses
RestHandler (HTTP layer — Rest Assured)
    ↓ calls
play-qa.com API
```

No HTTP calls in tests. No raw JSON in step definitions.

---

## CI/CD

Tests run automatically on:
- Every push to `main` / `develop`
- Every pull request to `main`
- Daily at 08:00 UTC (cron)
- Manual trigger via GitHub Actions UI

Allure results and Cucumber HTML reports are uploaded as build artifacts after each run.

---

## API Coverage

| Endpoint | Method | Status |
|---|---|---|
| `/api/v1/users/create` | POST | ✅ Tested |
| `/api/v1/login` | POST | ✅ Tested |
| `/api/v1/users/get/:id` | GET | ✅ Tested |
| `/api/v1/users/list` | GET | ✅ Tested |
| `/api/v1/users/exists/:id` | HEAD | ✅ Tested |
| `/api/v1/users/update/:id` | PUT | ✅ Tested |
| `/api/v1/users/patch/:id` | PATCH | ✅ Tested |
| `/api/v1/users/delete/:id` | DELETE | ✅ Tested |
| `/api/v1/users/logout/:id` | POST | ✅ Tested |
| `/api/v1/health` | GET | ✅ Tested |
| `/api/v1/auth/basic` | GET | ✅ Tested |
| `/api/v1/users/options` | GET | ✅ Tested |
| `/api/v1/mail/*` | various | ✅ Tested |
