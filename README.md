# play-api-java

BDD API test framework for [play-qa.com](https://www.play-qa.com) — Java 17, Cucumber 7 on the
JUnit 5 Platform, Rest Assured, typed Jackson/Lombok DTOs and Allure reporting. The framework code
itself is held to Uncle Bob's [ub-mode](https://github.com/unclebob/ub-mode) quality gates.

**[Latest Allure report →](https://romasky.github.io/play-api-java/latest/)** ·
**[Wiki →](https://github.com/romasky/play-api-java/wiki)**

---

## At a glance

- **152 live scenarios** in 17 feature files: users (create / get / list / exists / update / patch /
  delete), login / logout, health, options, temp mail, and token security (auth bypass, IDOR —
  a valid token of another user must get `403 FORBIDDEN`).
- **Strict object pattern** — every request and response is a typed DTO; no raw JSON in steps.
- **Offline quality gates** — unit tests for every framework class, CRAP < 10, every PIT mutant
  killed, structural duplication checked by the real Dryer. They never call the API.
- **CI** — live suite on push / PR / daily 08:00 UTC, Allure report published to GitHub Pages;
  a separate offline job runs the quality gates.

## Tech stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language (`--release 17`) |
| Maven | 3.9+ | Build |
| Rest Assured | 5.4.0 | HTTP client |
| Cucumber | 7.15.0 | BDD, run on the JUnit 5 Platform (5.10.2) |
| Jackson / Lombok | 2.17.0 / 1.18.38 | DTO (de)serialization and builders |
| Allure Java | 2.35.5 | `allure-cucumber7-jvm`, `allure-junit5`; `@Step` woven by AspectJ 1.9.26 |
| Allure Report | 3.22.1 | Report generator (via `allure-maven` 3.1.0) |
| JaCoCo / PIT / PMD CPD | 0.8.15 / 1.25.7 / 3.28.0 | Quality gates (`-Pquality`) |

---

## Running tests

| What | Command |
|---|---|
| Full live suite (calls play-qa.com) | `mvn test` |
| One feature file | `mvn test -Dcucumber.features=classpath:tests/play_qa_api/LoginTests.feature` |
| Step bindings only, no HTTP | `mvn test -Dcucumber.execution.dry-run=true` → `Tests run: 152` |
| Another environment | `mvn test -DbaseUrl=https://staging.example.com` |
| Allure report | `mvn allure:serve`, or `mvn allure:report` → `target/site/allure-maven-plugin/index.html` |
| Offline quality gates | `mvn -Pquality verify` and `tools/dryer/dryer-gate.sh` |

> **`-Dcucumber.filter.tags` has no effect.** `RunnerForTest` sets the tag filter
> (`@Run and not @Ignore and not @Bug and not @NotImplemented`) via `@ConfigurationParameter`,
> which overrides the system property — and with it the `tags` input of the manual CI run.
> Narrow a run with `-Dcucumber.features=...` instead.

Live runs are slow on purpose: scenarios in the `User_Management` suite pause 2 s and the `Login`
sub-suite 13 s, to stay under the server's rate limits.

---

## How it fits together

```
Feature files (Gherkin, src/test/resources/tests/play_qa_api)
   ↓
Step classes (steps/play_qa_api: Accounts, Common, Health, Mail, Options)
   ↓ context + helpers: BaseSteps, ScenarioContext; request bodies: UserFixtures
RestHandler (Rest Assured; one Allure @Step + response attachment per call)
   ↓
play-qa.com API
```

**Context variables** — steps save and read values by name. The suffix sets the scope:
`name_g` is global for the whole run; `name_l` or no suffix belongs to the current scenario and
falls back to global. A name that is not in the context is used as a literal, so
`"Bearer abc"` can be passed where a variable is expected.

```gherkin
When Create minimal user and save response as "createResp"
Then Get and check status code 201 from "createResp"
And Convert create user response "createResp" to CreateUserResp and save as "user"
And Save field accessToken from CreateUserResp "user" as "token"
```

**DTOs** — requests are Lombok builders, responses are deserialized by Jackson:

```java
CreateUserReq body = UserFixtures.randomUser().build();   // random email, username, password, profile
CreateUserResp user = response.as(CreateUserResp.class);
```

### Project layout

```
src/test/java/com/testFramework/
├── RunnerForTest.java          JUnit Platform suite (glue, plugins, tag filter)
├── core/                       RestHandler, PropertyHandler, Generator (+ unit tests)
├── play_qa_api/                ApiPaths, UserFixtures, models/ (request/response DTOs)
├── steps/play_qa_api/          step classes, BaseSteps, ScenarioContext,
│                               pin tests (StepCatalogPinTest, RequestPinTest) and unit tests
└── support/                    offline test helpers: LocalBaseUrlServer, Fake, StepContext, AllureFixture
src/test/resources/
├── tests/play_qa_api/          17 feature files
├── pins/                       goldens: step-catalog.txt, requests.txt
├── categories.json             Allure failure categories
└── config.properties           base URL, timeouts (-Dkey=value overrides)
tools/                          crap/Crap.java, pit/MutationGate.java, dryer/ (quality gates)
```

---

## Quality gates (ub-mode)

All framework code — `core`, `play_qa_api` (DTO models excluded) and every step class — is gated
offline, in this order:

```bash
mvn -Pquality verify        # unit tests → JaCoCo → CRAP → CPD → PIT → MutationGate
tools/dryer/dryer-gate.sh   # Dryer (Python 3.11+)
```

| Gate | Tool | Passes when |
|---|---|---|
| Unit tests | JUnit 5 `*Test` classes | green — every step class has offline tests against fake responses or a local HTTP server |
| Crapper | JaCoCo + `tools/crap/Crap.java` | every method CRAP < 10 |
| CPD clone check | PMD CPD | no exact token clone ≥ 85 tokens (an extra check, not Dryer) |
| Mutator | PIT + `tools/pit/MutationGate.java` | **every** mutant `KILLED` — no survivor, no uncovered mutant, no timeout or error |
| Dryer | real [unclebob/dryer](https://github.com/unclebob/dryer), pinned | no structural duplicate ≥ 0.8, except pairs of one-statement Cucumber bindings (accepted by rule) |

Step-layer refactors are protected by pins: `StepCatalogPinTest` (every step expression) and
`RequestPinTest` (every HTTP request a step sends) compare against the goldens in
`src/test/resources/pins/`. The only gate exemption is PIT on the two rate-limit hooks, which only
call the unit-tested `BaseSteps.pause`. Details, failure reading and the reasons behind each
setting: [Quality Gates wiki page](https://github.com/romasky/play-api-java/wiki/Quality-Gates).

**Claude Code agents** in `.claude/` apply the same discipline: the `ub-mode` skill plus
`ub-implementer` (test-first changes), `ub-refactorer` (pin → change → prove), `ub-gatekeeper`
(read-only gate run and review), `ub-scribe` (log, commits) and `senior-test-engineer`
(scenarios and bug reports).

---

## CI/CD

| Workflow | Runs on | Does |
|---|---|---|
| `test.yml` — API Tests | push to `main`/`develop`, PR to `main`, daily 08:00 UTC, manual | live suite; Allure + Cucumber reports as artifacts; on `main`, publishes the Allure report to [GitHub Pages](https://romasky.github.io/play-api-java/latest/) |
| `quality.yml` — Quality Gates | push to `main`/`develop`, PR to `main`, manual | `mvn -Pquality verify` + Dryer; offline, so API outages never block it |

---

## API coverage

| Endpoint | Method |
|---|---|
| `/api/v1/health` | GET |
| `/api/v1/login` | POST |
| `/api/v1/users/create` | POST |
| `/api/v1/users/get/{id}` | GET |
| `/api/v1/users/list` | GET (`page`, `per_page` query) |
| `/api/v1/users/exists/{id}` | HEAD, GET |
| `/api/v1/users/update/{id}` | PUT |
| `/api/v1/users/patch/{id}` | PATCH |
| `/api/v1/users/delete/{id}` | DELETE |
| `/api/v1/users/logout/{id}` | POST |
| `/api/v1/users/options` | OPTIONS |
| `/api/v1/mail/create`, `/mail/{token}`, `/mail/{token}/messages[/{id}]`, `/mail/{token}/send` | POST, GET, DELETE |

Per-feature scenario counts, every step expression and the DTO reference are in the
[wiki](https://github.com/romasky/play-api-java/wiki).
