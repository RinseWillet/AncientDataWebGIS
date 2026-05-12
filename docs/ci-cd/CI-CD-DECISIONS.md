# AncientData CI/CD Decisions Log

This file is the source of truth for CI/CD rollout decisions for the AncientData project.

---

## Project Summary (last updated: 2026-05-12)

### What this project did

Implemented an incremental, low-risk CI/CD pipeline for the full AncientData stack across two separate GitHub repositories.

**Before:**
- No automated CI for the frontend.
- Backend had a partial, unmaintained CI config and a basic Docker publish workflow.
- Backend tests were all failing due to an H2/Spatial schema init conflict.
- Log files were accidentally tracked in git and causing constant dirty-state noise.
- Docker publish workflow could be triggered from PRs and had no proper job sequencing.

**After:**
- Frontend repo (`AncientDataWebGIS_FE`) has a CI pipeline: lint → test → build on every push/PR to `main`.
- Backend repo (`AncientDataWebGIS`) has: a CI pipeline (test + build), a hardened Docker build/publish workflow (test → build → Docker build → conditional publish), an optional compose smoke test, and a log-file hygiene guard.
- Backend tests are fully green.
- Docker image is published to Docker Hub only on trusted push to `main` or version tags — never from PRs or forks.

### Architecture: how frontend changes reach the Docker image

The frontend (`AncientDataWebGIS_FE`) is a **separate repo** from the backend. The Spring Boot app serves the frontend as **static assets** bundled inside `src/main/resources/static/` — these are the output of `npm run build` in the frontend project.

**Current flow (manual):**
1. Developer builds the frontend locally (`npm run build`).
2. Developer copies the `dist/` output into `AncientDataWebGIS/src/main/resources/static/`.
3. Developer commits and pushes the backend repo.
4. Backend CI runs: test → build → Docker build → Docker publish.

**The gap:** A frontend-only push to `AncientDataWebGIS_FE/main` does **not** automatically trigger a new Docker image. Phase H (below) addresses this with an automated frontend build step inside the backend Docker pipeline.

### Files created/changed

| File | Repo | Purpose |
|---|---|---|
| `.github/workflows/frontend-ci.yml` | FE | Lint, test, build on push/PR |
| `.github/workflows/backend-ci.yml` | BE | Test, build, log guard on push/PR |
| `.github/workflows/docker-image.yml` | BE | Hardened Docker build/publish pipeline |
| `.github/workflows/compose-smoke-ci.yml` | BE | Optional integration smoke test |
| `.github/docker/docker-compose.smoke.yml` | BE | CI-only compose stack for smoke test |
| `src/test/resources/application-test.properties` | BE | Fixed H2 geometry domain init conflict |
| `CI-CD-DECISIONS.md` | BE | This file — decisions log |

### Full CI/CD flow after Phase H

```
AncientDataWebGIS_FE/main push
  └─► Frontend CI (lint → test → build)   [AncientDataWebGIS_FE repo]

AncientDataWebGIS/main push (or manual trigger)
  └─► backend-verify job:
        1. Checkout backend
        2. Checkout AncientDataWebGIS_FE @ main
        3. npm ci + npm run build
        4. Copy dist/ → src/main/resources/static/
        5. ./gradlew test
        6. ./gradlew build -x test
        7. Upload JAR artifact
  └─► docker-build job:
        1. Download JAR
        2. Docker build (no push) — validates image
  └─► docker-publish job (main/tags only):
        1. Download JAR
        2. Docker build + push to Docker Hub
```

### Known remaining items

- **Formatting gate (deferred):** `npm run format:check` currently fails on the existing unformatted baseline; gate deferred until formatting is applied project-wide.

---

## Scope

- Backend repo: `AncientDataWebGIS`
- Frontend repo: `AncientDataWebGIS_FE`
- Goal: incremental, low-risk, PR-friendly CI/CD rollout

## How to use this file

- Update this file in every CI/CD phase PR.
- Keep each entry short: what changed, why, risk, rollback.
- Do not store secrets or sensitive values here.

## Decision format

Use this structure for new entries:

```md
## Phase X - <title> (<date>)

### What changed
- ...

### Why
- ...

### Risk level
- Low | Medium | High

### Rollback
- ...

### Notes
- ...
```

---

## Phase A - Frontend CI only (2026-05-11)

### What changed
- Added frontend workflow: `AncientDataWebGIS_FE/.github/workflows/frontend-ci.yml`.
- Added checks in this order:
  - `npm ci`
  - `npm run lint`
  - `npm run test:run`
  - `npm run build`
- Added npm dependency caching via `actions/setup-node` (`cache: npm`).
- Kept permissions minimal (`contents: read`).

### Why
- Establishes fast feedback for frontend changes with no runtime behavior changes.
- Keeps first phase small and easy to review/rollback.

### Risk level
- Low

### Rollback
- Remove or disable `AncientDataWebGIS_FE/.github/workflows/frontend-ci.yml`.

