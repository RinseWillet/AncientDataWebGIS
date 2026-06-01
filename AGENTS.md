# AGENTS.md – AncientDataWebGIS Backend (ADW)

## Behavioral Guidelines

Behavioral guidelines to reduce common LLM coding mistakes.

> Tradeoff: These guidelines bias toward caution over speed. For trivial tasks, use judgment.

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

### 3. Surgical Changes

Touch only what you must. Clean up only your own mess.

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

Define success criteria. Loop until verified.

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## Architecture Overview

Spring Boot 3 REST API for the AncientData WebGIS project, serving archaeological spatial data (sites, roads, modern/ancient references) with PostGIS geometry, JWT authentication, and Flyway-managed schema.

- **Stack:** Java 21, Spring Boot 3.5.x, Spring Data JPA + Hibernate Spatial, PostgreSQL/PostGIS, Flyway, JTS, Lombok, JJWT.
- **Base path:** REST controllers are served under `/api` (e.g. `/api/sites`, `/api/roads`).
- **Layering (package `com.webgis.ancientdata`):**
  - `web/controller` — REST endpoints; `web/mapper` — entity↔DTO mapping; `web/exception` — `GlobalExceptionHandler`.
  - `application/service` — business logic (`SiteService`, `RoadService`, `DashboardService`, `SuggestionService`, `AuthService`, `ModernReferenceService`).
  - `domain/model` — JPA entities; `domain/repository` — Spring Data repositories; `domain/dto` — request/response DTOs.
  - `security` — `JwtFilter`, `JwtUtil`, `SecurityConfig`, `CustomUserDetailService`.
  - `config`, `constants`, `utils` — cross-cutting config, `ErrorMessages`, GeoJSON/JSON helpers.
- **Build:** Gradle wrapper (`./gradlew`).

See `README.md` and `docs/` (`architecture/`, `ci-cd/`, `security/`) for more detail.

## Hard Rules for Agents

- Layering: controllers stay thin and delegate to `application/service`; services use `domain/repository`. Do not call repositories directly from controllers or embed business logic in controllers.
- DTO boundary: controllers expose DTOs from `domain/dto` via `web/mapper`. Never return or accept JPA entities directly across the HTTP boundary.
- Configuration & secrets: read config via Spring properties; never hard-code DB credentials, JWT secrets, or CORS origins. Local secrets live in `.env` (loaded via `spring.config.import`) and must never be committed.
- Security: keep auth enforcement in the `security` package and `SecurityConfig`. Do not bypass `JwtFilter`/`JwtUtil` or weaken security config to make a feature work without calling it out.
- Spatial data: geometry uses JTS + Hibernate Spatial; use the existing `GeoJsonConverter`/`JsonUtils` helpers for (de)serialisation rather than ad-hoc conversion.
- Database changes go through Flyway migrations (versioned scripts under `src/main/resources/db/migration`). Do not rely on `ddl-auto` for schema changes; never edit an already-applied migration — add a new one.
- Error handling: surface errors through `GlobalExceptionHandler` and reuse messages in `constants/ErrorMessages` rather than scattering custom error responses.
- Tests: add or update tests under `src/test` for behavior changes and bug fixes; keep `./gradlew test` green.
- All documentation, comments, and agent-generated text must be written in English.
- Skill file naming: new skill files must be named `STEAM_SKILL_{topic}.md` (e.g. `STEAM_SKILL_run-migrations.md`). This prefix enables quick discovery via the `#` file picker across the whole workspace.

## Quick Commands

| Task | Command |
|------|---------|
| Dev server (dev profile) | `./gradlew bootRun --args='--spring.profiles.active=dev'` |
| All tests | `./gradlew test` |
| Targeted tests | `./gradlew test --tests "*<ClassNameOrPattern>*"` |
| Build | `./gradlew build` |
| Health check | `curl http://localhost:8080/actuator/health` |
| Docker stack | `docker compose up --build` |

Test reports: `build/reports/tests/test/index.html`.

## Standard Request Template

```markdown
Goal:
- <one concrete backend outcome>

Scope (allowed files):
- <file path 1>
- <file path 2>

Constraints:
- Keep changes surgical; do not refactor unrelated code.
- Match existing project style.

Success criteria:
- <observable behavior/test outcome>

Verify:
- ./gradlew test --tests <targeted.test.ClassName>
- ./gradlew test

Non-goals:
- <explicitly list what should not be changed>
```

## Definition of Done

- Behavior is verifiable from tests or a reproducible manual check.
- Diff only touches files needed for the goal.
- New or changed behavior has corresponding tests where practical.
- No secrets or environment values from `.env` are committed.

 
## Plan-First Workflow

