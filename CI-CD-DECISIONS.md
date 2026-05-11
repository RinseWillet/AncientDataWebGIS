# AncientData CI/CD Decisions Log

This file is the source of truth for CI/CD rollout decisions for the AncientData project.

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

## Planned next phases

- Phase C: Unified CI gate (require frontend + backend checks on mainline PRs).
- Phase D: Docker image build/publish hardening (main/tags only, no publish from fork PRs).
- Phase E: Optional `docker-compose` smoke/integration check.

## Update checklist for future phase PRs

- [ ] Add a new phase section with date.
- [ ] List exact workflow/file changes.
- [ ] Explain why the change was made.
- [ ] Record risk and rollback plan.
- [ ] Record known caveats (test baseline, env constraints, etc.).

