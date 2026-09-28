# ADR-016: 2026-08-20 NAS Host-Wide Outage — Postmortem and Resilience Follow-up

**Status:** Accepted
**Date:** 2026-09-28
**Decision makers:** Project owner
**Epic/Story:** E13 (`E13-1` .. `E13-5`)

---

## Context

On 2026-08-20, loading a large DEM layer through GeoServer/WMS caused enough host
memory pressure on the NAS (Synology DS923+, 4GB RAM — see `ADR-012`) that the
Synology Docker daemon (`dockerd`) itself hung and was replaced by its supervising
process. Because every container across all three compose stacks on the box
(`ancientdata`/`geoserver` in this repo's `docker-compose.yml`, the `postgis_admin`
stack, and the `ancientdataworkspace` edge-proxy/tunnel stack) runs under the same
single `dockerd`, this was a **daemon-level failure, not a per-container kernel
OOM-kill** — confirmed via `dmesg`/cgroup/daemon-log inspection — and it stopped
every container in every stack at once. None of those services had a `mem_limit`
or a restart policy that survives a clean daemon-triggered exit, so nothing came
back on its own.

The proximate trigger traced back to `RasterProxyService.forward()`
(`src/main/java/com/webgis/ancientdata/application/service/RasterProxyService.java`),
the read-only GeoServer proxy this app exposes per `ADR-012`'s architecture: it
called `response.getBody().readAllBytes()` inside its `RestClient.exchange()`
callback, fully buffering each upstream WMS response into a `byte[]` in the JVM
heap before returning it. Against the live GeoServer (`192.168.2.13:2675`), the
actual implicated layer — `ancientdata:dem_Mönchengladbach-Neuss-hillshade`, a
DGM1-resolution DEM/hillshade COG — returns a 16.7MB GeoTIFF for a single
4096×4096 `GetMap` tile request. That is a real, repeatable multi-megabyte-plus
payload class on a 4GB-RAM host with no per-request size bound, not a hypothetical
edge case.

`mem_limit` and `restart: unless-stopped` were applied immediately to every
service across all three stacks as a stopgap fix, ahead of any deeper
investigation. Epic E13 tracks the follow-up work this incident exposed:
credential rotation (`E13-1`), externalizing infra compose credentials
(`E13-2`), evaluating Docker daemon `live-restore` (`E13-3`), fixing the actual
memory-pressure trigger (`E13-4`), and this postmortem (`E13-5`).

---

## Decision

**Adopt a layered defense: fix the root-cause memory trigger in application code,
keep the daemon/container restart policy as a baseline safety net across all
three stacks, and do not rely on Docker `live-restore` as a mitigation for this
specific failure mode on this NAS.**

1. **Root cause fixed at the source ([[feedback_fix_at_source]]-style):**
   `RasterProxyService.forward()` (`E13-4`) was changed to stream upstream
   GeoServer responses instead of buffering them. It now uses `RestClient`'s
   `exchange(fn, false)` overload (`close=false`, keeping the upstream
   `ClientHttpResponse` open past the callback) and returns
   `ResponseEntity<StreamingResponseBody>` from both `RasterProxyService.forward()`
   and `RasterProxyController.proxy()`. The `StreamingResponseBody` pipes
   `response.getBody()` to the servlet output stream via `InputStream#transferTo`
   (bounded ~8KB buffer) inside `try (response)`, so the upstream connection is
   released once streaming ends regardless of outcome. Headers/status/caching
   logic (GWC diagnostic header passthrough, the `public, max-age=3600` default)
   are still read synchronously before streaming starts; upstream 4xx/5xx and
   connection failures still map to the same status codes as before. This is the
   fix that actually prevents a repeat of this specific trigger — a large DEM
   response can no longer materialize as a multi-megabyte `byte[]` in the app's
   heap.
2. **`mem_limit` + `restart: unless-stopped` kept as the baseline defense, not
   removed once the root cause was fixed.** Confirmed live in this repo's
   `docker-compose.yml`: `ancientdata` (`mem_limit: 512m`,
   `JAVA_TOOL_OPTIONS=-Xmx384m -Xms192m`) and `geoserver` (`mem_limit: 1024m`,
   `restart: unless-stopped` on both). These bound each container's own memory
   footprint and ensure a container that does exit comes back automatically —
   the layer that protects against *other*, not-yet-identified memory-pressure
   sources, not just the one found in `E13-4`.
3. **Docker daemon `live-restore` (`E13-3`) is enabled but explicitly not relied
   upon.** `live-restore: true` was added to the real, running config
   (`/var/packages/ContainerManager/etc/dockerd.json`, a symlink to
   `/volume1/@appconf/ContainerManager/dockerd.json` — not the decoy template at
   `/volume1/@appstore/ContainerManager/config/dockerd.json`) and confirmed active
   via `docker info`. A controlled test (restarting just the
   `pkg-ContainerManager-dockerd` systemd unit) showed it does **not** protect
   this NAS: Synology's Container Manager spawns `containerd` as a child process
   of `dockerd`'s own startup rather than as an independently-supervised systemd
   service, so every `containerd-shim` process shared `dockerd`'s exact restart
   timestamp and all 8 containers came back with uptimes reset to ~1 minute. This
   is unlike stock Debian/Ubuntu Docker, where `docker.service`/`containerd.service`
   are genuinely decoupled and live-restore's protection actually holds. It is
   left enabled as a harmless backstop for a narrower case (e.g. a kernel OOM-kill
   that hits only the `dockerd` PID) but is not counted on for a repeat of this
   incident's failure mode.
