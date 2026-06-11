# AncientData Feature Spec Backlog (Jira + Copilot Ready)

Date: 2026-05-12

This file translates the product analysis into a prioritized, implementation-ready backlog.
It is structured to support:
- Jira epic/story creation
- Sprint planning
- Copilot-assisted implementation

---

## 1) Product Goals

1. Evolve from passive map viewer to active research workbench.
2. Add analytical visibility (dashboard metrics and trends).
3. Add visual evidence (photos/media linked to sites/roads).
4. Add raster intelligence (historical maps, plans, DEM).
5. Improve field usability (tablet/mobile UX).
6. Optional thematic mode (synthwave) without hurting legibility.

---

## 2) Epics

| Epic ID | Epic Name | Status | Outcome |
|---|---|---|---|
| E0 | Platform Hardening | ✅ Done | Remove blockers and reduce technical/security risk before feature growth |
| E1 | Research Dashboard v1 | ✅ Done | Deliver aggregate metrics for roads/sites/lengths |
| E1.1x | Dashboard UX Hardening | ✅ Done | Harden dashboard layout, charts, accessibility, and state management |
| E2 | Photo & Media Integration | In Progress | Link and present images/media for roads/sites |
| E3 | Raster / GeoTIFF Delivery | To Do | Publish and consume large rasters via tile services |
| E4 | Responsive UX for Field Use | To Do | Improve mobile/tablet workflows on map and list views |
| E5 | Synthwave Theme (Optional) | To Do | Add alternate visual theme with persistent preference |

---

## 3) Story Backlog (Prioritized)

## P0 - Start Here

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E0-1 | E0 | Replace hardcoded compose credentials with env-based secrets in `AncientDataWebGIS/docker-compose.yml` | ✅ Done | Critical | S | None |
| E0-2 | E0 | Normalize HTTP/HTTPS layer URLs in `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.tsx` | ✅ Done | High | S | E0-1 |
| E0-3 | E0 | Fix `pleiadesid` vs `pleiadesId` end-to-end mismatch in site edit flow | ✅ Done | High | S | None |
| E0-4 | E0 | Add baseline API contract tests for road/site fetch + update flows | ✅ Done | High | M | E0-3 |
| E1-1 | E1 | Add `GET /api/dashboard/summary` combining road + site metrics | ✅ Done | High | M | E0-4 |
| E1-2 | E1 | Add PostGIS road length aggregation (km total + by type) | ✅ Done | High | M | E1-1 |
| E1-3 | E1 | Add frontend dashboard page with loading/error states | ✅ Done | High | M | E1-1 |

