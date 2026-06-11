# Architecture Decision Records (ADR)

This directory is the **central record for all architectural and process decisions** made during the development of the AncientData WebGIS project.

## What is an ADR?

An Architecture Decision Record captures a single decision, its context, the alternatives considered, and its consequences. ADRs are numbered sequentially and are **immutable once accepted** — if a decision is superseded, a new ADR is created that references the old one.

## ADR Index

| ADR | Title | Status | Date | Location |
|-----|-------|--------|------|----------|
| [ADR-001](./ADR-001-media-storage-strategy.md) | Media Storage Strategy | Accepted | 2026-05-29 | This directory |
| [ADR-002](./ADR-002-db-migration-strategy.md) | Database Migration Strategy | Accepted | 2026-05-12 | This directory |
| [ADR-003](./ADR-003-data-governance-policy.md) | Data Governance Policy | Accepted | 2026-05-12 | This directory |
| [ADR-004](./ADR-004-ci-cd-pipeline-design.md) | CI/CD Pipeline Design | Accepted | 2026-05-11 | This directory |
| [ADR-005](./ADR-005-documentation-workflow.md) | Documentation & Planning Workflow | Accepted | 2026-06-01 | This directory |

## How to create a new ADR

1. Copy `ADR-TEMPLATE.md` and rename it `ADR-{NNN}-{short-title}.md`.
2. Fill in all sections.
3. Add a row to the index table above.
4. Submit for review in the same PR as the code change it governs.

## Conventions

- **Numbering:** Three-digit, zero-padded (e.g., `ADR-001`).
- **Status values:** `Proposed` → `Accepted` → optionally `Superseded by ADR-NNN` or `Deprecated`.
- **Immutability:** Never edit the Decision or Alternatives of an accepted ADR. Add a new ADR that supersedes it instead.
- **Scope:** ADRs cover both backend (ADW) and frontend (ADW-FE) when a decision spans repos.

