# Database Migration Strategy

Date: May 12, 2026

## Decision: External schema ownership — Flyway disabled

The PostgreSQL/PostGIS database runs in a **shared container** that is also managed
by QGIS. The schema (tables, indexes, geometry columns, extensions) is owned and
maintained **outside** of this Spring Boot application.

**Consequence: this application must not run schema-altering migrations.**

## Runtime configuration

In `src/main/resources/application.properties`:

```properties
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
```

- Flyway is **permanently disabled** (`spring.flyway.enabled=false`).
- Hibernate DDL is set to `none` — the application only reads/writes, never alters schema.
- The only Flyway migration present (`V1__Create_users_table.sql`) was created for the
  `users` table which IS owned by this backend. This file remains as documentation of that
  schema contract but is not executed at runtime because Flyway is disabled.

## Dev profile override

`application-dev.properties` sets `spring.jpa.hibernate.ddl-auto=validate` so the local
dev startup verifies entities match the live DB columns without changing anything.

## Schema change process

1. Schema changes (new tables, columns, indexes) are applied **directly to the PostGIS
   container** via QGIS or a DBA-approved SQL script run outside of this application.
   - Store reviewed SQL scripts in `docs/architecture/sql/` for traceability.
2. This application's entities/DTOs are then updated to match the new schema.
3. If a schema column only this backend owns needs to change, it is done via a coordinated
   update with whoever manages the shared DB — not via automated Flyway in CI/CD.

## H2 test profile

Unit/integration tests run against an in-memory H2 database with
`spring.jpa.hibernate.ddl-auto=create-drop` so tests are fully self-contained and do not
depend on the shared PostGIS container.
See `src/test/resources/application-test.properties`.

