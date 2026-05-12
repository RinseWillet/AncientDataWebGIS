# Data Governance Policy

Date: May 12, 2026

## Purpose

Protect the scholarly integrity of the Roman roads dataset while still allowing external contributions.

## Authoritative data ownership

- **Canonical geospatial data (roads/sites geometry and core attributes)** is curated in QGIS against the shared PostGIS container.
- The web application is **read-only for geometry mutations**.

## Web application write policy

- Allowed:
  - User authentication/authorization
  - Modern reference linking (with role checks)
  - Suggestion submission/review workflow
- Not allowed:
  - Creating/updating/deleting road or site geometry through web endpoints

## Suggestion workflow

1. Authenticated users submit a suggestion (`PENDING`) with:
   - target type (`ROAD`/`SITE`/`GENERAL`)
   - optional target id
   - summary/details
   - optional image URL
2. Admin reviews and marks as `APPROVED` or `REJECTED`.
3. Approved changes are applied manually in QGIS by curator/research owner.

## Shared DB constraint

Because PostGIS is shared with QGIS and externally managed:

- Flyway remains disabled in runtime config.
- This backend does not auto-apply schema migrations.
- New app-owned tables (for example `data_suggestions`) must be created via coordinated SQL run on the DB container.

## Operational checklist for every accepted suggestion

- [ ] Reviewer decision recorded in application (`APPROVED`).
- [ ] Data change applied in QGIS.
- [ ] Result verified in web map/API read endpoints.
- [ ] Reviewer notes updated when needed.

