# ADR-011: Book Content Storage and Backup — Git-Versioned Files (Single-Author)

**Status:** Accepted
**Date:** 2026-08-07
**Decision makers:** Project owner
**Epic/Story:** E8 (Interactive Book / Research Narrative), E8-8, E8-9

---

## Context

The project owner is a single author preparing ~20 pages of long-form research narrative (prehistoric roads, Roman road-building, methodology, etc.) plus QGIS-generated map images, intended to be read alongside the interactive map/data explorer (E8). Two questions needed a decision before implementation:

1. Where should chapter text and illustrative images physically live?
2. How is this content protected against loss, given the project already has a two-tier backup story for the database and uploaded media (`docs/BACKUP-STRATEGY.md`: `NasBackupService` + `DbBackupService` in-app, plus the external `scripts/backup.sh` NAS-cron archive)?

The content is currently authored by one person (the project owner) directly, with no separate non-technical editors requiring a web-based editing UI.

---

## Decision

**Store book chapter text and illustrative images as files inside the `AncientDataWebGIS_FE` git repository, and treat git (commit + push to remote) as the backup/versioning mechanism for this content — no new database entities, no headless CMS, no additional backup service.**

Concretely:

- Chapter prose: Markdown files under `AncientDataWebGIS_FE/src/content/book/*.md`.
- Illustrative images (QGIS exports, sketches, diagrams not tied to a specific database record): bundled under `AncientDataWebGIS_FE/src/assets/book/`, resolved via Vite's `import.meta.glob`.
- Any photograph that **is** also a scientific/dataset record (i.e. tied to an actual site or road) continues to be uploaded through the existing `MediaAsset` pipeline (`MediaUploadForm` → backend → `/app/media`) rather than duplicated as a static book asset — this keeps it automatically covered by the existing `NasBackupService`/`scripts/backup.sh` media backup path with zero additional work.
- The only new operational requirement is a documented expectation (E8-8) that book content is pushed to the remote git host regularly, the same as any other source change.

---

## Alternatives Considered

### A. Database-backed content (new `book_chapter` Spring entity, editable via admin UI)

- **Rejected for now.** Would automatically inherit `DbBackupService`/`pg_dump` coverage and allow runtime editing without a deploy, but requires a new CRUD API, admin rich-text editing UI, and its own migration — significant engineering for a single-author, deploy-anyway workflow. No current requirement for non-technical or multi-author editing that would justify this cost. Revisit if that requirement appears (see "When to Revisit").

### B. Headless CMS (Contentful, Sanity, Strapi) or git-backed CMS (TinaCMS, Decap CMS)

- **Rejected for now.** Solves multi-author, non-technical editing well, and git-backed variants (TinaCMS/Decap) would still commit Markdown/MDX to the repo, preserving git-as-backup. Adds a new service/dependency and account/auth setup with no current second author to justify it. Noted as the natural next step if collaboration needs grow, without committing to a specific vendor now.

### C. Standalone static-site generator (Docusaurus, VitePress) alongside the SPA

- **Rejected.** Would duplicate the Redux store, `MapComponent`, `MediaGallery`, and API client, or require awkward cross-app integration, undermining the goal of deep prose-to-data cross-linking (E8-10) planned for this feature. Keeping content inside the existing Vite/React SPA (rendered via `react-markdown`, with a future MDX upgrade) avoids a second toolchain entirely.

---

## Consequences

### Positive

- Zero new infrastructure, dependencies (beyond `react-markdown`), or credentials.
- Git provides full edit history for prose — arguably stronger than the DB/media backup story, which only captures periodic snapshots, not per-sentence revision history.
- Content and code stay in the same review/PR flow already used for the rest of the frontend.
- Data-linked photographs are not duplicated — a single source of truth (`MediaAsset`) remains authoritative and already backed up.

### Negative

- **Backup coverage depends on discipline, not automation.** Unlike `NasBackupService`'s scheduled sync, there is no automatic reminder to push book content commits to the remote. If the author works locally for an extended period without pushing, uncommitted/unpushed chapters are only as safe as the local machine.
- **No runtime editing without a deploy.** Any content change requires a rebuild/deploy of the frontend, unlike a DB-backed or CMS-backed model. Acceptable for the current cadence (occasional content additions) but would become friction if content changes needed to ship independently of code releases.
- **No built-in non-technical editing UI.** Fine for a single technical author; would block a non-developer co-author from contributing directly.

### When to Revisit

- If a second, non-technical author needs to contribute content independently → adopt a git-backed CMS (TinaCMS/Decap CMS) per Alternative B, preserving git-as-backup while adding a web editing UI.
- If content needs to update independently of frontend deploys (e.g. frequent corrections without a full CI/CD cycle) → move to Alternative A (DB-backed chapters), which would also bring it under `DbBackupService`/`pg_dump` coverage automatically.
- If the volume of illustrative images grows large enough to bloat the frontend bundle/repo size noticeably → reconsider serving book images through the same object-storage/media path as data photographs rather than bundling them in the frontend repo.

---

## Implementation References

- Feature spec: `docs/features/E8-interactive-book.md`
- Backlog: `docs/features/FEATURE-SPEC-BACKLOG.md` § "E8 — Interactive Book / Research Narrative"
- Related backup docs: `docs/BACKUP-STRATEGY.md`, ADR-001 (media storage), ADR-006 (media backup NAS sync)
- Content locations (once implemented): `AncientDataWebGIS_FE/src/content/book/`, `AncientDataWebGIS_FE/src/assets/book/`

