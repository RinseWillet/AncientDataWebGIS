# ADR-009: Upgrade to Spring Boot 4.1.0 and Java 25 (Eclipse Temurin)

**Status:** Accepted
**Date:** 2026-07-27
**Decision makers:** Backend team
**Epic/Story:** Spring Boot 4 + Java 25 upgrade

---

## Context

The project was on Spring Boot 3.5.16 / Java 21. Per ADR-008 (support-window confirmation), Spring Boot 3.5.x's free OSS support ended 2026-06-30 — already past at implementation time. Java 25 is the current LTS release, GA since 2025-09-22.

Four spikes were run beforehand on isolated, non-merged branches to de-risk this change:
- `spike/spring-boot-4` — Spring Boot 4.1.0 alone (Java 21 unchanged)
- `spike/java-25-toolchain` — Java 25/Temurin alone (Spring Boot 3.5.16 unchanged)
- `spike/boot4-java25-combined` — both together
- Live support-window confirmation (ADR-008), querying `endoflife.date`

All four passed with no blocking issues. This ADR records the real implementation, built on `java-springboot-upgrade`.

---

## Decision

Upgraded:
- **Spring Boot:** `3.5.16` → `4.1.0` (Gradle plugin + BOM)
- **Java toolchain:** `21` → `25`, vendor pinned to `JvmVendorSpec.ADOPTIUM` (Eclipse Temurin) in `build.gradle`, matching the Docker base image
- **Docker base image:** `eclipse-temurin:21-jre` → `eclipse-temurin:25-jre`
- **CI (GitHub Actions):** `actions/setup-java` bumped from `java-version: "21"` to `"25"` across all three workflows (`backend-ci.yml`, `docker-image.yml`, `compose-smoke-ci.yml`)
- **Docs:** `README.md` and `AGENTS.md` updated to reflect Java 25 / Spring Boot 4.1.x

### Required code fix (only one found across all spikes)

Spring Boot 4 modularized the web/test stack. Fixed by:
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-webmvc-test'         // AutoConfigureMockMvc moved here
testImplementation 'org.springframework.boot:spring-boot-starter-security-test' // SecurityMockMvcAutoConfiguration moved here
```
Plus updating the `AutoConfigureMockMvc` import in 8 test classes from `org.springframework.boot.test.autoconfigure.web.servlet` to `org.springframework.boot.webmvc.test.autoconfigure`.

No other application code (`SecurityConfig`, `GlobalExceptionHandler`, `WebConfig`, JPA entities, services, controllers) required changes.

### Verification performed

| Check | Result |
|---|---|
| `./gradlew clean build` | ✅ `BUILD SUCCESSFUL` |
| Full test suite | ✅ 148 tests, 0 failures, 0 errors |
| Compiled bytecode | major version 69 (JDK 25) confirmed |
| Resolved key versions | Hibernate `7.4.1.Final`, Spring Security `7.1.0`, Spring Core `7.0.8` |
| `docker build` with new `Dockerfile` | ✅ builds successfully on Ubuntu 26.04-based Temurin 25 image |
| `docker run` smoke test | JVM confirmed running Java 25.0.3; Spring context bootstraps correctly (Tomcat 11.0.22 embedded server initializes, JPA repositories scan, bean wiring succeeds) up to the point of failing on a deliberately-omitted `JWT_SECRET` env var — this is expected behavior (no secrets were passed to the bare `docker run`), not a Boot4/Java25 issue |

**Not yet performed at time of writing:** a live smoke test against a real PostGIS database (dev/staging) with actual application secrets configured — this remained an outstanding follow-up since no local PostGIS instance was available during implementation (production only connects to a NAS-hosted DB, per `docker-compose.yml`).

**Update (2026-08-05):** The application has since been deployed to production (`rinsewillet.net/webgis/`) on this Spring Boot 4.1.0 / Java 25 stack, connected to the real NAS-hosted PostGIS database with production secrets. The deployment is serving live traffic without incident, which satisfies this follow-up in practice. No separate dev/staging smoke test was run before the production deploy, but production itself now stands as the live verification.

---

## Alternatives Considered

### A. Stay on Spring Boot 3.5.x / Java 21
- Rejected — 3.5.x OSS support has already lapsed (ADR-008); all technical spikes showed the upgrade is low-risk and mostly mechanical, so there's no reason to delay further.

### B. Target Spring Boot 4.0.x instead of 4.1.0
- Rejected — 4.0.x's own OSS support window ends sooner (2026-12-31) than 4.1.x (2027-07-31); 4.1.0 is both newer and longer-supported.

### C. Split into two separate PRs (Boot 4 first, then Java 25)
- Rejected in favor of a single combined PR — the combined spike (`spike/boot4-java25-combined`) showed no interaction effects between the two changes, so shipping them together avoids an intermediate, partially-upgraded state.

---

## Consequences

### Positive

- Restores the project to a Spring Boot line with active free OSS security support (until 2027-07-31).
- Adopts Java 25, a genuine LTS with support until 2031-09-30.
- Minimal code churn: 1 dependency/import fix across test infrastructure only; zero changes to business logic, security config, or entities.
- CI pipelines (`backend-ci.yml`, `docker-image.yml`, `compose-smoke-ci.yml`) updated consistently to build/test on the same JDK 25 the app now targets.

### Negative

- The dual Jackson 2.x (`com.fasterxml.jackson`, our explicit pins) / 3.x (`tools.jackson`, pulled in internally by Boot 4) coexistence on the classpath is a latent complexity to watch, particularly around `GeoJsonConverter`/`JsonUtils`/`jackson-datatype-jts`. No issue observed yet.
- Lombok (`1.18.46`, BOM-managed) emits a non-blocking `sun.misc.Unsafe` deprecation warning under JDK 25 — compiles fine today; will need re-checking if a future JDK removes that API.
- Live PostGIS smoke test still outstanding — must happen in a real dev/staging environment before this reaches `main`/production.

### When to Revisit

- ~~Before merging to `main`: run a live smoke test against a real PostGIS database with real secrets configured.~~ Resolved 2026-08-05 — production deployment at `rinsewillet.net/webgis/` is the live smoke test.
- If Jackson 2/3 coexistence causes any serialization discrepancy in real usage, investigate migrating custom Jackson-based code to the `tools.jackson` 3.x API.
- Re-check Lombok's `Unsafe` usage status if upgrading past JDK 25 in the future.

---

## Implementation References

- `build.gradle` — plugin/toolchain bump, test dependency fix.
- `Dockerfile` — base image bump to `eclipse-temurin:25-jre`.
- `.github/workflows/backend-ci.yml`, `.github/workflows/docker-image.yml`, `.github/workflows/compose-smoke-ci.yml` — CI JDK bump to 25.
- `README.md`, `AGENTS.md` — stack documentation updated.
- 8 test classes with `AutoConfigureMockMvc` import fix (`RoadControllerTests`, `AuthControllerTests`, `SuggestionControllerTests`, `MediaControllerTests`, `DashboardControllerTests`, `SiteControllerTests`, `JwtSecurityIntegrationTests`, `ModernReferenceControllerTests`).
- Related: ADR-008 (support-window confirmation); prior spike branches `spike/spring-boot-4`, `spike/java-25-toolchain`, `spike/boot4-java25-combined` (not merged, exploratory only).

