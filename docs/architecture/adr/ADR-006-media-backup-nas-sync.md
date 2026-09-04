# ADR-006: Media Backup Strategy — NAS Filesystem Sync (supersedes ADR-001 backup section)

**Status:** Accepted  
**Date:** 2026-06-11  
**Decision makers:** Project owner  
**Epic/Story:** E2-BACKUP-NAS-1/2/3  
**Supersedes:** backup section of ADR-001 (the offsite backup approach only; primary storage decision stands)

---

## Context

ADR-001 selected NAS filesystem as primary media storage and specified Google Drive (via `rclone`) as the offsite backup destination. Before implementation began, two factors changed the calculus:

1. **Operational coupling:** `rclone` requires OS-level installation, cron configuration outside the Spring application, and separate credential management for a Google service account. This breaks the self-contained Docker Compose model and introduces a third-party CLI dependency.
2. **Scope creep:** A scheduled Google Drive sync is effectively a second background process with its own lifecycle, monitoring, and failure modes. For a single-operator home-hosted deployment, this overhead outweighs the benefit.

The primary requirement is that uploaded media files are mirrored to a second persistent path (the NAS backup volume) so that a disk failure on the primary volume does not result in total data loss.

---

## Decision

**Replace the Google Drive / rclone backup plan with a Spring-native NAS filesystem sync service.**

The `NasBackupService` component (introduced alongside this ADR) runs inside the existing Spring Boot container and mirrors the application's `media.storage-path` directory to a second NAS-mounted path (`backup.nas.mount-path`). 

Key design points:

- **Transport:** local filesystem copy via `java.nio.file.Files` — no network API, no credentials, no rate limits.
- **Schedule:** `@Scheduled(cron = "${backup.nas.sync-cron:0 0 3 * * SUN}")` — configurable via env var, default weekly on Sunday at 03:00 UTC.
- **Sync logic:** bidirectional; copies new/modified files (by last-modified timestamp), deletes remote orphans whose local source was removed.
- **Manual trigger:** `POST /api/backup/sync` (`ADMIN`-only) via `BackupController` — enables on-demand smoke testing and operator-initiated backup without waiting for the cron window.
- **Toggle:** `backup.nas.enabled=false` by default; activated via `.env`.
- **Configuration class:** `NasBackupConfig` (`@ConfigurationProperties(prefix = "backup.nas")`) — no hardcoded paths in Java source.

### What this replaces

| Old plan (ADR-001) | New plan (this ADR) |
|--------------------|---------------------|
| `rclone` CLI installed on NAS OS | Spring `NasBackupService` inside existing container |
| Google service account credentials in `.env` | No additional credentials; uses existing NAS bind mounts |
| `docs/architecture/backup-media-to-gdrive.sh` (rclone script) | `NasBackupService.sync()` triggered by cron or `POST /api/backup/sync` |
| Requires NAS task scheduler / external cron | Spring `@Scheduled` within application lifecycle |

> Note: `backup-media-to-gdrive.sh` is retained in the repository as a reference/fallback script but is **not the active backup mechanism**.

---

## Alternatives Considered

### A. Keep Google Drive / rclone as planned in ADR-001

- **Rejected.** Requires `rclone` binary inside the Docker image (adds image size and OS dependency) or on the NAS host OS (breaks container encapsulation). Google service account credentials add a secret rotation burden. For a single-operator NAS deployment, this operational overhead is disproportionate to the benefit.

### B. Synology HyperBackup only (NAS-level backup)

- **Retained as a complementary layer, not the primary application backup.** HyperBackup backs up the entire volume, not specifically the media directory delta. It does not provide application-controlled triggers or the ability to query sync status via API. ADR-001 already notes HyperBackup as a background layer.

### C. MinIO / S3-compatible replication

- **Deferred** (as in ADR-001). Still a valid future path; `MediaStorageService` interface is untouched by this decision.

---

## Consequences

### Positive

- Zero new dependencies — pure Spring + JDK `java.nio` file operations.
- Admin-controllable via the existing API (`POST /api/backup/sync`) — testable as a smoke test without NAS-OS access.
- Enabled/disabled via env var; safe-off by default.
- No credential management beyond what already exists.
- Backup logic is visible, debuggable, and unit-testable (see `NasBackupServiceTests`).

### Negative

