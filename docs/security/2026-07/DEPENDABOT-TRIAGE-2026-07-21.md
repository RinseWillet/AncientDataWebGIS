# Dependabot Triage — Week of Jul 14–21, 2026

**Source:** Dependabot security update digest for `AncientDataWebGIS` and `AncientDataWebGIS_FE`.

## Backend (`AncientDataWebGIS`)

| Dependency | Alert | Resolved (before) | Action |
|---|---|---|---|
| `org.apache.commons:commons-lang3` | CVE-2025-48924, needs ≥3.18.0 | `3.20.0` | None — already safe, stale alert (dependency graph lag). |
| `com.fasterxml.jackson.core:jackson-databind` | CVE-2026-54515, needs ≥2.21.5 | `2.21.4` (Spring Boot `3.5.16` BOM) | Pinned explicitly to `2.21.5` in `build.gradle` (BOM doesn't manage this version yet). |

Verified via `./gradlew dependencies --configuration runtimeClasspath` and `./gradlew test` (all green).

## Frontend (`AncientDataWebGIS_FE`)

| Dependency | Alert | Resolved (before) | Action |
|---|---|---|---|
| `axios` (direct) | needs ≥1.18.0 | `1.16.0` | Bumped `package.json` to `^1.18.0` → resolves `1.18.1`. |
| `vite` (direct) | needs ≥6.4.3 | `6.4.2` | Bumped `package.json` to `^6.4.3`. |
| `react-router` (transitive via `react-router-dom`) | needs ≥6.30.4 | `6.30.3` | Added to `overrides` → `6.30.4`. |
| `form-data` (transitive via `axios`) | needs ≥4.0.6 | `4.0.5` | Added to `overrides` → `4.0.6`. |
| `@babel/core` (transitive, dev) | needs ≥7.29.6 | `7.29.0` | Added to `overrides` → `7.29.6`. |
| `js-yaml` (transitive, dev) | needs ≥4.2.0 | `4.1.1` | Added to `overrides` → `4.3.0` (also clears a newer `npm audit` advisory, GHSA-52cp-r559-cp3m, affecting 4.0.0–4.2.0). |

**Cleanup:** Deleted the stray `yarn.lock` — the project uses `npm` exclusively (`frontend-ci.yml` runs `npm ci` against `package-lock.json`; no yarn scripts exist). Its presence duplicated Dependabot alerts across two lockfiles.

**Verification:** `npm run test:run` (71/71 passed), `npm run lint` (0 warnings), `npm run build` (succeeded).

**Remaining, out of scope:** `npm audit` still flags `brace-expansion <1.1.16` (pre-existing override at `1.1.13`, transitive via `eslint`'s `minimatch`). Not part of this Dependabot digest; fixing it requires a breaking `eslint@10` major upgrade. Flagged for separate follow-up.

