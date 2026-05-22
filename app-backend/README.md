# FindOut — Backend

Spring Boot 4 service for the FindOut Android app. Exposes nearby-locations search and JWT-based authentication.

---

## Features Implemented

### 1. Locations API
- `POST /api/locations/nearby` — body `{ "latitude": ..., "longitude": ... }`, returns up to 10 locations within 1500 m of the point, sorted by `average_score` desc. Backed by PostGIS (`ST_DWithin`, `ST_MakePoint`) on a Supabase Postgres instance.
- `GET /api/locations/search?q=<name>` — case-insensitive, diacritic-insensitive substring search on the location name. Returns up to 50 results, sorted alphabetically by `name`. Same `LocationDto` JSON shape as `/nearby`.
  - Query is normalised the same way the indexed `normalized_name` column is: trim → collapse internal whitespace → lowercase under `Locale.ROOT` → strip Unicode combining marks. So `q=Herastrau` finds **Herăstrău Park**.
  - Blank query (`q=` or whitespace only) returns `200 []` — never the full table.
  - `q.length() > 100` returns `400 Bad Request`.
  - Requires a JWT — covered by `SecurityConfig`'s `anyRequest().authenticated()` rule, no extra wiring needed.
- Entity: `Location` (PostGIS `Point`, accessibility/toilet flags, score, price tier, plus the new `normalized_name` column).

### 2. Authentication API (new — built in this pass)
- `POST /auth/register` → `201 Created` + `{ token, expiresAt }`. Creates the user, BCrypts the password, returns a fresh JWT.
- `POST /auth/login` → `200 OK` + `{ token, expiresAt }`. Verifies BCrypt match, returns a fresh JWT.
- `POST /auth/change-password` → `204 No Content`. Requires `Authorization: Bearer <jwt>`. Verifies old password, BCrypts and stores the new one.
- `expiresAt` is **epoch milliseconds**, matching the Android `AuthTokenStore` contract.

### 3. Persistence
- New `users` table created automatically by Hibernate (`ddl-auto=update`):
  - `id UUID PK`, `email VARCHAR UNIQUE NOT NULL`, `password_hash VARCHAR NOT NULL`, `created_at TIMESTAMP`.
- Email is normalised to lowercase + trimmed before lookup/insert, so `Foo@bar.com` and `foo@bar.com` are the same account.

### 4. Security wiring
- Stateless Spring Security filter chain.
- `BCryptPasswordEncoder(12)` bean.
- `JwtAuthenticationFilter` placed before `UsernamePasswordAuthenticationFilter`.
- `permitAll()` on `/auth/register`, `/auth/login`, and Swagger endpoints; `authenticated()` on everything else — including `/api/locations/nearby` and `/api/hello`, which are no longer public.
- A `RestControllerAdvice` translates auth errors into the right status codes:
  - `409 Conflict` for duplicate email.
  - `401 Unauthorized` for *any* invalid-credentials case (same body for "no such email" and "wrong password" — anti-enumeration).
  - `400 Bad Request` for bean-validation failures.
- All auth-failure responses outside of `/auth/*` are turned into bare `401`s by `HttpStatusEntryPoint` (no HTML login page).

### 5. JWT issuance & validation
- HS256 signing, secret loaded from `${JWT_SECRET}` env var (or a clearly-marked dev fallback).
- 1-hour TTL by default, tunable via `security.jwt.ttl-seconds`.
- Token payload is minimal: `sub = userId`, `iat`, `exp`. No PII, no roles (yet).
- On every protected request, the filter parses the token, looks the user up by id, and stores the resulting `User` in the `SecurityContext` so controllers can grab it via `@AuthenticationPrincipal User user`.

### 6. Search index strategy
- `Location` carries a persisted `normalized_name` column populated by `@PrePersist`/`@PreUpdate` (entity hook → `TextNormalizer.normalize`). Inserts and updates pay the normalisation cost once, not on every search.
- `idx_location_normalized_name` (B-tree). Effective for prefix lookups; substring (`LIKE '%foo%'`) still scans, but the per-row `LOWER(...)` work is gone — we'd need a trigram index for true substring index use, which the spec rules out.
- Existing rows pre-date the column and `@Immutable` blocks JPA from updating them. `NormalizedNameBackfill` runs once on `ApplicationReadyEvent` and uses `JdbcTemplate` to populate `NULL` rows. Idempotent.

### 7. Validation
- `@Email`, `@NotBlank`, `@Size(min = 8)` on registration & change-password DTOs.
- A weak password or malformed email returns `400` before any DB or BCrypt work happens.

---

## Configuration

```properties
# application.properties
security.jwt.secret=${JWT_SECRET:dev-only-change-me-please-change-me-32b!!}
security.jwt.ttl-seconds=3600
```

