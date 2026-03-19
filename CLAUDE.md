# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./gradlew build
./gradlew clean build

# Run
./gradlew bootRun

# Test
./gradlew test
./gradlew test --tests "com.uf.assistance.service.ChatKeywordServiceTest"
./gradlew test --tests "com.uf.assistance.service.ChatKeywordServiceTest.specificMethodName"
```

- **Java 17**, Spring Boot 3.4.2, Gradle wrapper
- Default active profile: `dev` (server port: 8081)
- Swagger UI available at `http://localhost:8081/swagger-ui.html`

## Architecture Overview

Standard Spring layered architecture: `Controller → Service → Repository → Entity`

**Main Application:** `@SpringBootApplication` + `@EnableJpaAuditing` + `@EnableAsync`

### Core Packages

| Package | Role |
|---------|------|
| `domain/` | JPA entities and Spring Data repositories |
| `service/` | Business logic (transactional) |
| `web/` | REST controllers |
| `config/` | Spring Security, WebSocket, Swagger, JWT configuration |
| `batchjob/` | Quartz scheduled jobs (dynamic scheduling via `DynamicSchedulerService`) |
| `handler/` | JWT entry point, access denied, OAuth2 success handler |
| `event/` | Spring application events |
| `dto/` | Request/response DTOs |

### Authentication & Security

- **Stateless JWT**: `JwtAuthenticationFilter` (login) → `JwtAuthorizationFilter` (validate) → `JwtRequestFilter`
- **OAuth2**: Google OAuth2 with custom `OAuth2SuccessHandler`
- **Refresh tokens**: Stored in `RefreshToken` entity; upserted on re-auth
- Public endpoints: `/api/login/**`, `/api/join/**`, `/api/oauth2/**`, `/api/image/**`, Swagger paths
- Authenticated endpoints: `/api/auth/**`
- WebSocket: `/chat/**`

### Key Domain Entities

`User` ← many → `Chat`, `AISubscription`, `UserInterest`, `RefreshToken`
`Chat` → `AISubscription`, `ChatKeyword`
`ScheduledJob` ↔ Quartz scheduler (JDBC job store)
`PromptTemplate`, `CustomAI` — configurable AI behavior per user/subscription

### Async & Scheduling

- `@EnableAsync` — service methods may be `@Async`; check for `CompletableFuture` return types
- Quartz uses JDBC job store backed by the same PostgreSQL DB
- `DynamicSchedulerService` creates/modifies Quartz jobs at runtime from `ScheduledJob` entities

### External Integrations

- **OpenAI** via Spring AI framework (`spring.ai.openai.*` config)
- **Flask API** (`http://3.39.234.47:5001`) — called from services for vector/interest operations
- **PostgreSQL** (`3.39.234.47:5432/youf`) in dev/test profiles
- **H2 in-memory** for local profile (`application-local.yml`, DDL: `create`)

## Configuration Profiles

| Profile | DB | Notes |
|---------|-----|-------|
| `dev` (default) | PostgreSQL remote | Debug logging, Quartz JDBC store |
| `local` | H2 in-memory | Schema recreated on start, use for isolated dev |
| `test` | PostgreSQL remote | Hibernate DDL: `update` |
