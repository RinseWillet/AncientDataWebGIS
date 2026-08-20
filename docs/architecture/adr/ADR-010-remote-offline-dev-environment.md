# ADR-010: Remote & Offline Developer Environment

**Status:** Accepted
**Date:** 2026-08-06
**Decision makers:** Project owner
**Epic/Story:** E7 (Remote & Offline Dev Environment) — see `docs/features/FEATURE-SPEC-BACKLOG.md`

---

## Context

The shared PostGIS database only exists on the home NAS and is deliberately **never**
exposed via its public IP/port — only reachable on the home LAN (`192.168.1.50:2665`)
or, when off-site, via Cloudflare WARP private network routing (already documented in
`ancientdataworkspace/deploy/README.md` §8 for QGIS/GeoServer access).

This meant that running a local backend smoke test from anywhere other than the home
LAN (e.g. from an office) failed with a `SocketTimeoutException` — the local `.env` had
drifted to point at the NAS's public IP, which was never going to answer on that port,
and there was no documented/scripted fallback for developing with no NAS access at all
(network outage, NAS offline, or simply not wanting to depend on it for quick UI/logic
iteration).

---

## Decision

Support two explicit, documented ways to develop away from home, instead of one
undocumented one:

1. **Cloudflare WARP (primary)** — reuses the Tunnel/Zero Trust setup already in place
   for GeoServer/QGIS access. Once configured, `192.168.1.50:2665` is reachable from
   anywhere with no `.env`/code changes — this is the "develop against the real data"
   path and should be preferred whenever real data matters (dashboard numbers, media
   galleries, etc.).
2. **Local throwaway PostGIS container (fallback)** — a new optional
   `docker-compose.local-dev.yml` spins up a disposable `postgis/postgis` container,
   auto-seeded via `docs/architecture/sql/local-dev-seed.sql` with a handful of
   synthetic sites/roads/a dev admin user. Paired with a new `local-dev` Spring profile
   (`application-local-dev.properties`) that forces NAS/DB backup features off and
   points media storage at a separate local folder. This path requires no network
   access at all and is for UI/logic development where realistic *volume* or *accuracy*
   of data doesn't matter.

Both paths are documented in the backend `README.md` ("Developing Away From Home /
Office") and `.env.example`, so the correct `DB_URL` convention (LAN IP, never public
IP) is discoverable without re-deriving it from a stack trace.

---

## Alternatives Considered

### A. Forward the database port on the router for remote access

- **Rejected.** Directly contradicts the existing, deliberate security posture
  (`docker-compose.yml` comments, deploy README §6/§8) of never exposing the DB port to
  the internet. WARP already solves "reach it from anywhere" without this risk.

### B. Flyway-managed local schema, kept in lockstep with the real DB

- **Rejected/deferred.** ADR-002 already establishes that the real schema is
  QGIS-owned and Flyway is permanently disabled for it. Introducing Flyway just for a
  local convenience DB would add a second migration mechanism to maintain, with no
  guarantee it stays in sync with schema changes applied directly via QGIS. Plain
  `CREATE TABLE IF NOT EXISTS` scripts (mirroring the existing `docs/architecture/sql/*.sql`
  files) are simpler and make the "not a schema authority" caveat unambiguous.

### C. Synology WireGuard VPN instead of/alongside Cloudflare WARP

- **Deferred, not rejected.** Documented as a valid alternative in the deploy README
  already; not duplicated here since WARP is the primary recommendation and already
  set up for GeoServer/QGIS use.

---

## Consequences

### Positive

- No more silent `.env` drift toward the (always-unreachable) public IP — the
  LAN-IP-only convention is now written down in two places (`README.md`, `.env.example`).
- Developers without home-LAN/WARP access yet still have a fully offline path to run
  and test the app end-to-end.
- The local-dev schema mirror makes explicit (via comments + ADR-002 cross-reference)
  that it is not a source of schema truth, avoiding confusion with the real DB.

### Negative

- The local-dev seed schema must be manually kept in sync with real schema changes
  applied via QGIS/DBA scripts — there is no automated check for drift.
- Seed data is synthetic; not suitable for testing data-accuracy-sensitive features
  (e.g. exact dashboard aggregate values, real photo galleries).

### When to Revisit

- If the local-dev schema mirror drifts noticeably from the real schema (e.g. after
  several uncoordinated QGIS-side changes), consider a lightweight schema-diff check
  or regenerating the mirror from a `pg_dump --schema-only` snapshot instead of
  hand-maintained SQL.
- If WARP setup friction turns out to be higher than expected in practice, consider
  documenting the WireGuard alternative (Alternative C) in full instead of just
  referencing it.

---

## Implementation References

- `docker-compose.local-dev.yml` (new)
- `docs/architecture/sql/local-dev-seed.sql` (new)
- `src/main/resources/application-local-dev.properties` (new)
- `.env.example` (updated with LAN-IP convention + local-dev alternative)
- `README.md` "Developing Away From Home / Office" section
- `ancientdataworkspace/deploy/README.md` §8 (WARP setup steps, pre-existing)
- `docs/architecture/adr/ADR-002-db-migration-strategy.md` (schema ownership context)

