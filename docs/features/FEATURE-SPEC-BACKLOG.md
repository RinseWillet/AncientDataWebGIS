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
| E3 | Raster / GeoTIFF Delivery | 🚧 In Progress | Publish and consume large rasters via tile services (depends on E9) |
| E4 | Responsive UX for Field Use | ✅ Done | Improve mobile/tablet workflows on map and list views |
| E5 | Synthwave Theme (Optional) | To Do | Add alternate visual theme with persistent preference |
| E7 | Remote & Offline Dev Environment | ✅ Done | Enable developing/smoke-testing away from the home LAN, with or without network access |
| E8 | Interactive Book / Research Narrative | 🚧 In Progress | Publish long-form research narrative chapters (Markdown, with embedded QGIS-generated images) alongside the data explorer |
| E10 | Site & Road Type Registry Consolidation | ✅ Done | Replace the scattered site/road type label, icon, and style definitions with one typed, single-source-of-truth registry, so adding/renaming/restyling a type (e.g. a new "watermill" site type) is a single-file change |
| E11 | OAuth2/OIDC Migration | To Do | Replace the custom username/password + JWT auth flow with a self-hosted Keycloak IdP, converting the backend into an OAuth2 Resource Server and the frontend to Authorization Code + PKCE |
| E12 | k3s Migration | To Do | Migrate the NAS deployment from Docker Compose to a single-node k3s cluster, with a validated rollback path to Compose (depends on E11 being stable first) |
| E13 | NAS Infra Resilience & Incident Follow-up | To Do | Harden the NAS deployment against a repeat of the 2026-08-20 host-wide outage (Docker-daemon-level failure under memory pressure while loading a large DEM via GeoServer/WMS), and close the raster-pipeline documentation gap it exposed |
| E14 | Backend & Frontend Road/Site Duplication Hardening | To Do | Reduce Road/Site duplication and layering violations surfaced by the 2026-08-25 readability/maintainability audit — backend: HTTP exceptions leaking into `application/service`, duplicated CRUD/exception-translation logic across `RoadService`/`SiteService`, JPA-unsafe Lombok `@Data` entities; frontend (`AncientDataWebGIS_FE`): near-duplicate `RoadInfo`/`SiteInfo` pages, two different Redux Toolkit patterns for the same "fetch by id" operation, an oversized multi-concern `useMapInteractions.ts` — no change to external API behavior or, beyond E14-7, to user-visible UI behavior |

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

## P0.6 - NAS Infra Resilience (Post-Incident, 2026-08-20)

Follow-up from the 2026-08-20 host-wide outage: loading a large DEM WMS layer
triggered enough host memory pressure to make the Synology Docker daemon
itself hang and get replaced, which stopped every container in all three
compose stacks at once (confirmed via `dmesg`/cgroup/daemon-log inspection to
be a daemon-level failure, not a per-container kernel OOM-kill). `mem_limit`
and `restart: unless-stopped` were already applied to every service across
all three stacks as the immediate fix; these stories are the remaining
follow-up work.

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E13-1 | E13 | Rotate PostGIS root password and pgAdmin admin credentials — currently plaintext in the live NAS `compose.yaml` and exposed during this incident's investigation session | To Do | Critical | S | None |
| E13-2 | E13 | Migrate `/volume1/docker/ancientdata/postgis_admin/compose.yaml` to the `${VAR}`-from-`.env` credential pattern already used by `docs/ci-cd/docker-infra-compose.yml`, and `chmod 600` the file (currently world-readable/writable) | To Do | High | S | E13-1 |
| E13-3 | E13 | Investigate enabling Docker daemon `live-restore` on Synology Container Manager, so a future daemon restart reattaches to already-running containers instead of stopping every container across all three compose stacks at once (`dockerd.json` was not found at its expected path during investigation — may only be settable via the Container Manager GUI) | To Do | High | M | None |
| E13-4 | E13 | Replace `RasterProxyService.forward()`'s `response.getBody().readAllBytes()` full-buffering with a streaming proxy response, so a large upstream GeoServer/WMS response can't fully load into `ancientdata`'s JVM heap | To Do | High | M | None |
| E13-5 | E13 | Write `ADR-012-raster-publishing-pipeline.md`, covering the DEM/COG delivery decision (E3-1/E3-4) and the 2026-08-20 outage postmortem — root cause was a Docker-daemon-level failure under host memory pressure, which stopped every container in every stack at once because none had `mem_limit` or a restart policy that survives a clean exit | To Do | Medium | S | E13-3, E13-4 |

## P1.5 - Map Clarity & Layer Redesign (✅ Done — unblocked E3)

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

## P1.6 - Backend & Frontend Road/Site Duplication Hardening (Code Quality Audit, 2026-08-25)

Follow-up from a readability/maintainability audit of both `AncientDataWebGIS` (backend)
and `AncientDataWebGIS_FE` (frontend) against SOLID/Clean Code practices (no incident, no
user-facing bug driving this — pure technical debt). Full findings live in the assistant
conversation that produced this section.

**Backend headline issues:** `RoadService`/`SiteService` throw Spring's
`ResponseStatusException` directly, coupling the `application/service` layer to HTTP
semantics and making business logic unusable outside a web context; `save`/`update`
in both services duplicate the same try/catch/exception-translation shape and a
`getModernReferenceDTOList` helper verbatim; and JPA entities (`Road`, `Site`, etc.)
use Lombok `@Data`, which generates `equals`/`hashCode` over all fields including
relations — a known JPA foot-gun for lazy-loading/collection correctness. A handful
of smaller, low-risk, single-file backend issues from the same audit were fixed directly
without a story (see commit history around 2026-08-25) rather than tracked here.

