# play-api-java

> **API Test Automation Framework** for [play-qa.com](https://www.play-qa.com) — built with Java 17, Rest Assured, Cucumber 7 (BDD), JUnit 5, Allure Reporting, and Lombok.

**[Allure Report →](https://romasky.github.io/play-api-java/latest/)**

---

## Features

- **Strict Object Pattern** — every request/response is a typed DTO. No raw JSON, no `Map<String, Object>`.
- **BDD Scenarios** — human-readable Cucumber `.feature` files covering positive and negative paths.
- **Allure Reports** — rich HTML reports with step-level detail, published automatically to GitHub Pages.
- **Temp Mail flows** — end-to-end mailbox tests (create → send → list/read → delete) driven directly through the `play-qa.com` mail API in `MailSteps` (no Selenium needed).
- **Security coverage** — auth-bypass and cross-account (IDOR) guards on mutating endpoints: empty / malformed Bearer tokens and revoked-token attacks are asserted to return `401` (see `TokenSecurityTests.feature`).
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
| Allure Adapter | 2.29.1 | JVM adapter (allure-cucumber7-jvm) |
| Allure Report | 3.8.0 | Report generator (new UI) |
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
│   │   └── *Test.java                  # Unit tests (run by -Pquality)
│   ├── support/
│   │   └── LocalBaseUrlServer.java     # Local HTTP recorder for unit/pin tests
│   ├── play_qa_api/
│   │   ├── ApiPaths.java               # All API endpoint paths
│   │   ├── Constants.java              # Shared constants (enums, timeouts)
│   │   ├── UserFixtures.java           # Request bodies for create/update/patch user
│   │   └── models/
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
│       ├── CommonSteps.java            # Generic reusable steps (generate data, assert)
│       ├── HealthSteps.java            # Health endpoint step definitions
│       ├── MailSteps.java              # Mail API step definitions
│       ├── OptionsSteps.java           # User options step definitions
│       ├── GoldenFile.java             # Golden-file helper for pin tests
│       ├── StepCatalogPinTest.java     # Pins every step expression
│       ├── RequestPinTest.java         # Pins every outgoing request
│       └── BaseStepsTest.java          # Pins the scenario-context contract
└── resources/
    ├── config.properties               # Base URL, timeouts
    ├── cucumber.properties             # Cucumber publish settings
    ├── junit-platform.properties       # Cucumber plugin and glue config
    ├── categories.json                 # Allure failure categories
    ├── pins/                           # Goldens: step-catalog.txt, requests.txt
    └── tests/play_qa_api/
        ├── CreateUserTests.feature     # POST /users/create scenarios
        ├── DeleteUserTests.feature     # DELETE /users/delete/:id scenarios
        ├── GetUserTests.feature        # GET /users/get/:id scenarios
        ├── HealthTests.feature         # GET /health scenarios
        ├── ListUsersTests.feature      # GET /users/list scenarios
        ├── LoginTests.feature          # POST /login scenarios
        ├── LogoutTests.feature         # POST /users/logout/:id scenarios
        ├── MailTests.feature           # Mail API scenarios
        ├── OptionsTests.feature        # OPTIONS /users/options scenarios
        ├── PatchUserTests.feature      # PATCH /users/patch/:id scenarios
        ├── TokenSecurityTests.feature  # Auth-bypass / IDOR guards on mutating endpoints
        ├── UpdateUserTests.feature     # PUT /users/update/:id scenarios
        └── UserExistsTests.feature     # HEAD /users/exists/:id scenarios
tools/crap/Crap.java                    # CRAP gate over the JaCoCo report (-Pquality)
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

### Run a single feature

```bash
mvn test -Dcucumber.features=classpath:tests/play_qa_api/LoginTests.feature
```

> `-Dcucumber.filter.tags` currently has **no effect**: the tag filter declared on
> `RunnerForTest` (`@Run and not @Ignore and not @Bug and not @NotImplemented`) overrides it.
> Narrow runs by feature file instead.

### Check step bindings without calling the API

```bash
mvn test -Dcucumber.execution.dry-run=true
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

## Quality Gates (ub-mode)

The framework code itself is held to [ub-mode](https://github.com/unclebob/ub-mode) gates.
They run **offline** — unit tests only, never the live API:

```bash
mvn -Pquality verify
```

| Gate | Tool | Bar | Report |
|---|---|---|---|
| Unit tests | JUnit 5 `*Test` classes next to the code | green | `target/surefire-reports` |
| Crapper | JaCoCo + `tools/crap/Crap.java` | CRAP < 10 per method | stdout, `target/site/jacoco` |
| Dryer | PMD CPD (test sources included) | no clone over the calibrated token threshold | `target/cpd.xml` |
| Mutator | PIT | mutation ≥ 90 %, line coverage ≥ 90 % | `target/pit-reports/index.html` |

Pins protect step-layer refactors: `StepCatalogPinTest` (every Gherkin expression) and
`RequestPinTest` (every HTTP request a step sends, against a local server) compare with goldens
in `src/test/resources/pins/`.

Claude Code agents for this workflow live in `.claude/`: the `ub-mode` skill plus
`ub-implementer`, `ub-refactorer`, `ub-gatekeeper` and `ub-scribe`. The action log and glossary
are in `docs/ub-mode/`.

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

A separate offline job, `.github/workflows/quality.yml`, runs `mvn -Pquality verify` on every
push and pull request. It does not depend on play-qa.com, so API outages never block it.

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
| `/api/v1/users/options` | OPTIONS | ✅ Tested |
| `/api/v1/mail/*` | various | ✅ Tested |