## P1 - Next

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E1-4 | E1 | Add date/type filters on dashboard | ✅ Done | Medium | M | E1-3 |
| E1-5 | E1 | Add CSV export for dashboard aggregates | ✅ Done | Medium | S | E1-3 |
| E2-0 | E2 | Define media storage foundation and delivery strategy (filesystem/NAS vs object storage), with operator runbook | ✅ Done | High | M | E0-4 |
| E2-1 | E2 | Add `media_asset` model (polymorphic: ROAD/SITE + id) | ✅ Done | High | M | E2-0 |
| E2-2 | E2 | Store media metadata (caption/license/source/author/date) | ✅ Done | High | S | E2-1 |
| E2-3 | E2 | Add gallery UI to `RoadInfo` and `SiteInfo` | ✅ Done | High | M | E2-1 |
| E2-4 | E2 | Add map info card cover image | To Do | Medium | S | E2-3 |
| E2-5 | E2 | Add admin media moderation flow | ✅ Done | Medium | M | E2-1 |
| E2-UI-1 | E2 | Admin upload component — MediaUploadForm with file picker, metadata fields, calls POST /api/media | ✅ Done | High | M | E2-3 |
| E2-UI-2 | E2 | Admin media management — edit/delete controls on gallery thumbnails (PATCH/DELETE /api/media/{id}) | ✅ Done | High | M | E2-UI-1 |
| E2-UI-3 | E2 | Admin view of pending/hidden media — admins see all statuses with badges via GET /api/media/admin | ✅ Done | High | S | E2-UI-2 |
| ~~E2-BACKUP-1~~ | E2 | ~~Add Google Drive backup service~~ — superseded by NAS-sync approach (see ADR-006) | Won't Do | — | — | — |
| ~~E2-BACKUP-2~~ | E2 | ~~Add scheduled Google Drive sync task~~ — superseded by NAS-sync approach (see ADR-006) | Won't Do | — | — | — |
| ~~E2-BACKUP-3~~ | E2 | ~~Add manual Google Drive sync trigger~~ — superseded by NAS-sync approach (see ADR-006) | Won't Do | — | — | — |
| E2-BACKUP-NAS-1 | E2 | Add NAS filesystem backup service — `NasBackupService` + `NasBackupConfig` syncing local media dir to a mounted NAS path | ✅ Done | High | M | E2-1 |
| E2-BACKUP-NAS-2 | E2 | Scheduled NAS sync — cron-based background sync via `@Scheduled`, configurable via `backup.nas.sync-cron` env var | ✅ Done | High | S | E2-BACKUP-NAS-1 |
| E2-BACKUP-NAS-3 | E2 | Manual sync trigger endpoint — `POST /api/backup/sync` (ADMIN) for on-demand backup, returns sync status | ✅ Done | Medium | S | E2-BACKUP-NAS-1 |
| E2-GEO-1 | E2 | Photo geotagging — extract/store GPS coordinates from EXIF data and allow manual placement on map; display geotagged photos as markers | To Do | Medium | M | E2-UI-1 |

## P2 - Then

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E3-1 | E3 | Define raster publishing pipeline (GeoTIFF -> tiled service) | To Do | High | L | E0-1 |
| E3-2 | E3 | Add raster layer catalog endpoint (name/source/bounds/zoom/attribution) | To Do | High | M | E3-1 |
| E3-3 | E3 | Add layer manager (toggle/opacity/order) in map UI | To Do | High | M | E3-2 |
| E3-4 | E3 | Implement DEM delivery strategy for ~80GB source (overviews/tiling) | To Do | High | L | E3-1 |
| E3-5 | E3 | Add raster legend + metadata drawer | To Do | Medium | S | E3-3 |
| E4-1 | E4 | Add mobile bottom-sheet interaction replacing side info card on narrow screens | To Do | High | M | E1-3 |
| E4-2 | E4 | Improve touch target spacing/sizing for controls | To Do | High | S | E4-1 |
| E4-3 | E4 | Improve `DataList` mobile readability and interactions | To Do | Medium | M | E4-2 |
| E4-4 | E4 | Add responsive QA matrix and regression checklist | To Do | High | S | E4-1 |

## P3 - Optional

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E5-1 | E5 | Add theme tokens + switcher based on CSS variables in `AncientDataWebGIS_FE/src/App.css` | To Do | Medium | S | E0-2 |
| E5-2 | E5 | Persist theme preference in local storage | To Do | Low | S | E5-1 |
| E5-3 | E5 | Add synthwave map style profile | To Do | Medium | M | E5-1 |

---

## 4) Acceptance Criteria (Per Priority Wave)

### P0 Done Criteria
- No committed plaintext secrets in compose files.
- No mixed-content map layer errors in HTTPS deployments.
- Site edit API uses consistent DTO naming and persists expected values.
- Contract tests cover core read/update routes and common error scenarios.
- Dashboard endpoint returns typed payload with stable schema.
- Dashboard frontend route is accessible and resilient (loading/error/empty states).

### P1 Done Criteria
- Media storage strategy is selected and documented with operational steps (setup, backups, permissions, URL strategy).
- Media entities can store multiple files per road/site with metadata.
- Road/Site detail pages display media galleries with attribution.
- Optional moderation flow exists for admin visibility control.

### P2 Done Criteria
- Raster layers are discoverable via API catalog and controllable in UI.
- DEM serving is tiled and performs acceptably at target zoom ranges.
- Mobile/tablet map interactions are practical (bottom-sheet + touch targets).