For production, set the env var to a long random string (≥ 32 bytes):

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```

---

## Code Map (auth pieces)

| File | Role |
|---|---|
| `model/User.java` | JPA entity for the `users` table |
| `repository/UserRepository.java` | `findByEmail`, `existsByEmail` |
| `dto/auth/RegisterRequest.java` | `email`, `password` (validated) |
| `dto/auth/LoginRequest.java` | `email`, `password` |
| `dto/auth/ChangePasswordRequest.java` | `oldPassword`, `newPassword` |
| `dto/auth/AuthResponse.java` | `token`, `expiresAt` (epoch millis) |
| `service/JwtService.java` | Issues + parses HS256 JWTs |
| `service/AuthService.java` | Hash, verify, persist, issue token |
| `controller/AuthController.java` | `/auth/register`, `/auth/login`, `/auth/change-password` |
| `controller/AuthExceptionHandler.java` | Maps auth exceptions to HTTP status |
| `config/JwtAuthenticationFilter.java` | Reads `Bearer ...` header, sets `SecurityContext` |
| `config/SecurityConfig.java` | Filter chain, password encoder, route rules |
| `config/NormalizedNameBackfill.java` | One-shot startup job that backfills `normalized_name` on legacy rows |
| `service/TextNormalizer.java` | Shared trim/lowercase/strip-diacritics helper used at write and read |

---

## Spring Security Concepts — Explained

This section is meant as a teach-me reference for the parts you just touched. Each concept is paired with the file in this repo where it actually lives, so you can read along.

### 1. The Spring Security filter chain

When a request hits the server, it doesn't go straight to your controller. It passes through a **chain of servlet filters** that Spring Security registered. Each filter can inspect the request, decide whether to let it through, modify it, or short-circuit with an error.

The filter chain we built (in `SecurityConfig.securityFilterChain`):

```
HTTP request
    ↓
[CSRF filter — disabled]
    ↓
[CORS filter]
    ↓
[SessionManagementFilter — STATELESS, so it does nothing useful]
    ↓
[JwtAuthenticationFilter]   ← OUR custom filter
    ↓
[UsernamePasswordAuthenticationFilter — unused, but Spring still keeps it]
    ↓
[AuthorizationFilter — checks the rules from authorizeHttpRequests(...)]
    ↓
Controller (or 401/403 short-circuit)
```

Why do we put `JwtAuthenticationFilter` *before* `UsernamePasswordAuthenticationFilter`? Because we want our JWT to be considered *first*. By the time the authorization filter at the bottom asks "is this request authenticated?", we've either already populated the SecurityContext with the user (and they pass) or we haven't (and they're treated as anonymous → 401).

### 2. `SecurityContext` and `SecurityContextHolder`

Spring Security represents the currently-logged-in user as an `Authentication` object stored in a `SecurityContext`. The static `SecurityContextHolder` is the gateway to that context. By default the holder uses **thread-local storage**, so each request thread sees its own context.

Our flow:
1. The filter parses the JWT and looks up the `User`.
2. Wraps it in a `UsernamePasswordAuthenticationToken(user, null, authorities)`.
3. Calls `SecurityContextHolder.getContext().setAuthentication(auth)`.
4. The downstream `AuthorizationFilter` reads it back and decides whether the request passes the rule (`anyRequest().authenticated()`).
5. The controller can pull the user out via `@AuthenticationPrincipal User user`.

Notice we pass `null` as credentials — once we've validated the token, we don't keep the password around. Good practice; never carry secrets further than they need to go.

### 3. Stateless sessions

```java
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

By default Spring Security wants to create an HTTP session and stash the `SecurityContext` in it (cookie-based auth). For a JWT API you don't want that — the JWT *is* the session. `STATELESS` tells Spring: don't create sessions, don't read sessions, don't set `JSESSIONID`. Every request must carry its own credentials. Auth state lives entirely in the token and lasts only as long as the request thread.

### 4. CSRF — and why we disabled it

CSRF (Cross-Site Request Forgery) attacks rely on the browser **automatically** sending cookies to your site when a malicious page tricks the user into making a request. Spring's CSRF filter mitigates this by requiring a token in a header that an attacker on another domain can't read.

We disabled CSRF because:
- We don't use cookies — the JWT is sent in the `Authorization` header.
- A malicious site **cannot** automatically attach an `Authorization` header to a cross-origin request.
- So the attack the CSRF filter defends against doesn't apply to this API.

The rule of thumb: cookie-based auth → keep CSRF on. Header/token auth → safe to disable.

### 5. Authentication vs Authorization

Two different questions, often confused:
- **Authentication**: *Who are you?* — done by `JwtAuthenticationFilter`. Verifies the token signature, looks up the user, populates `SecurityContext`.
- **Authorization**: *Are you allowed to do this?* — done by `authorizeHttpRequests(...)`. Reads the SecurityContext and matches the request URL against rules.

Our authorization rules:
```java
.requestMatchers("/auth/register", "/auth/login", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
.anyRequest().authenticated()
```

