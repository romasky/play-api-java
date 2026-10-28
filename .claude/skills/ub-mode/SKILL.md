---
name: ub-mode
description: >
  Uncle Bob's reduced engineering principles (github.com/unclebob/ub-mode), adapted to the
  play-api-java framework. Every change follows First Make it Work, then Then Make it Right,
  then Follow Practice. Use when the user runs /ub-mode, asks for "ub-mode" / "по ub-mode",
  or when any ub-* agent (ub-implementer, ub-refactorer, ub-gatekeeper, ub-scribe) is working.
---

<!-- Upstream rules below are verbatim from unclebob/ub-mode SKILL.md (master).
     The project appendix at the end maps them onto this repository. -->

# ub-mode

Every time you are asked to make a change follow:
* First Make it Work.
* Then Make it Right.
* Follow Practice

# First Make it Work

## Laziness Protocol

* One decision lives in one place.
* Ship the smallest edit that solves the problem.

## Foundational Thinking

* Get the data shape right before you write the logic.
* Do first whatever helps every later phase.
* If a concurrent writer would matter, do not share state.

## TDD

* Write the failing test before you change the code.
* See it fail for the reason you expect.
* Make the smallest change that makes it pass.
* If an honest test is impractical, do not write a bad one.

## Redesign From First Principles

* Do not bolt a new requirement onto the old design.
* Ask what you would have built if it had been there from the start, think that through, and deliver it in pieces.

## Attack the Premise

* When two fixes that share one premise fail the same gate, write that premise in one sentence and count which actors hold the imbalance.
* Remove the assignment.
* Do not start another fix until you have done that.

## Subtract Before You Add

* Take complexity out before you add anything.

## Outcome-Oriented Execution

* Aim at the end state you can check.
* A break in the middle is allowed when you planned it and can undo it.

## Build the Lever

* If the work is not obvious at a glance, build the tool that does it.
* Do one unit by hand, then make the tool match that unit.

## Architecture

### Model the Domain

* Put the domain in a structure that makes the illegal states impossible.
* Maintain a glossary of domain terms.
* If the code is already clear and local, leave it.

### Boundary Discipline

* Check the data where it enters.
* Inside, trust it.
* Keep the business logic in a pure function.

### Type System Discipline

* Make the illegal value impossible to write.
* Parse outside data once, where it comes in.
* Do not silence the compiler.

### Make Operations Idempotent

* Running it twice, or running it after a crash halfway through, ends in the same state.

### Separate Before Serializing Shared State

* If they do not need the same mutable thing, give each its own.
* If they do, allow one writer, and enforce that in the structure.

# Then Make it Right

## Fix Root Causes

* Reproduce it.
* Fix the cause.
* A guard that only hides the crash is not a fix.

## Sequence work into verifiable units

* Finish one small unit and check it.
* Then start the next.

## Test Behavior, Not Implementation

* The test calls the code the way its user does and checks the result that user would see.
* If the test would still pass when every function returned nothing, throw it out.

## Crapper

* Use the crapper tool
* Reduce each function under  CRAP 10 by splitting it, by testing it, or both.
* Consider whether case statements are worth reducing.

## Dryer

* Use the dryer tool to eliminate duplication that rank 0.8 or above.

## Mutator

* Use the mutator tool
* Cover the uncovered.
* Kill the survivors.
* Delete dead code.

## Refactoring

* Pin what the code does.
* Change the structure.
* Prove the behavior did not change.

# Practice

## Keep a Log

* Keep a log of every action you take and the reason you took it.
* Report that log upon completion.

## Guard the Context Window

* Keep the main thread small.
* Send the bulk elsewhere, and keep the summary.

## Worktree Cleanup

* Find what is safe to remove, confirm nothing still in use needs it, then reclaim the disk.
* Uncommitted work waits for a decision.

## Commit and Push

* Commit often, in small ordered commits.
* Each commit says why, what changed, and how you proved it.
* Push to the main line at the end.

# Project Appendix — play-api-java

The rules above are universal. This appendix says what they mean **here**. Where the two
seem to conflict, the rule wins and the appendix is wrong — fix the appendix.

## Two kinds of code, two TDD loops

| Code | Lives in | "The test" is | Loop |
|---|---|---|---|
| Framework logic — `core/*`, `play_qa_api/ApiPaths`, `play_qa_api/UserFixtures`, `steps/play_qa_api/BaseSteps` | `src/test/java` | a plain JUnit 5 `*Test` next to the class | offline, seconds |
| Step glue + `.feature` scenarios | `steps/play_qa_api/*Steps`, `src/test/resources/tests` | the live play-qa.com API | online, minutes (rate limits) |

* For framework logic, TDD is literal: write the failing `*Test`, see it fail for the
  expected reason (a compile error on the new API counts), make the smallest change.