### Notes
- `npm run format:check` currently fails on existing formatting baseline in many files.
- Formatting gate intentionally deferred to avoid large unrelated churn.

---

## Phase B - Backend CI only (2026-05-11)

### What changed
- Added backend workflow: `AncientDataWebGIS/.github/workflows/backend-ci.yml`.
- Added checks:
  - `./gradlew --no-daemon test`
  - `./gradlew --no-daemon build -x test`
- Added Gradle caching (`actions/setup-java` with `cache: gradle` and `gradle/actions/setup-gradle`).
- Added test report artifact upload (`build/reports/tests/test`) with `if: always()`.
- Kept permissions minimal (`contents: read`).

### Why
- Makes backend quality gate explicit and reproducible in PRs.
- Captures test report artifacts to speed up debugging on CI failures.

### Risk level
- Low to Medium

### Rollback
- Remove or disable `AncientDataWebGIS/.github/workflows/backend-ci.yml`.

### Notes
- Local baseline currently shows failing backend tests (H2 SQL grammar/context init issues).
- This is pre-existing behavior observed during verification, not introduced by the workflow.

---


## Phase C - Unified CI gate strategy (2026-05-11)

### What changed
- Standardized CI intent across repos:
  - Frontend workflow remains `Frontend CI` in `AncientDataWebGIS_FE/.github/workflows/frontend-ci.yml`.
  - Backend workflow remains `Backend CI` in `AncientDataWebGIS/.github/workflows/backend-ci.yml`.
- Defined gate approach for split-repo setup:
  - Require `Frontend CI` in frontend repo PRs.
  - Require `Backend CI` in backend repo PRs.
  - Docker publish workflow must depend on backend verification and run only on trusted push events.

### Why
- Backend and frontend are currently managed in separate repositories, so one single required check cannot gate both in one PR.
- Per-repo required checks keep protection clear, auditable, and low-risk.

### Risk level
- Low

### Rollback
- Remove required-check enforcement in repository branch protection settings.

### Notes
- Branch protection rules are a GitHub repository setting, not stored in repo files.

---

## Phase D - Docker build/publish hardening (2026-05-11)

### What changed
- Refactored backend Docker workflow in `AncientDataWebGIS/.github/workflows/docker-image.yml`.
- New job split:
  - `backend-verify`: runs backend test/build before any container work.
  - `docker-build`: builds image for validation on PRs/pushes without publishing.
  - `docker-publish`: publishes only on push to `main` or version tags.
- Replaced manual docker CLI login/push with official Docker actions:
  - `docker/login-action`
  - `docker/metadata-action`
  - `docker/build-push-action`
- Added Docker Buildx cache (`cache-from/cache-to: type=gha`).
- Added workflow concurrency cancellation to avoid duplicate in-flight runs.
- Kept permissions minimal (`contents: read`).

### Why
- Prevents image publishing from pull requests and fork PR contexts.
- Keeps PR validation useful by still building Docker images (without push).
- Reduces CI time/cost with Buildx caching and cancels superseded runs.

### Risk level
- Medium

### Rollback
- Revert `AncientDataWebGIS/.github/workflows/docker-image.yml` to previous version or disable `docker-publish` job.

### Notes
- Publishing still relies on existing repository secrets (`DOCKERHUB_USERNAME`, `DOCKERHUB_PASSWORD`, `REPO_NAME`).
- No secrets or environment values were changed.

---

## Phase E - Optional docker-compose smoke check (2026-05-11)

### What changed
- Added smoke compose definition: `AncientDataWebGIS/.github/docker/docker-compose.smoke.yml`.
- Added optional smoke workflow: `AncientDataWebGIS/.github/workflows/compose-smoke-ci.yml`.
- Workflow behavior:
  - Triggers on `workflow_dispatch` and selected `pull_request` path changes.
  - Builds backend JAR (`bootJar -x test`) to satisfy Dockerfile artifact expectations.
  - Starts compose stack, waits for `GET /actuator/health` endpoint reachability (HTTP `200` or `401`), prints logs on failure, always tears down.
- Uses no new secrets and keeps permissions minimal (`contents: read`).

### Why
- Provides integration-level confidence (container build + app startup + DB dependency) without making it a hard gate for every PR.
- Limits CI runtime/cost by running only on relevant changes or manual trigger.

### Risk level
- Medium

### Rollback
- Remove or disable `AncientDataWebGIS/.github/workflows/compose-smoke-ci.yml`.
- Remove `AncientDataWebGIS/.github/docker/docker-compose.smoke.yml` if no longer needed.

### Notes
- This smoke stack is CI-focused and intentionally separate from production/local `docker-compose.yml`.
- Backend test baseline issues remain separate from this smoke check.

---

## Phase F - Backend test stabilization: H2 spatial init fix (2026-05-12)

### What changed
- Updated test datasource URL in `AncientDataWebGIS/src/test/resources/application-test.properties`.
- Removed H2 URL `INIT=CREATE DOMAIN GEOMETRY AS OBJECT` from test DB configuration.