### P3 Done Criteria
- Theme switch works globally and persists.
- Synthwave mode remains legible for map and text UI.

---

## 5) Jira Import - Ready-to-Paste CSV

Note: Jira field names differ by workspace. Map these columns as needed:
- `Epic Link` may be called `Parent` or use issue keys after epics are created.
- `Story Points` may be a custom numeric field.

```csv
Issue Type,ID,Summary,Epic Name,Epic Link,Priority,Story Points,Labels,Description,Acceptance Criteria,Dependencies
Epic,E0,Platform Hardening,Platform Hardening,,Highest,,ancientdata;hardening,"Remove security/config blockers before major features.","All P0 hardening criteria met",-
Epic,E1,Research Dashboard v1,Research Dashboard v1,,High,,ancientdata;dashboard,"Expose and visualize roads/sites/length metrics.","Dashboard API and UI criteria met",E0
Epic,E2,Photo & Media Integration,Photo & Media Integration,,High,,ancientdata;media,"Store and display image/media assets linked to entities.","Media model + gallery + metadata criteria met",E0
Epic,E3,Raster / GeoTIFF Delivery,Raster / GeoTIFF Delivery,,High,,ancientdata;raster,"Serve historical maps/plans/DEM through tiled services.","Raster catalog + layer manager + DEM strategy criteria met",E0
Epic,E4,Responsive UX for Field Use,Responsive UX for Field Use,,High,,ancientdata;ux;mobile,"Improve map/list usability on tablet and mobile.","Bottom-sheet and touch target criteria met",E1
Epic,E5,Synthwave Theme (Optional),Synthwave Theme (Optional),,Medium,,ancientdata;theme,"Add optional visual theme mode with persistence.","Theme toggle/persistence criteria met",E0
Story,E0-1,Externalize compose credentials,,E0,Critical,2,security;config,"Replace hardcoded credentials in docker-compose with env vars and document .env usage.","No plaintext credentials committed; startup works with env values",-
Story,E0-2,Normalize HTTPS map layer URLs,,E0,High,2,frontend;map,"Ensure all map tile/WMS URLs are HTTPS-safe or proxied.","No mixed-content errors in HTTPS context",E0-1
Story,E0-3,Fix pleiades DTO naming mismatch,,E0,High,2,frontend;backend;api,"Align `pleiadesId` naming across DTOs/forms/services.","Site updates persist the intended field correctly",-
Story,E0-4,Add API contract tests for core data flows,,E0,High,3,test;backend,"Add tests for read/update road and site endpoints with validation failures.","Tests cover happy path and key error cases",E0-3
Story,E1-1,Create dashboard summary API,,E1,High,3,backend;dashboard,"Add `/api/dashboard/summary` with roads and sites aggregate metrics.","Endpoint returns stable typed schema",E0-4
Story,E1-2,Add road length PostGIS aggregations,,E1,High,3,backend;postgis,"Compute total km and per-type km aggregations using PostGIS.","Values validated with representative SQL checks",E1-1
Story,E1-3,Build dashboard frontend page,,E1,High,5,frontend;dashboard,"Create dashboard route with KPI cards and charts.","Loading/error/empty states implemented",E1-1
Story,E1-4,Add dashboard filters,,E1,Medium,3,frontend;dashboard,"Add date/type filters for dashboard widgets.","Filter changes update displayed metrics",E1-3
Story,E1-5,Add dashboard CSV export,,E1,Medium,2,frontend;backend,"Export current filtered dashboard state as CSV.","User can download CSV snapshot",E1-3
Story,E2-0,Define media storage foundation and runbook,,E2,High,3,backend;ops;media,"Choose storage backend and document setup/backup/security/URL strategy for media delivery.","Runbook approved and executable in target environment",E0-4
Story,E2-1,Add media_asset data model and API,,E2,High,5,backend;db;media,"Create media entity linked to ROAD/SITE and CRUD endpoints.","Migration + API + tests merged",E2-0
Story,E2-2,Support rich media metadata,,E2,High,2,backend;media,"Persist caption/license/source/author/date with media.","Metadata round-trips via API",E2-1
Story,E2-3,Add galleries to RoadInfo and SiteInfo,,E2,High,5,frontend;media,"Render media collections with captions and attribution.","Gallery UX available on both entity pages",E2-1
Story,E2-4,Add map info card cover image,,E2,Medium,2,frontend;map;media,"Show a cover thumbnail in map info card where available.","Selected feature displays thumbnail",E2-3
Story,E2-5,Add admin media moderation,,E2,Medium,3,admin;media,"Allow approve/reject/hide media visibility.","Admin can control visibility state",E2-1
Story,~~E2-BACKUP-1~~,~~Add Google Drive backup service~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",E2-1
Story,~~E2-BACKUP-2~~,~~Add scheduled Google Drive sync task~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",~~E2-BACKUP-1~~
Story,~~E2-BACKUP-3~~,~~Add manual Google Drive sync trigger~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",~~E2-BACKUP-1~~
Story,E2-BACKUP-NAS-1,Add NAS filesystem backup service,,E2,High,3,backend;media;backup,"NasBackupService + NasBackupConfig syncing local media dir to mounted NAS path. Enabled/disabled via backup.nas.enabled env var.","Service copies new/modified files to NAS mount; deletes orphan remote files; cron-configurable.",E2-1
Story,E2-BACKUP-NAS-2,Scheduled NAS sync,,E2,High,1,backend;media;backup,"@Scheduled cron sync driven by backup.nas.sync-cron env var (default: Sunday 03:00 UTC).","Sync runs on schedule when enabled=true.",E2-BACKUP-NAS-1
Story,E2-BACKUP-NAS-3,Manual NAS sync trigger endpoint,,E2,Medium,1,backend;media;backup;admin,"POST /api/backup/sync — ADMIN-only endpoint for on-demand backup trigger.","Admin can trigger sync via API; returns {status,message}.",E2-BACKUP-NAS-1
Story,E2-GEO-1,Photo geotagging,,E2,Medium,5,backend;frontend;media;geo,"Extract/store GPS from EXIF and allow manual placement; display geotagged photos as markers.","Geotagged photos appear on map",E2-UI-1
Story,E3-1,Define raster publish pipeline,,E3,High,8,geoserver;raster,"Define ingestion and publishing path for historical maps/plans/DEM.","Documented and repeatable pipeline",E0-1
Story,E3-2,Add raster layer catalog endpoint,,E3,High,3,backend;raster,"Expose available raster layers and metadata for frontend discovery.","Catalog includes bounds/zoom/attribution",E3-1
Story,E3-3,Add map layer manager controls,,E3,High,5,frontend;map;raster,"Allow toggle, opacity, ordering for raster overlays.","Controls apply instantly and persist session state",E3-2
Story,E3-4,Implement large DEM serving strategy,,E3,High,8,raster;dem;performance,"Use overviews and tiling for DEM serving, avoid raw file delivery.","Acceptable performance at target zoom ranges",E3-1
Story,E3-5,Add raster legend and metadata drawer,,E3,Medium,2,frontend;raster,"Show active raster legend and attribution details.","Legend/metadata visible for active layer",E3-3
Story,E4-1,Mobile bottom-sheet map details,,E4,High,5,frontend;ux;mobile,"Replace side info card with bottom-sheet on narrow viewports.","Usable and stable on target mobile breakpoints",E1-3
Story,E4-2,Improve touch targets and spacing,,E4,High,2,frontend;ux;mobile,"Ensure controls meet touch-size guidance.","Interactive controls are comfortably tappable",E4-1
Story,E4-3,Improve DataList mobile UX,,E4,Medium,3,frontend;ux;mobile,"Refine table/list behavior for small screens.","Readability and actions remain usable",E4-2
Story,E4-4,Add responsive QA matrix,,E4,High,2,qa;ux;mobile,"Create repeatable responsive regression checklist.","Checklist adopted in release flow",E4-1
Story,E5-1,Add theme switcher and tokens,,E5,Medium,2,frontend;theme,"Implement synthwave-ready theme token system and switcher.","Theme switches globally without breaking readability",E0-2
Story,E5-2,Persist selected theme,,E5,Low,1,frontend;theme,"Save and load theme preference from storage.","Preference survives reload",E5-1
Story,E5-3,Add synthwave map style profile,,E5,Medium,3,frontend;theme;map,"Tune map colors/icons for synthwave mode.","Map remains legible in synthwave",E5-1
```