* For a new **scenario**, the scenario is the test and the API is the code under test. "See it
  fail for the reason you expect" means: run it and read the response. A scenario that asserts
  the correct contract and fails because the server is wrong is a **server bug** — tag it
  `@Bug` (not `@Run`) and report it; never bend the assertion to match the bug.
* "If an honest test is impractical, do not write a bad one": a live-only behavior (rate
  limiting, mail delivery timing) gets a scenario, not a mocked unit test that proves nothing.

## The lever: commands

| Gate | Command | Passes when |
|---|---|---|
| Unit tests only | `mvn -q -Pquality test -Djacoco.skip=true` | green |
| **All gates** | `mvn -Pquality verify` | unit tests → JaCoCo → CRAP → CPD → PIT all green |
| Crapper | `java tools/crap/Crap.java target/site/jacoco/jacoco.xml 10` | every method CRAP < 10 |
| Dryer | `mvn -Pquality pmd:cpd-check` (report: `target/cpd.xml`) | no duplication over the calibrated token threshold |
| Mutator | part of `verify`; report `target/pit-reports/index.html` | mutation ≥ 90 %, coverage ≥ 90 %, aim for 100 % |
| Step bindings (no network) | `mvn test -Dcucumber.execution.dry-run=true` | `Tests run: 152` (update the number when scenarios are added on purpose) |
| One live feature | `mvn test -Dcucumber.features=classpath:tests/play_qa_api/<File>.feature` | green or only known `@Bug`s |
| Full live suite | `mvn test` | as CI |

Notes that cost real time to learn:
* `-Dcucumber.filter.tags` is **ignored** — `RunnerForTest`'s `@ConfigurationParameter`
  overrides it. Narrow live runs with `-Dcucumber.features=...` instead.
* Never put `cucumber.features` back into `junit-platform.properties`: it makes the Cucumber
  engine ignore selectors, so every unit-test and PIT launch would run the live suite.
* PIT needs `src/main/java/` to exist on disk (it holds only a README). Do not delete it.
* CRAP has a floor: at 100 % coverage CRAP = CC. A method with CC ≥ 10 must be split.
* Live runs are slow on purpose: `User_Management` suite pauses 2 s, `Login` subSuite 13 s per
  scenario (server rate limits). Prove step refactors offline first (pins, dry-run), then run
  only the affected feature files live.

## Pins (how "Pin what the code does" is done here)

* `steps/play_qa_api/StepCatalogPinTest` — every Gherkin expression + hook; golden
  `src/test/resources/pins/step-catalog.txt`. A step-layer refactor must not change it.
* `steps/play_qa_api/RequestPinTest` — every request-producing step against a local
  `HttpServer`; golden `src/test/resources/pins/requests.txt` (method, path, query,
  Authorization absent/empty/value, Content-Type, normalized JSON body). A refactor must not
  change it. An intended behavior change updates the golden **in the same commit**, and the
  commit message says why.
* Updating a golden to make a red pin green without an intended behavior change is forbidden.

## Model the domain / boundary / type discipline — applied

* Data enters through: Gherkin arguments (strings), `config.properties`/`-D` properties, and
  API responses. Parse them once there: responses into DTOs (`models/**`, strict Object
  Pattern — no raw JSON or `Map` bodies), context keys into `BaseSteps.ContextKey`.
* Make the illegal call impossible to write. Precedent: `RestHandler.get(path, Map query, ...)`
  replaced `get(path, String[] query, ...)` because a bare array silently bound to the header
  varargs and sent pagination as headers.
* Fail loudly at the boundary, never drop silently. Precedent: an unpaired header array throws
  instead of quietly sending no `Authorization` header (which would make a "no auth → 401"
  test pass for the wrong reason).
* Glossary of domain terms: `docs/ub-mode/glossary.md`. Add a term the first time you use it.

## Practice — here

* Log: `docs/ub-mode/log.md`, newest first. Every action and its reason. Report it at the end.
* Commits: small, ordered, each says **why / what / proof**. **No `Co-Authored-By` or any
  Claude attribution** — this project's owner forbids it. Never commit `.claude/settings*.json`.
* Push to the main line only when the user asks.
* Known findings are never silently dropped: put them in the log and in the final report.

## The team

| Agent | Phase | Writes code? |
|---|---|---|
| `ub-implementer` | First Make it Work — TDD changes to framework and steps | yes |
| `ub-refactorer` | Refactoring — pin → change structure → prove | yes |
| `ub-gatekeeper` | Then Make it Right — runs all gates, reviews the diff against these rules | no (reports) |
| `ub-scribe` | Practice — log, glossary, commits, worktree cleanup | docs + git only |
| `senior-test-engineer` | test design, scenarios, bug reports (follows this skill for code changes) | yes |
