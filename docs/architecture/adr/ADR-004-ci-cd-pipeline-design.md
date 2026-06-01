# ADR-004: CI/CD Pipeline Design

**Status:** Accepted
**Date:** 2026-05-11
**Decision makers:** Project owner

---

## Context

The AncientData project spans two repositories (backend and frontend). The Spring Boot backend serves the frontend as static assets baked into the JAR. A CI/CD pipeline was needed to automate testing, building, and Docker image publishing while handling the split-repo constraint safely.

---

## Decision

**Incremental, per-repo CI gates with an automated frontend build inside the backend Docker pipeline. Docker images are published only on trusted push to `main` or version tags.**

Key design choices:
- Frontend CI (lint → test → build) runs independently in the frontend repo.
- Backend Docker pipeline checks out the latest frontend from `main`, rebuilds it, and bakes it into the JAR — ensuring the Docker image always includes the latest frontend.
- Docker image rebuild in the publish job is intentionally duplicated for safety (validation vs. publishing are separate jobs).
- Redundancy is by design, not accident — every redundancy serves a safety purpose.

---

## Alternatives Considered

### A. Single monorepo

- **Rejected.** The repos are already established separately with independent CI needs. Merging would create unnecessary churn.

### B. Frontend artifact caching across repos

- **Deferred.** The ~30s rebuild cost is small; the current self-contained approach is safer and simpler.

### C. Combined docker-build/publish job

- **Rejected.** Loses independent validation of buildability on PRs.

---

## Consequences

### Positive

- Each job does one clear thing (auditability).
- PR validation is independent from publishing (safety).
- Easy to add future stages (Helm deploy, security scan) without entangling cache/artifact reuse.

### Negative

- Frontend is rebuilt ~30s extra per backend push (acceptable cost).
- Docker image is built twice on main push (~2–3m, mitigated by Buildx cache).

### When to Revisit

- If CI time becomes a bottleneck (see Workflow Analysis for micro-optimizations).
- If the project moves to a monorepo.

---

## Implementation References

- Full decision log: `docs/ci-cd/CI-CD-DECISIONS.md`
- Workflow analysis: `docs/architecture/WORKFLOW-ANALYSIS.md`
- Frontend CI: `AncientDataWebGIS_FE/.github/workflows/frontend-ci.yml`
- Backend CI: `.github/workflows/backend-ci.yml`
- Docker pipeline: `.github/workflows/docker-image.yml`

