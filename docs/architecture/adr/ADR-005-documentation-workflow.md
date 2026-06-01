# ADR-005: Documentation & Planning Workflow

**Status:** Accepted
**Date:** 2026-06-01
**Decision makers:** Project owner

---

## Context

As the project grew through E0–E2, documentation accumulated across several formats and locations:
- Feature specs in `docs/features/`
- Decision-like documents scattered in `docs/architecture/` (DB strategy, data governance) and `docs/ci-cd/`
- One formal ADR (`ADR-001`)
- A large feature-spec backlog (`FEATURE-SPEC-BACKLOG.md`) located in `docs/architecture/` rather than with other planning docs

There was no single, consistent decision record format, and no documented workflow for planning new features before implementation.

---

## Decision

**Three changes to the documentation and planning workflow:**

### 1. ADRs as the central decision record

All architectural, process, and infrastructure decisions are recorded as ADRs in `docs/architecture/adr/`. Existing decision documents (`DB-MIGRATION-STRATEGY.md`, `DATA-GOVERNANCE-POLICY.md`, `CI-CD-DECISIONS.md`) are retroactively indexed as ADR-002 through ADR-004. The original files remain in place as detailed references; the ADR wrappers provide a consistent index entry and summary.

### 2. Plan-first workflow for new features

New epics and stories must be planned using Copilot plan mode before implementation begins. The plan is reviewed and approved before any code is written. This is documented in the `AGENTS.md` files for both repos.

### 3. Feature-spec backlog relocated

`FEATURE-SPEC-BACKLOG.md` is moved from `docs/architecture/` to `docs/features/` where it logically belongs alongside the per-epic feature specs.

---

## Alternatives Considered

### A. Rewrite all existing decision docs into ADR format

- **Rejected.** Unnecessary churn. The existing documents are well-written and thorough. ADR wrappers that summarize and link to them are sufficient.

### B. Use a wiki or external tool for decisions

- **Rejected.** Keeping decisions in the repo ensures they are versioned, reviewed in PRs, and co-located with the code they govern.

---

## Consequences

### Positive

- Single discoverable index of all project decisions (`docs/architecture/adr/README.md`).
- Consistent format for future decisions (ADR template).
- Planning before execution reduces rework and scope creep.
- Feature backlog is co-located with feature specs.

### Negative

- Slight overhead: creating an ADR for each decision requires discipline.
- Plan-first workflow adds a step before implementation (intentional friction).

### When to Revisit

- If the ADR count grows large enough to warrant tooling (e.g., auto-generated index).
- If the plan-first workflow proves too heavy for small changes.

---

## Implementation References

- ADR index: `docs/architecture/adr/README.md`
- ADR template: `docs/architecture/adr/ADR-TEMPLATE.md`
- Feature backlog: `docs/features/FEATURE-SPEC-BACKLOG.md`
- Backend agent rules: `AGENTS.md` (Plan-First Workflow section)
- Frontend agent rules: `../AncientDataWebGIS_FE/AGENTS.md` (Plan-First Workflow section)

