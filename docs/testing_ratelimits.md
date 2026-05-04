# Rate Limits & Testing Guide

## Current Rate Limits

### Nginx (first barrier — blocks before reaching Go)

| Zone | Rate | Burst | Applies to |
|---|---|---|---|
| `general` | 10 req/s | 20 | All endpoints |
| `create_user` | 30 req/min | 10 | `POST /api/v1/users/create` only |
| `addr` (conn) | — | 10 conn | All (connection limit) |

### Go middleware (second barrier)

| Limiter | Rate | Burst | Applies to |
|---|---|---|---|
| `GlobalRateLimiter` | 10 req/s | 20 | All endpoints |
| `CreateUserRateLimiter` | 100 req/min | 100 | `POST /api/v1/users/create` |
| `LoginRateLimiter` | 5 req/min | 5 | `POST /api/v1/login` |

> **Rule:** Nginx limits are the effective ceiling. If nginx limit < Go limit, Go middleware is never reached.  
> After the fix: nginx allows 10 burst → 30/min, Go allows 100/min — nginx is the binding constraint for create.

---

## What Was Fixed (2026-05-04)

**Problem:** `POST /api/v1/users/create` had nginx rate limit of `2r/m, burst=2`.  
Automated test suites sending 3+ requests in quick succession got `429 Too Many Requests` from nginx even though the Go middleware allowed up to 100/min.

**Fix in `nginx.conf`:**
- Zone rate: `2r/m` → `30r/m`
- Burst: `2` → `10`

Both server blocks updated (`ru.play-qa.com` + `play-qa.com`).

---

## Tips for Writing Tests (Java + JUnit5 + RestAssured)

### 1. Always use a unique email per test

```java
private String uniqueEmail() {
    return "test+" + System.nanoTime() + "@example.com";
}
```

Without this, repeated runs hit `409 DUPLICATE_USER` (intentional behavior — user accounts have 24h TTL but emails must be unique within that window).

### 2. Control test execution order

JUnit5 does not guarantee method order by default. Use `@TestMethodOrder` to avoid burst collisions between test methods:

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserApiTest {
    @Test @Order(1)
    void createUser_success() { ... }

    @Test @Order(2)
    void createUser_withDuplicateEmail_returns409() { ... }
}
```

### 3. Testing the rate limit itself (intentional 429)

```java
@Test
void createUser_exceedsBurst_returns429() {
    // First 10 requests should pass (burst=10)
    for (int i = 0; i < 10; i++) {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("email", uniqueEmail(), "password", "Test1234!"))
        .when()
            .post("/api/v1/users/create")
        .then()
            .statusCode(not(429));
    }
    // 11th request in the same second should be rate-limited
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", uniqueEmail(), "password", "Test1234!"))
    .when()
        .post("/api/v1/users/create")
    .then()
        .statusCode(429);
}
```

### 4. Retry / wait pattern for flaky tests

If a test needs to wait for the rate limit window to reset, use Awaitility instead of `Thread.sleep`:

```java
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

await().atMost(35, SECONDS).pollInterval(2, SECONDS).until(() -> {
    int status = given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", uniqueEmail(), "password", "Test1234!"))
    .when()
        .post("/api/v1/users/create")
    .getStatusCode();
    return status != 429;
});
```

### 5. Login rate limit is Go-level only (5 req/min, burst=5)

`POST /api/v1/login` is not in a separate nginx zone — it falls under `general` (10r/s).  
The binding limit is Go's `LoginRateLimiter`: **5 requests per minute per IP**.  
Leave at least 12 seconds between login attempts in tests, or use different IPs.

---

## Known Intentional Behaviors (Do Not Fix)

These are teaching scenarios for QA practice:

| Endpoint | Behavior | Reason |
|---|---|---|
| `GET /api/v1/auth/basic` | Hardcoded `admin:admin` | Basic Auth practice |
| `GET /api/v1/users/list` | No auth required | Intentional public directory |
| `GET /api/v1/users/get/:id` | No auth required | Intentional public profile access |
| `POST /api/v1/users/create` | `409 DUPLICATE_USER` on existing email | Intentional enumeration scenario |