### Why
- Hibernate Spatial already contributes H2GIS geometry handling during test startup.
- The extra `INIT` statement attempted to recreate `GEOMETRY`, causing `Domain "GEOMETRY" already exists` and preventing Spring test context initialization.

### Risk level
- Low

### Rollback
- Revert `AncientDataWebGIS/src/test/resources/application-test.properties` to reintroduce the previous URL.

### Notes
- Verification after change:
  - `./gradlew --no-daemon test --tests "com.webgis.ancientdata.AncientdataApplicationTests"` passed.
  - `./gradlew --no-daemon test --tests "com.webgis.ancientdata.roadtests.RoadControllerTests"` passed.
  - `./gradlew --no-daemon test` passed.
  - `./gradlew --no-daemon build` passed.

---

## Phase D.1 - Docker workflow fix: JAR artifact passing between jobs (2026-05-12)

### What changed
- Added `Upload JAR artifact` step at the end of `backend-verify` job in `docker-image.yml`.
  - Uploads `build/libs/ancientdata-0.0.1-SNAPSHOT.jar` using `actions/upload-artifact@v4` with `retention-days: 1`.
- Added `Download JAR artifact` step at the start of `docker-build` and `docker-publish` jobs.
  - Downloads the artifact to `build/libs/` before `docker/build-push-action` runs.

### Why
- Each GitHub Actions job runs on a fresh runner; the JAR built in `backend-verify` was not available in `docker-build`/`docker-publish`.
- The `Dockerfile` `COPY ./build/libs/...jar` step was failing with "file not found".

### Risk level
- Low

### Rollback
- Remove the `Upload JAR artifact` and `Download JAR artifact` steps; combine build and Docker steps into a single job.

### Notes
- Artifact retention set to 1 day to avoid accumulating stale build artifacts.
- No Gradle duplication: Docker jobs no longer need Java/Gradle setup.

---

## Phase G - Log file hygiene: untrack + CI guard (2026-05-12)

### What changed
- Untracked `logs/ancientdata.log` from git index via `git rm --cached logs/ancientdata.log`.
  - `logs/` was already in `.gitignore`; the file had slipped into git before the ignore rule applied.
- Added a `Check no log files are tracked` step to `AncientDataWebGIS/.github/workflows/backend-ci.yml`.
  - Step runs `git ls-files --error-unmatch 'logs/*.log' 'logs/*.gz'` and fails the job if any match.

### Why
- Log files were appearing as dirty/staged on every `./gradlew` run, creating noise in diffs and risk of accidental commits.
- The CI guard provides a permanent safety net: if any log file is accidentally re-tracked, the job will catch it immediately.

### Risk level
- Low

### Rollback
- Remove the `Check no log files are tracked` step from `backend-ci.yml`.
- Re-track the file with `git add logs/ancientdata.log` if needed (not recommended).

### Notes
- Frontend `.gitignore` already correctly ignores `logs/` and `*.log` — no changes needed there.

---

---

## Phase H - Automated frontend build inside backend Docker pipeline (2026-05-12)

### What changed
- Added three steps to the `backend-verify` job in `AncientDataWebGIS/.github/workflows/docker-image.yml`, before the Gradle build:
  1. `Checkout frontend` — checks out `RinseWillet/AncientDataWebGIS_FE @ main` into `frontend-src/`.
  2. `Set up Node.js` — Node 20 with npm cache keyed on `frontend-src/package-lock.json`.
  3. `Build frontend` — runs `npm ci && npm run build` in `frontend-src/`.
  4. `Copy frontend dist into backend static resources` — clears `src/main/resources/static/` and copies `frontend-src/dist/*` into it.

### Why
- The frontend `dist/` is served as static assets baked into the Spring Boot JAR (`src/main/resources/static/`).
- Previously this copy was done manually; the Docker image would not reflect frontend changes unless a developer manually ran the copy and committed it.
- Now any push to `AncientDataWebGIS/main` automatically builds the latest frontend from `AncientDataWebGIS_FE/main` and includes it in the published image.

### Safety chain
- `AncientDataWebGIS_FE` frontend CI gate ensures `main` is always in a buildable/tested state.
- `backend-verify` then pulls that validated frontend, runs all backend tests, and builds the JAR.
- `docker-publish` only runs on trusted push events after both prior jobs succeed.

### Risk level
- Low to Medium

### Rollback
- Remove the four frontend steps from `backend-verify` in `docker-image.yml`.
- The static assets committed in `src/main/resources/static/` will be used as the fallback.

### Notes
- Local verification: `npm ci && npm run build` ✅, copy step ✅, `./gradlew test` ✅, `./gradlew build -x test` ✅.
- The `frontend-src/` directory is never committed — it exists only during the CI runner's lifetime.

---

## Update checklist for future phase PRs

- [ ] Add a new phase section with date.
- [ ] List exact workflow/file changes.
- [ ] Explain why the change was made.
- [ ] Record risk and rollback plan.
- [ ] Record known caveats (test baseline, env constraints, etc.).