`permitAll()` short-circuits the authentication check entirely (you don't need a token to register). `authenticated()` says: there must be an `Authentication` object in the SecurityContext, and it must not be the anonymous one.

We did **not** add `hasRole(...)` or `hasAuthority(...)` rules — every authenticated user has the same powers right now. When you add admin features later, you'll grant authorities in the filter (e.g. `new SimpleGrantedAuthority("ROLE_ADMIN")`) and gate routes with `.hasRole("ADMIN")`.

### 6. `PasswordEncoder` and BCrypt

```java
return new BCryptPasswordEncoder(12);
```

BCrypt is a **slow** hash function (deliberately). The `12` is the *cost factor* — each increment doubles the work. Cost 12 takes ~250ms on a modern laptop. That's invisible to a legitimate user logging in once, but ruinous to an attacker trying to crack a leaked database hash by hash.

Two methods:
- `encode(rawPassword)` — generates a random salt, runs BCrypt, and returns a 60-character string that *contains* the salt. Used on register and change-password.
- `matches(rawPassword, storedHash)` — extracts the salt from `storedHash`, re-runs BCrypt, compares. Used on login. **Constant-time comparison**, so attackers can't time the response to learn anything.

You never need to manage salts yourself; they're embedded in the hash.

### 7. JWT — what it actually is

A JWT is just three Base64URL-encoded chunks separated by dots: `<header>.<payload>.<signature>`. Decode any of the first two and you'll see plain JSON. **The payload is not encrypted** — anyone holding the token can read it. The signature is what makes it untamperable: if you change the payload, the signature no longer matches the secret on the server, and `parseSignedClaims()` throws.

What our token carries:
- `sub` (subject) → the user id, as a UUID string.
- `iat` (issued at) → timestamp.
- `exp` (expiration) → timestamp; rejected after this.

What it does **not** carry:
- The user's email, password, or any PII.
- Roles or permissions (we don't use them yet — when you add them, prefer looking them up server-side rather than baking them in, so revocation is immediate).

Validation in `JwtService.parseUserId`:
```java
Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
```
This single call:
1. Re-computes the signature using `signingKey`. If it doesn't match → `SignatureException`.
2. Checks `exp`. If expired → `ExpiredJwtException`.
3. Returns the claims if everything is good.

Our filter catches *any* exception from this and treats the request as unauthenticated. Failing closed.

### 8. Why HS256 specifically?

HS256 is symmetric — the same secret signs and verifies. That's fine because the only party verifying tokens is *us*. If multiple services needed to verify tokens issued by an auth server they don't trust with signing power, you'd switch to RS256 (asymmetric: private key signs, public key verifies). Not relevant here.

### 9. `OncePerRequestFilter`

`JwtAuthenticationFilter extends OncePerRequestFilter`. Spring sometimes dispatches a request internally (forwards, error handling) which can re-enter the filter chain. `OncePerRequestFilter` guarantees `doFilterInternal` runs at most once per real HTTP request. Without it you could end up parsing the JWT twice, or seeing surprising state on error pages.

### 10. Anti-enumeration in the AuthService

Look at `AuthService.login`:
```java
User user = userRepository.findByEmail(...)
        .orElseThrow(InvalidCredentialsException::new);
if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
    throw new InvalidCredentialsException();
}
```

Both branches throw the **same** exception, which `AuthExceptionHandler` maps to the **same** body (`{"error":"INVALID_CREDENTIALS"}`) and the **same** status code (`401`). An attacker probing the endpoint can't distinguish "this email isn't registered" from "this email is registered but the password is wrong". They learn nothing about who has an account.

(The timing is technically still distinguishable — BCrypt only runs in the second branch — but the easy enumeration vector via response shape is closed. If you want to harden timing too, run a dummy BCrypt against a fixed hash on the first branch.)

### 11. `@AuthenticationPrincipal`

```java
public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user, ...) { ... }
```

The principal is whatever object you stored as the first argument of `UsernamePasswordAuthenticationToken` in the filter. We stored the full `User` entity, so the controller can take `@AuthenticationPrincipal User user` directly. No need to re-parse the token, no need to read the SecurityContext manually.

### 12. What we deliberately did **not** build

These would be the natural next steps but are out of scope for this pass (CLAUDE.md TODO list):

- **Refresh tokens** — when the access token expires after 1 h the user has to log in again.
- **Rate limiting on `/auth/login`** — without this, BCrypt's slowness still lets an attacker make ~4 login attempts per second, per IP. A token-bucket filter (e.g. bucket4j) would cap that to a few per minute.
- **Password reset via email** — needs an email channel.
- **Roles / authorities** — single-tier auth right now.
- **Audit logging** — successful and failed auth events should land somewhere durable.

---

## Running

```bash
# dev (uses fallback secret — fine for local)
./mvnw spring-boot:run

# prod
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

The Android app (`appfrontend/`) points at `http://10.0.2.2:8080/` from the emulator and `https://...` from a real device build.