---

## 6) Copilot Workflow (Per Story)

### Step 1: Plan (required for epics, recommended for M/L stories)

Use Copilot **Plan** mode to generate an implementation plan before writing code.
See `AGENTS.md` → "Plan-First Workflow" for details.

### Step 2: Implement

Use this prompt template when implementing each story in small PRs:

```text
You are implementing story <STORY_ID> from docs/features/FEATURE-SPEC-BACKLOG.md.

Scope:
- Only implement acceptance criteria for this story.
- Keep changes minimal and backward compatible.
- Add/adjust tests for all new behavior.

Required output:
1) Code changes
2) Test updates
3) Short migration notes (if schema/config changed)
4) Verification commands
5) ADR needed? (yes/no — if yes, create one in docs/architecture/adr/)

Story details:
- Summary: <SUMMARY>
- Acceptance criteria: <AC>
- Dependencies already completed: <DEPENDENCIES>
```

---

## 7) Suggested First Sprint (Execution Order)

1. `E0-1` Externalize compose credentials
2. `E0-2` Normalize HTTPS layer URLs
3. `E0-3` Fix DTO naming mismatch
4. `E0-4` Add API contract tests
5. `E1-1` Dashboard summary endpoint
6. `E1-2` PostGIS length aggregation
7. `E1-3` Dashboard frontend page