**Frontend headline issues (same root cause: `Road` and `Site` built as two hand-written
parallel tracks that drifted):** `pages/RoadInfo.tsx` and `pages/SiteInfo.tsx` are ~90%
structurally identical (edit-form state, `isEditing` toggle, save/cancel wiring, Map/
MediaGallery/ModernReferencePicker layout); `roadSlice`/`roadThunks` use RTK's modern
`createAsyncThunk` + `extraReducers` for "fetch by id" while `siteSlice`/`siteThunks` use
the older manual-thunk + hand-written action-creators pattern for the identical operation;
and `useMapInteractions.ts` (582 lines) bundles three largely independent concerns
(marker highlighting, auto-zoom-on-navigation, and the Physical/Historical-Maps layer-panel
state machine) in one file. A `getErrorMessage` helper duplicated across 4 files and a
repeated page-wrapper/Tooltip-formatter pattern in `Dashboard.tsx` were fixed directly
without a story for the same reason as the backend's small fixes.

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E14-1 | E14 | Introduce domain-level exceptions (e.g. `NotFoundException`, `ConflictException`, `InvalidRequestException`) in `application/service`, translated to HTTP status only in `GlobalExceptionHandler`; remove `ResponseStatusException` usage from every service class | To Do | Medium | L | None |
| E14-2 | E14 | Deduplicate `RoadService`/`SiteService` `save`/`update` field-mapping and exception-translation logic (shared helper), remove the duplicated `getModernReferenceDTOList`, and move `RoadService.update()`'s inline "short refs" string-building out of the service | To Do | Medium | M | E14-1 |
| E14-3 | E14 | Audit Lombok `@Data` usage on JPA entities (`Road`, `Site`, `MediaAsset`, etc.) and replace with explicit id-based `equals`/`hashCode` (e.g. `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on the `id` field) to avoid proxy/lazy-loading/collection correctness issues | To Do | Medium | M | None |
| E14-4 | E14 | Replace `RoadService.getDashBoardData()`'s in-memory full-table load-and-tally with `RoadRepository.countByTypeRaw()` (already used elsewhere for dashboard aggregation, e.g. E1-2), keeping the returned `RoadDashboardDTO` shape identical | To Do | Low | S | None |
| E14-5 | E14 | (`AncientDataWebGIS_FE`) Extract a shared entity-info-page layout/hook consolidating `RoadInfo.tsx`/`SiteInfo.tsx` (edit-form state + `isEditing` toggle + save/cancel wiring + Map/MediaGallery/ModernReferencePicker layout), parameterized per entity type | To Do | Medium | L | None |
| E14-6 | E14 | (`AncientDataWebGIS_FE`) Unify `roadSlice`/`roadThunks` and `siteSlice`/`siteThunks` on one Redux Toolkit pattern (`createAsyncThunk` + `extraReducers` for "fetch by id"), retiring the manual action-creator style currently used for sites | To Do | Medium | M | None |
| E14-7 | E14 | (`AncientDataWebGIS_FE`) Replace the blocking `alert()` save-feedback in `RoadInfo`/`SiteInfo` with an in-app inline banner/toast consistent with the existing loading/error UX elsewhere (e.g. `Dashboard.tsx`) | To Do | Medium | S | None |
| E14-8 | E14 | (`AncientDataWebGIS_FE`) Split `useMapInteractions.ts` into per-concern modules (`useMarkerHighlight`, `useAutoZoom`, `useLayerPanelControl` + its gating helpers), updating consumer imports (`MapContent.tsx`, `LayerPanel.tsx`) and the corresponding test file(s) | To Do | Low | M | None |

**E14 Done Criteria:**
- No class under `application/service` imports `org.springframework.web.server.ResponseStatusException`; all business-error paths raise a domain exception mapped once in `GlobalExceptionHandler`.
- `RoadService`/`SiteService` no longer contain duplicated save/update/exception-translation or `getModernReferenceDTOList` code.
- Every `@Entity` class's `equals`/`hashCode` strategy is deliberate (id-only) rather than Lombok's all-fields default.
- `RoadDashboardDTO` values from `GET /api/dashboard/...` are unchanged before/after E14-4 (covered by existing dashboard tests).
- `./gradlew test` stays green throughout; ADR written for the new domain-exception pattern per `AGENTS.md`'s "establishing new patterns" trigger (next free number under `docs/architecture/adr/` at implementation time — `ADR-013` is taken and `ADR-014` is provisionally claimed by `E12-6`, so confirm before naming the file).
- `RoadInfo`/`SiteInfo` share one layout/hook with no behavior change to editing/saving/viewing either entity type.
- `roadSlice`/`siteSlice` (and their thunks) follow one shared Redux Toolkit pattern for equivalent operations.
- `useMapInteractions.ts` no longer exists as a single 500+ line file; each extracted module has a single clear concern.
- `npm run test:run`, `npm run lint` (`--max-warnings 0`), and `npm run build` stay green throughout.

## P2 - Then

| Story ID | Epic | Story | Status  | Priority | Size | Dependencies |
|---|---|---|---------|---|---|---|
| E3-1 | E3 | Define raster publishing pipeline (GeoTIFF -> tiled service) | ✅ Done | High | L | E0-1, E9-4 |
| E3-2 | E3 | Add raster layer catalog endpoint (name/source/bounds/zoom/attribution) | ✅ Done | High | M | E3-1 |
| E3-3 | E3 | Add "Physical" group entries (toggle/opacity/order) to the `LayerPanel` from E9 | ✅ Done | High | M | E3-2, E9-4 |
| E3-4 | E3 | Implement DEM delivery strategy for ~80GB source (overviews/tiling) | ✅ Done | High | L | E3-1 |
| E3-5 | E3 | Add DEM color-ramp data to `MapLegend`'s DEM hook (from E9-5) + metadata drawer | ✅ Done | Medium | S | E3-3, E9-5 |
| E3-6 | E3 | DB-backed raster catalog (replacing E3-2's static Java list, 30 entries) with admin-only CRUD endpoints (no admin UI — see write-up) | ✅ Done | Low | L | E3-2 |
| E3-7 | E3 | Gate Physical-layer selectability in `LayerPanel` by current map viewport: disable a raster layer's toggle unless its `bounds` (already in `RasterLayerDTO`/`PhysicalLayerState`, unused for gating today) intersects the visible map extent, and disable the whole Physical group below a global minimum zoom floor — so a fully zoomed-out user can't enable every published layer at once and overload GeoServer/the NAS | ✅ Done | High | M | E3-3 |
| E3-8 | E3 | Extend E3-7's viewport-gating to Historical Maps sheets, but per-entry rather than a shared floor: a sheet is selectable only when zoom ≥ that entry's own curated `RasterZoomDTO.min` (already in `RasterLayerDTO`, unused for gating today, same situation `bounds` was in before E3-7) AND its bounds intersect the viewport — needed because sheet scale varies wildly (city-scale historical topo sheets vs. much larger-scale upcoming cadastral maps), so a single global zoom floor can't filter a small in-viewport sheet out at a wide zoom the way it could for Physical/DEM | ✅ Done | High | M | E3-7 |
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
| E10-1 | E10 | Consolidate site type label/icon definitions (currently split across `utils/siteTypes.ts`, `siteIcons.ts`, `Styles/markerStyles.ts`, and `Dashboard.tsx`'s own `LABEL_MAPPING`) into one typed `siteTypesConfig.ts`, sourced by `MapContent`, `MapInfoCard`, `SiteInfo`, `MapLegend`, and `Dashboard`; fixes `ptum` incorrectly rendering with the confirmed-tumulus icon instead of `possibleTumulusIcon` | ✅ Done | Medium | M | E9-5 |
| E10-2 | E10 | Consolidate road type label/style definitions into one typed source, building on `roadStyleEntries`/`roadStyleDifferentiator` (`utils/roadTypes.ts`, added in E9-5) as the starting point | ✅ Done | Medium | S | E9-5 |
| E10-3 | E10 | Document the "add a new site/road type" workflow (e.g. a comment block in the new config file(s) or a short `AGENTS.md` section) now that it is a single-file change | ✅ Done | Low | S | E10-1, E10-2 |

**E10 implementation notes:**
- Trigger: raised during E9-5 smoke testing — site type labels/icons/styles are currently spread across `utils/siteTypes.ts` (`siteTypeLabels`), `siteIcons.ts` (`siteIconMap`), and `Styles/markerStyles.ts` (`siteTypeIconUrls`, plus the underlying `Icon` constructors), so adding a new type (e.g. "watermill") means touching 3+ files and keeping their keys in sync by hand. `E9-5` already did the equivalent consolidation for road styles (`roadStyleEntries`/`roadStyleDifferentiator` in `utils/roadTypes.ts`) — `E10-2` is mostly confirming/extending that, not starting from scratch.
- `siteType` is a free-form `String` in the backend (`Site.java`/`SiteDTO.java`), not a closed enum — this is a frontend-only consolidation with no backend/schema change required to add a new type.
- Model after the `layersConfig.ts` pattern from `E9-3`: one typed array/record as the source of truth, with existing call sites (`siteTypeConverter`, `getSiteIcon`, `MapLegend`, `MapContent`'s `pointToLayer`) refactored to derive from it rather than maintaining parallel maps.
- Bug to fix as part of E10-1 (found during pre-work analysis, not a new regression to chase separately): `siteIconMap` (`siteIcons.ts`) and `siteTypeIconUrls` (`markerStyles.ts`) both map `ptum` ("possible barrow") to `tumulusIcon`/`tumulus.png` instead of the already-built-but-unused `possibleTumulusIcon`/`ptumulus.png`, so possible-tumulus sites currently render with the confirmed-tumulus marker on both the map and in `MapLegend`. Every other confirmed/possible pair (`castellum`/`pos_castellum`, `villa`/`pvilla`, `ship`/`pship`) already has a visually distinct icon — only tumulus regressed. Labels are unaffected (`siteTypeLabels` and `Dashboard.tsx`'s `LABEL_MAPPING` already distinguish `tum`/`ptum` correctly); this is an icon-wiring fix only, not a data-model or dating/certainty change.
- `Dashboard.tsx` maintains a fifth, independent label source (`LABEL_MAPPING`, used for chart labels) that duplicates `siteTypeLabels` with different wording for the same keys. Fold it into `siteTypesConfig.ts` as the label source — `Dashboard` only needs text, not icons — so the registry is truly single-source, per the epic outcome.

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

## E11 — OAuth2/OIDC Migration

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E11-1 | E11 | Add Keycloak + a dedicated Postgres DB (separate from the shared PostGIS instance) as new internal-only services in `ancientdataworkspace/deploy` (`webgis-edge` network) | To Do | High | M | None |
| E11-2 | E11 | Replace JWT issuance/validation in `SecurityConfig`/`JwtFilter`/`JwtUtil` with `oauth2ResourceServer().jwt()` validating Keycloak-issued tokens; retire the classes it replaces | To Do | High | L | E11-1 |
| E11-3 | E11 | Map Keycloak realm roles to the existing `ADMIN`/`USER` roles used in `SecurityConfig`'s `requestMatchers`; update or retire `CustomUserDetailService` | To Do | High | M | E11-2 |
| E11-4 | E11 | Replace the frontend login form + `authStorage.ts` manual token handling with Authorization Code + PKCE (`react-oidc-context` or `oidc-client-ts`) | To Do | High | M | E11-2, E11-3 |
| E11-5 | E11 | Write `ADR-013-oauth2-oidc-migration.md` (decision, backward-compat/rollback plan, existing-user migration) | To Do | Medium | S | E11-1 |

## E12 — k3s Migration

| Story ID | Epic | Story | Status | Priority | Size | Dependencies |
|---|---|---|---|---|---|---|
| E12-1 | E12 | Install k3s on the Synology NAS (single-node), alongside the existing Compose stacks; verify resource headroom before cutting anything over | To Do | High | M | E11 (stable) |
| E12-2 | E12 | Write k8s manifests/Helm chart for `ancientdata`, `geoserver`, and the edge stack (nginx + cloudflared), mirroring current `docker-compose.yml` volumes/networking | To Do | High | L | E12-1 |
| E12-3 | E12 | Move `.env` values into `Secret`/`ConfigMap` resources; keep PostGIS/media persistence identical via NAS-backed volumes | To Do | High | M | E12-2 |
| E12-4 | E12 | Run the k3s stack in parallel with Compose, validate end-to-end (including the E11 OAuth2 flow), then cut Cloudflare Tunnel routing over | To Do | High | M | E12-3, E11-4 |
| E12-5 | E12 | Document the new deployment path and rollback-to-Compose steps in `ancientdataworkspace/docs/deployment-recovery.md` | To Do | Medium | S | E12-4 |
| E12-6 | E12 | Write `ADR-014-k3s-migration.md` | To Do | Medium | S | E12-1 |

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

### P1.5 Done Criteria (E9 — ✅ met, gate cleared for P2/E3)
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
- A new site or road type (icon, label, style) can be added or changed by editing one config file/entry, with no other file requiring a matching manual edit. ✅ met (E10)

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
Epic,E11,OAuth2/OIDC Migration,OAuth2/OIDC Migration,,High,,ancientdata;security;auth,"Replace custom username/password + JWT auth with a self-hosted Keycloak IdP; backend becomes an OAuth2 Resource Server, frontend uses Authorization Code + PKCE.","Backend validates Keycloak-issued tokens; frontend login uses PKCE flow; ADR-013 written",-
Epic,E12,k3s Migration,k3s Migration,,High,,ancientdata;devops;kubernetes,"Migrate the NAS deployment from Docker Compose to a single-node k3s cluster with a validated rollback path.","App runs on k3s with parity to Compose; rollback documented and tested; ADR-014 written",E11
Epic,E13,NAS Infra Resilience & Incident Follow-up,NAS Infra Resilience & Incident Follow-up,,High,,ancientdata;infra;security;incident,"Harden the NAS deployment against a repeat of the 2026-08-20 host-wide outage and close the raster-pipeline documentation gap it exposed.","Credentials rotated/externalized; live-restore evaluated; RasterProxyService streams instead of buffering; ADR-012 written",-
Epic,E14,Backend & Frontend Road/Site Duplication Hardening,Backend & Frontend Road/Site Duplication Hardening,,Medium,,ancientdata;backend;frontend;refactor;code-quality,"Reduce Road/Site duplication and layering violations across both repos (backend: HTTP exceptions in application/service, duplicated CRUD/exception-translation logic, JPA-unsafe Lombok @Data entities; frontend: near-duplicate RoadInfo/SiteInfo pages, inconsistent Redux Toolkit patterns, oversized useMapInteractions.ts) surfaced by a 2026-08-25 readability/maintainability audit.","No change to external API behavior or (beyond E14-7) user-visible UI behavior; ./gradlew test and npm run test:run/lint/build stay green; ADR written for the domain-exception pattern",-
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
Story,E3-4,Implement large DEM serving strategy,,E3,High,8,raster;dem;performance,"Use overviews and tiling for DEM serving, avoid raw file delivery.","Acceptable performance at target zoom ranges",✅ Done
Story,E3-5,Wire DEM color ramp into MapLegend,,E3,Medium,2,frontend;raster,"Feed DEM color-ramp data into the E9 MapLegend's DEM hook + attribution details.","Legend/metadata visible for active DEM layer",✅ Done
Story,E3-6,DB-backed raster catalog with admin CRUD endpoints,,E3,Low,8,backend;raster,"Replace E3-2's static Java catalog list (30 entries) with a raster_layer DB table + admin-only CRUD endpoints (no admin UI this pass).","✅ Done — public GET /api/raster/catalog contract unchanged",E3-2
Story,E3-7,Gate Physical layer selectability by map viewport,,E3,High,5,frontend;raster;performance,"Disable a raster layer's toggle in the Physical group unless its bounds intersect the current map view, and disable the whole group below a minimum zoom floor, so a zoomed-out user can't enable every layer and overload GeoServer/the NAS.","Out-of-view or below-floor layers are disabled with an explanatory hint; enabling one is blocked",E3-3
Story,E3-8,Gate Historical Maps sheet selectability by viewport + per-layer scale,,E3,High,5,frontend;raster;performance,"Extend E3-7's viewport-gating to the Historical Maps sheet list, using each entry's own curated RasterZoomDTO.min as a per-layer zoom floor (not a shared constant), since sheet scale varies wildly between historical topo sheets and much larger-scale cadastral maps.","A sheet is selectable only when zoom >= its own zoom.min AND bounds intersect the viewport; non-selectable sheets are hidden, not disabled; an empty collection is hidden entirely",E3-7
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
Story,E10-1,Consolidate site type registry,,E10,Medium,5,frontend;map;devx,"Merge siteTypeLabels/siteIconMap/siteTypeIconUrls/Dashboard LABEL_MAPPING into one typed siteTypesConfig.ts consumed by MapContent/MapInfoCard/SiteInfo/MapLegend/Dashboard; fix ptum rendering with the confirmed-tumulus icon instead of possibleTumulusIcon.","Adding a new site type requires editing only one file; possible-tumulus sites render with a distinct icon from confirmed tumuli",✅ Done
Story,E10-2,Consolidate road type registry,,E10,Medium,2,frontend;map;devx,"Confirm/extend roadStyleEntries/roadStyleDifferentiator (utils/roadTypes.ts) as the single source for road type labels/styles.","Adding a new road type requires editing only one file",✅ Done
Story,E10-3,Document type registry workflow,,E10,Low,1,docs;devx,"Document how to add/rename/restyle a site or road type now that it is a single-file change.","Workflow documented in the config file or AGENTS.md",✅ Done
Story,E7-1,Document Cloudflare WARP remote-DB access convention,,E7,Medium,1,docs;devx,"Document LAN-IP-only DB_URL convention and WARP remote access in backend README.","README section explains WARP and never-forward-DB-port convention",✅ Done
Story,E7-2,Add local-dev throwaway PostGIS container + profile,,E7,Medium,3,devx;docker;backend,"docker-compose.local-dev.yml + application-local-dev.properties for fully offline development.","docker compose -f docker-compose.local-dev.yml up -d works; local-dev profile boots app",✅ Done
Story,E7-3,Add local-dev synthetic schema/seed script,,E7,Medium,1,devx;sql,"docs/architecture/sql/local-dev-seed.sql mirrors schema with synthetic rows, clearly marked non-authoritative.","Seed script auto-applies on container first start",✅ Done
Story,E7-4,Document remote/offline dev paths + ADR,,E7,Low,1,docs;devx,".env.example documents both paths; ADR-010 records the decision and alternatives considered.","ADR-010 Accepted; .env.example updated",✅ Done
Story,E11-1,Add Keycloak + dedicated Postgres services,,E11,High,5,security;auth;infra,"Add Keycloak and a dedicated Postgres DB as internal-only services in ancientdataworkspace/deploy (webgis-edge network).","Keycloak reachable internally, has its own DB, no unintended public exposure",-
Story,E11-2,Replace JWT auth with OAuth2 Resource Server,,E11,High,8,backend;security;auth,"Replace SecurityConfig/JwtFilter/JwtUtil JWT issuance/validation with oauth2ResourceServer().jwt() validating Keycloak-issued tokens.","Backend accepts only valid Keycloak-issued tokens; obsolete JWT classes removed",E11-1
Story,E11-3,Map Keycloak roles to app roles,,E11,High,3,backend;security;auth,"Map Keycloak realm roles to existing ADMIN/USER roles used in SecurityConfig; update/retire CustomUserDetailService.","requestMatchers role checks behave identically to before the migration",E11-2
Story,E11-4,Migrate frontend login to Authorization Code + PKCE,,E11,High,5,frontend;security;auth,"Replace manual login form + authStorage.ts token handling with react-oidc-context or oidc-client-ts PKCE flow.","Login/logout works end-to-end against Keycloak; no manual token storage code remains",E11-2;E11-3
Story,E11-5,Write OAuth2/OIDC migration ADR,,E11,Medium,2,docs;adr,"Document the decision, backward-compat/rollback plan, and existing-user migration in ADR-013.","ADR-013 Accepted",E11-1
Story,E12-1,Install k3s on the NAS,,E12,High,5,devops;kubernetes;infra,"Install single-node k3s on the Synology NAS alongside existing Compose stacks; verify resource headroom.","k3s cluster running and reachable via kubectl; resource usage documented as acceptable",E11
Story,E12-2,Write k8s manifests/Helm chart,,E12,High,8,devops;kubernetes,"Write manifests/Helm chart for ancientdata, geoserver, and the edge stack mirroring current docker-compose.yml volumes/networking.","Manifests apply cleanly and reproduce Compose-stack topology",E12-1
Story,E12-3,Move env vars to Secret/ConfigMap,,E12,High,3,devops;kubernetes;security,"Move .env values into Kubernetes Secret/ConfigMap resources; keep PostGIS/media persistence identical.","No plaintext secrets in manifests; persistence verified after redeploy",E12-2
Story,E12-4,Cut over from Compose to k3s,,E12,High,5,devops;kubernetes,"Run k3s stack in parallel with Compose, validate end-to-end including the E11 OAuth2 flow, then cut Cloudflare Tunnel routing over.","rinsewillet.net serves from k3s with full functionality parity",E12-3;E11-4
Story,E12-5,Document deployment + rollback,,E12,Medium,2,docs;devops,"Document the new deployment path and rollback-to-Compose steps in ancientdataworkspace/docs/deployment-recovery.md.","Runbook covers both deploy and rollback, verified by a dry run",E12-4
Story,E12-6,Write k3s migration ADR,,E12,Medium,2,docs;adr,"Document the decision in ADR-014.","ADR-014 Accepted",E12-1
Story,E13-1,Rotate PostGIS/pgAdmin credentials,,E13,Critical,1,security;infra,"Rotate PostGIS root password and pgAdmin admin credentials, currently plaintext in the live NAS compose.yaml and exposed during incident investigation.","New credentials in place; old ones invalidated",-
Story,E13-2,Externalize infra compose credentials + lock file permissions,,E13,High,2,security;infra,"Migrate postgis_admin/compose.yaml to the ${VAR}-from-.env pattern used by docker-infra-compose.yml; chmod 600 the file.","No plaintext credentials in compose.yaml; file not world-readable/writable",E13-1
Story,E13-3,Evaluate Docker daemon live-restore on Synology,,E13,High,3,infra;docker,"Investigate enabling live-restore so a future dockerd restart reattaches to running containers instead of stopping every container across all stacks at once.","Documented finding (enabled, or why not possible on this DSM version) plus config location",-
Story,E13-4,Stream RasterProxyService responses instead of buffering,,E13,High,3,backend;raster;performance,"Replace readAllBytes() full-buffering in RasterProxyService.forward() with a streaming proxy response.","Large upstream WMS/raster responses no longer fully load into ancientdata's JVM heap",-
Story,E13-5,Write raster publishing pipeline ADR + incident postmortem,,E13,Medium,2,docs;adr,"Write ADR-012-raster-publishing-pipeline.md covering the DEM/COG delivery decision (E3-1/E3-4) and the 2026-08-20 outage postmortem.","ADR-012 Accepted, includes root cause and the mem_limit/restart-policy/live-restore follow-ups",E13-3;E13-4
Story,E14-1,Introduce domain-level exceptions,,E14,Medium,8,backend;refactor;error-handling,"Add NotFoundException/ConflictException/InvalidRequestException (or similar) in application/service, mapped to HTTP status only in GlobalExceptionHandler; remove ResponseStatusException from every service class.","No service class imports ResponseStatusException; existing error-response bodies/status codes unchanged for all covered endpoints; ADR written",-
Story,E14-2,Deduplicate Road/Site service CRUD logic,,E14,Medium,5,backend;refactor,"Extract shared save/update field-mapping + exception-translation logic and the duplicated getModernReferenceDTOList helper out of RoadService/SiteService; move the inline modern-reference short-refs string-building out of RoadService.update().","RoadService/SiteService behavior unchanged per existing tests; duplicated helper/logic removed from both classes",E14-1
Story,E14-3,Audit Lombok @Data on JPA entities,,E14,Medium,5,backend;refactor;jpa,"Replace Lombok @Data's all-fields equals/hashCode on JPA entities with explicit id-based equals/hashCode.","Each @Entity class has a deliberate id-only equals/hashCode; existing tests (including relation-heavy ones) stay green",-
Story,E14-4,Use PostGIS aggregation for road dashboard counts,,E14,Low,2,backend;dashboard;performance,"Replace RoadService.getDashBoardData()'s in-memory full-table tally with RoadRepository.countByTypeRaw().","RoadDashboardDTO values returned by the dashboard endpoint are identical before/after the change",-
Story,E14-5,Extract shared entity-info-page layout,,E14,Medium,8,frontend;refactor,"Extract a shared entity-info-page layout/hook consolidating RoadInfo.tsx/SiteInfo.tsx (edit-form state, isEditing toggle, save/cancel wiring, Map/MediaGallery/ModernReferencePicker layout).","RoadInfo/SiteInfo behavior unchanged per existing tests; duplicated layout/logic removed from both pages",-
Story,E14-6,Unify road/site Redux Toolkit pattern,,E14,Medium,5,frontend;refactor,"Unify roadSlice/roadThunks and siteSlice/siteThunks on one Redux Toolkit pattern (createAsyncThunk + extraReducers) for the equivalent fetch-by-id operation.","Both slices/thunks follow the same pattern; existing tests for both stay green",-
Story,E14-7,Replace blocking alert() save feedback,,E14,Medium,3,frontend;ux,"Replace the blocking alert() save-feedback in RoadInfo/SiteInfo with an in-app inline banner/toast consistent with existing loading/error UX.","Save success/failure is communicated without a blocking native alert(); existing save/cancel behavior otherwise unchanged",-
Story,E14-8,Split useMapInteractions.ts by concern,,E14,Low,5,frontend;refactor,"Split useMapInteractions.ts into per-concern modules (useMarkerHighlight, useAutoZoom, useLayerPanelControl + gating helpers), updating consumer imports and tests.","No behavior change; each extracted module has one clear concern; existing useMapInteractions/MapContent/LayerPanel tests stay green",-
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

---

### E10 — Site & Road Type Registry Consolidation ✅

**Status:** Complete (August 2026)

**Trigger:** Raised during E9-5 smoke testing — site type labels/icons/styles were spread
across `utils/siteTypes.ts`, `siteIcons.ts`, and `Styles/markerStyles.ts`, so adding a new
type meant touching 3+ files and keeping their keys in sync by hand. Pre-work analysis also
found `ptum` ("possible barrow") incorrectly rendering with the confirmed-tumulus icon.

**What was delivered:**
- E10-1: New `AncientDataWebGIS_FE/src/utils/siteTypesConfig.ts` — one typed
  `siteTypeEntries: SiteTypeEntry[]` array (`{ type, label, icon, iconUrl }`) covering all
  17 site types, replacing `siteTypeLabels`, `siteIconMap`, and `siteTypeIconUrls`, with
  `getSiteIcon`/`siteTypeConverter` helpers mirroring the old call shapes so consumers only
  changed imports. Fixed the `ptum` bug: it now resolves to `possibleTumulusIcon`/
  `ptumulus.png` instead of the confirmed-tumulus assets. Consumed by `MapContent`,
  `MapComponent`, `useMapInteractions`, `MapInfoCard`, `SiteInfo`, `MapLegend`, and
  `Dashboard`. `markerStyles.ts` was trimmed to the generic fallback icon, the `makeIcon`
  helper, and road styles — `siteTypes.ts`/`siteIcons.ts` were deleted outright.
  `Dashboard.tsx`'s own `LABEL_MAPPING` (which had drifted to different wording, e.g.
  "Tumulus" vs. the registry's "barrow") was deleted in favor of reading from the shared
  `siteTypeEntries`/`roadStyleEntries` registries — a deliberate call to let registry
  wording win, confirmed with the repo owner before implementing.
- E10-2: Confirmed `roadStyleEntries`/`roadStyleDifferentiator` (`utils/roadTypes.ts`,
  built in E9-5) already satisfied the "one file per type" bar — no structural changes
  needed, just a short doc-comment addition (see E10-3).
- E10-3: Added a "how to add/rename/restyle a type" comment block to both
  `siteTypesConfig.ts` and `roadTypes.ts`, plus a new `## Type Registries` section in
  `AGENTS.md` pointing at both files as the sole place to touch.

**Impact:**
- Adding, renaming, or restyling a site or road type is now a single-file change
  (`siteTypesConfig.ts` or `roadTypes.ts`), satisfying the P3 Done Criteria bar.
- Possible-tumulus sites now render with a visually distinct marker from confirmed
  tumuli, on both the map and in `MapLegend`.
- Confirmed by the repo owner via manual smoke testing in the Atlas.

**Files changed:**
- `AncientDataWebGIS_FE/src/utils/siteTypesConfig.ts` (new), `siteTypesConfig.test.ts` (new)
- `AncientDataWebGIS_FE/src/utils/siteTypes.ts` (deleted), `roadTypes.ts` (doc comment only)
- `AncientDataWebGIS_FE/src/components/MapComponent/siteIcons.ts` (deleted)
- `AncientDataWebGIS_FE/src/components/MapComponent/Styles/markerStyles.ts`, `MapContent.tsx`, `MapComponent.tsx`, `MapInfoCard.tsx`, `useMapInteractions.ts`
- `AncientDataWebGIS_FE/src/components/MapLegend/MapLegend.tsx`, `demColorRamp.ts` (doc-comment cross-reference only)
- `AncientDataWebGIS_FE/src/pages/SiteInfo.tsx`, `Dashboard.tsx`
- `AncientDataWebGIS_FE/AGENTS.md` (new `## Type Registries` section)

**Tests:**
- New `siteTypesConfig.test.ts`: regression test asserting `getSiteIcon('ptum')` resolves
  to a different icon than `getSiteIcon('tum')`.
- Full frontend suite: 162/162 tests passing; lint and `npm run build` both clean.

---

### E3 — Raster / GeoTIFF Delivery 🚧 (In Progress)

**Status:** E3-1 through E3-8 all delivered (August 2026).

**Decision record:** `AncientDataWebGIS/docs/architecture/adr/ADR-012-raster-publishing-pipeline.md`
**Runbook:** `AncientDataWebGIS/docs/features/E3.1-raster-publishing-pipeline.md`

**What was delivered (E3-1):**
- Confirmed GeoServer (`kartoza/geoserver:2.24.1`) was already deployed in `docker-compose.yml`, used only for direct LAN/WARP QGIS-admin access — not previously reachable by the frontend at all.
- Chose (with project owner sign-off) to keep GeoServer's exposure unchanged (LAN/WARP-only, no new public port, no edits to the shared `ancientdataworkspace` edge-proxy stack) and instead front it with a new read-only proxy in the already-public `ancientdata` backend: `GET /api/raster/**` forwards GET requests container-to-container to `geoserver:8080` over the internal `webgis-edge` network, explicitly blocking any upstream path containing `/rest/` or `/web/` (GeoServer's admin REST API / web admin UI).
- Decided GeoTIFF sources are backed up at the **NAS level** (e.g. Synology HyperBackup pointed at `/volume1/docker/ancientdata/geoserver`), not by the application — unlike user-uploaded media (`NasBackupService`/`ADR-006`), since source rasters are large, infrequently-changing, authoritative files closer in nature to the Postgres data volume than to app-owned media.
- Established Cloud-Optimized GeoTIFF (COG) conversion + overviews as a required pre-publish step for future rasters (especially the DEM), and documented it as the reason GeoServer + GWC caching stays viable on the NAS's 4GB RAM even for a large source — full DEM-specific conversion work remains scoped to **E3-4**.
- Documented remote GeoServer admin access (WARP/LAN) for operating on a machine other than the NAS, and the manual per-raster publishing steps (GDAL conversion, GeoServer store/layer/style creation, enabling GWC).