New features must be planned before implementation. This reduces rework, scope creep, and misaligned implementations.

### When to plan

- **Always plan:** Every new epic (E-level) must have a Copilot plan created and reviewed before any code is written.
- **Recommended:** Individual stories (especially M/L-sized) benefit from a plan, particularly when they span both repos or introduce new API contracts.
- **Skip for:** Trivial bug fixes, typo corrections, or single-file config changes where the change is self-evident.

### How to plan

1. Open Copilot chat and select **Plan** mode (or use `@workspace /plan`).
2. Provide the story/epic context from `docs/features/FEATURE-SPEC-BACKLOG.md`.
3. Review the generated plan — it should identify files to change, dependencies, and verification steps.
4. Adjust the plan if needed, then confirm before switching to implementation.

### Plan output expectations

A good plan should include:
- **Scope:** Which files/modules will be touched (backend, frontend, or both).
- **Steps:** Ordered list of implementation steps.
- **Dependencies:** What must exist before this work can start.
- **Verification:** How to confirm the feature works (test commands, manual checks).
- **ADR trigger:** Whether this change warrants a new ADR (see below).

### When to create an ADR

Create a new ADR in `docs/architecture/adr/` when a decision:
- Introduces a new technology, library, or infrastructure component.
- Changes the storage, serving, or backup strategy.
- Alters the CI/CD pipeline structure.
- Modifies the security model or auth flow.
- Establishes a new architectural pattern that future work should follow.

Use `docs/architecture/adr/ADR-TEMPLATE.md` as a starting point.

## Documentation Pointers

| Document | Location | Purpose |
|----------|----------|---------|
| ADR index | `docs/architecture/adr/README.md` | Central record of all architectural decisions |
| Feature backlog | `docs/features/FEATURE-SPEC-BACKLOG.md` | Prioritized epic/story backlog |
| Feature specs | `docs/features/README.md` | Index of per-epic specifications |
| CI/CD decisions | `docs/ci-cd/CI-CD-DECISIONS.md` | Detailed CI/CD phase log (indexed as ADR-004) |
| DB migration strategy | `docs/architecture/DB-MIGRATION-STRATEGY.md` | Schema change process (indexed as ADR-002) |
| Data governance | `docs/architecture/DATA-GOVERNANCE-POLICY.md` | Write policy and suggestion workflow (indexed as ADR-003) |

## Skills

Reusable agent skills are stored as `STEAM_SKILL_{topic}.md` files. Skills encode step-by-step procedures for recurring tasks so agents can execute them reliably without re-deriving the process each time.

- **Naming convention:** `STEAM_SKILL_{topic}.md` (e.g., `STEAM_SKILL_run-migrations.md`).
- **Discovery:** Use the `#` file picker to search for `STEAM_SKILL_` across the workspace.
- **Status:** No skills have been created yet. Candidate topics for future skill files will be identified as recurring patterns emerge during development.

## API Contract-Change Mode

When an endpoint/schema changes, include all four items in the response:

1. Backend contract delta (route, payload, validation, error format).
2. Backward compatibility decision (compatible vs breaking) and rationale.
3. Frontend impact note (what callers in ADW-FE must change).
4. Verification list (backend tests plus at least one end-to-end request example).

## Related Repositories

This repo is part of the AncientData WebGIS project. See `../ancientdataworkspace/CLAUDE.md` for the workspace map.

| Repo | Code | Purpose | Local path |
|------|------|---------|------------|
| AncientDataWebGIS | ADW | Spring Boot backend (this repo) | `../AncientDataWebGIS` |
| AncientDataWebGIS_FE | ADW-FE | React + Vite frontend | `../AncientDataWebGIS_FE` |
| ancientdataworkspace | — | Cross-repo workspace root, shared agent rules and workflows | `../ancientdataworkspace` |

## Cross-Repo Work

Some changes span both backend (ADW) and frontend (ADW-FE).

- Be explicit when a change affects both API contracts.
- Validate each impacted repo independently before declaring done.
- Keep changes scoped; avoid opportunistic cross-repo refactors.

Sibling verification:

- Backend: `cd ../AncientDataWebGIS && ./gradlew test`
- Frontend: `cd ../AncientDataWebGIS_FE && npm run test:run && npm run lint && npm run build`

## Source of Truth

This file is the single source of agent guidance for this repo and supersedes any earlier `CLAUDE.md` / `CODEX.md` / workspace rule files. `.github/copilot-instructions.md` simply points here. Review and adjust these rules after major model updates or at least monthly.

