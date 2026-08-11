# ADR-012: Raster Publishing Pipeline — GeoServer via Backend Proxy

**Status:** Accepted
**Date:** 2026-08-10
**Decision makers:** Project owner
**Epic/Story:** E3-1

---

## Context

E3 (Raster / GeoTIFF Delivery) needs a way to publish historical maps/plans and a large (~60–80GB) DEM as tiled layers the frontend can consume, following on from E9's `layersConfig.ts`/`LayerPanel`/`MapLegend` work.

Relevant existing state:

- **GeoServer is already deployed** (`kartoza/geoserver:2.24.1`, service `geoserver` in `AncientDataWebGIS/docker-compose.yml`), data dir bind-mounted at `/volume1/docker/ancientdata/geoserver`, on the same `webgis-edge` Docker network as the `ancientdata` backend. Today it is used **only** for direct QGIS/admin access — LAN IP (`192.168.1.50:2675`) at home, or Cloudflare WARP Private Network routing when off-LAN (`ancientdataworkspace/deploy/README.md` §8). It is not reachable from the public internet.
- The **only** container the public reverse proxy (`ancientdataworkspace/deploy/nginx/nginx.conf`, fronted by `cloudflared`) forwards to is `ancientdata:8080` at `/webGIS/`. That repo's own README states it is "the ONLY stack that should ever be reachable from the internet."
- The frontend already consumes third-party WMS layers (`AncientDataWebGIS_FE/src/components/MapComponent/layersConfig.ts`), so an own-hosted WMS/WMTS layer fits an existing client-side pattern.
- The NAS is resource-constrained: Synology DS923+, **4GB RAM**, 2×WD Red HDD in RAID (per ADR-001). GeoServer already competes for that RAM; an 80GB DEM served naively (full-resolution, uncached, on-the-fly) would be untenable on this hardware.
- Per `DB-MIGRATION-STRATEGY.md`, the shared PostGIS container is externally (QGIS-)managed; by extension, GeoServer's data dir is also managed outside this application's normal deploy/backup flow (see Consequences).

### Constraints

- Do not require edits to the shared `ancientdataworkspace` edge-proxy/tunnel stack (out of scope for this repo, and explicitly the most internet-exposed piece of the whole deployment).
- Do not change GeoServer's current exposure posture (LAN/WARP-only) — no new public port.
- Must be viable on 4GB RAM for both small historical-map rasters and one very large DEM.

---

## Decision

**Use GeoServer (already running) as the sole raster authoring/rendering system, with its bundled GeoWebCache (GWC) for tile caching, fronted by a new thin read-only proxy endpoint in the existing public `ancientdata` Spring Boot backend.**

