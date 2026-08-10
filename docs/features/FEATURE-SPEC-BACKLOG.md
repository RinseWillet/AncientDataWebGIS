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
| E2 | Photo & Media Integration | ✅ Done | Link and present images/media for roads/sites |
| E6 | Security & Dependency Hardening | ✅ Done | Resolve open Dependabot alerts and confirm a clean dependency graph before NAS deployment |
| E9 | Map Clarity & Layer Control Redesign | ✅ Done | Restrict selection/base-layer chrome to the Atlas, add a legend, and replace the bulky Leaflet grouped-layer control with a custom collapsible side panel — **unblocks E3** |
| E3 | Raster / GeoTIFF Delivery | To Do | Publish and consume large rasters via tile services (depends on E9) |
| E4 | Responsive UX for Field Use | ✅ Done | Improve mobile/tablet workflows on map and list views |
| E5 | Synthwave Theme (Optional) | To Do | Add alternate visual theme with persistent preference |
| E7 | Remote & Offline Dev Environment | ✅ Done | Enable developing/smoke-testing away from the home LAN, with or without network access |
| E8 | Interactive Book / Research Narrative | 🚧 In Progress | Publish long-form research narrative chapters (Markdown, with embedded QGIS-generated images) alongside the data explorer |
| E10 | Site & Road Type Registry Consolidation | To Do | Replace the scattered site/road type label, icon, and style definitions with one typed, single-source-of-truth registry, so adding/renaming/restyling a type (e.g. a new "watermill" site type) is a single-file change |

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
| E2-4 | E2 | Add map info card cover image | ✅ Done | Medium | S | E2-3 |
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
| E2-BACKUP-NAS-4 | E2 | Database backup (`DbBackupService`, pg_dump) + `backup_history` table + `GET /api/backup/status` + FE "Back up now" button with staleness indicator in `AdminPanel` | ✅ Done | Medium | M | E2-BACKUP-NAS-3 |
| E2-GEO-1 | E2 | Photo geotagging — extract/store GPS coordinates from EXIF data and allow manual placement on map; display geotagged photos as markers | ✅ Done | Medium | M | E2-UI-1 |

