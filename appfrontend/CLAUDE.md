# FindOut — Project Context for Claude

This file is loaded into Claude Code sessions to give the assistant durable context about the codebase. Keep it up to date when major architectural decisions change.

---

## Project Overview

**FindOut** is an Android app (Jetpack Compose) backed by a Spring Boot service. It surfaces nearby points of interest (Bucharest locations) on a landing screen, lets a user open a detail view for any location, and provides authentication so each user has a private account.

- **Frontend**: `appfrontend/` — Android, Kotlin, Jetpack Compose, Hilt, Retrofit, Room
- **Backend**: separate Spring Boot project (not in this repo) exposing REST endpoints under `/api/...` and `/auth/...`
- **DB**: Room (on-device cache) + Postgres/MySQL on the backend

## Architecture (frontend)

```
ui/
├── navigation/      NavRoutes, AppNavHost, BottomNavBar
├── screen/          Login, Register, Landing, Search, Profile, Attraction
├── viewmodel/       @HiltViewModel classes, one per screen, plus AppAuthViewModel
└── state/           Sealed UI state types (LocationUiState, LoginFormState, ...)

data/
├── local/           Room DAOs/entities + AuthTokenStore (EncryptedSharedPreferences)
├── remote/
│   ├── api/         ApiService (Retrofit interface)
│   ├── dto/         Wire types: LocationDto, JustCoordinatesDto, auth/*
│   └── AuthInterceptor   attaches `Authorization: Bearer <jwt>` to every call
└── repository/      LocationRepository, SessionRepository, AuthRepository

di/                  Hilt modules: NetworkModule, DatabaseModule, *RepositoryModule
```

Conventions in use:
- ViewModels expose **`StateFlow<UiState>`**; UI collects with `collectAsStateWithLifecycle()`
- UI state types are **sealed interfaces** when multiple states are possible (`LocationUiState`), or **data classes** for form state (`LoginFormState`)
- Repositories are interfaces, with `*Impl` classes bound by Hilt `@Binds` modules
- Navigation is string-based (`NavRoutes` sealed class); arguments are passed in the route path and read via `SavedStateHandle` in the receiving ViewModel

---

## Authentication Architecture

### Security Model (THE RULES)

1. **Passwords travel as plaintext over HTTPS.** TLS protects them in transit. Do NOT hash on the client — that would just make the hash the new password.
2. **Passwords are stored as BCrypt hashes on the backend.** Never as plaintext, never reversibly encrypted. The DB should only ever see `$2a$...` 60-char hashes.
3. **The backend issues a signed JWT after register or login.** The token is opaque to the client and validated server-side on every request.
4. **Same error message** for "user not found" and "wrong password" — prevents email enumeration.

### Frontend Flow (implemented)

```
First launch                       Subsequent launches
─────────────                      ───────────────────
AppAuthViewModel.isLoggedInOnStart AppAuthViewModel.isLoggedInOnStart
  → false                            → true (token present + not expired)
NavHost starts at Login            NavHost starts at Landing
User registers or logs in          AuthInterceptor attaches JWT to every call
  → POST /auth/register|login        to protected endpoints
  → AuthResponse { token, expiresAt }
  → AuthTokenStore saves them
  → navigate to Landing, pop Login
```

**Persistence**: `AuthTokenStore` wraps `EncryptedSharedPreferences` (file `auth_prefs`). The encryption key lives in the Android Keystore (`AES256_GCM` master key). Token + `expiresAt` are stored; `isLoggedIn()` returns true only if a non-blank token exists and `expiresAt > now`.

**Network**: `NetworkModule` builds an `OkHttpClient` with `AuthInterceptor`. The interceptor skips `/auth/register` and `/auth/login` (so login can run unauthenticated) and adds `Authorization: Bearer <jwt>` to every other request.

**Logout**: `AppAuthViewModel.logout()` clears the token store, then `AppNavHost` navigates back to Login with `popUpTo(0) { inclusive = true }` to wipe the back stack.

### API Contract (frontend ↔ backend)

The frontend expects these endpoints. The backend must implement them with matching JSON shapes.

#### `POST /auth/register`
**Request body**
```json
{ "email": "user@example.com", "password": "plaintext-min-8-chars" }
```
**Success** (200 or 201)
```json
{ "token": "<JWT>", "expiresAt": 1715712000000 }
```
`expiresAt` is **epoch milliseconds (Long)** — the moment the token stops being valid.

**Errors**
- `409 Conflict` — email already exists (frontend shows "An account with this email already exists")
- `400 Bad Request` — validation failure (weak password, malformed email)

#### `POST /auth/login`
**Request body**
```json
{ "email": "user@example.com", "password": "plaintext" }
```
**Success** (200) — same shape as register.
**Errors**
- `401 Unauthorized` — invalid credentials (use the SAME message for unknown email and wrong password)

