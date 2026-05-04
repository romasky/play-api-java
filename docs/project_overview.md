# play-api-java — Project Documentation

## Purpose

API test automation framework for [play-qa.com](https://www.play-qa.com). Tests verify the REST API contract: correct status codes, response bodies, headers, and error codes across all endpoints.

---

## Tech Stack

| Tool | Version | Role |
|---|---|---|
| Java | 17 | Language |
| Maven | 3.9+ | Build & dependency management |
| Rest Assured | 5.4.0 | HTTP client |
| JUnit 5 | 5.10.2 | Test runner (Cucumber integration) |
| Cucumber | 7.15.0 | BDD: `.feature` files → step definitions |
| Allure | 2.27.0 | HTML test reports |
| Lombok | 1.18.38 | DTO boilerplate reduction |
| Jackson | 2.17.0 | JSON deserialization |

---

## Project Structure

```
src/test/
├── java/com/testFramework/
│   ├── RunnerForTest.java              # JUnit 5 Suite entry point
│   ├── core/
│   │   ├── Generator.java              # Random test data generation
│   │   ├── PropertyHandler.java        # Config loader (config.properties)
│   │   ├── RestHandler.java            # Rest Assured HTTP wrapper
│   │   ├── RunMode.java                # PROD/DEV enum
│   │   └── TempMailHandler.java        # Temp mailbox helper (play-qa.com mail API)
│   ├── play_qa_api/
│   │   ├── ApiPaths.java               # All endpoint path constants and builders
│   │   ├── Constants.java              # Shared enums and timeout values
│   │   └── models/
│   │       ├── createUser/             # CreateUserReq, ProfileReq, PreferencesReq, ...
│   │       │   └── response/           # CreateUserResp, UserResp, UsersListResp
│   │       ├── error/                  # ErrorResp, ErrorDetailResp
│   │       ├── health/                 # HealthResp
│   │       ├── login/                  # LoginReq, LoginResp
│   │       ├── mail/                   # CreateMailboxReq/Resp, MessageResp, SendMessageReq/Resp
│   │       └── options/                # UserOptionsResp
│   └── steps/play_qa_api/
│       ├── ScenarioContext.java        # Variable storage (global _g / local _l)
│       ├── BaseSteps.java              # Core helpers: save, get, assertStatusCode
│       ├── AccountsSteps.java          # User CRUD, login, logout step definitions + rate-limit hooks
│       ├── CommonSteps.java            # Generic steps: generate data, assert body/headers/regex
│       ├── HealthSteps.java            # Health endpoint steps
│       ├── MailSteps.java              # Mail API steps
│       └── OptionsSteps.java           # User options steps
└── resources/
    ├── config.properties               # baseUrl, run mode
    ├── cucumber.properties             # Cucumber publish settings
    ├── junit-platform.properties       # Glue package, feature path, plugins
    └── tests/play_qa_api/
        ├── CreateUserTests.feature     # POST /api/v1/users/create
        ├── DeleteUserTests.feature     # DELETE /api/v1/users/delete/:id
        ├── GetUserTests.feature        # GET /api/v1/users/get/:id
        ├── HealthTests.feature         # GET /api/v1/health
        ├── ListUsersTests.feature      # GET /api/v1/users/list
        ├── LoginTests.feature          # POST /api/v1/login
        ├── LogoutTests.feature         # POST /api/v1/users/logout/:id
        ├── MailTests.feature           # /api/v1/mail/* (create, get, messages, send)
        ├── OptionsTests.feature        # OPTIONS /api/v1/users/options
        ├── PatchUserTests.feature      # PATCH /api/v1/users/patch/:id
        ├── UpdateUserTests.feature     # PUT /api/v1/users/update/:id
        └── UserExistsTests.feature     # HEAD /api/v1/users/exists/:id
```

---

## Architecture

### Layer Separation

```
Feature files (.feature)
    — human-readable BDD scenarios
    ↓
Step Definitions (*Steps.java)
    — translate Gherkin steps to Java calls
    ↓
RestHandler.java
    — thin Rest Assured wrapper, no business logic
    ↓
play-qa.com REST API
```

**Rules:**
- No raw HTTP calls in step definitions — all go through `RestHandler`
- No raw JSON strings in step definitions — all requests/responses use typed DTOs
- No infrastructure concerns (retry, auth schemes) in `RestHandler`

### Strict Object Pattern

Every request is a Lombok `@Builder` DTO. Every response is deserialized via `response.as(SomeResp.class)`.

```java
// Request
CreateUserReq req = CreateUserReq.builder()
    .email("user@play-qa.com")
    .username("johndoe")
    .password("SecurePass123!")
    .profile(ProfileReq.builder().firstName("John").lastName("Doe").build())
    .build();

// Response
CreateUserResp resp = response.as(CreateUserResp.class);
String token = resp.getAccessToken();
```

### ScenarioContext — Variable Storage

Step definitions share state through `ScenarioContext`. Variables are stored by name with suffix conventions:

| Suffix | Scope | Lifetime |
|---|---|---|
| `_g` | Global | Entire test suite run |
| `_l` | Local | Current scenario only |
| (none) | Local | Current scenario only |

```gherkin
Given Generate email and save as "email"         # local
Given Generate email and save as "adminEmail_g"  # global, reused across scenarios
```

### Generator — Test Data

All dynamic test data is produced by `Generator.java`. No hardcoded strings for data that could collide between runs.

| Method | Example output |
|---|---|
| `Generator.email()` | `xk3ab9mn2p@play-qa.com` |
| `Generator.username()` | `user_ab4kx9mn` |
| `Generator.password()` | `Pass_Xy3kA1!` |
| `Generator.firstName()` | `Testkmnbvc` |
| `Generator.invalidEmail()` | `notanemail_ab3k` |
| `Generator.shortPassword()` | `ab3k` |
| `Generator.fakeMongoId()` | `483920183847291038473829` |

Use **literal `Save string`** only for structural sentinels: fixed IDs, invalid-format probes, boundary violation values.

---

## Cucumber Tags

### Feature-level tags (on every feature file)

| Tag | Meaning |
|---|---|
| `@AllTests` | Included in every full suite run |
| `@Users` | Triggers 2s pacing hook (nginx rate limit) |
| `@Auth` | Auth-related features |
| `@Login` | Triggers 13s pacing hook (Go rate limit) |
| `@CRUD` | User CRUD operations |
| `@Mail` | Mail API |
| `@Health` | Health endpoint |
| `@Options` | OPTIONS endpoint |

### Scenario-level tags

| Tag | Meaning |
|---|---|
| `@Run` | Included in standard runs |
| `@Smoke` | Subset for smoke/fast verification |
| `@Bug` | Known failing — excluded from CI (`@Run and not @Bug`) |

### CRUD group tags (new, one per split feature file)

`@GetUser`, `@ListUsers`, `@UserExists`, `@UpdateUser`, `@PatchUser`, `@Logout`, `@DeleteUser`

### Running by tag

```bash
mvn test -Dcucumber.filter.tags="@Smoke"
mvn test -Dcucumber.filter.tags="@CRUD"
mvn test -Dcucumber.filter.tags="@DeleteUser"
mvn test -Dcucumber.filter.tags="@Run and not @Bug"
mvn test -Dcucumber.filter.tags="@AllTests"
```

---

## Rate Limits & Pacing

The API has two rate limiters that affect test execution. Both are handled via Cucumber `@Before` hooks in `AccountsSteps.java` — not in `RestHandler`.

| Limiter | Limit | Binding layer | Hook tag | Pause |
|---|---|---|---|---|
| nginx `create_user` zone | 30 req/min, burst=10 | nginx | `@Users` | 2s between scenarios |
| Go `LoginRateLimiter` | 5 req/min, burst=5 | Go middleware | `@Login` | 13s between scenarios |

**Principle:** pacing is an orchestration concern, not an HTTP client concern. `RestHandler` is a dumb wrapper — it sends requests and returns responses.

---

## API Coverage

| Endpoint | Method | Feature file |
|---|---|---|
| `/api/v1/users/create` | POST | `CreateUserTests.feature` |
| `/api/v1/login` | POST | `LoginTests.feature` |
| `/api/v1/users/get/:id` | GET | `GetUserTests.feature` |
| `/api/v1/users/list` | GET | `ListUsersTests.feature` |
| `/api/v1/users/exists/:id` | HEAD | `UserExistsTests.feature` |
| `/api/v1/users/update/:id` | PUT | `UpdateUserTests.feature` |
| `/api/v1/users/patch/:id` | PATCH | `PatchUserTests.feature` |
| `/api/v1/users/logout/:id` | POST | `LogoutTests.feature` |
| `/api/v1/users/delete/:id` | DELETE | `DeleteUserTests.feature` |
| `/api/v1/health` | GET | `HealthTests.feature` |
| `/api/v1/users/options` | OPTIONS | `OptionsTests.feature` |
| `/api/v1/mail/*` | POST/GET/DELETE | `MailTests.feature` |

---

## CI/CD

GitHub Actions workflow (`.github/workflows/`):

- Triggers: push to `main`/`develop`, PR to `main`, daily cron at 08:00 UTC, manual dispatch
- Java 17 (Temurin), Maven test
- Tag filter: `@Run`
- Artifacts: Allure results, Cucumber HTML report

```bash
# Workflow command
mvn test -Dcucumber.filter.tags="@Run" -DbaseUrl="https://www.play-qa.com"
```

---

## Known Intentional API Behaviors

These are by design — do not "fix" them in tests:

| Endpoint | Behavior | Reason |
|---|---|---|
| `GET /api/v1/users/list` | No auth required | Intentionally public |
| `GET /api/v1/users/get/:id` | No auth required | Intentionally public profile |
| `DELETE/PUT/PATCH` non-existent user | Returns `401 INVALID_TOKEN`, not `404` | Token ownership validated before resource existence |
| `POST /users/create` | `409 DUPLICATE_USER` on existing email | Intentional enumeration scenario for QA practice |

---

## Adding New Tests

1. Add a new scenario to the relevant `.feature` file (or create a new file with matching tags)
2. If the step doesn't exist, add it to the appropriate `*Steps.java` class
3. If new test data is needed, add a method to `Generator.java` and a `@Given` step in `CommonSteps.java`
4. If a new endpoint is added, add its path constant to `ApiPaths.java`
5. Run locally: `mvn test -Dcucumber.filter.tags="@YourNewTag"`
