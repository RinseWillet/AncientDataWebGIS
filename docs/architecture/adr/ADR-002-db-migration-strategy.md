# ADR-002: Database Migration Strategy

**Status:** Accepted
**Date:** 2026-05-12
**Decision makers:** Project owner

---

## Context

The PostgreSQL/PostGIS database runs in a shared container that is also managed by QGIS. The schema (tables, indexes, geometry columns, extensions) is owned and maintained outside of the Spring Boot application. A strategy is needed to prevent the application from accidentally altering the shared schema while still supporting app-owned tables.

---

## Decision

**Flyway is permanently disabled at runtime. Hibernate DDL is set to `none`. Schema changes are applied externally via QGIS or DBA-approved SQL scripts.**

- App-owned tables (e.g., `users`, `media_asset`) are created via coordinated SQL scripts stored in `docs/architecture/sql/`.
- The dev profile uses `ddl-auto=validate` as a safety check.
- H2 test profile uses `ddl-auto=create-drop` for self-contained tests.

---

## Alternatives Considered

### A. Enable Flyway for all schema management

- **Rejected.** The shared PostGIS database is co-managed with QGIS. Automated migrations could conflict with external schema changes and break the QGIS workflow.

### B. Enable Flyway only for app-owned tables

- **Deferred.** Adds complexity for a small number of app-owned tables. Coordinated SQL scripts are sufficient for now.

---

## Consequences

### Positive

- Zero risk of the application corrupting the shared PostGIS schema.
- Clear separation: QGIS owns geospatial schema; backend owns auth/media tables.
- Dev profile validation catches entity/schema drift early.

### Negative

- Schema changes for app-owned tables require manual coordination.
- No automated migration history tracked by Flyway.

### When to Revisit

- If the number of app-owned tables grows significantly.
- If the shared DB constraint is removed (dedicated DB per service).

---

## Implementation References

- Full details: `docs/architecture/DB-MIGRATION-STRATEGY.md`
- Runtime config: `src/main/resources/application.properties`
- Dev override: `src/main/resources/application-dev.properties`
- Test config: `src/test/resources/application-test.properties`
- SQL scripts: `docs/architecture/sql/`