#### `POST /auth/change-password`
**Headers**: `Authorization: Bearer <jwt>` required.
**Request body**
```json
{ "oldPassword": "...", "newPassword": "..." }
```
**Success** (204 No Content).
**Errors**: `401` on wrong `oldPassword`, `400` on weak `newPassword`.

#### Protected business endpoints
Every endpoint other than `/auth/register` and `/auth/login` MUST require a valid JWT. Specifically:
- `POST /api/locations/nearby` — currently public, must become authenticated
- `GET /api/hello` — currently public, must become authenticated

Frontend already sends the header. Backend must reject (401) any request without a valid token.

### What the Backend Must Build

Spring Boot project, separate from this directory. Required pieces:

1. **Dependencies**: `spring-boot-starter-security`, `spring-boot-starter-validation`, `io.jsonwebtoken:jjwt-api:0.12.x` + impl + jackson runtime.
2. **`users` table**: `id UUID PK`, `email VARCHAR UNIQUE NOT NULL`, `password_hash VARCHAR NOT NULL`, `created_at TIMESTAMP`.
3. **`BCryptPasswordEncoder` bean** (cost factor 10–12). `encoder.encode(rawPwd)` on register/change-password. `encoder.matches(rawPwd, storedHash)` on login.
4. **`JwtService`**: generates HS256-signed JWTs containing `sub = userId` and `exp` ~1 hour from now. Secret loaded from env var, NEVER hardcoded.
5. **`JwtAuthenticationFilter`** in the Spring Security filter chain: reads the `Authorization: Bearer ...` header, validates, sets `SecurityContext`.
6. **`SecurityConfig`**: `permitAll()` for `/auth/register`, `/auth/login`; `.anyRequest().authenticated()` for everything else. Stateless session policy (no cookies).
7. **`AuthController`**: the three endpoints above, returning `AuthResponse(token, expiresAt)`.

### Threats to Avoid

| Mistake | What goes wrong |
|---|---|
| Hashing the password client-side | The hash becomes the password. Stolen-in-transit = account compromise. |
| Storing plain JWT in regular SharedPreferences | Rooted device or backup extraction leaks the session token. Use the `Encrypted*` variant — already implemented here. |
| Different error messages for "wrong email" vs "wrong password" | Lets attackers enumerate which emails are registered. |
| Hardcoded JWT secret | A leaked git history forges valid tokens forever. Use env vars. |
| Long JWT expiry (days/weeks) without refresh tokens | A stolen token stays valid for a long time. Use ~1 hour and add refresh tokens later. |
| Returning the `password_hash` in any API response | Even hashes shouldn't leak — they help offline cracking. |
| Logging the request body on the auth endpoints | Logs end up shipped to ops tools containing plaintext passwords. |
| Production cleartext HTTP | `NetworkModule.kt` has `http://10.0.2.2:8080/` for emulator only — release builds MUST point to an HTTPS URL. |

### Frontend File Map (auth-specific)

| File | Purpose |
|---|---|
| `data/local/AuthTokenStore.kt` | EncryptedSharedPreferences wrapper |
| `data/remote/AuthInterceptor.kt` | OkHttp interceptor that attaches `Bearer` header |
| `data/remote/dto/auth/AuthDtos.kt` | `RegisterRequest`, `LoginRequest`, `ChangePasswordRequest`, `AuthResponse` |
| `data/remote/api/ApiClient.kt` | Retrofit interface — auth endpoints live here |
| `data/repository/AuthRepository.kt` + `Impl` | Calls API, persists token, exposes `isLoggedIn()`/`logout()` |
| `di/AuthRepositoryModule.kt` | Hilt binding |
| `di/NetworkModule.kt` | Wires OkHttp with the interceptor |
| `ui/state/AuthFormState.kt` | `LoginFormState`, `RegisterFormState` |
| `ui/viewmodel/LoginScreenViewModel.kt`, `RegisterScreenViewModel.kt` | Form state + submit logic |
| `ui/viewmodel/AppAuthViewModel.kt` | Exposes `isLoggedInOnStart`, `logout()` to the nav host |
| `ui/screen/LoginScreen.kt`, `RegisterScreen.kt` | Compose UI |
| `ui/navigation/NavRoutes.kt` | Adds `Login`, `Register` routes |
| `ui/navigation/AppNavHost.kt` | Conditional start destination, hides bottom bar on Login/Register/AttractionDetail |

### Open Items / TODO

- [ ] Refresh tokens (currently the user must log in again after JWT expiry)
- [ ] Password reset via email
- [ ] Rate limiting on `/auth/login` (backend, e.g. bucket4j)
- [ ] Replace the hard-coded ngrok URL in `NetworkModule` with a config-driven production URL when a real backend is deployed
- [ ] Add backend-side input validation annotations (`@Email`, `@Size`)
- [ ] Consider biometric unlock for the stored token (BiometricPrompt + Keystore-wrapped key)
