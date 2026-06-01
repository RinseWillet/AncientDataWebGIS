# ADR-003: Data Governance Policy

**Status:** Accepted
**Date:** 2026-05-12
**Decision makers:** Project owner

---

## Context

The AncientData dataset contains curated scholarly geospatial data (Roman roads and archaeological sites). The web application must allow contributions and metadata editing without compromising the integrity of the canonical dataset managed in QGIS.

---

## Decision

**The web application is read-only for geometry mutations. Canonical geospatial data is owned by the QGIS curator. The web app allows auth, modern reference linking, media management, and suggestion submission — but never direct geometry creation/update/deletion.**

- Authenticated users submit suggestions (`PENDING` → `APPROVED`/`REJECTED`).
- Approved changes are applied manually in QGIS by the curator.
- Media assets are managed through the web app (separate from geometry).

---

## Alternatives Considered

### A. Allow geometry editing through the web app

- **Rejected.** Risks unreviewed changes to scholarly data. The QGIS workflow provides quality control and citation tracking that the web app does not replicate.

### B. Automated apply of approved suggestions

- **Deferred.** Requires a reliable sync mechanism between the web app and QGIS-managed schema. Not justified for the current contributor volume.

---

## Consequences

### Positive

- Scholarly integrity of the dataset is preserved.
- Clear separation of roles: curator vs. contributor.
- Suggestion workflow provides an audit trail.

### Negative

- Approved suggestions require manual QGIS intervention (latency).
- Contributors cannot see their changes reflected immediately.

### When to Revisit

- If contributor volume grows and manual review becomes a bottleneck.
- If an automated QGIS→PostGIS sync pipeline is established.

---

## Implementation References

- Full details: `docs/architecture/DATA-GOVERNANCE-POLICY.md`
- Suggestion workflow: `application/service/SuggestionService.java`
- Security config: `security/SecurityConfig.java`