Deliverable target: secure baseline + first usable dashboard.

---

## 8) Completed Epics

### E0 — Platform Hardening ✅

**Status:** Complete

**What was delivered:**
- E0-1: Externalized docker-compose credentials; `.env` file with documented secrets management
- E0-2: Normalized HTTPS/HTTP map layer URLs for mixed-content safety
- E0-3: Resolved `pleiadesId` DTO naming consistency across site edit flows
- E0-4: Added comprehensive API contract tests for road/site CRUD and validation error scenarios

**Impact:**
- Secure baseline established before major feature rollout
- Safe for production deployment with proper credential rotation
- Consistent API contracts documented and tested

**Files/areas changed:**
- `AncientDataWebGIS/docker-compose.yml` (env-based secrets)
- `AncientDataWebGIS_FE/src/components/MapComponent/` (URL normalization)
- Backend DTOs and serialization (pleiades naming fix)
- Test suite for core endpoints

---

### E1 — Research Dashboard v1 ✅

**Status:** Complete (E1-1, E1-2, E1-3 merged; E1-4, E1-5 deferred to future)

**What was delivered:**
- E1-1: `/api/dashboard/summary` endpoint combining roads and sites aggregate metrics
- E1-2: PostGIS aggregations for total km and per-type km calculations
- E1-3: Frontend dashboard page with KPI cards, pie/bar charts, loading/error/empty states

**Impact:**
- Data scientists and admins now have analytics visibility into dataset composition
- Metrics auto-refresh on demand; responsive across breakpoints
- Foundation for subsequent hardening (E1.1x) and filtering (E1-4/E1-5)

**Files/areas changed:**
- Backend: dashboard controller + PostGIS queries
- Frontend: `AncientDataWebGIS_FE/src/pages/Dashboard.tsx`, `Dashboard.css`, `Dashboard.test.tsx`
- API service layer: `DashboardService.ts`

**Notes:**
- E1-4 (date/type filters) and E1-5 (CSV export) are implemented and functional (delivered during E1.1x hardening cycle).

---

### E1.1x — Dashboard UX Hardening ✅

**Status:** Complete (May 2026)

**Detailed specification:** `AncientDataWebGIS/docs/features/E1.1x-dashboard-ux-hardening.md`