**Impact:**
- E3-2 (raster catalog endpoint) and E3-3 (frontend `LayerPanel` Physical group) now have a concrete, decided transport mechanism (`/api/raster/**`) to build against, instead of an open architecture question.
- No changes were needed to the shared `ancientdataworkspace` edge-proxy repo, and GeoServer's admin attack surface is unchanged from before this story.

**Files changed (backend):**
- `src/main/java/com/webgis/ancientdata/config/GeoServerProxyConfig.java` (new)
- `src/main/java/com/webgis/ancientdata/application/service/RasterProxyService.java` (new)
- `src/main/java/com/webgis/ancientdata/web/controller/RasterProxyController.java` (new)
- `src/main/java/com/webgis/ancientdata/security/SecurityConfig.java` (`RASTER_URL` permitAll GET rule)
- `src/main/resources/application.properties` (`geoserver.internal-url`), `.env.example` (`GEOSERVER_INTERNAL_URL`)
- `docs/architecture/adr/ADR-012-raster-publishing-pipeline.md` (new), `docs/architecture/adr/README.md` (index entry)
- `docs/features/E3.1-raster-publishing-pipeline.md` (new — runbook)

**Tests:**
- `RasterProxyServiceTests` (4 tests): forwards allowed paths with query string and propagates upstream status/content-type/body (using a JDK `HttpServer` stub, no mocking framework needed for the HTTP layer), blocks `/rest/**` and `/web/**` paths with 403, propagates upstream error status (404) unchanged.
- `RasterProxyControllerTests` (2 tests): correct subpath/query-string extraction and delegation to the service; endpoint reachable without authentication (matches `SecurityConfig`'s public GET rule).
- Full backend suite: `./gradlew test` green.

**Outstanding / manual follow-up (not yet done by the project owner):**
- Confirm NAS backup coverage of `/volume1/docker/ancientdata/geoserver` (see runbook's "Backup" section).
- Configure GeoServer's proxy base URL so `GetCapabilities` documents self-reference the public `/api/raster` path rather than GeoServer's internal address (needed before any external client consumes GetCapabilities directly — E3-2's catalog endpoint sidesteps this for now).

---

**What was delivered (E3-2):**
- Added `GET /api/raster/catalog`, a public read-only endpoint returning a JSON array of published raster layers: `name`, `source` (GeoServer `workspace:layer`, usable directly as a WMS `layers` param against `/api/raster/<workspace>/wms`), `bounds` (WGS84 south/west/north/east), `zoom` (curated min/max), and `attribution` — the exact field set named in the story.
- **Storage decision (discussed and confirmed with the project owner, no ADR needed — see below):** GeoServer's WMS `GetCapabilities` doesn't carry attribution or a sensible tile-pyramid zoom range, so this metadata can't be a live pass-through; it needs to be curated somewhere this backend owns. Considered three options: (A) a small static/curated list in Java, (B) a new backend-owned DB table (would need a manual SQL script per `DB-MIGRATION-STRATEGY.md`, since Flyway is disabled and schema is externally owned), (C) a hybrid pulling bounds live from GeoServer capabilities while curating name/attribution/zoom locally. Chose **(A)** — a static `List<RasterLayerDTO>` in `RasterCatalogService` — mirroring the frontend's own `layersConfig.ts` precedent (E9-3). The project owner confirmed ~20+ historical maps/DEM-like layers are planned; a static list holds that many entries fine as *data* — the thing that would actually get painful is the *edit workflow* (PR + redeploy per layer), and that's a separate, larger concern (DB table + auth-gated CRUD + admin UI) than this story's scope. Publishing a raster into GeoServer already requires several manual GDAL/admin steps per the E3.1 runbook, so one more code-reviewed edit isn't a new bottleneck yet at current scale.
- Deferred the DB-backed/admin-editable version of this catalog as a new backlog story, **E3-6** (Low priority, not started) — to be picked up once the redeploy-per-layer workflow is actually the bottleneck, not pre-built speculatively.
- Seeded the catalog with the two real published layers (`ancientdata:1818-de-man-a2`/`a3`), using bounds/title values the project owner read directly from GeoServer's layer "Publishing" tab (Lat/Lon Bounding Box, already WGS84) and a shared attribution string ("1818 De Man - Nijmegen"); zoom 12–19 for both (city-scale historical map).
- `GET /api/raster/catalog` shares the `RASTER_URL` (`/api/raster/**`) permitAll rule already in `SecurityConfig` from E3-1 — no security config change needed. Verified via a full-context `MockMvc` test that the specific `/catalog` route resolves to the new controller rather than falling through to `RasterProxyController`'s `/api/raster/**` catch-all (which would otherwise try to forward it to GeoServer and 502).

**Impact:**
- E3-3 (frontend `LayerPanel` Physical group) now has a concrete catalog endpoint to fetch and map into `layersConfig.ts`-shaped entries.
- Two real historical map layers are discoverable end-to-end (GeoServer → proxy → catalog), ready for E3-3 to wire up.

**Files changed (backend):**
- `src/main/java/com/webgis/ancientdata/domain/dto/RasterLayerDTO.java` (new), `RasterBoundsDTO.java` (new), `RasterZoomDTO.java` (new)
- `src/main/java/com/webgis/ancientdata/application/service/RasterCatalogService.java` (new — holds the curated static catalog)
- `src/main/java/com/webgis/ancientdata/web/controller/RasterCatalogController.java` (new — `GET /api/raster/catalog`)
- `docs/features/FEATURE-SPEC-BACKLOG.md` (this write-up; added deferred `E3-6` backlog stub)

**Tests:**
- `RasterCatalogServiceTests` (2 tests): catalog contains both published De Man sheets by `source`; every entry has non-blank name/source/attribution, valid bounds (south < north, west < east), and valid zoom (min ≤ max).
- `RasterCatalogControllerTests` (2 tests): `GET /api/raster/catalog` returns 200 with the expected JSON shape (name/source/bounds/zoom/attribution) and — using a mocked, `verifyNoInteractions`-asserted `RasterProxyService` — proves the request never falls through to the raster proxy; endpoint reachable without authentication.
- Full backend suite: `./gradlew test` green (all existing + 4 new tests).

**ADR:** Not needed — no new storage mechanism, library, or security/CI-CD strategy was introduced (a static Java list is not new tech), per the ADR criteria in `AGENTS.md`. The storage-approach discussion is recorded in this write-up instead.

**Outstanding / manual follow-up (E3-2):**
- None for the two seeded layers (real bounds/attribution/zoom provided by the project owner). Each future published layer needs a new `RasterLayerDTO` entry added to `RasterCatalogService` by hand until/unless `E3-6` is picked up.

---

**What was delivered (E3-3):**
- **Backend contract addition to E3-2 (small, additive):** discovered mid-implementation that `RasterLayerDTO` had no way to distinguish a DEM/elevation layer from any other raster overlay (e.g. the De Man historical map sheets). Wiring `MapLegend`'s "Elevation" section to "any visible Physical layer" would have shown that section for a historical map, which is factually wrong. Added `RasterLayerCategory` (`HISTORICAL_MAP` | `DEM`) and a `category` field to `RasterLayerDTO`/`RasterCatalogController`'s response; both existing De Man entries are `HISTORICAL_MAP`. Backwards compatible (additive field only). Confirmed with the project owner before implementing.
- Frontend: extended `useLayerPanelControl` (`useMapInteractions.ts`) with a new `physicalLayers` state slice — unlike the exclusive single-select Historical/Aerial groups, any number of Physical (raster catalog) layers can be visible simultaneously, each with independent opacity. The array's order doubles as map z-order (index 0 = topmost/frontmost, matching the LayerPanel's top-to-bottom row order) via `L.TileLayer.setZIndex`; toggling visibility adds/removes the actual `L.tileLayer.wms` instance, opacity changes mutate the existing layer in place (`setOpacity`) rather than remounting it (avoids tile-reload flicker).
- The raster catalog (`GET /api/raster/catalog`) is fetched once via a new `RasterService.ts`, gated on the Leaflet `map` instance being non-null — `useLayerPanelControl` receives `null` for `map` on Home/RoadInfo/SiteInfo (per E9-2's existing `layerPanel`-gating convention), so those pages never issue the request; only Atlas (`layerPanel=true`) does.
- New WMS tile URLs are built as `${apiBaseUrl}/raster/<workspace>/wms` (workspace parsed from the catalog's `source` field), reusing `apiClient`'s already-resolved, environment-aware base URL (`api/config.ts`, newly exported as `apiBaseUrl`) instead of hardcoding `/api/raster/...`, so it resolves correctly behind a production base path (`VITE_BASE_PATH`) as well as in dev.
- `LayerPanel.tsx` gained a "Physical" section — rendered only when the catalog returns ≥1 entry (same "no empty-group placeholder UI" rule E9-4 established) — with a checkbox (toggle visible), an opacity slider (0–1, step 0.1), and ▲/▼ reorder buttons per row (simple array-swap reordering; no drag-and-drop dependency, per discussion with the project owner).
- `useActiveDemLayer` (previously an inert stub always returning `null`, per E9-5) now takes the name of the topmost visible **DEM-category** Physical layer (or `null`) as an argument and returns it as-is; `MapContent` computes that value from `physicalLayers` (filtering specifically on `category === 'DEM'`, not "any visible Physical layer") and passes it to `MapLegend` as `activeDemLayerName`. `MapLegend`'s "Elevation" section now appears only for a real DEM layer — verified this doesn't fire for a `HISTORICAL_MAP`-category layer via a dedicated regression test (see below). E3-5 remains responsible for the section's actual color-ramp content; today the catalog has no real DEM entries yet (that's E3-4's job), so the section stays inert in practice until then, same as before.
- "Persist session state" (from the story's original CSV acceptance note) is interpreted as in-memory persistence for the lifetime of the mounted panel — matching the existing base/historical/aerial/overlay state, none of which persist to `localStorage`/`sessionStorage` either. Adding real cross-navigation persistence only for the Physical group would be inconsistent with its siblings and wasn't requested.

**Impact:**
- The two published De Man historical map sheets are now toggleable, opacity-adjustable raster overlays in the Atlas `LayerPanel`, stacked in user-controlled order.
- `useActiveDemLayer`/`MapLegend`'s Elevation section has real (if still practically inert, pending E3-4) wiring, closing out the seam E9-5 intentionally left open.
- E3-4 (DEM delivery) and E3-5 (DEM color-ramp content) now have a working Physical-group UI and a correct DEM/historical-map distinction to build on.

**Files changed (backend):**
- `src/main/java/com/webgis/ancientdata/domain/dto/RasterLayerCategory.java` (new — `HISTORICAL_MAP` | `DEM` enum)
- `src/main/java/com/webgis/ancientdata/domain/dto/RasterLayerDTO.java` (added `category` field)
- `src/main/java/com/webgis/ancientdata/application/service/RasterCatalogService.java` (both catalog entries set to `HISTORICAL_MAP`)
- `src/test/java/com/webgis/ancientdata/rastertests/RasterCatalogServiceTests.java`, `RasterCatalogControllerTests.java` (updated/added assertions for `category`)

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/types/raster.ts` (new), `src/services/RasterService.ts` (new)
- `AncientDataWebGIS_FE/src/api/config.ts` (exported `apiBaseUrl`)
- `AncientDataWebGIS_FE/src/components/MapComponent/useMapInteractions.ts` (`physicalLayers` state, catalog fetch, Leaflet sync/z-order effects, `togglePhysicalLayer`/`setPhysicalLayerOpacity`/`movePhysicalLayer`)
- `AncientDataWebGIS_FE/src/components/MapComponent/mapUtils.ts` (`buildPhysicalLayer`)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.tsx` (computes `activeDemLayer` from `physicalLayers`, passes `activeDemLayerName` to `MapLegend`)
- `AncientDataWebGIS_FE/src/components/LayerPanel/LayerPanel.tsx`, `LayerPanel.css` (new "Physical" section)
- `AncientDataWebGIS_FE/src/components/MapLegend/MapLegend.tsx`, `useActiveDemLayer.ts` (accept/pass through `activeDemLayerName`)

**Tests:**
- Backend: `RasterCatalogServiceTests`/`RasterCatalogControllerTests` updated for the new `category` field (both De Man entries assert as `HISTORICAL_MAP`); full suite green.
- `LayerPanel.test.tsx` (+6 tests): no Physical section when the catalog is empty; a row per catalog entry with an opacity slider; `togglePhysicalLayer`/`setPhysicalLayerOpacity`/`movePhysicalLayer` called correctly; boundary reorder buttons disabled at the top/bottom of the list.
- `MapLegend.test.tsx` (+1 test): `activeDemLayerName` passed through to the real (unmocked) `useActiveDemLayer` shows the Elevation section.
- `MapContent.test.tsx` (+2 tests, real unmocked Leaflet rendering, `RasterService` mocked): toggling a Physical catalog entry adds a real WMS tile layer (`img[src*="/raster/ancientdata/wms"]`) to the map; a dedicated regression test proves the Elevation section appears for a visible `DEM`-category layer but *not* for a visible `HISTORICAL_MAP`-category layer toggled on first — this is the exact bug the category field was added to prevent.
- Full frontend suite: 113/113 tests passing. `npm run lint` and `npm run build` both clean.

**Outstanding / manual follow-up (E3-3):** None. Automated verification relied on the real-DOM `MapContent.test.tsx` tests (live-browser verification wasn't possible from the initial dev sandbox — no reachable PostGIS/GeoServer backend, same limitation noted under E9). The project owner subsequently smoke-tested live from a real browser (backend on `local-dev` DB profile + real GeoServer over Cloudflare WARP) and confirmed both `1818-de-man-a2`/`a3` render correctly in the Atlas `LayerPanel`'s Physical section, toggle/opacity work, and `useActiveDemLayer`'s category gating behaves as intended (no Elevation section fires for these `HISTORICAL_MAP`-category layers). Tile load latency during that test was higher than production will be, due to the WARP-tunnel-hop dev path — not a code issue (see ADR-012's GWC caching notes).

**Also found during live smoke testing (E3-3, not a code defect — documented here for traceability):**
- The NAS's actual current LAN IP is `192.168.2.13`, not `192.168.1.50` as `ADR-010`, the E3.1 runbook, `docker-compose.yml`'s comments, and `.env.example` all state — those docs are stale and should be corrected in a follow-up pass.
- Reaching GeoServer from off-LAN via WARP required a Private Network CIDR route the project's Cloudflare Tunnel didn't have configured yet (Zero Trust dashboard → tunnel → **Add a route → Private CIDR** → `192.168.2.0/24`), plus a WARP client Device Settings Profile Split Tunnel setting switched from the default "Exclude" mode (which excludes all private IP ranges by default) to "Include IPs and domains" with that same CIDR explicitly listed. Neither of these was previously documented as a required one-time setup step for a *new* WARP client device beyond what `ADR-010`/the E3.1 runbook already describe for reusing an *already-configured* one.

---

**What was delivered (E3-4):**
- Confirmed with the project owner (before implementing, per this story's explicit "don't invent architecture silently" gate) that ADR-012's primary approach — COG + GWC, GeoServer-rendered — remains the plan for the 80GB DEM; Option B (pre-tiled static XYZ pyramid) stays deferred as documented in the ADR's "When to Revisit," not adopted preemptively.
- Established DEM-specific GDAL conversion parameters, added as a new section to the E3.1 runbook rather than new application code — consistent with this story's precedent (E3-1 was also mostly ADR + runbook, not app code) and with the fact that the actual GDAL/publish work is manual/operator-driven for every raster, DEM included:
  - **Resampling:** `RESAMPLING=AVERAGE`/`OVERVIEW_RESAMPLING=AVERAGE` on the `gdal_translate -of COG` conversion, replacing the historical-map pipeline's untouched (nearest-neighbor) default — confirmed with the project owner that nearest-neighbor is wrong for continuous elevation data (risks erasing or aliasing subtle microrelief at zoomed-out overview levels).
  - **Zoom range:** 8–18 (vs. 12–19 for the historical map sheets) — derived from project-owner input on the DEM's actual use case: a wide-area source (Utrecht/Gelderland, NL, to Duisburg, DE) that needs to serve both regional geomorphology (Veluwe/Reichswald moraines, Rhine valley — visible from z8–10) and near-native-resolution archaeological microrelief (Roman road embankments, tumuli, Celtic field lynchets near Kleve — only visible at z15–18). Documented as a starting point pending the project owner confirming native pixel size via `gdalinfo` on the real source file.
  - **Compression:** kept `DEFLATE` (lossless) as the default, matching the historical-map pipeline, but documented `LERC_ZSTD` with a bounded `MAX_Z_ERROR` as an optional, separately-tested alternative for further shrinking the 80GB source if DEFLATE alone proves insufficient for the NAS's 4GB-RAM serving budget — not adopted by default since it's a fidelity/size tradeoff that should be validated against the real file, not assumed.
  - **Elevation visualization:** flagged (not decided) that a flat elevation color ramp likely won't make the actual research-relevant microrelief features visible, and that a hillshade/shaded-relief rendering may be needed instead — explicitly left for **E3-5** to decide once real DEM tiles exist to evaluate against.
- Confirmed with the project owner that this story's actual application-code output is a catalog entry, and only once a real DEM layer exists in GeoServer (mirroring E3-2's precedent of only adding `RasterLayerDTO` entries backed by real GeoServer-read bounds) — no placeholder/fabricated entry was added. The runbook now documents that step (`category = RasterLayerCategory.DEM`) so it's a small, mechanical addition once the project owner has actually run the conversion and published the layer.

**Impact:**
- The previously-deferred "how" for DEM-specific conversion (ADR-012's `Decision` section explicitly punted overview levels/compression/resampling specifics to this story) is now a documented, reasoned recipe instead of an open question — unblocks the project owner's actual GDAL/publish work on the real 80GB file.
- `RasterLayerCategory.DEM` (added in E3-3) now has a concrete plan for when a real entry using it will exist; `useActiveDemLayer`/`MapLegend`'s Elevation section (also E3-3) remains correctly inert until that catalog entry is actually added.
- E3-5 (DEM color-ramp) has an explicit flag that a plain color ramp may not be the right rendering choice, rather than discovering that only once real data is available.

**Files changed:**
- `docs/features/E3.1-raster-publishing-pipeline.md` (new "DEM-specific conversion (E3-4)" section)
- `docs/features/FEATURE-SPEC-BACKLOG.md` (this write-up; status updates)

**Tests:** None — no application code changed. `RasterCatalogService`/`RasterLayerDTO`/`RasterLayerCategory` (from E3-2/E3-3) are untouched; adding the DEM's actual catalog entry is deferred until a real layer exists to source bounds from (see "Outstanding" below), at which point it's the same small, already-tested code path E3-2 established (`RasterCatalogServiceTests`/`RasterCatalogControllerTests` already assert the general shape and would cover a new entry without further test scaffolding).

**ADR:** Not needed. Per `AGENTS.md`'s ADR criteria (new tech/libraries, or a change to storage/CI-CD/security strategy), this story doesn't qualify — it stays within ADR-012's already-decided COG+GWC approach and fills in parameters the ADR explicitly deferred to this story, rather than introducing a new decision. Matches E3-2's precedent of recording a non-architectural decision in this write-up instead of touching the ADR. `ADR-012` itself is unchanged.

**Outstanding / manual follow-up (E3-4, project owner) — resolved since this write-up, see E3-5 below:**
- ~~Run `gdalinfo` on the real source DEM(s)~~ — done; real measured min/max per area recorded in `docs/features/dem-elevation-ramp.sld`'s header comment.
- ~~Run the `gdal_translate -of COG` conversion, publish to GeoServer, enable GWC~~ — done; five DEM areas (Swalmen, Venlo-Geldern, Mönchengladbach x2, the regional Gelderland-NRW layer), each with an elevation layer and a hillshade sibling, are live in the catalog (`docs/architecture/sql/raster_layer.sql`).
- ~~Decide DEM visualization style (plain color ramp vs. hillshade)~~ — resolved as **both, combined**: a shared elevation colour-ramp GeoServer style plus an independently-toggleable hillshade layer blended on top client-side. See E3-5.
- Still open: verify actual tile-serving performance on the NAS's 4GB RAM under real usage — no measurement recorded yet; the documented fallback (ADR-012 "When to Revisit") remains available if it proves unacceptable.

---

**What was delivered (E3-5):**
- **Visualization-style decision** (left open by E3-4): resolved as **hillshade + colour ramp combined**, not one or the other. A shared GeoServer `RasterSymbolizer` style (`docs/features/dem-elevation-ramp.sld`) renders every DEM-category elevation layer with one colour ramp; each elevation layer also has an independently-toggleable multidirectional-hillshade sibling (GeoServer's default greyscale style, flagged via a new `hillshade` boolean on `RasterLayerDTO`/the catalog). The ramp intentionally targets regional geomorphology reading (Veluwe/Reichswald moraine highs vs. Rhine/Maas lowlands) at small-to-mid zoom; revealing archaeological microrelief is the hillshade layer's job instead.
- **Ramp range sourced from real data, not assumed:** `gdalinfo -stats` was run against all five published DEM areas; combined measured range -21.07m to 151.21m, padded to -25/155 for the 8-stop ramp — recorded in the SLD's header comment for traceability.
- **Frontend rendering:** hillshade tiles render via a new `.physical-layer--hillshade` CSS class (`mix-blend-mode: multiply`) so they darken/texture whatever's beneath them instead of drawing as an opaque layer, relying on the hillshade sibling being ordered directly above its elevation counterpart in the Physical group's z-order (same `id`-ordering convention `raster_layer` already preserves, per E3-6). `RasterLayerDTO`'s new `hillshade` field threads through `RasterService.ts` → `PhysicalLayerState` (`useMapInteractions.ts`) → `buildPhysicalLayer` (`mapUtils.ts`).
- **`MapLegend`'s Elevation section:** built out `demColorRamp.ts` (hand-kept TS mirror of the SLD's 8 `<ColorMapEntry>` stops — same "static/curated, kept in sync by hand" pattern as `roadStyleEntries`/`siteTypeEntries`) and wired it into the section body, plus the name/attribution meta line already scaffolded by E9-5/E3-3. Critically, the section keys off `useActiveDemLayer`'s DEM-**category** signal (E3-3), not "any visible Physical layer with `hillshade: true`" — a hillshade sibling has no colour ramp of its own to explain, so a hillshade-only-visible DEM area correctly does *not* trigger the Elevation section (dedicated regression test, see below).
- Delivered across two commits: the backend SLD/`hillshade` field/GWC-header-passthrough work (`d19f7eb`, 2026-08-18) and the frontend `MapLegend`/`demColorRamp.ts` wiring (`1fa2e93`, 2026-08-24) in `AncientDataWebGIS_FE`.

**Impact:**
- The Atlas `MapLegend` now shows real elevation colour-ramp content and attribution whenever a DEM layer is visible, closing the seam E9-5/E3-3 intentionally left open.
- Hillshade layers give the microrelief detail (Roman road embankments, tumuli, Celtic field lynchets) the flat colour ramp alone couldn't reveal, without requiring a second "Elevation" legend entry or new UI concept — same Physical-group toggle/opacity controls as any other raster.

**Files changed (backend):**
- `docs/features/dem-elevation-ramp.sld` (new)
- `src/main/java/com/webgis/ancientdata/domain/dto/RasterLayerDTO.java` (`hillshade` field), `RasterCatalogService.java` (catalog entries), `RasterProxyService.java` (GWC cache-header passthrough)
- `docs/architecture/sql/raster_layer.sql` (10 real DEM/hillshade entries across 5 areas)

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/components/MapLegend/demColorRamp.ts` (new), `MapLegend.tsx`, `MapLegend.css`
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.css` (`.physical-layer--hillshade`), `MapContent.tsx`, `mapUtils.ts`, `useMapInteractions.ts`
- `AncientDataWebGIS_FE/src/types/raster.ts` (`hillshade` field)

**Tests:**
- `MapLegend.test.tsx`: every `demColorRamp` stop renders in the Elevation section once `activeDemLayerName` is set.
- `MapContent.test.tsx`: a hillshade catalog entry renders with the `.physical-layer--hillshade` class; a dedicated regression test confirms the Elevation section does **not** appear when only a hillshade-category-`DEM` layer (not its elevation sibling) is visible.
- Full frontend suite: 162/162 passing (current `AncientDataWebGIS_FE` state); `npm run lint`/`npm run build` clean.

**Outstanding:** None known for this story specifically. NAS-scale tile-serving performance under real usage remains open under E3-4 (see above), not specific to the legend/hillshade rendering.

**Also fixing:** This write-up itself — E3-5 had already shipped in both repos but was never documented here, so it read as outstanding in status summaries. No code changes were needed, only this backlog entry.

---

**What was delivered (E3-7):**
- **Confirmed three open decisions with the project owner before implementing (per this story's explicit "don't invent silently" gate):**
  1. Global minimum zoom floor: **8** — matches every DEM layer's own curated `zoom.min` in `RasterCatalogService` (all 5 areas + their hillshade siblings start at 8), so the floor stops the "whole research area visible, enable all 5 DEM areas at once" scenario at zoom 0–7 without being any stricter than tiles would render anyway.
  2. Viewport source for the intersection check: the map's **exact current viewport** (`L.Map.getBounds()`), not a buffered/coarser approximation — matches the story text ("current map viewport") and needs no new tunable.
  3. Disabled-state explanation copy: **two distinct messages** so the user knows which gate is blocking them — `"Zoom in further to enable Physical layers."` below the zoom floor, `"Pan the map to this layer's area to enable it."` when in-view fails but the floor doesn't.
- Frontend-only change; no backend/DTO edits needed — `RasterLayerDTO`/`RasterBoundsDTO` (E3-2) and their frontend mirrors (`RasterLayer`/`RasterBounds`, E3-3) already carried real per-layer bounds, just unused for gating until now.
- Added `boundsIntersectViewport` (`mapUtils.ts`) — a pure axis-aligned rectangle intersection between a catalog entry's WGS84 `bounds` and the map's `L.LatLngBounds` viewport (no antimeridian handling needed; the research area is a single contiguous NL/DE region).
- Extended `PhysicalLayerState` (`useMapInteractions.ts`) with `bounds` (now actually populated from the catalog, previously dropped by `toLayerState`), `disabled`, and `disabledReason`. `useLayerPanelControl` tracks live viewport changes via a single `moveend`/`zoomend` listener on the Leaflet map (Leaflet's own zoom/bounds getters aren't reactive React state, so a version counter forces recomputation) and derives a gated copy of `physicalLayers` each render through a new pure `gatePhysicalLayer(layer, map)` function.
- `useLayerPanelControl`'s returned `togglePhysicalLayer` checks the gated state before delegating to the actual visibility toggle, blocking the *on* transition for an out-of-view or below-floor layer. Historical Maps sheets reuse the same shared `PhysicalLayerState` shape but are never gated (`disabled` stays `false`) — only the Physical group is in scope for this story.
- Scope note: the zoom floor is a single global constant, not each layer's own `RasterZoomDTO.min`/`max` (those remain curated display-range metadata, unused for gating either before or after this story — out of scope per the confirmed decision above).

**Found during the project owner's live testing — two follow-up fixes before this story was considered complete:**
1. **A layer left on while panning/zooming away kept requesting tiles for the new location.** The first cut only ever blocked the *on* transition and never force-disabled an already-visible layer, specifically to avoid stranding the user with a checked-but-un-toggleable row. Testing surfaced the flaw in that: Leaflet's WMS layer keeps re-requesting tiles for whatever the *current* viewport is, regardless of the layer's own bounds, so a DEM left "on" while the user roamed elsewhere kept hitting GeoServer for empty tiles outside its actual area — undermining the story's own overload-prevention goal. **Confirmed fix with the project owner:** `useLayerPanelControl`'s viewport-change handler now also auto-turns off (unchecks + removes from the map) any visible Physical layer whose bounds/zoom no longer satisfy the gate, rather than leaving it on indefinitely. This still avoids the original "stuck on" trap — the row simply becomes selectable again, not disabled, once you're back in range — while actually stopping the wasted requests.
2. **The Physical list felt cluttered with every catalog entry always shown, disabled + hinted when not selectable.** **Confirmed fix with the project owner:** `LayerPanel.tsx` now filters the Physical group to only the currently-selectable rows, hiding out-of-view/below-floor entries entirely instead of listing them disabled; a single fallback message (reusing the same two hint strings from decision 3 above) explains why the list is empty or shorter than the full catalog. Historical Maps sheets are unaffected (never gated, so never filtered).
   - **Bug this surfaced and fixed as part of the same change:** the Physical group's "move up/down" reorder buttons swap a layer with its nearest same-group neighbor in the underlying array — with hidden rows in the list, that neighbor could be one of the now-invisible ones, so a "move" click could silently swap with a row the user can't see and appear to do nothing. `createLayerGroupHandlers`'s `move` now takes an optional skip predicate; the Physical group's handler skips gated-off neighbors (Historical Maps' handler is unaffected, predicate defaults to skipping nothing), so reordering only ever swaps among the rows actually rendered in the filtered list.

**Impact:**
- A fully zoomed-out Atlas user can no longer enable all 5 DEM areas (plus hillshade siblings) simultaneously, and can no longer leave a DEM "on" while roaming to an unrelated area — both the enable-gate and the auto-off together address the GeoServer/NAS overload risk E3-4 called out.
- The Physical group only ever lists layers actually usable from the current view, cutting panel clutter without losing the explanation for why the list is short.
- No change to already-published Historical Maps sheet behavior.

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/components/MapComponent/mapUtils.ts` (`boundsIntersectViewport`), `mapUtils.test.ts` (new)
- `AncientDataWebGIS_FE/src/components/MapComponent/useMapInteractions.ts` (`PhysicalLayerState.bounds`/`disabled`/`disabledReason`, `PHYSICAL_MIN_ZOOM_FLOOR`, `gatePhysicalLayer`, `isPhysicalLayerWithinGate`, combined viewport-change/auto-off handler, gated `togglePhysicalLayer`, `createLayerGroupHandlers`'s new move-skip predicate), `useMapInteractions.test.ts` (new)
- `AncientDataWebGIS_FE/src/components/LayerPanel/LayerPanel.tsx` (filters the Physical group to selectable rows, renders a fallback message when none qualify), `LayerPanel.css` (`.layer-panel__section-empty-hint`), `LayerPanel.test.tsx` (updated builders + tests)

**Tests:**
- `mapUtils.test.ts` (new, 5 tests): `boundsIntersectViewport` — fully inside, partial overlap, fully outside, layer bounds containing the viewport, edge-touching bounds.
- `useMapInteractions.test.ts` (new, 11 tests): `gatePhysicalLayer` pure-function cases (6: visible layer never gated even out-of-view/below-floor; no map yet → not gated; below floor → zoom reason regardless of bounds; exactly at floor → not gated; at/above floor but out of view → pan reason; in view and at/above floor → not gated) plus 5 end-to-end tests against `useLayerPanelControl` driving a real (jsdom) `L.Map` — `togglePhysicalLayer` blocks enabling an out-of-view or below-floor layer and allows it once panned/zoomed into range; a visible layer auto-turns off after panning out of view and again after zooming below the floor; `movePhysicalLayer` skips a hidden gated-off neighbor and swaps with the next actually-visible one.
- `LayerPanel.test.tsx` (2 tests, replacing the first pass's per-row-disabled tests): a gated-off row is absent from the list with its reason shown as the section's fallback message; a mix of gated/ungated rows only renders the selectable one.
- Full frontend suite: 141/141 passing. `npm run lint` (eslint, `--max-warnings 0`) and `npx tsc --noEmit` both clean.
- Backend: no application code changed (this story is frontend-only); full backend suite unaffected.

**Migration notes:** None — no schema, config, or deployment changes. Purely client-side gating logic layered on data (`RasterLayerDTO.bounds`) the backend already served since E3-2.

**ADR:** Not needed. Per `AGENTS.md`'s ADR criteria (new tech/libraries, or a change to storage/CI-CD/security strategy), this story doesn't qualify — it's a client-side UI gating rule within the already-decided E3-2/E3-3 catalog+`LayerPanel` architecture, introducing no new dependency or infrastructure decision. All confirmed decisions (zoom floor value, viewport source, hint copy, auto-off, list filtering) are recorded in this write-up instead, matching E3-2/E3-4 precedent for non-architectural decisions.

**Outstanding / manual follow-up (E3-7):**
- Live smoke test on a real browser once a reachable backend/GeoServer is available (same sandbox limitation noted under E3-3/E9) — to confirm the fallback message's layout at the `LayerPanel`'s 260px width, that pan/zoom gating (including the new auto-off) feels responsive rather than laggy against real tile loads, and that auto-off doesn't fire disruptively during normal in-area panning.
- Zoom floor 8 was set from the DEM layers' own curated minimum, not from a measured concurrent-request budget — if that still allows too many simultaneous enables in practice (e.g. two or more DEM areas whose bounds are all visible at once at zoom 8, unlike the tighter single-area historical map sheets), the project owner may want to raise it; not tuned further without real load data.

---

**What was delivered (E3-8):**
- **Confirmed decisions before implementing (per this story's explicit "don't invent silently" gate, same as E3-7):**
  1. **Per-layer `zoom.min` only, no global backstop.** `RasterZoomDTO.min`/`max` are non-nullable backend `int` fields, already set to a real curated value on every existing catalog entry (both De Man sheets: `zoom(12, 19)`) — there's no "unset" case in practice today. A low global backstop would only guard against a future author forgetting to set a sensible value, better caught by review/schema validation on the catalog entry than by a silent runtime fallback that could itself mask a misconfigured sheet.
  2. **Collection "select all" acts only on currently-selectable sheets.** E3-7 never had to answer this (Historical Maps sheets were never gated, so `CollectionToggleCheckbox`/`toggleCollection` always operated on the full unfiltered collection). Once sheets can be hidden, "select all" must be scoped to the rendered/selectable subset — otherwise it would attempt to turn on sheets the gate would immediately reject, and its tri-state (`allVisible`/`someVisible`) would be computed against rows the user can't see.
  3. **A third hint message, not a reuse of E3-7's two strings verbatim.** With a per-sheet floor that varies sheet-to-sheet, E3-7's "Zoom in further..." framing (built around one shared floor) doesn't fit, and since sheets are hidden rather than shown-disabled there's no per-row message anyway, only the section's empty-state fallback. New copy: *"No historical maps match this area/zoom — pan or zoom in to reveal sheets."*
  4. **Priority/size and cadastral-map count carried as High/M provisionally, not resolved here** — not answerable from the code; it's a project-owner call on the strength of the same overload-prevention rationale as E3-7, to revisit once the actual inbound cadastral-map count is known.
- Frontend-only change; no backend/DTO edits needed — `RasterZoomDTO`/its frontend mirror `RasterZoom` (E3-2/E3-3) already carried each entry's real curated `zoom.min`, just unused for gating (and, per E3-7's own scope note, not even carried onto `PhysicalLayerState` yet) until now.
- Added `zoom: RasterZoom` to `PhysicalLayerState` (`useMapInteractions.ts`) — `toLayerState` now copies `entry.zoom` for both Physical and Historical Maps catalog entries, fixing the gap E3-7 explicitly left (`toLayerState` previously dropped it entirely).
- Added `gateHistoricalMapSheet`/`isHistoricalSheetWithinGate` (`useMapInteractions.ts`), mirroring E3-7's `gatePhysicalLayer`/`isPhysicalLayerWithinGate` but reading the zoom floor off `layer.zoom.min` instead of the shared `PHYSICAL_MIN_ZOOM_FLOOR` constant. Both share the same `boundsIntersectViewport` (`mapUtils.ts`, E3-7) for the bounds check.
- `useLayerPanelControl` now derives `gatedHistoricalMapSheets` the same way as `gatedPhysicalLayers` (recomputed off the existing `viewportVersion` counter), and the viewport `moveend`/`zoomend` handler auto-turns off an already-visible Historical Maps sheet that drifts out of its own gate, exactly like E3-7's Physical auto-off.
- `toggleHistoricalMapSheet` now blocks the *on* transition for a gated-off sheet, mirroring `togglePhysicalLayer`'s E3-7 behavior (turning an already-visible sheet off remains ungated).
- `createLayerGroupHandlers`'s shared `move`/`toggleCollection` handlers now take one `isGatedOff` predicate (renamed from E3-7's move-only `shouldSkipForMove`) used for both: `move` skips gated-off neighbors (as E3-7 already did for Physical), and `toggleCollection`'s "select all" now excludes gated-off sheets from both its bulk-set and its all/some-visible computation (decision 2 above) — a behavior change from E3-7's original `toggleCollection`, which had no gating to account for.
- `LayerPanel.tsx` filters `state.historicalMapSheets` to the selectable subset before grouping into atlas collections (mirroring E3-7's Physical-group filtering); because `groupHistoricalMapSheets` only creates a collection entry for sheets present in its input, filtering first also means a collection with zero currently-selectable sheets is hidden entirely with no extra logic needed (decision 3 in the original story draft). A fallback message (decision 3 above) renders when the catalog has sheets but none are currently selectable.
- Scope confirmed unchanged: the exclusive-select NRW WMS basemap radios (`layersConfig` entries like "1926") remain out of scope — no per-entry bounds/zoom metadata exists to gate them on, and they're a different UI element (single-select radios, not independently toggleable catalog entries via `PhysicalLayerState`).

**Impact:**
- A BENELUX/Western-Europe-wide zoom no longer lists every catalog-driven Historical Maps sheet at once — a small future cadastral sheet stays hidden until the user is actually zoomed in near it, even though its bounds are fully contained within (not just overlapping) the wide viewport, which `boundsIntersectViewport` alone couldn't have filtered.
- An already-visible sheet left "on" while the user pans/zooms far away now turns itself off instead of continuing to request WMS tiles for an area it no longer covers, same overload-prevention benefit E3-7 delivered for Physical/DEM.
- An atlas collection with nothing currently selectable (e.g. all its sheets out of view) no longer renders as a confusing empty subsection.
- No change to Physical/DEM behavior — E3-7's global-floor gating for that group is untouched.

**Files changed (frontend):**
- `AncientDataWebGIS_FE/src/components/MapComponent/useMapInteractions.ts` (`PhysicalLayerState.zoom`, `toLayerState` now copies `entry.zoom`, `gateHistoricalMapSheet`, `isHistoricalSheetWithinGate`, `HISTORICAL_MAP_SHEET_GATE_HINT`, `gatedHistoricalMapSheets`, gated `toggleHistoricalMapSheet`, viewport-change auto-off extended to Historical Maps sheets, `createLayerGroupHandlers`'s `toggleCollection` now respects `isGatedOff`), `useMapInteractions.test.ts` (new `gateHistoricalMapSheet` + end-to-end `useLayerPanelControl` Historical Maps gating tests)
- `AncientDataWebGIS_FE/src/components/LayerPanel/LayerPanel.tsx` (filters Historical Maps sheets to the selectable subset before grouping, renders a fallback message when none qualify), `LayerPanel.test.tsx` (updated builders with `zoom`, new gating/empty-collection tests)
- `AncientDataWebGIS_FE/src/components/MapComponent/MapContent.test.tsx` (bumped the shared test map's zoom from 9 to 12 so its default `HISTORICAL_MAP` fixture, whose `zoom.min` is 12, stays selectable under the new gate — unrelated pre-existing tests were asserting WMS-tile/legend behavior, not gating)
- `docs/features/FEATURE-SPEC-BACKLOG.md` (this write-up; status updates)

**Tests:**
- `useMapInteractions.test.ts`: `gateHistoricalMapSheet` pure-function cases (7, mirroring E3-7's `gatePhysicalLayer` suite plus one dedicated "tiny cadastral-scale sheet stays gated at a wide zoom that already selects a city-scale sheet" case exercising the story's core scenario — bounds fully contained within a wide viewport, only the per-entry zoom floor tells the two sheets apart) plus 6 end-to-end tests against `useLayerPanelControl` driving a real (jsdom) `L.Map`: `toggleHistoricalMapSheet` blocks enabling an out-of-view or below-its-own-floor sheet and allows it once panned/zoomed into range; a visible sheet auto-turns off after zooming below its own floor; `moveHistoricalMapSheet` skips a hidden gated-off neighbor; `toggleHistoricalMapCollection` only flips the currently-selectable sheet in a mixed collection, leaving the gated-off one untouched.
- `LayerPanel.test.tsx` (3 new tests): a gated-off sheet is absent from the list with its reason shown as the section's fallback message; a mix of gated/ungated sheets only renders the selectable one; an atlas collection with zero currently-selectable sheets is hidden entirely (not rendered as an empty subsection) while the fallback message still shows.
- Full frontend suite: 156/156 passing. `npm run lint` (eslint, `--max-warnings 0`) and `npx tsc --noEmit` both clean.
- Backend: no application code changed (frontend-only story); full backend suite unaffected.

**Migration notes:** None — no schema, config, or deployment changes. Purely client-side gating logic layered on data (`RasterLayerDTO.zoom`) the backend already served since E3-2.

**ADR:** Not needed. Same reasoning as E3-7: a client-side UI gating rule within the already-decided E3-2/E3-3/E3-7 catalog+`LayerPanel` architecture, introducing no new dependency or infrastructure decision. All confirmed decisions are recorded in this write-up instead.

**Outstanding / manual follow-up (E3-8, project owner):**
- Confirm actual priority/size and the real inbound cadastral-map count (open question 4 above) — this story's High/M was carried provisionally on E3-7's overload-prevention rationale, not from real numbers.
- Live smoke test on a real browser once a reachable backend/GeoServer is available (same limitation as E3-7) — particularly to confirm the "contained bounds at a wide zoom" scenario this story targets actually shows up as expected once a real large-scale (cadastral or otherwise) sheet is published, and that the fallback message reads sensibly at the `LayerPanel`'s 260px width alongside the Physical group's own fallback.

---

**What was delivered (E3-6):**
- **Trigger confirmed with the project owner before implementing (per this story's own deferral note):** `RasterCatalogService.java`'s static list had actually grown to 30 hand-written entries (16 historical-map sheets + 14 DEM/hillshade layers), past the ~20+ threshold E3-2 flagged as the point worth revisiting this decision. Scope was narrowed in discussion: no admin UI this pass (project owner wants catalog edits restricted to themselves, not exposed to any browser-facing surface, even an admin-gated one) and no GeoServer-publishing/backup metadata folded into this table (flagged as a separate, out-of-scope concern — see "Outstanding" below).
- Replaced the static `List<RasterLayerDTO>` with a `raster_layer` Postgres table, read via a new JPA entity (`RasterLayer`) and Spring Data repository (`RasterLayerRepository`), following `MediaAsset`/`MediaAssetRepository`'s established flat-column, `IDENTITY`-id style exactly. `RasterCatalogService.getCatalog()` now maps `findAllByOrderByIdAsc()` through a new `RasterLayerMapper` — `id` ordering preserves the static list's original ordering (notably the hillshade-immediately-before-its-elevation-sibling convention E3-4 established for z-order).
- `RasterLayerCategory` moved from `domain.dto` to `domain.model` (previously `HISTORICAL_MAP`/`DEM` lived in the DTO package) so the new JPA entity and the public DTO can share one enum, matching how `TargetType`/`VisibilityStatus` are shared between `MediaAsset` and `MediaAssetDTO`. Purely a package move — no behavior change, no frontend impact (enum values unchanged).
- Added admin-only write endpoints on the existing `RasterCatalogController`: `GET /api/raster/catalog/admin`, `POST /api/raster/catalog`, `PATCH /api/raster/catalog/{source}`, `DELETE /api/raster/catalog/{source}` — all `@PreAuthorize("hasRole('ADMIN')")`, mirrored by matching `SecurityConfig` URL-pattern rules (placed before the existing `GET RASTER_URL permitAll` wildcard, matching declaration-order semantics already used for `/api/media/admin` vs `/api/media`). `source` (not the internal surrogate `id`) is the path key for update/delete, since it's already the stable identifier the frontend and now the admin API both key off. The existing public `GET /api/raster/catalog` is untouched — same route, same handler, same `RasterLayerDTO` response shape (still id-free).
- Validation (non-blank name/source/attribution, bounds south<north & west<east, zoom min<=max) lives in `RasterCatalogService`, not in the JSON body's bean-validation annotations alone — cross-field bounds/zoom checks can't be expressed as simple field annotations, so a manual check re-validates the merged entity on both create and update (an update that would leave the entity in an invalid state is rejected and not saved, even if the individual changed fields look valid in isolation).
- **No frontend changes.** `RasterService.ts` stays GET-only, `LayerPanel.tsx`/`useMapInteractions.ts`/`MapLegend` are untouched — they only ever consumed the public GET, whose shape didn't change. Per the scope discussion above, there's no admin UI this pass; new layers are registered by the project owner via a direct authenticated API call (see the runbook addition below), which is already strictly less manual work than the PR-per-layer workflow this story set out to remove.

**Impact:**
- Publishing a new raster layer to the catalog no longer requires editing Java code and opening a PR — one authenticated `POST /api/raster/catalog` call (documented in the E3.1 runbook) replaces that workflow. Deleting/renaming/re-bounding an existing entry is the same story (`PATCH`/`DELETE` by `source`).
- `RasterCatalogService`'s public contract (`getCatalog(): List<RasterLayerDTO>`) is unchanged, so E3-3/E3-4/E3-5/E3-7/E3-8's frontend work built on top of it needed zero changes.

**Files changed (backend):**
- `docs/architecture/sql/raster_layer.sql` (new) — table, `updated_at` trigger, grants, 30-row seed transcribed from the static list.
- `src/main/java/com/webgis/ancientdata/domain/model/RasterLayer.java` (new), `RasterLayerCategory.java` (new — moved from `domain/dto`).
- `src/main/java/com/webgis/ancientdata/domain/repository/RasterLayerRepository.java` (new).
- `src/main/java/com/webgis/ancientdata/domain/dto/RasterLayerCreateRequest.java`, `RasterLayerUpdateRequest.java` (new); `RasterLayerDTO.java` (import updated for the enum move).
- `src/main/java/com/webgis/ancientdata/web/mapper/RasterLayerMapper.java` (new).
- `src/main/java/com/webgis/ancientdata/application/service/RasterCatalogService.java` (rewritten — repository-backed, static list and its Javadoc removed, create/update/delete + validation added).
- `src/main/java/com/webgis/ancientdata/web/controller/RasterCatalogController.java` (new admin endpoints added).
- `src/main/java/com/webgis/ancientdata/security/SecurityConfig.java` (new `RASTER_URL` admin rules).
- `src/main/java/com/webgis/ancientdata/constants/ErrorMessages.java` (new `RASTER_LAYER_*` messages).
- `docs/features/E3.1-raster-publishing-pipeline.md` (new "Registering a layer in the catalog" step).

**Tests:**
- `RasterCatalogServiceTests` (13 tests, rewritten as a Mockito-based unit test against a mocked `RasterLayerRepository` — matching `MediaServiceTests`'s established pattern rather than the old plain-static-list style): catalog mapping, create (valid/duplicate-source/invalid-bounds/invalid-zoom/blank-name/blank-attribution), update (partial-merge/not-found/resulting-invalid-state), delete (found/not-found).
- `RasterCatalogControllerTests` (18 tests, `@SpringBootTest`/`@MockitoBean`/`@WithMockUser`, matching `MediaControllerTests`'s pattern): public GET shape/no-proxy-interaction/no-auth-required preserved from E3-2 unmodified in intent (now backed by a mocked service instead of the real static list); 401/403/200/400/409/404 coverage across the four new endpoints.
- Full backend suite: `./gradlew test` green (191 tests, 0 failures).
- Frontend: no changes, so no new frontend tests — existing `RasterService`/`LayerPanel`/`useMapInteractions` tests were not touched and were not expected to need touching (confirms the public contract held; not independently re-run as part of this backend-only story).

**Migration notes (manual DBA step required):**
- `docs/architecture/sql/raster_layer.sql` must be applied manually against the shared PostGIS container (pgAdmin or `psql`) — same process as `media_asset.sql` (E2-0) — **before** deploying the application code that reads from `raster_layer` (the app has no fallback to the old static list once this ships; `spring.jpa.hibernate.ddl-auto=none` means the app will not create the table itself, per `DB-MIGRATION-STRATEGY.md`/`ADR-002`).
- The script includes all 30 seed rows transcribed from the pre-cutover static list (`ON CONFLICT (source) DO NOTHING`, safe to re-run), so applying it is a content-neutral no-op for the existing catalog — no historical-map or DEM entry is lost or needs re-entering.
- Who applies it: the project owner (sole DBA for the shared PostGIS container, per `DB-MIGRATION-STRATEGY.md`).

**ADR:** Yes — `docs/architecture/adr/ADR-013-raster-catalog-storage.md`, explicitly superseding the storage-strategy call recorded in E3-2's write-up above (Option A/static-list chosen over Option B/DB-table at the time). Per `AGENTS.md`'s ADR trigger ("changing storage strategy"), this qualifies where E3-7/E3-8 didn't (pure client-side gating, no storage change).

**Outstanding / manual follow-up (E3-6, project owner):**
- Apply `raster_layer.sql` to the shared PostGIS container before deploying this code (see "Migration notes" above).
- GeoServer publishing/backup: the project owner separately raised wanting the GeoServer store/layer *publishing* work itself (not just catalog display metadata) protected against data loss, so a GeoServer container failure doesn't mean redoing every manual GDAL/admin step. Explicitly kept out of this story's scope (confirmed in discussion) — `raster_layer` only holds the same display fields the old static list held. The actual fix is NAS-level backup of GeoServer's `data_dir`/config (workspaces, stores, layer XML), which `ADR-012`'s "Backup" section and the E3.1 runbook already address for the *source GeoTIFFs* (`rastermaps/`) but not yet for GeoServer's own operational config — worth a dedicated backlog item if it isn't covered by existing NAS-level backup jobs already.
- No admin UI was built (confirmed scope decision, not an oversight) — if that changes later, Part C of this story's original scope (a `RasterService.ts` CRUD-methods + admin panel section, modeled on `MediaGallery`'s E2-UI-2 pattern) is still a reasonable starting point.