## P0.5 - Security Hardening (Pre-Deployment)

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E6-1 | E6 | Fix stale/broken Dependabot dependency-graph submission (JDK mismatch, unpinned actions in old `ci.yml`) | ✅ Done | Critical | S | None |
| E6-2 | E6 | Confirm dependency graph refreshes on `main` and re-verify all open alerts against actually-resolved versions | ✅ Done | Critical | S | E6-1 |
| E6-3 | E6 | Triage `jackson-databind` manifest alert — confirm Spring Boot BOM version is safe or pin explicitly | ✅ Done | High | S | E6-2 |
| E6-4 | E6 | Triage 4 critical `tomcat-embed-core` alerts (#15, #62, #65, #67) — confirm resolved 10.1.54 fixes them or upgrade further | To Do | Critical | S | E6-2 |
| E6-5 | E6 | Document a recurring dependency-alert triage cadence (e.g. monthly check + `./gradlew dependencies` verification steps) | To Do | Medium | S | E6-4 |

## P1.5 - Map Clarity & Layer Redesign (Blocks E3)

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E9-1 | E9 | Add `selectable`/`showLayerChrome` props threaded through `MapComponent` → `MapBuilder` → `MapContent` | ✅ Done | High | M | E0-2 |
| E9-2 | E9 | Disable click-to-select (`MapInfoCard`) on Home/RoadInfo/SiteInfo maps; Home also drops all layer chrome down to a single fixed Positron tile | ✅ Done | High | S | E9-1 |
| E9-3 | E9 | Extract layer definitions from `BaseLayers.tsx` into a typed, group-driven `layersConfig.ts` (Topographical / Aerial Imagery / Historical Maps) | ✅ Done | High | M | E0-2 |
| E9-4 | E9 | Build a fully custom, BEM-styled, collapsible `LayerPanel` left sidebar for Atlas, rendered from `layersConfig.ts` + the sites/roads/photos overlay group, replacing the Leaflet grouped-layer control | ✅ Done | High | L | E9-3 |
| E9-5 | E9 | Build `MapLegend` component (site type icons/labels, road style key) with a hook to surface a DEM color-ramp explanation once a Physical/DEM layer exists | ✅ Done | Medium | M | E9-3 |
| E9-6 | E9 | Update/add tests (`Home`, `RoadInfo`, `SiteInfo`, `Atlas`, `LayerPanel`, `MapLegend`) and document the E3 dependency gate | ✅ Done | High | S | E9-4, E9-5 |

**E9 implementation notes (lock in design decisions from planning, not just re-derivable from code):**
- `E9-1`: Add `selectable?: boolean` (default `true`) and `showLayerChrome?: boolean` (default `true`) to `MapComponentProps`/`MapBuilderProps`/`MapContentProps`. `selectable=false` must skip attaching `onEachFeature`'s `click` handler on both the sites and roads `GeoJSON` layers in `MapContent.tsx` (so `clickSite`/`clickRoad` never fire → `setShowInfoCard`/`MapInfoCard` never triggers). `showLayerChrome=false` must skip rendering `<BaseLayers />` and the `<LayersControl>` wrapper entirely, replacing them with a single unconditionally-rendered `<TileLayer>` using the Positron URL/attribution already defined in `BaseLayers.tsx`'s `baseLayerConfigs`, plus unwrapped (always-rendered, non-toggleable) `<GeoJSON>` for sites/roads/photos.
- `E9-2`: `Home.tsx` passes `selectable={false} showLayerChrome={false}`. `RoadInfo.tsx`/`SiteInfo.tsx` pass `selectable={false}` only (keep existing `<BaseLayers />`/`<LayersControl>`). `Atlas.jsx` passes neither (both stay at their `true` default).
- `E9-4`: This fully replaces the `leaflet-groupedlayercontrol` control (`BaseLayers.tsx`) in Atlas — not a restyle of it. `BaseLayers.tsx` and the `leaflet-groupedlayercontrol` dependency become dead code/removable once `E9-4` ships (confirm no other page still needs `<BaseLayers />` before deleting — `RoadInfo`/`SiteInfo` still use it per `E9-2`, so keep the component, just stop using it in `Atlas`).
- `E9-4`: A config group (e.g. "Physical") must not render its sidebar section at all while `layersConfig.ts` has zero entries tagged with that group — no disabled/greyed-out placeholder rows.

## P2 - Then

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E3-1 | E3 | Define raster publishing pipeline (GeoTIFF -> tiled service) | To Do | High | L | E0-1, E9-4 |
| E3-2 | E3 | Add raster layer catalog endpoint (name/source/bounds/zoom/attribution) | To Do | High | M | E3-1 |
| E3-3 | E3 | Add "Physical" group entries (toggle/opacity/order) to the `LayerPanel` from E9 | To Do | High | M | E3-2, E9-4 |
| E3-4 | E3 | Implement DEM delivery strategy for ~80GB source (overviews/tiling) | To Do | High | L | E3-1 |
| E3-5 | E3 | Add DEM color-ramp data to `MapLegend`'s DEM hook (from E9-5) + metadata drawer | To Do | Medium | S | E3-3, E9-5 |
| E4-1 | E4 | Add mobile bottom-sheet interaction replacing side info card on narrow screens | ✅ Done | High | M | E1-3 |
| E4-2 | E4 | Improve touch target spacing/sizing for controls | ✅ Done | High | S | E4-1 |
| E4-3 | E4 | Improve `DataList` mobile readability and interactions | ✅ Done | Medium | M | E4-2 |
| E4-4 | E4 | Add responsive QA matrix and regression checklist | ✅ Done | High | S | E4-1 |

## P3 - Optional

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E5-1 | E5 | Add theme tokens + switcher based on CSS variables in `AncientDataWebGIS_FE/src/App.css` | To Do | Medium | S | E0-2 |
| E5-2 | E5 | Persist theme preference in local storage | To Do | Low | S | E5-1 |
| E5-3 | E5 | Add synthwave map style profile | To Do | Medium | M | E5-1 |
| E10-1 | E10 | Consolidate site type label/icon definitions (currently split across `utils/siteTypes.ts`, `siteIcons.ts`, `Styles/markerStyles.ts`) into one typed `siteTypesConfig.ts`, sourced by `MapContent`, `MapInfoCard`, `SiteInfo`, and `MapLegend` | To Do | Medium | M | E9-5 |
| E10-2 | E10 | Consolidate road type label/style definitions into one typed source, building on `roadStyleEntries`/`roadStyleDifferentiator` (`utils/roadTypes.ts`, added in E9-5) as the starting point | To Do | Medium | S | E9-5 |
| E10-3 | E10 | Document the "add a new site/road type" workflow (e.g. a comment block in the new config file(s) or a short `AGENTS.md` section) now that it is a single-file change | To Do | Low | S | E10-1, E10-2 |

**E10 implementation notes:**
- Trigger: raised during E9-5 smoke testing — site type labels/icons/styles are currently spread across `utils/siteTypes.ts` (`siteTypeLabels`), `siteIcons.ts` (`siteIconMap`), and `Styles/markerStyles.ts` (`siteTypeIconUrls`, plus the underlying `Icon` constructors), so adding a new type (e.g. "watermill") means touching 3+ files and keeping their keys in sync by hand. `E9-5` already did the equivalent consolidation for road styles (`roadStyleEntries`/`roadStyleDifferentiator` in `utils/roadTypes.ts`) — `E10-2` is mostly confirming/extending that, not starting from scratch.
- `siteType` is a free-form `String` in the backend (`Site.java`/`SiteDTO.java`), not a closed enum — this is a frontend-only consolidation with no backend/schema change required to add a new type.
- Model after the `layersConfig.ts` pattern from `E9-3`: one typed array/record as the source of truth, with existing call sites (`siteTypeConverter`, `getSiteIcon`, `MapLegend`, `MapContent`'s `pointToLayer`) refactored to derive from it rather than maintaining parallel maps.

## E8 — Interactive Book / Research Narrative

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E8-1 | E8 | Add Markdown content loader infra (`react-markdown` dep, `import.meta.glob`-based chapter loader for `src/content/book/*.md`) | ✅ Done | High | S | None |
| E8-2 | E8 | Add `Figure`/caption image renderer resolving bundled QGIS map assets from `src/assets/book/` | ✅ Done | High | S | E8-1 |
| E8-3 | E8 | Add `BookChapter` page + `/book/:slug` route (lazy-loaded, matches existing `App.tsx` route conventions) | ✅ Done | High | M | E8-1 |
| E8-4 | E8 | Add `chapters.ts` manifest + `TableOfContents` sidebar/nav component | ✅ Done | Medium | M | E8-3 |
| E8-5 | E8 | Add `ChapterNav` prev/next chapter footer component | ✅ Done | Medium | S | E8-4 |
| E8-6 | E8 | Responsive CSS pass for book chapters (prose max-width via `ch` units, image scaling, breakpoints matching existing `InfoPage.css` conventions) | ✅ Done | High | S | E8-3 |
| E8-7 | E8 | Migrate first 1-2 chapters from author's existing written content into `src/content/book/` as a working smoke test | ✅ Done | High | M | E8-1, E8-2, E8-3, E8-6 |
| E8-8 | E8 | Document git-push-as-backup expectation for book content in frontend `README.md`/`AGENTS.md` | To Do | Medium | S | E8-1 |
| E8-9 | E8 | Verify/document that a `media/book/` convention (for data-linked photos, if used) is swept by the existing NAS backup sync — verification only, no new backend code | To Do | Low | S | None |
| E8-10 | E8 | **(Deferred)** Upgrade Markdown pipeline to MDX for embedded live components (`<SiteLink>`, `<InlineMap>`, `<InlinePhotoGallery>`) enabling deep links from prose into the map/data explorer | Deferred | Medium | L | E8-7 |
| E8-11 | E8 | Redesign `About` page as a lightweight project intro (photo, "how this project came about", tech stack, GitHub repo links) now that long-form research content lives in the Book | ✅ Done | Medium | S | E8-7 |
| E8-12 | E8 | Convert `News` page into a dated changelog list; publish the `rinsewillet.net` deployment announcement | ✅ Done | Low | S | None |
| E8-13 | E8 | **(Deferred)** Manuscript ingestion workflow — convert the author's existing ~20-page `.docx` manuscript into `src/content/book/*.md` chapters (e.g. via `pandoc`), with a review pass per chapter | Deferred | Medium | L | E8-7 |



## Dev Tooling (Out of priority wave — infra/developer experience)

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E7-1 | E7 | Document Cloudflare WARP remote-DB access convention (LAN-IP-only `DB_URL`) in backend `README.md` | ✅ Done | Medium | S | None |
| E7-2 | E7 | Add optional `docker-compose.local-dev.yml` throwaway PostGIS container + `local-dev` Spring profile | ✅ Done | Medium | M | None |
| E7-3 | E7 | Add `docs/architecture/sql/local-dev-seed.sql` synthetic schema/seed mirror for offline dev | ✅ Done | Medium | S | E7-2 |
| E7-4 | E7 | Document both remote/offline dev paths in `.env.example` and record decision in `ADR-010` | ✅ Done | Low | S | E7-1, E7-2 |

---

## 4) Acceptance Criteria (Per Priority Wave)

### P0 Done Criteria
- No committed plaintext secrets in compose files.
- No mixed-content map layer errors in HTTPS deployments.
- Site edit API uses consistent DTO naming and persists expected values.
- Contract tests cover core read/update routes and common error scenarios.
- Dashboard endpoint returns typed payload with stable schema.
- Dashboard frontend route is accessible and resilient (loading/error/empty states).

### P0.5 Done Criteria
- Dependency graph on `main` is fresh (submitted via JDK 21, pinned action ref).
- Zero open High/Critical/Critical-severity Dependabot alerts, or each remaining alert explicitly triaged and documented as a false positive/accepted risk.
- Recurring dependency-alert triage cadence documented.
- Gates NAS deployment: deployment should not proceed while E6 stories are open.

### P1 Done Criteria
- Media storage strategy is selected and documented with operational steps (setup, backups, permissions, URL strategy).
- Media entities can store multiple files per road/site with metadata.
- Road/Site detail pages display media galleries with attribution.
- Optional moderation flow exists for admin visibility control.

### P1.5 Done Criteria (E9 — gates P2/E3)
- Home page map shows only sites/roads on a single fixed Positron base tile: no layer-control chrome, no click-to-select/`MapInfoCard`.
- RoadInfo/SiteInfo maps keep their existing base-layer picker but no longer open `MapInfoCard` on click of other markers/roads.
- Atlas's Leaflet grouped-layer control is fully replaced by a custom, collapsible, BEM-styled left `LayerPanel` sourced from `layersConfig.ts`; only groups with real entries render (no placeholder/disabled UI for not-yet-built layers).
- Atlas displays a `MapLegend` covering all site type icons/labels and road style keys; the legend's DEM color-ramp section is wired to activate once a Physical/DEM layer exists, but stays inert until E3 adds one.
- `E3-1`/`E3-3`/`E3-5` are documented as depending on this epic's `layersConfig.ts`/`LayerPanel`/`MapLegend` output.

### P2 Done Criteria
- Raster layers are discoverable via API catalog and controllable in UI.
- DEM serving is tiled and performs acceptably at target zoom ranges.
- Mobile/tablet map interactions are practical (bottom-sheet + touch targets).
- Depends on E9: raster/DEM layers slot into the `LayerPanel`'s Physical group and `MapLegend`'s DEM hook rather than a new ad-hoc control.

### P3 Done Criteria
- Theme switch works globally and persists.
- Synthwave mode remains legible for map and text UI.
- A new site or road type (icon, label, style) can be added or changed by editing one config file/entry, with no other file requiring a matching manual edit.

### E8 Done Criteria
- Chapters are written as Markdown files under `src/content/book/`, rendered via a dynamic `/book/:slug` route.
- QGIS-generated map images render inline with captions, scale responsively, and are resolved via bundled asset imports (no manual path/hash management).
- A table of contents and prev/next chapter navigation exist so chapters read like a book.
- Book content is versioned via git (push-to-remote is the documented backup expectation); any data-linked photos routed through the media pipeline are covered by the existing NAS/DB backup services.
- MDX upgrade (live embedded components) is explicitly deferred and tracked as E8-10, not silently dropped.

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
Epic,E3,Raster / GeoTIFF Delivery,Raster / GeoTIFF Delivery,,High,,ancientdata;raster,"Serve historical maps/plans/DEM through tiled services.","Raster catalog + layer manager + DEM strategy criteria met",E0;E9
Epic,E9,Map Clarity & Layer Control Redesign,Map Clarity & Layer Control Redesign,,High,,ancientdata;frontend;map;ux,"Restrict selection/base-layer chrome to the Atlas, add a legend, and replace the Leaflet grouped-layer control with a custom collapsible side panel.","P1.5 Done Criteria met; E3 raster layers can slot into the resulting LayerPanel/MapLegend",E0
Epic,E4,Responsive UX for Field Use,Responsive UX for Field Use,,High,,ancientdata;ux;mobile,"Improve map/list usability on tablet and mobile.","Bottom-sheet and touch target criteria met",E1
Epic,E5,Synthwave Theme (Optional),Synthwave Theme (Optional),,Medium,,ancientdata;theme,"Add optional visual theme mode with persistence.","Theme toggle/persistence criteria met",E0
Epic,E7,Remote & Offline Dev Environment,Remote & Offline Dev Environment,,Medium,,ancientdata;devx,"Enable developing/smoke-testing away from the home LAN, with or without network access.","WARP path and local-dev fallback both documented and working",-
Epic,E10,Site & Road Type Registry Consolidation,Site & Road Type Registry Consolidation,,Medium,,ancientdata;frontend;map;devx,"Replace scattered site/road type label/icon/style definitions with one typed single-source-of-truth registry per type.","P3 Done Criteria (type registry bullet) met",E9
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
Story,E2-4,Add map info card cover image,,E2,Medium,2,frontend;map;media,"Show a cover thumbnail in map info card where available.","Selected feature displays thumbnail",✅ Done
Story,E2-5,Add admin media moderation,,E2,Medium,3,admin;media,"Allow approve/reject/hide media visibility.","Admin can control visibility state",E2-1
Story,~~E2-BACKUP-1~~,~~Add Google Drive backup service~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",E2-1
Story,~~E2-BACKUP-2~~,~~Add scheduled Google Drive sync task~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",~~E2-BACKUP-1~~
Story,~~E2-BACKUP-3~~,~~Add manual Google Drive sync trigger~~ (superseded — see ADR-006),,E2,Won't Do,0,backend;media;backup;superseded,"Superseded by NAS filesystem sync approach (ADR-006).","n/a",~~E2-BACKUP-1~~
Story,E2-BACKUP-NAS-1,Add NAS filesystem backup service,,E2,High,3,backend;media;backup,"NasBackupService + NasBackupConfig syncing local media dir to mounted NAS path. Enabled/disabled via backup.nas.enabled env var.","Service copies new/modified files to NAS mount; deletes orphan remote files; cron-configurable.",E2-1
Story,E2-BACKUP-NAS-2,Scheduled NAS sync,,E2,High,1,backend;media;backup,"@Scheduled cron sync driven by backup.nas.sync-cron env var (default: Sunday 03:00 UTC).","Sync runs on schedule when enabled=true.",E2-BACKUP-NAS-1
Story,E2-BACKUP-NAS-3,Manual NAS sync trigger endpoint,,E2,Medium,1,backend;media;backup;admin,"POST /api/backup/sync — ADMIN-only endpoint for on-demand backup trigger.","Admin can trigger sync via API; returns {status,message}.",E2-BACKUP-NAS-1
Story,E2-GEO-1,Photo geotagging,,E2,Medium,5,backend;frontend;media;geo,"Extract/store GPS from EXIF and allow manual placement; display geotagged photos as markers.","Geotagged photos appear on map",✅ Done
Story,E6-1,Fix stale Dependabot dependency-graph submission,,E6,Critical,2,ci;security,"Remove deprecated ci.yml (wrong JDK, unpinned actions); add correct dependency-submission job to backend-ci.yml.","Workflow submits graph on JDK 21 with pinned action ref",✅ Done
Story,E6-2,Re-verify alerts against resolved versions,,E6,Critical,2,security;dependencies,"Confirm dependency graph is fresh on main and re-check each open alert against ./gradlew dependencies output.","All alerts confirmed real or closed as stale",✅ Done
Story,E6-3,Triage jackson-databind alert,,E6,High,1,security;dependencies,"Confirm Spring Boot BOM-managed jackson-databind version is patched, or pin explicitly if not.","jackson-databind alert closed or explicitly pinned to safe version",✅ Done
Story,E6-4,Triage critical Tomcat alerts,,E6,Critical,2,security;dependencies,"Confirm tomcat-embed-core 10.1.54 (or later) resolves CVEs #15/#62/#65/#67, upgrade if not.","All 4 Tomcat alerts closed or explicitly resolved with upgrade",✅ Done
Story,E6-5,Document dependency-alert triage cadence,,E6,Medium,1,security;process;docs,"Add a recurring process/checklist for reviewing Dependabot alerts.","Cadence documented in docs (e.g. CI-CD-DECISIONS.md or this backlog)",✅ Done
Story,E3-1,Define raster publish pipeline,,E3,High,8,geoserver;raster,"Define ingestion and publishing path for historical maps/plans/DEM.","Documented and repeatable pipeline",E0-1;E9-4
Story,E3-2,Add raster layer catalog endpoint,,E3,High,3,backend;raster,"Expose available raster layers and metadata for frontend discovery.","Catalog includes bounds/zoom/attribution",E3-1
Story,E3-3,Add Physical group entries to LayerPanel,,E3,High,5,frontend;map;raster,"Add toggle/opacity/order controls for raster overlays as a Physical group in the E9 LayerPanel.","Controls apply instantly and persist session state",E3-2;E9-4
Story,E3-4,Implement large DEM serving strategy,,E3,High,8,raster;dem;performance,"Use overviews and tiling for DEM serving, avoid raw file delivery.","Acceptable performance at target zoom ranges",E3-1
Story,E3-5,Wire DEM color ramp into MapLegend,,E3,Medium,2,frontend;raster,"Feed DEM color-ramp data into the E9 MapLegend's DEM hook + attribution details.","Legend/metadata visible for active DEM layer",E3-3;E9-5
Story,E9-1,Add selectable/showLayerChrome props,,E9,High,3,frontend;map,"Thread selectable/showLayerChrome props through MapComponent -> MapBuilder -> MapContent.","Props control click-to-select and layer chrome independently",✅ Done
Story,E9-2,Disable selection on Home/RoadInfo/SiteInfo maps,,E9,High,2,frontend;map;ux,"Home map drops to a single fixed Positron tile with no chrome; all three pages disable click-to-select MapInfoCard.","No MapInfoCard opens from these pages' maps; Home shows only sites/roads on Positron",✅ Done
Story,E9-3,Extract layersConfig.ts,,E9,High,3,frontend;map,"Extract BaseLayers.tsx layer definitions into a typed, group-driven layersConfig.ts.","New layers/groups addable via config only, no component changes",✅ Done
Story,E9-4,Build custom LayerPanel sidebar,,E9,High,8,frontend;map;ux,"Fully custom, BEM-styled, collapsible left sidebar for Atlas replacing the Leaflet grouped-layer control, grouped by layersConfig.ts + sites/roads/photos.","Only groups with real entries render; per-session collapse state; Atlas map area unobstructed when collapsed",✅ Done
Story,E9-5,Build MapLegend with DEM hook,,E9,Medium,5,frontend;map;ux,"Legend covering site type icons/labels and road style keys, with a DEM color-ramp section wired to activate once a Physical/DEM layer exists.","Legend visible in Atlas; DEM section inert until a real DEM layer is added",✅ Done
Story,E9-6,Update tests and document E3 dependency gate,,E9,High,3,test;docs,"Update/add tests for Home/RoadInfo/SiteInfo/Atlas/LayerPanel/MapLegend; document E3's dependency on E9 outputs.","All new/changed behavior covered by tests; backlog reflects the dependency gate",✅ Done
Story,E4-1,Mobile bottom-sheet map details,,E4,High,5,frontend;ux;mobile,"Replace side info card with bottom-sheet on narrow viewports.","Usable and stable on target mobile breakpoints",✅ Done
Story,E4-2,Improve touch targets and spacing,,E4,High,2,frontend;ux;mobile,"Ensure controls meet touch-size guidance.","Interactive controls are comfortably tappable",✅ Done
Story,E4-3,Improve DataList mobile UX,,E4,Medium,3,frontend;ux;mobile,"Refine table/list behavior for small screens.","Readability and actions remain usable",✅ Done
Story,E4-4,Add responsive QA matrix,,E4,High,2,qa;ux;mobile,"Create repeatable responsive regression checklist.","Checklist adopted in release flow",✅ Done
Story,E5-1,Add theme switcher and tokens,,E5,Medium,2,frontend;theme,"Implement synthwave-ready theme token system and switcher.","Theme switches globally without breaking readability",E0-2
Story,E5-2,Persist selected theme,,E5,Low,1,frontend;theme,"Save and load theme preference from storage.","Preference survives reload",E5-1
Story,E5-3,Add synthwave map style profile,,E5,Medium,3,frontend;theme;map,"Tune map colors/icons for synthwave mode.","Map remains legible in synthwave",E5-1
Story,E10-1,Consolidate site type registry,,E10,Medium,5,frontend;map;devx,"Merge siteTypeLabels/siteIconMap/siteTypeIconUrls into one typed siteTypesConfig.ts consumed by MapContent/MapInfoCard/SiteInfo/MapLegend.","Adding a new site type requires editing only one file",E9-5
Story,E10-2,Consolidate road type registry,,E10,Medium,2,frontend;map;devx,"Confirm/extend roadStyleEntries/roadStyleDifferentiator (utils/roadTypes.ts) as the single source for road type labels/styles.","Adding a new road type requires editing only one file",E9-5
Story,E10-3,Document type registry workflow,,E10,Low,1,docs;devx,"Document how to add/rename/restyle a site or road type now that it is a single-file change.","Workflow documented in the config file or AGENTS.md",E10-1;E10-2
Story,E7-1,Document Cloudflare WARP remote-DB access convention,,E7,Medium,1,docs;devx,"Document LAN-IP-only DB_URL convention and WARP remote access in backend README.","README section explains WARP and never-forward-DB-port convention",✅ Done
Story,E7-2,Add local-dev throwaway PostGIS container + profile,,E7,Medium,3,devx;docker;backend,"docker-compose.local-dev.yml + application-local-dev.properties for fully offline development.","docker compose -f docker-compose.local-dev.yml up -d works; local-dev profile boots app",✅ Done
Story,E7-3,Add local-dev synthetic schema/seed script,,E7,Medium,1,devx;sql,"docs/architecture/sql/local-dev-seed.sql mirrors schema with synthetic rows, clearly marked non-authoritative.","Seed script auto-applies on container first start",✅ Done
Story,E7-4,Document remote/offline dev paths + ADR,,E7,Low,1,docs;devx,".env.example documents both paths; ADR-010 records the decision and alternatives considered.","ADR-010 Accepted; .env.example updated",✅ Done
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

**Pre-deployment gate:** Before NAS deployment and before starting E3+, complete `E6-2` through `E6-5` (Security & Dependency Hardening). See P0.5 section above.

**E3 dependency gate:** ✅ Cleared (August 2026) — `E9` (Map Clarity & Layer Control Redesign) is complete, so `E3` may now start. `E3-1`, `E3-3`, and `E3-5` build directly on the `layersConfig.ts`/`LayerPanel`/`MapLegend` outputs of E9 — see the E9 write-up in section 8 for the exact shape of those outputs (e.g. `LayerGroupName`, `LayerPanelControl`, `useActiveDemLayer`) before starting E3-3/E3-5.

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
- E2-4: `MapInfoCard` cover thumbnail — fetches approved media for the selected site/road (`MediaService.findByTarget`) and renders the `isCover` asset (falling back to the first available photo) above the feature details

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
- `AncientDataWebGIS_FE/src/components/MapComponent/MapInfoCard.tsx` (E2-4: fetches and renders cover/first photo above feature details)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapInfoCard.css` (E2-4: `.infoCard-coverImage` thumbnail styling)

**Tests:**
- `MediaGallery.test.tsx`: 16 tests (original 6 + 10 admin feature tests)
- `MediaUploadForm.test.tsx`: 6 tests (toggle, form fields, validation, submit, error states)
- `MapInfoCard.test.tsx`: added 4 tests for cover image (isCover match, fallback to first photo, no-media case, correct `targetType`/`id` passed to `MediaService.findByTarget`)
- Backend: existing MediaController + MediaService tests cover all API flows


---

### E2-GEO-1 — Photo Geotagging ✅

**Status:** Complete (July 2026)

**What was delivered:**
- Backend: nullable `latitude`/`longitude` columns added to `media_asset` (manual SQL script per externally-owned schema process, see `docs/architecture/sql/media_asset_add_geotag.sql`)
- Backend: automatic GPS EXIF extraction on upload via `com.drewnoakes:metadata-extractor`, in `ExifGpsExtractor` + `MediaService.upload`
- Backend: manual pin coordinates accepted on `POST /api/media` and `PATCH /api/media/{id}`; manual pin always takes precedence over EXIF, including on later edits
- Frontend: `PhotoLocationPicker` component (click-to-place Leaflet map) integrated into `MediaUploadForm` and the `MediaGallery` edit dialog
- Frontend: geotagged photos render as camera-pin markers in a new "Photos" map overlay on `RoadInfo`/`SiteInfo`, clicking a marker opens a popup with the photo and caption

**Impact:**
- Admins can either rely on a photo's built-in GPS data or manually pin the exact spot a photo was taken — especially useful for multi-kilometer road features where a single road/site geometry can't represent where along its length a photo was taken
- Visitors see geotagged photos as markers directly on the road/site detail map

**Files changed (backend):**
- `docs/architecture/sql/media_asset_add_geotag.sql` (new — schema change script, requires DBA/QGIS application)
- `build.gradle` (added `metadata-extractor` dependency)
- `src/main/java/com/webgis/ancientdata/utils/ExifGpsExtractor.java` (new)
- `src/main/java/com/webgis/ancientdata/domain/model/MediaAsset.java`, `domain/dto/MediaAssetDTO.java`, `domain/dto/MediaUploadRequest.java`, `domain/dto/MediaUpdateRequest.java`, `web/mapper/MediaAssetMapper.java`
- `src/main/java/com/webgis/ancientdata/application/service/MediaService.java`, `web/controller/MediaController.java`

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/components/MediaGallery/PhotoLocationPicker.tsx` (new) + `.css`
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaUploadForm.tsx`, `MediaGallery.tsx`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.tsx`, `MapBuilder.tsx`, `MapComponent.tsx`, `Styles/markerStyles.ts`, `MapContent.css`
- `AncientDataWebGIS_FE/src/pages/RoadInfo.tsx`, `SiteInfo.tsx`
- `AncientDataWebGIS_FE/src/services/MediaService.ts`, `src/types/media.ts`

**Tests:**
- Backend: `ExifGpsExtractorTests` (3 tests, using generated JPEG fixtures with/without GPS EXIF in `src/test/resources/media/`), plus new `MediaServiceTests`/`MediaControllerTests` cases for EXIF extraction, manual pin, and manual-pin-overrides-EXIF precedence
- Frontend: new `MediaUploadForm.test.tsx`/`MediaGallery.test.tsx` cases for the location picker toggle, upload with manual coordinates, `onAssetsChange` callback, and edit-form geotag updates

**Deployment note:** The schema change (`media_asset_add_geotag.sql`) must be applied to the shared PostGIS container by whoever manages the database before this feature is live in an environment (see `DB-MIGRATION-STRATEGY.md`).

---

### E6-1/E6-2/E6-3 — Dependency Graph Fix + Alert Triage ✅

**Status:** Complete (July 2026)

**What was delivered:**
- E6-1: Removed deprecated `.github/workflows/ci.yml` (JDK 17, unpinned `@master`/floating action refs); added a `dependency-submission` job to `backend-ci.yml` using JDK 21 and a pinned `gradle/actions/dependency-submission@v4` ref, running on push to `main`.
- E6-2: Confirmed the fix merged to `main` (PR #86) and the `Backend CI` workflow's `dependency-submission` job completed successfully on the merge commit (`520c0bb`), refreshing the dependency graph. Re-verified every dependency flagged in the original alert against `./gradlew dependencies` output on `main`:
  - `spring-web`/`spring-webmvc`/`spring-context` → 6.2.18 (alert flagged < 6.1.12/13/14)
  - `tomcat-embed-core` → 10.1.54 (alert flagged < 10.1.34)
  - `logback-core` → 1.5.32 (alert flagged < 1.5.13)
  - `spring-boot` → 3.5.14, `jackson-core` → 2.21.3, `json-smart` → 2.5.2, `xmlunit-core` → 2.10.4, `commons-lang3` → 3.20.0, `org.json` → 20251224 — all above safe thresholds
  - `commons-compress` and `activemq-artemis` are not present in the dependency graph at all (false positives)
- E6-3: Confirmed `jackson-databind` resolves to **2.21.2** (Spring Boot 3.5.14 BOM-managed, not a direct dependency), well above any version affected by known jackson-databind CVEs (e.g. 2.9.x/2.12.x/2.13.x-era deserialization issues). No explicit pin needed.

**Impact:**
- Dependency graph submitted to GitHub is now accurate and current, sourced from a JDK 21 build matching the project's actual toolchain.
- Confirmed all originally-flagged alerts were caused by the stale graph, not real vulnerabilities in the resolved dependency tree.

**Verification:**
- `Backend CI` run `29500726122` on commit `520c0bb`: `test-build` and `dependency-submission` jobs both `success`.
- `./gradlew test` green throughout; no `build.gradle` changes required.

**Outstanding:** None — see E6-5 below.

---

### E6-4 — Tomcat Critical CVE Triage ✅

**Status:** Complete (July 2026)

**What was found:** Cross-referenced the 4 critical Tomcat alerts against the GitHub Advisory Database (public API, no auth needed):

| Alert | CVE | GHSA | Fixed in (10.1.x line) | Status vs our 10.1.54 |
|---|---|---|---|---|
| #15 Potential RCE via partial PUT | CVE-2025-24813 | GHSA-83qj-6fr2-vhqg | 10.1.35 | ✅ Already fixed (10.1.54 > 10.1.35) |
| #62 HTTP/2 request headers not validated | CVE-2026-41293 | GHSA-r29c-68gh-xp6x | 10.1.55 | ❌ Vulnerable (10.1.54 < 10.1.55) |
| #65 Digest authenticator authenticates any unknown user | CVE-2026-43512 | GHSA-h6fc-48rj-7qqh | 10.1.55 | ❌ Vulnerable (10.1.54 < 10.1.55) |
| #67 Security constraints not correctly applied | CVE-2026-43515 | GHSA-5m62-pw8w-7w9f | 10.1.55 | ❌ Vulnerable (10.1.54 < 10.1.55) |

**What was delivered:**
- Confirmed Spring Boot **3.5.16**'s BOM manages `tomcat.version` **10.1.55**, which fixes all 3 remaining CVEs.
- Bumped `build.gradle` plugin version `org.springframework.boot` from `3.5.14` → `3.5.16` (patch-only bump, same minor line).
- Verified via `./gradlew dependencies` that `tomcat-embed-core` now resolves to `10.1.55`.
- `./gradlew test` and `./gradlew build` both green after the bump.

**Files changed:**
- `build.gradle` (Spring Boot plugin version bump only)

**Impact:** All 4 critical Tomcat alerts are now resolved on `main` once this change merges and the dependency graph refreshes.

---

### E6-5 — Dependency Alert Triage Cadence ✅

**Status:** Complete (August 2026)

**What was delivered:**
- Documented a recurring (monthly, or on-alert) triage process in `docs/ci-cd/CI-CD-DECISIONS.md` (Phase J): confirm dependency graph freshness, cross-check resolved versions via `./gradlew dependencies` against the GitHub Advisory Database, close/upgrade/false-positive each alert, and record newly-triaged Critical/High alerts.

**Impact:**
- Epic E6 is now fully complete; the dependency-alert process is no longer a one-off cleanup but a repeatable checklist for future maintainers.

**Files changed:**
- `docs/ci-cd/CI-CD-DECISIONS.md` (new Phase J section)

---

### E4 — Responsive UX for Field Use ✅

**Status:** Complete (August 2026)

**Detailed specification:** `AncientDataWebGIS/docs/features/E4-responsive-ux-field-use.md`

**What was delivered:**
- E4-1: `BottomSheetCard` component — mobile (≤600px) map info card now behaves as a proper bottom sheet (half/full snap states, tap-to-expand, swipe-down-to-dismiss); desktop/tablet behaviour unchanged.
- E4-2: Touch-target audit and fixes across `MapInfoCard` (close button, details button), the bottom-sheet drag handle, Leaflet's native zoom/layers controls (mobile only), and `DataList` pagination buttons — all now meet the ≥44×44px guideline.
- E4-3: Confirmed/closed out `DataList` mobile readability (scrollable table wrapper and responsive font/padding steps were already in place from a prior pass); pagination touch-target fix from E4-2 closes the remaining gap.
- E4-4: Documented a responsive QA breakpoint matrix and a manual regression checklist for future changes touching map/nav/list components.
- Fixed a pre-existing test-infrastructure gap: jsdom doesn't implement `PointerEvent`, which silently broke `fireEvent.pointerDown/Move/Up` coordinate propagation in tests. Added a small `PointerEvent` polyfill to `src/test/setupTests.js`.

**Impact:**
- Field researchers on phones/tablets get a native-feeling bottom-sheet interaction instead of a cramped fixed side panel.
- All interactive map/list controls are comfortably tappable on touch devices.
- Future responsive changes have a documented breakpoint matrix and checklist to test against.

**Files changed:**
- `AncientDataWebGIS_FE/src/components/MapComponent/BottomSheetCard.tsx` (new), `BottomSheetCard.css` (new), `BottomSheetCard.test.tsx` (new)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapInfoCard.tsx`, `MapInfoCard.css`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapComponent.css`
- `AncientDataWebGIS_FE/src/pages/DataList.css`
- `AncientDataWebGIS_FE/src/test/setupTests.js` (PointerEvent polyfill)
- `AncientDataWebGIS/docs/features/E4-responsive-ux-field-use.md` (new)

**Tests:**
- `BottomSheetCard.test.tsx`: 4 tests (render/handle, half↔full toggle, drag-past-threshold dismiss, small-drag no-op)
- `MapInfoCard.test.tsx`: existing 6 tests pass unchanged with the new `BottomSheetCard` wrapper
- Full frontend suite: 78/78 tests passing after these changes

---

### E7 — Remote & Offline Dev Environment ✅

**Status:** Complete (August 2026)

**Trigger:** A local smoke test away from the home LAN (office) failed with a
`SocketTimeoutException` because `.env`'s `DB_URL` had drifted to the NAS's public IP —
a port that's intentionally never forwarded externally.

**What was delivered:**
- Root-caused the failure and fixed local `.env` to use the NAS's LAN IP convention.
- Documented two supported ways to develop away from home in backend `README.md`
  ("Developing Away From Home / Office"):
  1. **Cloudflare WARP** (recommended) — reuses the Tunnel/Private Network routing
     already configured for GeoServer/QGIS access (`ancientdataworkspace/deploy/README.md`
     §8); no `.env`/code changes needed once WARP is set up.
  2. **Local throwaway PostGIS container** (fully offline fallback) — new
     `docker-compose.local-dev.yml` + `local-dev` Spring profile
     (`application-local-dev.properties`) + synthetic schema/seed mirror
     (`docs/architecture/sql/local-dev-seed.sql`), clearly marked as never a schema
     authority (per ADR-002).
- `.env.example` updated with the LAN-IP-only convention and the local-dev alternative,
  so this is discoverable without re-deriving it from a stack trace next time.
- Recorded the decision, alternatives considered (router port-forward, Flyway-managed
  local schema, WireGuard), and consequences in `docs/architecture/adr/ADR-010-remote-offline-dev-environment.md`.
- Hardened `.gitignore` (`.env.*` with `!.env.example`) after finding stray
  `.env.bak`/`.env.local-dev-test` files with a real JWT secret sitting untracked in
  the working tree.

**Impact:**
- Development and smoke-testing can continue away from home, either against real data
  (via WARP) or fully offline (via the local-dev container) — no more silent `.env`
  drift toward an always-unreachable public IP.
- This is infrastructure/developer-experience work, not a NAS-wide concern: the shared
  WARP Tunnel setup already lived in `ancientdataworkspace/deploy/README.md` and needed
  no duplication; only the app-specific offline fallback belonged in this repo.

**Files changed:**
- `README.md` ("Developing Away From Home / Office" section)
- `.env.example` (LAN-IP convention + local-dev alternative)
- `.gitignore` (`.env.*` pattern, keeping `.env.example` tracked)
- `docker-compose.local-dev.yml` (new)
- `src/main/resources/application-local-dev.properties` (new)
- `docs/architecture/sql/local-dev-seed.sql` (new)
- `docs/architecture/adr/ADR-010-remote-offline-dev-environment.md` (new)

---

### E8 — Interactive Book / Research Narrative 🚧 (In Progress)

**Status:** Core reading experience delivered (August 2026); E8-8, E8-9 (docs/verification) still open; E8-10, E8-13 explicitly deferred.

**Detailed specification:** `AncientDataWebGIS/docs/features/E8-interactive-book.md`
**Decision record:** `AncientDataWebGIS/docs/architecture/adr/ADR-011-book-content-storage-and-backup.md`

**What was delivered:**
- E8-1: `react-markdown` dependency + `import.meta.glob`-based Markdown chapter loader (`src/content/book/*.md`)
- E8-2: `MarkdownImage` component — resolves bundled QGIS/illustration assets by filename, renders `<figure>`+`<figcaption>` from the Markdown title syntax
- E8-3: `BookChapter` page + `/book/:slug` lazy-loaded route; "Research" nav link added to `NavbarHook`
- E8-4/E8-5: `chapters.ts` manifest + `TableOfContents` sidebar + `ChapterNav` prev/next footer
- E8-6: Responsive CSS pass (`Book.css`) — `70ch` prose max-width, scaling images, breakpoints matching `InfoPage.css` conventions
- E8-7: First two chapters migrated/drafted (`01-introduction.md` — the former `About.tsx` narrative content — and a `02-prehistoric-roads.md` placeholder demonstrating the image-caption syntax)
- E8-11: `About` page redesigned as a lightweight project intro (photo, "how this project came about", tech stack, GitHub repo links to both `AncientDataWebGIS_FE` and `AncientDataWebGIS`), now that the long-form research narrative lives in the Book
- E8-12: `News` page converted to a dated changelog list; published the `rinsewillet.net` deployment announcement as the newest entry

**Impact:**
- Long-form research content has a proper home with chapter navigation and captioned images, separate from the app's short informational pages.
- `About` and `News` are now focused, maintainable pages instead of a hard-coded wall of text.

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/content/book/chapters.ts`, `01-introduction.md`, `02-prehistoric-roads.md` (new)
- `AncientDataWebGIS_FE/src/components/Book/MarkdownImage.tsx`, `TableOfContents.tsx`, `ChapterNav.tsx`, `Book.css` (new)
- `AncientDataWebGIS_FE/src/pages/BookChapter.tsx` (new)
- `AncientDataWebGIS_FE/src/pages/About.tsx`, `About.css` (new) — redesigned
- `AncientDataWebGIS_FE/src/pages/News.tsx`, `News.css` (new) — redesigned as changelog
- `AncientDataWebGIS_FE/src/App.tsx` (`/book/:slug` route), `src/components/NavBarHook/NavbarHook.tsx` ("Research" link)

**Outstanding:**
- E8-8: document the git-push-as-backup expectation in `README.md`/`AGENTS.md`.
- E8-9: verify/document NAS backup coverage for any future `media/book/` data-linked photos.
- E8-10 (deferred): MDX upgrade for embedded live components.
- E8-13 (deferred): ingest the author's ~20-page `.docx` manuscript into further chapters (conversion workflow not yet defined — candidate approach: `pandoc manuscript.docx -o chapter.md`, followed by a manual per-chapter review/cleanup pass and image extraction).

---

### E9 — Map Clarity & Layer Control Redesign ✅

**Status:** Complete (August 2026)

**What was delivered:**
- E9-1: `selectable`/`showLayerChrome` props threaded through `MapComponent` → `MapBuilder` → `MapContent`. `selectable=false` skips attaching the `click` handler on site/road `GeoJSON` layers (so `MapInfoCard` can never open); `showLayerChrome=false` swaps `<BaseLayers />`/`<LayersControl>` for a single fixed `<TileLayer>` (Positron) plus unwrapped, always-rendered `<GeoJSON>` for sites/roads/photos.
- E9-2: `Home.tsx` passes `selectable={false} showLayerChrome={false}`; `RoadInfo.tsx`/`SiteInfo.tsx` pass `selectable={false}` only; `Atlas.jsx` left both at their `true` default.
- E9-3: Extracted `layersConfig.ts` — one typed, group-driven array (`'Topographical' | 'Aerial Imagery' | 'Historical Maps'`) that `BaseLayers.tsx` now derives its base/overlay layers from, instead of duplicating the definitions.
- E9-4: Built `LayerPanel` (`src/components/LayerPanel/`) — a fully custom, BEM-styled, collapsible left sidebar for Atlas replacing the Leaflet grouped-layer control there (RoadInfo/SiteInfo keep the original `BaseLayers`/`leaflet-groupedlayercontrol` control — not removed, still in use). Sections render purely from `layersConfig.ts` groups plus a sites/roads/photos overlay group; a group with zero entries doesn't render at all. Backed by a new `useLayerPanelControl` hook (`useMapInteractions.ts`) that owns the active base/historical/aerial layer selection and drives the real Leaflet layers.
- E9-5: Built `MapLegend` (`src/components/MapLegend/`) — site type icons/labels (from `markerStyles.ts`/`siteTypes.ts`) and road style samples (extracted into a new shared `utils/roadTypes.ts`, also now used by `MapContent`'s own road styling so the two can't drift). Added `useActiveDemLayer` — an intentional stub returning `null` until a real DEM/Physical layer exists in `layersConfig.ts`; the Elevation section stays unrendered until then (no fabricated placeholder content).
- E9-6: Added `MapContent.test.tsx` (real, unmocked `<MapContainer>` rendering — the acceptance criteria live in `MapContent`, not the pages, so this is the highest-fidelity place to verify `selectable`/`showLayerChrome`/`layerPanel` actually work) plus prop-wiring assertions in `Home.test.tsx`/`RoadInfo.test.tsx`/`SiteInfo.test.tsx`/`Atlas.test.tsx` (which keep mocking `MapComponent`, so they verify each page passes the right flags rather than re-testing Leaflet behavior four times). `LayerPanel.test.tsx`/`MapLegend.test.tsx` were written alongside E9-4/E9-5. Documented the E3 dependency gate as cleared (see section 7).

**UX iteration during smoke testing (post-implementation, pre-E9-6):**
- `MapLegend` originally used `position: fixed` at the Atlas-page level, which landed below the map over the footer (`.pagebox` doesn't fill the full viewport). Moved inside `MapContent` (like `LayerPanel`/`MapInfoCard`), `position: absolute` relative to the map container.
- `LayerPanel`/`MapLegend` initially used `z-index: var(--z-fixed)` (100 in `App.css`) — lower than several of Leaflet's own internal panes (tile pane 200, marker pane 600, popup pane 700), so tiles painted over them during pan/zoom. Fixed to `z-index: 999`, matching `MapInfoCard`'s existing `.infoCard` convention.
- `LayerPanel` reworked from a floating top-left card into a sidebar docked flush to the map's left edge, with a slim vertical tab (`writing-mode: vertical-rl`) when collapsed.
- `MapLegend` docked to the right edge (mirroring `LayerPanel`'s left dock) and now fully unmounts (renders nothing, not even its collapsed tab) whenever a site/road is selected — `MapInfoCard` spans nearly the full right edge when open, so there's no free spot to relocate a tab to; it reappears (still collapsed) once the selection clears.

**Also found, not fixed here (out of scope for this epic):**
- A pre-existing schema drift in `AncientDataWebGIS`'s `docs/architecture/sql/local-dev-seed.sql`: it never creates the `ancientrefs` table that the `AncientReference` JPA entity now requires, so `./gradlew bootRun --profile=local-dev` fails against a freshly-seeded local-dev container. Blocked full live-browser E2E verification of this epic in the FE repo; verified instead via real (unmocked) `@testing-library/react` renders of `MapContent`/`LayerPanel`/`MapLegend`, plus manual smoke testing by the repo owner.
- Site/road type definitions (labels, icons, styles) remain spread across `utils/siteTypes.ts`, `siteIcons.ts`, `Styles/markerStyles.ts`, and now `utils/roadTypes.ts` — tracked as new epic **E10** (Site & Road Type Registry Consolidation, P3 - Optional).

**Impact:**
- Home/RoadInfo/SiteInfo maps can no longer accidentally open `MapInfoCard`; Home's preview map is now a lightweight, chrome-free Positron tile.
- Atlas has a custom, BEM-styled `LayerPanel` and `MapLegend` instead of the bulky `leaflet-groupedlayercontrol` widget plus no legend at all.
- `E3` (Raster / GeoTIFF Delivery) is unblocked: `E3-1`/`E3-3`/`E3-5` have a concrete `layersConfig.ts`/`LayerPanel`/`MapLegend` shape to build against.

**Files changed:**
- `AncientDataWebGIS_FE/src/components/MapComponent/MapComponent.tsx`, `MapBuilder.tsx`, `MapContent.tsx`, `MapContent.css`, `BaseLayers.tsx`, `layersConfig.ts` (new), `mapUtils.ts`, `useMapInteractions.ts`, `Styles/markerStyles.ts`
- `AncientDataWebGIS_FE/src/components/LayerPanel/LayerPanel.tsx` (new), `LayerPanel.css` (new), `LayerPanel.test.tsx` (new)
- `AncientDataWebGIS_FE/src/components/MapLegend/MapLegend.tsx` (new), `MapLegend.css` (new), `MapLegend.test.tsx` (new), `useActiveDemLayer.ts` (new)
- `AncientDataWebGIS_FE/src/utils/siteTypes.ts`, `roadTypes.ts` (new)
- `AncientDataWebGIS_FE/src/pages/Home.tsx`, `RoadInfo.tsx`, `SiteInfo.tsx`, `Atlas.jsx`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.test.tsx` (new), plus test updates to `Home.test.tsx`, `RoadInfo.test.tsx`, `SiteInfo.test.tsx`, `Atlas.test.tsx`

**Tests:**
- `MapContent.test.tsx` (new, 5 tests): real `<MapContainer>` rendering — `selectable=false` never calls `setShowInfoCard`/`setSearchItem`; `selectable=true` (default) does; `showLayerChrome=false` renders exactly one fixed Positron tile and no `.leaflet-control-layers`; `showLayerChrome=true` (default) renders the grouped control chrome; `layerPanel=true` renders `.layer-panel` and no Leaflet control chrome.
- `LayerPanel.test.tsx` (7 tests): section rendering from real `layersConfig` data, Photos row hidden without markers, correct callback args for base/historical/aerial/overlay rows, whole-panel and per-section collapse.
- `MapLegend.test.tsx` (8 tests): every site type/road style row renders with its label, DEM section hidden by default and shown once `useActiveDemLayer` reports an active layer, collapse/expand toggle, hides entirely while `hasSelection` is true and reappears collapsed once it clears.
- `Home.test.tsx`/`RoadInfo.test.tsx`/`SiteInfo.test.tsx`/`Atlas.test.tsx`: extended their existing `MapComponent` mocks to assert the `selectable`/`showLayerChrome`/`layerPanel` props each page actually passes.
- Full frontend suite: 105/105 tests passing.