**What was delivered:**
- Metro-style grid layout (Key Metrics / Sites / Roads) with responsive stacking on mobile
- Chart improvements: top-5 + "Other" pie for sites; horizontal bars for roads
- Comprehensive number formatting (locale separators, 2dp km, title-case labels)
- Skeleton loading placeholders + in-place retry (no page reload)
- Full accessibility baseline (landmarks, aria-labels, 44×44 touch targets)
- Complete test coverage for all 6 stories (E1.1x-1 through E1.1x-6)

**Files changed:**
- `AncientDataWebGIS_FE/src/pages/Dashboard.tsx`
- `AncientDataWebGIS_FE/src/pages/Dashboard.css`
- `AncientDataWebGIS_FE/src/pages/Dashboard.test.tsx`

**Verified at:** 393 px (Fairphone 5), 768 px (tablet), 1280 px (laptop), 1920 px (external screen)

**Known follow-up:** Horizontal margin/whitespace on ultra-wide screens; see spec for potential improvements (pagebox padding, fluid-width approach, CSS spacing tokens).

---

### E2 — Photo & Media Integration ✅

**Status:** Complete (May 2026)

**What was delivered:**
- E2-0: Media storage foundation — filesystem/NAS strategy with operator runbook (ADR-001)
- E2-1: `media_asset` data model with polymorphic ROAD/SITE targeting, full CRUD API, Flyway migration
- E2-2: Rich media metadata (caption, license, source, author, date taken) round-tripping via API
- E2-3: MediaGallery component with lightbox, captions, and attribution in SiteInfo and RoadInfo pages
- E2-5: Backend admin moderation (APPROVED/PENDING/HIDDEN visibility control)
- E2-UI-1: MediaUploadForm component (file picker, metadata fields, calls POST /api/media)
- E2-UI-2: Edit/delete controls on gallery thumbnails with confirmation dialog
- E2-UI-3: Admin view of all media statuses with badges, using GET /api/media/admin

- E2-BACKUP-NAS-1: `NasBackupService` + `NasBackupConfig` — Spring NAS filesystem sync service (enabled/disabled per env, bidirectional sync with orphan deletion)
- E2-BACKUP-NAS-2: Cron-scheduled sync via `@Scheduled(cron = "${backup.nas.sync-cron:...}")`, configurable via env var
- E2-BACKUP-NAS-3: `POST /api/backup/sync` (ADMIN) — manual trigger in `BackupController`

**Note:** Original E2-BACKUP-1/2/3 (Google Drive API backup) were superseded before implementation. The adopted approach (Spring NAS filesystem sync) is documented in ADR-006.

**Impact:**
- Admins can upload, edit metadata, and delete photos for any site or road via the browser
- Visitors see approved photos in SiteInfo/RoadInfo galleries
- Full flow: upload → filesystem storage → gallery display works end-to-end
- Admin moderation: visibility control (APPROVED/PENDING/HIDDEN) with status badges
- Offsite backup via NAS mount sync, triggerable on demand or via cron

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/services/MediaService.ts` (upload, updateMetadata, deleteMedia, findByTargetAdmin)
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaGallery.tsx` (admin controls, status badges, edit/delete)
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaGallery.css` (badge and admin UI styles)
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaUploadForm.tsx` (new upload form component)
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaUploadForm.css` (upload form styles)
- `AncientDataWebGIS_FE/src/pages/SiteInfo.tsx` (passes isAdmin to MediaGallery)
- `AncientDataWebGIS_FE/src/pages/RoadInfo.tsx` (passes isAdmin to MediaGallery)

**Tests:**
- `MediaGallery.test.tsx`: 16 tests (original 6 + 10 admin feature tests)
- `MediaUploadForm.test.tsx`: 6 tests (toggle, form fields, validation, submit, error states)
- Backend: existing MediaController + MediaService tests cover all API flows

**Outstanding (deferred):**
- E2-4: Cover thumbnail in MapInfoCard (medium priority, not blocking deployment)
- E2-GEO-1: Photo geotagging with EXIF extraction and map markers

