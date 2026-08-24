# ADR-013: Raster Catalog Storage — DB Table, Superseding the E3-2 Static List

**Status:** Accepted
**Date:** 2026-08-24
**Decision makers:** Project owner
**Epic/Story:** E3-6 (supersedes a decision recorded in E3-2's write-up)

---

## Context

E3-2 (`docs/features/FEATURE-SPEC-BACKLOG.md`) needed a place to curate per-layer display
metadata (name, WGS84 bounds, zoom range, attribution, category, collection, hillshade
flag) that GeoServer's own `GetCapabilities` can't supply. At the time, three options were
considered: (A) a static, hand-edited `List<RasterLayerDTO>` in `RasterCatalogService`, (B)
a backend-owned DB table (requiring a manual SQL script per `DB-MIGRATION-STRATEGY.md`,
since Flyway is disabled and the shared PostGIS schema is externally owned), or (C) a
hybrid pulling bounds live from GeoServer while curating the rest locally.

E3-2 chose **Option A**, explicitly recording that no ADR was needed for that choice: the
project owner confirmed ~20+ layers were planned, and a static list holds that many entries
fine *as data* — the thing that would actually get painful is the *edit workflow* (a PR +
redeploy per layer), which was deliberately deferred as a separate, lower-priority story
(E3-6, "picked up once the redeploy-per-layer workflow is actually the bottleneck, not
pre-built speculatively").

By August 2026, `RasterCatalogService.java`'s static list had grown to **30 hand-written
entries** (16 historical-map sheets across two atlases, plus 14 DEM/hillshade layers from
E3-4/E3-5) — past the ~20+ threshold E3-2 named. The project owner confirmed this
threshold had genuinely been hit (not speculatively pre-built) before this story was
picked up.

Scope was narrowed from E3-6's original backlog description in discussion with the project
owner:
- **No admin UI.** The project owner explicitly wants catalog edits restricted to
  themselves via direct authenticated API calls, not exposed through any browser-facing
  admin surface — even one gated to the `ADMIN` role, since these layers are structural to
  the app rather than user-facing content like media uploads.
- **No GeoServer-publishing/backup metadata.** The project owner separately raised wanting
  the GeoServer store/layer *publishing* work (not just catalog display metadata)
  protected against data loss. This was explicitly kept **out of scope** for this table —
  see "When to Revisit" below.

---

## Decision

**Replace the static `List<RasterLayerDTO>` with a new `raster_layer` table in the shared
PostGIS database, read via a JPA entity/repository, with admin-only REST CRUD endpoints
replacing the "edit Java, open a PR" workflow.**

- `docs/architecture/sql/raster_layer.sql` — a manually-DBA-applied script (per
  `ADR-002`/`DB-MIGRATION-STRATEGY.md`), following `media_asset.sql`'s (E2-0) precedent
  exactly: `CREATE TABLE IF NOT EXISTS`, an `updated_at` trigger, `GRANT ALL PRIVILEGES` to
  `webgis_client`. Includes seed `INSERT`s transcribing all 30 pre-cutover entries, so
  applying the script is a content-neutral no-op for the existing catalog.
- `source` (the GeoServer `workspace:layer` identifier) is `UNIQUE NOT NULL` and remains
  the stable key the frontend and the new admin endpoints address entries by — not the
  table's internal surrogate `id`, which never appears in any API response. This keeps
  `GET /api/raster/catalog`'s response shape (`List<RasterLayerDTO>`, byte-for-byte
  unchanged) a load-bearing, unmodified contract for the frontend's `RasterLayer` type and
  `useMapInteractions.ts`.
- New admin-only endpoints (`GET /catalog/admin`, `POST /catalog`, `PATCH
  /catalog/{source}`, `DELETE /catalog/{source}`), `@PreAuthorize("hasRole('ADMIN')")` +
  matching `SecurityConfig` URL rules, mirroring `MediaController`'s established
  belt-and-suspenders auth pattern. No admin UI is built on top of them this pass — they're
  driven directly (e.g. `curl` with an admin JWT from `POST /api/auth/login`), which is
  still strictly less manual work than the PR-per-layer workflow being replaced.

---

## Alternatives Considered

### A. Keep the static list, raise the entry count tolerance further

- **Rejected.** The problem E3-2 flagged was never about how many entries a `List.of(...)`
  can hold syntactically — it's that every single addition/edit/removal requires a Java
  code change, review, and redeploy. That cost doesn't shrink as the list grows; if
  anything, 30 entries (and more DEM/historical-map layers planned) makes each individual
  PR noisier to review (harder to spot one changed bounds value in a 300-line diff).

### B. Hybrid — live GeoServer `GetCapabilities` pull + local curation overlay

- **Deferred, not rejected** (same status E3-2 gave it). Still doesn't solve the curation
  problem (attribution, curated zoom range, category, collection grouping) — GeoServer's
  capabilities document has no fields for any of that. Would add a live upstream
  dependency (and its own caching/staleness concerns) to a currently pure DB read for no
  benefit over the chosen approach. Remains a future option if GeoServer's bounds/extent
  data needs to be treated as more authoritative than manually-entered bounds (e.g. to
  catch a GeoServer-side republish that changed a layer's extent without a matching catalog
  update) — not needed today.

### C. Extend `raster_layer` with GeoServer publishing/store metadata, so a DB row is enough to reconstruct a lost GeoServer layer

- **Rejected for this story, explicitly deferred as a separate concern.** The project owner
  raised this during scoping, but it's a materially different problem: it would require
  capturing GDAL conversion parameters, source file paths on the NAS, GeoServer
  store/workspace config, and SLD style references — none of which E3-2's `RasterLayerDTO`
  shape carries, and none of which the public catalog endpoint should ever expose. Backing
  up GeoServer's actual publishing state is more naturally a NAS-level backup problem
  (`ADR-012`'s "Backup" section already covers source GeoTIFFs under `rastermaps/`; it does
  not yet cover GeoServer's own `data_dir` config) than an extension of a
  frontend-display-metadata table. See "When to Revisit."

### D. Admin UI for catalog management (this story's original backlog scope)

- **Deferred, not rejected.** The original E3-6 backlog description included a
  `RasterService.ts` CRUD layer and an admin panel section (modeled on `MediaGallery`'s
  E2-UI-2 edit/delete pattern). The project owner chose to skip it for now — these are
  structural app layers they'd rather manage via direct authenticated API calls than expose
  through any browser UI, even an `ADMIN`-gated one. The CRUD REST endpoints built for this
  ADR's decision (Part B of the original scope) are UI-agnostic, so building this later
  requires no backend rework.

---

## Consequences

### Positive

- Publishing a new raster layer no longer requires a Java code change + PR + redeploy —
  registering it in the catalog is one authenticated API call, made right after the
  existing GeoServer/GDAL publishing steps (documented in the E3.1 runbook).
- `GET /api/raster/catalog`'s public contract is unchanged byte-for-byte — zero frontend
  changes required, and E3-3/E3-4/E3-5/E3-7/E3-8's already-shipped frontend work needed no
  rework.
- Follows established patterns in this codebase (`media_asset.sql`/`MediaAsset`/
  `MediaController`'s admin-CRUD/auth style) rather than inventing a new one.

### Negative

- Adds a manual DBA step (`raster_layer.sql`) that must be sequenced correctly before
  deploying the code that depends on it — the same operational overhead every table in this
  externally-owned schema carries (per `ADR-002`), but a new one for the raster domain
  specifically.
- No admin UI means catalog management still requires comfort with `curl`/an HTTP client
  and manually fetching a JWT — a deliberate tradeoff (see Alternative D), not a limitation
  of the endpoints themselves.
- GeoServer publishing/backup risk (the concern that prompted the project owner to revisit
  this story) is **not** addressed by this change — only catalog *display* metadata is now
  DB-backed and thus trivially re-creatable; the actual GeoServer store/layer configuration
  and source GeoTIFFs remain protected only by whatever NAS-level backup coverage exists
  today (see "When to Revisit").

### When to Revisit

- If an admin UI becomes worth building after all (e.g. someone other than the project
  owner needs to manage the catalog, or `curl`-driven management becomes its own friction
  point) — revisit Alternative D; the REST endpoints already exist.
- If GeoServer's own publishing state (stores, workspaces, layer configs) is confirmed
  **not** already covered by NAS-level backup (per `ADR-012`'s "Backup" section, which
  today only explicitly covers source GeoTIFFs under `rastermaps/`, not GeoServer's
  `data_dir` config) — that's a distinct backlog item, not a reason to extend
  `raster_layer` (see Alternative C).

---

## Implementation References

- Schema: `docs/architecture/sql/raster_layer.sql`
- Entity/repository: `src/main/java/com/webgis/ancientdata/domain/model/RasterLayer.java`,
  `RasterLayerCategory.java`,
  `src/main/java/com/webgis/ancientdata/domain/repository/RasterLayerRepository.java`
- Service: `src/main/java/com/webgis/ancientdata/application/service/RasterCatalogService.java`
- Controller: `src/main/java/com/webgis/ancientdata/web/controller/RasterCatalogController.java`
- Security: `src/main/java/com/webgis/ancientdata/security/SecurityConfig.java` (`RASTER_URL` admin rules)
- Runbook (registering a layer via the new endpoints): `docs/features/E3.1-raster-publishing-pipeline.md`
- Supersedes: the Option-A storage decision recorded in the E3-2 write-up,
  `docs/features/FEATURE-SPEC-BACKLOG.md`
