# FindOut

**A personalized place-discovery app for Android.** FindOut recommends places in a city based on what *you* care about (atmosphere, cleanliness, price, …) rather than one global star rating that is the same for everyone.

Built as my Bachelor's thesis project at the **University of Bucharest, Faculty of Mathematics and Computer Science** (June 2026). It is a full-stack system with a native Android client, a Spring Boot REST API and a PostgreSQL + PostGIS database.

> 📄 **Thesis:** [docs/thesis.pdf](docs/thesis.pdf). Written in Romanian, with an English abstract ([see below](#thesis)).

---

## The problem

Review platforms usually show every user the same list of places, ranked by an overall score. A quiet café with a 4.2 can be a better pick for someone who values calm than a loud 4.8 bar, but a single number can't show that.

## What FindOut does

- **Personalized recommendations.** Users rate how much several concepts matter to them (0–5). Each nearby place gets a **match score** computed from its per-attribute ratings, weighted by the user's preferences.
- **"Try something new" discovery.** This mode does the opposite: it surfaces well-rated places (≥ 3.5) that stand out on the dimensions the user usually *ignores*. The goal is to break the filter bubble.
- **Typo-tolerant live search.** Search-as-you-type with autocomplete. It handles spelling mistakes and Romanian diacritics (for example, `muzue` still finds `Muzeu`).
- **Multi-attribute reviews.** Users rate individual attributes, not just give one star rating. These ratings update running per-attribute averages and the place's weighted overall score.
- **Wishlist, profile and onboarding.** Users can save places for later, review their own reviews, and set preferences through an onboarding wizard.
- **Location details and photos.** Each place has opening hours (with live open/closed status), attributes, reviews and photos fetched through the Google Places API.

## Tech stack

| Layer | Technologies |
|---|---|
| **Android client** | Kotlin, Jetpack Compose, Material 3, MVVM, Hilt (DI), Navigation Compose, Kotlin Coroutines & Flow, Retrofit + OkHttp, Room, Coil, EncryptedSharedPreferences, Google Play Services Location |
| **Backend** | Java 21, Spring Boot 4, Spring Web MVC, Spring Data JPA / Hibernate 7, Hibernate Spatial, Spring Security, JWT (JJWT), Bean Validation, springdoc-openapi (Swagger UI), Lombok |
| **Database** | PostgreSQL on Supabase, **PostGIS** (geospatial queries), **pg_trgm** (trigram fuzzy search with GIN index) |
| **Data & integrations** | OpenStreetMap data (exported via Overpass Turbo), Google Places API (New) for photos and details |
| **Testing & tooling** | JUnit 5, Mockito, Spring MVC tests, H2, JUnit 4 (Android), Maven, Gradle (Kotlin DSL), Docker |

## Architecture

```
┌──────────────────────────┐   HTTPS / JSON + JWT   ┌──────────────────────────┐        ┌───────────────────────┐
│  Android app (Kotlin)    │ ─────────────────────► │  Spring Boot REST API    │ ─────► │ PostgreSQL (Supabase) │
│                          │                        │                          │  JPA   │  + PostGIS            │
│  Compose UI              │                        │  Controllers             │        │  + pg_trgm            │
│    ↕                     │                        │    ↕                     │        └───────────────────────┘
│  ViewModels (StateFlow)  │                        │  Services (scoring,      │
│    ↕                     │                        │    reviews, auth, …)     │ ─────► Google Places API
│  Repositories            │                        │    ↕                     │        (photos, details)
│    ↕            ↕        │                        │  Repositories (JPA +     │
│  Retrofit     Room cache │                        │    native PostGIS SQL)   │
└──────────────────────────┘                        └──────────────────────────┘
```

- **Client:** MVVM with a unidirectional data flow (Screen → ViewModel → Repository → API / local cache). Dependencies are wired with Hilt. The JWT is stored encrypted (AES-256-GCM) and attached to requests by an OkHttp interceptor.
- **Server:** Layered design (controller → service → repository) with DTOs at the boundary. Errors are handled centrally by a `GlobalExceptionHandler` that returns RFC 7807 `ProblemDetail` responses, and unique-constraint races are translated into `409 Conflict`. Authentication is stateless (JWT plus BCrypt).
- **Database:** The schema is managed externally and the app's DB role has DML-only privileges. Hibernate runs in `ddl-auto=validate` mode so the app fails fast if the entities drift from the live schema. Schema changes ship as reviewed SQL scripts (see [`app-backend/db/`](app-backend/db)).

## Technical highlights

**Match score.** For each place within 2 km of the user:

```
MatchScore = Σ (attributeAvg × userImportance × attributeWeight)
           / Σ (userImportance × attributeWeight)
```

Only rated attributes linked to concepts the user cares about are counted. Discovery mode uses the same formula with inverted importance (`5 − importance`), so strengths in neglected dimensions rank highest.

**Geospatial queries.** Nearby candidates are filtered in the database with PostGIS `ST_DWithin` on `geography` points. The native query returns only IDs. A second JPA query with fetch joins then loads the full entity graph in one round-trip, which avoids N+1 queries.

**Fuzzy search.** Names are normalized (lowercased, diacritics removed) and matched with `pg_trgm` trigram similarity, backed by a GIN index. Results are ranked by prefix match first, then similarity, then rating. On the client, a reactive Flow pipeline (`debounce` → `distinctUntilChanged` → `flatMapLatest`) limits request volume and cancels stale searches to avoid race conditions.

## API overview

Interactive docs are available at `/swagger-ui.html` when the backend is running.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/change-password` |
| Locations | `POST /api/locations/nearby`, `POST /api/locations/recommended`, `POST /api/locations/discover`, `GET /api/locations/search`, `GET /api/locations/{id}/details`, `GET /api/locations/{id}/reviews`, `GET /api/locations/{id}/photos` |
| Reviews | `POST /api/reviews`, `GET /api/reviews/me` |
| Preferences | `GET /api/preferences`, `PUT /api/preferences` |
| Wishlist | `GET /api/wishlist`, `GET /api/wishlist/{locationId}`, `POST /api/wishlist`, `DELETE /api/wishlist/{locationId}` |

## Project structure

```
findout/
├── app-backend/          Spring Boot REST API (Maven)
│   ├── src/main/java/…   config · controller · dto · model · repository · service
│   ├── src/test/java/…   service and controller tests
│   ├── db/               hand-applied SQL migrations
│   └── Dockerfile        multi-stage build (Maven → JRE 21)
└── appfrontend/          Android app (Gradle, Kotlin DSL)
    └── app/src/main/java/…  data (remote, local, repository) · di · ui (screen, viewmodel, components, theme)
```

## Running locally

### Backend

Requirements: JDK 21 and a PostgreSQL database with the `postgis` and `pg_trgm` extensions and the FindOut schema.

```bash
export SUPABASE_DB_URL="jdbc:postgresql://<host>:<port>/<db>"
export SUPABASE_DB_USER="<project-ref>"
export SUPABASE_DB_PASSWORD="<password>"
export JWT_SECRET="<random string, at least 32 bytes>"
export GOOGLE_PLACES_API_KEY="<optional; photos are disabled if unset>"

cd app-backend
./mvnw spring-boot:run      # or: docker build -t findout-api . && docker run -p 8080:8080 --env-file .env findout-api
./mvnw test                 # run the test suite
```

### Android app

Requirements: Android Studio, Android 9+ (minSdk 28).

1. Add `BACKEND_BASE_URL=http://<your-host>:8080/` to `appfrontend/local.properties`. On the emulator, the app automatically uses `http://10.0.2.2:8080/`.
2. Open `appfrontend/` in Android Studio and run the `app` configuration, or build from the command line with `./gradlew assembleDebug`.

## Status and future work

The app is fully working end to end against a managed (Supabase) database. The API currently runs locally. Planned next steps:

- Deploy the API to a managed host.
- Add a learned recommender trained on review data alongside the current weighted-score model.
- Add broader instrumented UI tests on Android.

## Thesis

**"Aplicație Android pentru găsirea unor locații personalizate într-un oraș"**
*(An Android application for finding personalized places in a city)*
Bachelor's thesis, University of Bucharest, Faculty of Mathematics and Computer Science, June 2026. Supervisor: Conf. dr. Radu Boriga.

📄 [Read the thesis (PDF, Romanian)](docs/thesis.pdf)

<details>
<summary>English abstract</summary>

This thesis presents the design and implementation of FindOut, a mobile Android application that helps users discover places in a city according to their personal preferences. Current review platforms generally show the same list of places to all users, ordered by an overall rating, without taking into account what matters to each person individually. To address this problem, the application lets the user set the importance they give to several concepts (such as atmosphere, cleanliness or price) and, based on these, computes a match score between a place and the user. Besides personalized recommendations, the application provides a feature for discovering places the user would not normally choose, as well as a typo-tolerant search adapted to the Romanian language. The system consists of a native Android client, written in Kotlin with Jetpack Compose, a REST server built with Spring Boot, and a PostgreSQL database with the PostGIS extension for geospatial data. The result is a working application that provides personalized recommendations and encourages the discovery of new places, less dependent on their popularity.

**Keywords:** Android application, Jetpack Compose, recommendation system, PostGIS, fuzzy search, Spring Boot.
</details>

---

**Author:** Andrei Oprescu · [GitHub](https://github.com/prezcu)
