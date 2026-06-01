# AGENTS.md – AncientDataWebGIS Backend (ADW)

## Behavioral Guidelines

### 1. Think Before Coding

Don't assume. Don't hide confusion. Surface tradeoffs.

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.
- Don't infer behavior from names. Always read the implementation of a function before making claims about what it does.
- Never take action without explicit user confirmation. Always present your plan or intention and wait for approval before executing.

### 2. Simplicity First

Minimum code that solves the problem. Nothing speculative.

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

> Bias toward caution over speed. For trivial tasks, use judgment.

- **Surgical changes.** Only touch what the request requires. Match existing style. Don't improve adjacent code, comments, or formatting. Mention unrelated dead code — don't delete it.
- **Clean up your own mess.** Remove imports/variables/functions YOUR changes made unused. Don't remove pre-existing dead code.
- **Goal-driven.** Turn tasks into verifiable goals. For multi-step work, state a brief plan with verification steps. Keep `./gradlew test` green.

## Architecture Overview

Spring Boot 3 REST API serving archaeological spatial data (sites, roads, modern/ancient references) with PostGIS geometry, JWT auth, and Flyway-managed schema.

- **Stack:** Java 21, Spring Boot 3.5.x, Spring Data JPA + Hibernate Spatial, PostgreSQL/PostGIS, Flyway, JTS, Lombok, JJWT.
- **Base path:** `/api` (e.g. `/api/sites`, `/api/roads`).
- **Layering (`com.webgis.ancientdata`):**
  - `web/controller` — REST endpoints; `web/mapper` — entity↔DTO; `web/exception` — `GlobalExceptionHandler`.
  - `application/service` — business logic.
  - `domain/model` — JPA entities; `domain/repository` — Spring Data repos; `domain/dto` — request/response DTOs.
  - `security` — `JwtFilter`, `JwtUtil`, `SecurityConfig`, `CustomUserDetailService`.
  - `config`, `constants`, `utils` — cross-cutting config, `ErrorMessages`, GeoJSON/JSON helpers.
- **Build:** `./gradlew`

## Hard Rules

- Controllers stay thin → delegate to `application/service` → services use `domain/repository`. No repo calls from controllers.
- DTO boundary: controllers expose DTOs via `web/mapper`. Never return/accept JPA entities across HTTP.
- Never hard-code DB credentials, JWT secrets, or CORS origins. Secrets live in `.env` (never committed).
- Auth enforcement stays in `security` package. Don't bypass `JwtFilter`/`JwtUtil` or weaken `SecurityConfig` without flagging it.
- Geometry: use JTS + Hibernate Spatial with existing `GeoJsonConverter`/`JsonUtils` — no ad-hoc conversion.
- Schema changes: Flyway migrations only (`src/main/resources/db/migration`). Never edit applied migrations — add new ones.
- Errors: route through `GlobalExceptionHandler`; reuse `constants/ErrorMessages`.
- Tests: add/update tests for behavior changes; keep `./gradlew test` green.
- All text (docs, comments, agent output) in English.

## Quick Commands

| Task | Command |
|------|---------|
| Dev server | `./gradlew bootRun --args='--spring.profiles.active=dev'` |
| All tests | `./gradlew test` |
| Targeted tests | `./gradlew test --tests "*<Pattern>*"` |
| Build | `./gradlew build` |
| Health check | `curl http://localhost:8080/actuator/health` |
| Docker stack | `docker compose up --build` |

Test reports: `build/reports/tests/test/index.html`.

## Definition of Done

- Behavior verified by tests or reproducible manual check.
- Diff only touches files needed for the goal.
- New/changed behavior has tests where practical.
- No `.env` secrets committed.

## Planning & ADRs

- **Always plan** epics (E-level) before coding. **Recommended** for M/L stories spanning both repos or new API contracts. **Skip** for trivial fixes.
- Create a new ADR (`docs/architecture/adr/`, use `ADR-TEMPLATE.md`) when introducing new tech/libraries, changing storage/CI-CD/security strategy, or establishing new patterns.

## Documentation Pointers

| Document | Location |
|----------|----------|
| ADR index | `docs/architecture/adr/README.md` |
| Feature backlog | `docs/features/FEATURE-SPEC-BACKLOG.md` |
| Feature specs | `docs/features/README.md` |
| CI/CD decisions | `docs/ci-cd/CI-CD-DECISIONS.md` |
| DB migration strategy | `docs/architecture/DB-MIGRATION-STRATEGY.md` |
| Data governance | `docs/architecture/DATA-GOVERNANCE-POLICY.md` |

## API Contract-Change Mode

When an endpoint/schema changes, include:

1. Backend contract delta (route, payload, validation, error format).
2. Backward compatibility decision (compatible vs breaking) and rationale.
3. Frontend impact note (what ADW-FE callers must change).
4. Verification list (backend tests + at least one e2e request example).

## Cross-Repo Work

| Repo | Code | Local path |
|------|------|------------|
| AncientDataWebGIS | ADW | `../AncientDataWebGIS` (this repo) |
| AncientDataWebGIS_FE | ADW-FE | `../AncientDataWebGIS_FE` |
| ancientdataworkspace | — | `../ancientdataworkspace` |

- Be explicit when a change affects both API contracts.
- Validate each repo independently before declaring done.
- Backend: `cd ../AncientDataWebGIS && ./gradlew test`
- Frontend: `cd ../AncientDataWebGIS_FE && npm run test:run && npm run lint && npm run build`

## Source of Truth

This file is the single source of agent guidance for this repo and supersedes any earlier `CLAUDE.md` / `CODEX.md` / workspace rule files. `.github/copilot-instructions.md` simply points here.