4. **Credential hygiene, addressed as a related but separable hardening pass
   (`E13-1`/`E13-2`):** the outage's investigation session displayed the live NAS
   `compose.yaml` in plaintext, exposing a password reused across PostGIS root,
   pgAdmin admin, and GeoServer admin. Rotated all three, and migrated
   `postgis_admin/compose.yaml` to the `${VAR}`-from-`.env` pattern (chmod 600
   on both the compose file and `.env`). Not a memory-pressure mitigation, but
   bundled into E13 because it was surfaced by the same incident response.

---

## Alternatives Considered

### A. Rely on Docker `live-restore` as the primary daemon-resilience mechanism

- **Investigated (`E13-3`) and rejected as insufficient.** On stock Linux Docker
  this would be the standard fix for "daemon restarts shouldn't kill containers."
  On this Synology Container Manager build (24.0.2-1706, DSM 7.4-90000),
  `containerd` is not independently supervised — it dies with `dockerd` — so
  live-restore's core guarantee does not hold here. See Decision §3.

### B. Stop at the `mem_limit`/`restart: unless-stopped` stopgap; treat E13-3/E13-4 as optional

- **Rejected.** The stopgap bounds *blast radius* (a container that dies comes
  back) but does nothing to prevent the daemon-level hang itself, which is a
  more severe failure mode than a single container OOM-kill — it takes down
  unrelated stacks (`postgis_admin`, the edge-proxy) that had nothing to do with
  the triggering request. Fixing the actual unbounded-buffering trigger
  (`E13-4`) removes the recurring cause instead of only softening its blast
  radius.

---

## Consequences

### Positive

- The specific trigger that caused this outage — unbounded buffering of a large
  upstream raster response — is fixed at the source; a repeat `GetMap` request
  against the same or a larger DEM layer streams instead of allocating a
  proportional `byte[]`.
- `mem_limit`/`restart: unless-stopped` now cover every service in this repo's
  stack as a baseline, independent of whether the specific trigger they were
  reacting to has been fixed — defense in depth rather than a single point of
  protection.
- `live-restore`'s actual (non-)protection on this specific NAS packaging is now
  documented, so it isn't mistaken for a solved mitigation in a future incident
  review.
- The password-reuse anti-pattern surfaced by the investigation was closed
  across all three previously-shared credentials, not just the one directly
  implicated.

### Negative

- The daemon-level failure mode itself — `dockerd` hanging under host memory
  pressure — has no direct fix; the response is entirely preventative (don't let
  any one container drive the host into memory pressure) rather than making
  `dockerd` itself more resilient. A sufficiently large memory-pressure event
  from an unrelated cause could still, in principle, reproduce a daemon-level
  hang.
- No host-level memory-pressure monitoring/alerting was added as part of this
  remediation — a recurring pressure event would still only be visible after
  the fact (containers having restarted) rather than via a proactive signal.
  Out of scope for E13; worth a dedicated backlog item if it recurs.
- `mem_limit` values (`512m` for `ancientdata`, `1024m` for `geoserver`) were set
  as a stopgap during incident response, not derived from measured peak usage —
  they could theoretically be too tight for a legitimate future workload and
  cause a container-level OOM-kill of their own. Not observed yet.

### When to Revisit

- If a host-wide outage recurs despite the streaming fix and `mem_limit`
  baseline, the next investigation should look for host-level memory-pressure
  monitoring/alerting (the gap noted above) rather than re-deriving root cause
  from scratch.
- If the NAS deployment migrates to k3s (`ADR-014`'s epic, `E12`), revisit
  whether `mem_limit`/`restart: unless-stopped` map cleanly to k3s resource
  limits/restart policies, and whether the daemon-coupling issue behind
  `live-restore`'s limitations here even applies under a different container
  runtime.

---

## Implementation References

- Streaming fix: `src/main/java/com/webgis/ancientdata/application/service/RasterProxyService.java`,
  `web/controller/RasterProxyController.java`
- Test coverage: `src/test/java/com/webgis/ancientdata/rastertests/RasterProxyServiceTests.java`
  (`forward_StreamsLargeUpstreamResponseInBoundedChunksRatherThanFullyBuffering`)
- Stopgap mitigation: `docker-compose.yml` (`mem_limit`, `restart: unless-stopped`
  on `ancientdata` and `geoserver`); `/volume1/docker/ancientdata/postgis_admin/compose.yaml`
  and the `ancientdataworkspace` edge-proxy stack carry the same mitigation on
  the NAS (not in this repo's checkout)
- `live-restore` config: `/volume1/@appconf/ContainerManager/dockerd.json`
  (NAS-only, not in this repo)
- Credential rotation/externalization: `postgis_admin/compose.yaml` + `.env`
  (NAS-only), `docs/features/FEATURE-SPEC-BACKLOG.md` E13-1/E13-2 write-ups
- Raster pipeline architecture this incident implicated: `ADR-012`
- Backlog: `docs/features/FEATURE-SPEC-BACKLOG.md` (Epic E13, `E13-1`–`E13-5`)