- `GET /api/raster/**` in the backend forwards GET requests container-to-container to `geoserver:8080/geoserver/**` over the internal `webgis-edge` Docker network. GeoServer itself gets no new public port and no edge-proxy route — its admin UI stays exactly as reachable as it is today (LAN/WARP-only).
- The proxy explicitly denies any upstream path containing `/rest/` or `/web/` (GeoServer's admin REST API and web admin UI), only forwarding OGC service traffic (WMS/WMTS/GWC tile/capabilities requests).
- Source GeoTIFFs are converted to **Cloud-Optimized GeoTIFF (COG)** — internally tiled, with pre-built overview pyramids (`gdal_translate -of COG` / `gdaladdo`) — before being published as a GeoServer store. This is what makes on-the-fly rendering (Option below) viable for the DEM: GeoServer reads only the overview level and internal tile needed for a given request instead of decoding the full raster. The DEM-specific conversion work is scoped to **E3-4**; this ADR only establishes it as a required pipeline step.
- GeoTIFF sources living on GeoServer's NAS data dir (`/volume1/docker/ancientdata/geoserver`) are backed up at the **NAS level** (e.g. Synology HyperBackup pointed at that path), not by the application. This mirrors how the Postgres data volume itself is protected — a large, infrequently-changing, authoritative dataset — rather than treating it like user-uploaded media (which `NasBackupService`/ADR-006 already owns end-to-end app-side). Pointing HyperBackup (or equivalent) at `/volume1/docker/ancientdata/geoserver` is a manual NAS-config action, not app code.

---

## Alternatives Considered

### A. Expose GeoServer WMS directly via the shared edge proxy

- **Rejected.** Requires editing `ancientdataworkspace`'s edge stack (the one piece of infrastructure explicitly meant to stay minimal/public-only-by-design) and grows GeoServer's admin attack surface. Also has no caching by default — every request re-renders, which is risky for an 80GB DEM on 4GB RAM.

### B. Pre-tile to a static XYZ pyramid, GeoServer stays purely internal

- **Deferred, not rejected.** Cheapest possible serving cost (static files, same pattern as `ADR-001` media), and GeoServer never needs any new exposure at all. Rejected as the primary approach because it duplicates storage (source GeoTIFF + generated tile pyramid — significant for an 80GB DEM) and requires a new regeneration pipeline; restyling means regenerating tiles rather than an SLD edit. Remains a fallback if COG + GWC caching (this ADR's approach) proves insufficient for the DEM once E3-4 is attempted — see "When to Revisit."

### C. Extend `NasBackupService` to also cover GeoServer's data dir (backup sub-decision)

- **Deferred.** Would require read-only bind-mounting GeoServer's data dir into the `ancientdata` container and generalizing a currently single-purpose service to take multiple source roots, plus a weekly in-app `Files.walk` over a 60–80GB tree on a 4GB-RAM box. NAS-level backup (this ADR's decision) achieves the same protection with zero new app code or container config.

---

## Consequences

### Positive

- No changes to the shared `ancientdataworkspace` edge-proxy repo.
- GeoServer's admin exposure is unchanged from today (LAN/WARP-only).
- Reuses infrastructure and RAM/CPU budget already committed, rather than standing up a new service.
- One authoring system (GeoServer) for both historical maps and the future DEM; styling changes (SLD) don't require regenerating anything.
- GWC caching means repeat tile requests at the same z/x/y never re-hit GeoServer's raster reader.

### Negative

- Adds a new proxy code path in the backend (streaming/timeout/allowlisting behavior to maintain).
- GeoServer's `GetCapabilities` documents will self-reference GeoServer's internal address unless its proxy base URL is configured to match the public `/api/raster` path — a manual GeoServer admin config step, not yet done (see runbook).
- COG conversion is a manual/GDAL step per raster, run before publishing — no automated ingestion pipeline exists yet.
- NAS-level backup coverage of GeoServer's data dir depends on the project owner confirming/configuring it (existing HyperBackup jobs, per `ADR-001`, are documented as covering `/volume1/docker/postgis_pgadmin_stack/`, a different parent path than `/volume1/docker/ancientdata/geoserver`).

### When to Revisit

- If COG + GWC caching still performs unacceptably for the DEM once E3-4 is attempted (4GB RAM proves too tight even with overviews) → fall back to Option B (pre-tiled static pyramid) for the DEM specifically, while keeping this ADR's approach for smaller historical-map rasters.
- If audience/traffic grows enough to need a CDN in front of tiles → revisit alongside any broader storage/CDN migration (see `ADR-001`'s "When to Revisit").

---

## Implementation References

- Proxy: `src/main/java/com/webgis/ancientdata/web/controller/RasterProxyController.java`, `application/service/RasterProxyService.java`, `config/GeoServerProxyConfig.java`
- Security: `src/main/java/com/webgis/ancientdata/security/SecurityConfig.java` (`RASTER_URL` rule)
- Config: `src/main/resources/application.properties` (`geoserver.internal-url`), `.env.example` (`GEOSERVER_INTERNAL_URL`)
- Runbook (remote GeoServer access, COG note, manual admin/backup steps): `docs/features/E3.1-raster-publishing-pipeline.md`
- Existing GeoServer service definition: `docker-compose.yml`

---

## Addendum (2026-08-11): Dedicated `rastermaps` directory, separate from GeoServer's data_dir

**Context:** While publishing the first real raster (E3-1's own dogfooding, ahead of E3-2), the flat "GeoTIFF sources live under GeoServer's data_dir" framing from the original Decision turned out to be imprecise — `data_dir` also holds GeoServer's own operational/regenerable state (most significantly, the GWC tile cache), which a NAS backup job has no reason to capture.

**Refinement (does not change the Decision above):** Source GeoTIFFs live in their own top-level NAS directory, `/volume1/docker/ancientdata/rastermaps`, bind-mounted into the `geoserver` container read-only at `/opt/geoserver/data_dir/rastermaps` — nested from the container's point of view (so GeoServer's store-creation file browser can reach it), but a fully independent directory on the NAS host, so the NAS backup job from this ADR's original Decision can target it precisely without also backing up GeoServer's regenerable cache.

**Files changed:** `docker-compose.yml` (new `rastermaps` bind mount on the `geoserver` service), `docs/features/E3.1-raster-publishing-pipeline.md` (updated paths).