- **Local redundancy only (for the sync path).** The backup destination is a second NAS mount, not an offsite cloud store. A fire or flood affecting the NAS hardware would take both copies. Synology HyperBackup to an external drive or cloud remains necessary for true offsite disaster recovery.
- **`nasBackupRoot` is initialized lazily in `@PostConstruct init()`** — if `init()` was not called (impossible in production via Spring, but relevant in certain test setups), `sync()` silently no-ops with a warning log. See code review note below.
- **`POST /api/backup/sync` returns 200 OK even if the mount path became unavailable after startup** (e.g., NAS disconnected after init). The `sync()` guard logs a warning and returns but the HTTP response is still `{"status":"ok"}`. This is a known limitation; operators should monitor application logs for `"NAS backup mount not available"` warnings.

### When to Revisit

- If the project moves off the home NAS to cloud hosting → migrate to S3/MinIO (see ADR-001, alternative C).
- If an offsite copy is required programmatically → re-evaluate rclone or an S3 sync layer.
- If audience growth demands CDN delivery → storage migration will likely accompany the CDN choice.

---

## Code Review Notes (recorded at decision time)

1. **`nasBackupRoot` is package-private and non-final** (`NasBackupService.java:27`). The field is set in `init()` and read in `sync()`. This is safe under Spring's `@PostConstruct` guarantee but exposes mutable state at package level. A future refactor could initialize it eagerly in the constructor (with a null-check fallback) to remove the temporal coupling.

2. **Silent no-op when mount path unavailable after init.** `BackupController.triggerSync()` gates on `backupConfig.isEnabled()` but not on whether `nasBackupRoot` was successfully resolved. If the NAS becomes unavailable after app startup, the endpoint returns `{"status":"ok","message":"Backup sync triggered"}` while `sync()` internally logs a warning and skips work. Consider adding a `/api/backup/status` endpoint to expose this state.

3. **`NasBackupConfig.getSyncCron()` returns `null` in unit-test contexts** (no Spring context → no property injection). The `@Scheduled` annotation reads directly from Spring Environment and is unaffected. The field is only `null` in unit tests that never read it.

4. **No validation that `backup.nas.mount-path` ≠ `media.storage-path`.** If both env vars are set to the same directory, `sync()` would attempt to copy files onto themselves, triggering `REPLACE_EXISTING` no-ops. Not harmful but confusing. A startup check in `init()` could guard this.

---

## Implementation References

- Config class: `src/main/java/com/webgis/ancientdata/config/NasBackupConfig.java`
- Service: `src/main/java/com/webgis/ancientdata/application/service/NasBackupService.java`
- Controller: `src/main/java/com/webgis/ancientdata/web/controller/BackupController.java`
- Security: `src/main/java/com/webgis/ancientdata/security/SecurityConfig.java` (`BACKUP_URL` rule)
- Tests: `src/test/java/com/webgis/ancientdata/backuptests/NasBackupServiceTests.java`
- Properties: `src/main/resources/application.properties` (`backup.nas.*` keys)
- Retained (non-active): `docs/architecture/backup-media-to-gdrive.sh`
- Smoke test: `docs/features/NAS-BACKUP-SMOKE-TEST.md`

---

## Addendum (2026-07-10): Database backup + manual trigger UI + status/staleness (E2-BACKUP-NAS-4)

**Context:** ADR-006 only covered media file sync — the database itself was never backed up by the application; it relied solely on the external `scripts/backup.sh` run via NAS cron. There was also no way to trigger a backup from the UI, nor any way to see when the last backup ran.

**Decision:** Extend the same in-app, credential-free approach used for media to the database:

- **`DbBackupService`** shells out to `pg_dump` (added to the Docker image via `postgresql-client`), using the same `DATABASE_HOST/PORT/NAME/USER/PASSWORD` env vars as `scripts/backup.sh` (superuser credentials, broader read access than the app's own `DB_USER`). Writes a timestamped `.sql` file to `backup.db.output-path` (`/backup/db`). **No retention/cleanup is implemented** — dumps accumulate; cleanup is manual for now given current DB size.
- **`backup_history` table** (new, manually-applied SQL per `DB-MIGRATION-STRATEGY.md` — see `docs/architecture/sql/backup_history.sql`) records every DB and media backup attempt (type, outcome, started/finished timestamps, message), including failures — so "never backed up" and "last attempt failed" are distinguishable.
- **`POST /api/backup/sync`** now triggers both `DbBackupService` and `NasBackupService` and reports both outcomes.
- **`GET /api/backup/status`** (new) reports the latest run per type plus an `isStale` flag, computed against `backup.staleness-threshold-hours` (default 192h/8 days — set slightly above the weekly media cron cadence so normal scheduling never falsely flags as stale). A type that has never run is always considered stale.
- **Frontend:** `AdminPanel.tsx` gained a "Back up now" button (triggers both backups on demand — the primary motivation being "back this up immediately after a night of data entry") and a status panel showing last-backup time/outcome per type with a visual warning when stale.

**Consequences:**
- Positive: the DB is finally backed up through a path the app controls and can report on; admins get immediate, visible feedback instead of relying on log files.
- Negative: no automated retention for app-triggered DB dumps yet (deferred, tracked as a follow-up); `pg_dump` invocation depends on network/DNS reachability to the shared PostGIS host from the app container, same as the existing JDBC connection.
- This does not change the offsite/cloud-backup gap noted above (Alternative A) — dumps still land on the same NAS. Synology HyperBackup (or similar) remains the recommended path for true offsite redundancy.

---

## Addendum (2026-09-04): Fix outcome reporting on `POST /api/backup/sync`

**Context:** This ADR's own "Negative" consequences section (item 3, and E2-BACKUP-NAS-4's addendum) already
flagged that `POST /api/backup/sync` returns `{"status":"ok"}` even when the underlying sync/dump actually
failed — `BackupController.runAndReport()` never inspected the real outcome. Investigating a report that the
admin panel's "Back up now" button "did not work," a second, more severe instance of the same root cause was
found: `NasBackupService.recordHistory()` / `DbBackupService.recordHistory()` both call
`backupHistoryRepository.save(...)` **outside** any try/catch. Because `backup_history` is schema owned
outside this app (Flyway disabled, manual SQL — see `DB-MIGRATION-STRATEGY.md` and
`docs/architecture/sql/backup_history.sql`), if that table was never applied to the live production database,
every backup attempt threw an uncaught `DataAccessException` straight out of `run()`/`sync()`, causing the
endpoint to return `500` and the frontend to show "Failed to trigger backup. Please try again." — a much more
direct explanation for "the button did not work" than the previously-documented false-positive.

**Decision:**
- `DbBackupService.run()` / `NasBackupService.sync()` now return a `BackupRunResult` (outcome + message)
  instead of `void`. `BackupController.runAndReport()` builds its response from this real result
  (`"ok"`/`"error"`) instead of a hardcoded success message.
- `recordHistory()` in both services now wraps `backupHistoryRepository.save(...)` in its own try/catch: a
  persistence failure is logged but no longer prevents the real pg_dump/sync outcome from reaching the
  caller. The trade-off is explicit — a `backup_history` write failure means that run won't show up in
  `GET /api/backup/status`/the admin panel's status list, but it will no longer take down the whole
  request or misreport the backup itself as having failed.

**Consequences:**
- Positive: the admin panel now shows the real outcome/message on the same click, and a missing/broken
  `backup_history` table degrades to "status not recorded" instead of "backup trigger crashes."
- Negative: still no alerting if `backup_history` silently stops recording (only a log line) — acceptable
  for a single-operator deployment, revisit if this proves easy to miss in practice.
- This closes item 3 of this ADR's "Negative" consequences and the "Negative" note in the E2-BACKUP-NAS-4
  addendum above (both superseded by the fix described here).
- See `docs/BACKUP-STRATEGY.md`'s Troubleshooting table for the operator-facing symptom description.

## Addendum (2026-09-04): Off-device redundancy resolved via Cloud Sync → Google Drive

The "Negative" consequence above ("Synology HyperBackup... remains the recommended path for true offsite
redundancy") and Alternative B ("retained as a complementary layer, not the primary application backup")
are now actioned — but via Synology **Cloud Sync** to Google Drive rather than Hyper Backup/USB Copy to a
physical drive (project owner preferred not to depend on physical media attached to the NAS).
`docs/BACKUP-STRATEGY.md` Part 3 documents configuring Cloud Sync to push both
`/volume1/docker/ancientdata/backup` and `/volume1/docker/ancientdata/backups` to a dedicated Google Drive
folder on a schedule. This is DSM-side configuration, not application code, so it isn't tracked as a code
change here — see the linked doc for setup steps.

**Why this doesn't repeat the original Google Drive rejection (this ADR's "Alternatives Considered" A, and
the abandoned `GoogleDriveBackupService`/`plan-nasBackupMigration.prompt.md`):** that attempt used a Google
Drive API **service account**, which has no storage quota of its own on personal (non-Workspace) Drive,
hence `storageQuotaExceeded`. Cloud Sync authenticates as the project owner's own Google account via OAuth
and writes against that account's normal Drive storage — a fundamentally different auth path, not a retry
of the same blocked approach. It also doesn't reintroduce the "operational coupling" objection from
Alternative A (`rclone` as an OS-level dependency with separate credential management) — Cloud Sync is a
first-party DSM package configured entirely through the GUI, the same operational tier as Hyper Backup,
which this ADR already accepted as the recommended offsite layer.


