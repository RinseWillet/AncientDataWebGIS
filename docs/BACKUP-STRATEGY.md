# AncientDataWebGIS Backup Strategy

## Overview

AncientDataWebGIS uses a **two-tier backup approach**:

1. **In-app backup service** (`NasBackupService` + `DbBackupService`) — syncs media files and dumps the database, on a schedule or on-demand via the admin UI/API
2. **Full backup script** — backs up both database and media as timestamped archives, run externally via NAS cron

This ensures both continuous protection and point-in-time recovery.

---

## Part 1: In-App Backup Service (Spring)

### How It Works

- **Media (`NasBackupService`)** — runs on a configurable schedule (default: weekly, Sunday 3 AM UTC):
  - Walks the local media directory (`/app/media`)
  - Copies new/modified files to the NAS mount (`/backup/media`)
  - Deletes orphaned files on NAS (if deleted locally)
- **Database (`DbBackupService`)** — on manual trigger only (no schedule):
  - Runs `pg_dump` against the configured database
  - Writes a timestamped `.sql` dump to `/backup/db`
  - No automatic retention/cleanup — old dumps must be removed manually for now

Both services record their outcome (success/failure, timestamp, message) in the `backup_history` table so status can be queried via the API.

### Manual Trigger (Admin UI)

Logged-in admins can click **"Back up now"** on the `/admin-panel` page to trigger both a database dump and a media sync immediately — useful right after a data-entry session, without waiting for the scheduled sync. The same page shows the last backup time for each type and flags it as stale if it exceeds `backup.staleness-threshold-hours` (default 192h / 8 days), or if no backup has ever run.

### Configuration

Set in `.env`:
```properties
BACKUP_NAS_ENABLED=true
BACKUP_NAS_MOUNT_PATH=/backup/media
BACKUP_NAS_SYNC_CRON=0 0 3 * * SUN

BACKUP_DB_ENABLED=true
BACKUP_DB_OUTPUT_PATH=/backup/db

BACKUP_STALENESS_THRESHOLD_HOURS=192

DATABASE_HOST=...
DATABASE_PORT=...
DATABASE_NAME=...
DATABASE_USER=...
DATABASE_PASSWORD=...
```

`DATABASE_*` are the credentials `pg_dump` connects with — typically the same superuser account used by `scripts/backup.sh` (not the app's own limited `DB_USER`/`DB_PASSWORD`), since a full dump needs broader read access.

The Docker image includes the `postgresql-client` package so `pg_dump` is available at runtime (see `Dockerfile`).

### Local Testing Setup

1. **Create NAS backup directories:**
   ```bash
   mkdir -p /backup/media /backup/db
   chmod 755 /backup/media /backup/db
   ```

2. **Mount NAS locally (SMB)** — optional for dev:
   ```bash
   mkdir -p /backup/media
   sudo mount -t cifs //nas-ip/backup /backup/media \
     -o username=user,password=pass,uid=1000,gid=1000,file_mode=0755,dir_mode=0755
   ```

3. **Start the app:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

4. **Trigger manual sync (curl, or use the "Back up now" button in `/admin-panel`):**
   ```bash
   curl -X POST http://localhost:8080/api/backup/sync \
     -H "Authorization: Bearer <ADMIN_JWT>"
   ```

5. **Check status:**
   ```bash
   curl http://localhost:8080/api/backup/status \
     -H "Authorization: Bearer <ADMIN_JWT>"
   ```

### Expected Behavior

- **Enabled, mount/DB reachable:** Files/DB sync automatically (media) or on-demand (both). Manual trigger works for both.
- **Enabled, mount missing / pg_dump fails:** Warns in logs, records a FAILURE row in `backup_history`, gracefully skips.
- **Disabled:** Service initializes but does nothing (logs message); `POST /api/backup/sync` reports the disabled type as an error in its response.

---

## Part 2: Full Backup Script

### Purpose

Backs up the **PostgreSQL database**, **media files**, and **GeoServer settings** (`data_dir` — workspaces,
stores, styles, security config, GWC cache config) as a single timestamped archive for disaster recovery.

Note: this GeoServer component is *not* the same thing as `NasBackupService`/`DbBackupService` above — it's
the only mechanism that backs up GeoServer config at all. It intentionally does **not** include
`/volume1/docker/ancientdata/rastermaps` (the raw source rasters) — ADR-012 keeps that mount separate from
`data_dir` specifically so it can be backed up on its own without bloating this config archive. It also
excludes GWC's rendered tile cache (`data_dir/gwc/<layer>/`) — regenerable render output, not config, that
would otherwise dwarf the archive and grow unbounded as layers are published. GWC's own config files
directly under `gwc/` (`geowebcache.xml`, `geowebcache-diskquota.xml`, etc.) are kept. A restore without the
tile cache works identically — GeoServer just re-renders each tile on first request instead of serving a
cached one, then GWC repopulates normally.

### Location

```
scripts/backup.sh
```

### Usage

```bash
./scripts/backup.sh [backup-dir]
```

**Examples:**
```bash
# Backup to NAS
./scripts/backup.sh /volume1/docker/ancientdata/backups

# Backup to current directory
./scripts/backup.sh

# Backup with logging
./scripts/backup.sh /volume1/docker/ancientdata/backups 2>&1 | tee backup.log
```

### Output

Creates timestamped archive:
```
ancientdata-backup-20260611-150000.tar.gz
├── ancientdata-backup-20260611-150000-db.sql         # PostgreSQL dump
├── ancientdata-backup-20260611-150000-media.tar.gz    # Media files
└── ancientdata-backup-20260611-150000-geoserver.tar.gz # GeoServer data_dir (settings)
```

### Configuration

Script reads from environment:
- `DATABASE_HOST` — PostgreSQL host (default: 84.84.172.110)
- `DATABASE_PORT` — PostgreSQL port (default: 2665)
- `POSTGRES_USER` — DB user (default: postgres)
- `POSTGRES_PASSWORD` — DB password (required, use .env)
- `POSTGRES_DB` — database name (default: webGIS_DB)
- `MEDIA_STORAGE_PATH` — media directory (default: /volume1/docker/ancientdata/media)
- `GEOSERVER_DATA_PATH` — GeoServer data_dir (default: /volume1/docker/ancientdata/geoserver)

### Retention Policy

Script automatically keeps the last 7 backups and deletes older ones.

---

## Deployment Setup

### NAS (Production)

1. **Create backup directories:**
   ```bash
   mkdir -p /volume1/docker/ancientdata/backup
   mkdir -p /volume1/docker/ancientdata/backups
   chmod 755 /volume1/docker/ancientdata/backup{,s}
   ```

   The `ancientdata` container runs as a non-root user (UID/GID 1000 by default —
   see `Dockerfile`). Ensure this UID/GID (or whatever you pass via
   `--build-arg APP_UID/APP_GID`) has write access to the media/backup directories
   bind-mounted below, e.g.:
   ```bash
   chown -R 1000:1000 /volume1/docker/ancientdata/media /volume1/docker/ancientdata/backup
   ```

2. **Copy backup script to NAS:**
   ```bash
   scp scripts/backup.sh nas:/volume1/docker/scripts/
   chmod +x /volume1/docker/scripts/backup.sh
   ```

3. **Schedule via cron (NAS):**
   ```bash
   # SSH into NAS and edit crontab
   ssh admin@nas
   crontab -e
   
   # Add (daily backup at 2 AM, before scheduled sync at 3 AM)
   0 2 * * * /volume1/docker/scripts/backup.sh /volume1/docker/ancientdata/backups >> /volume1/docker/ancientdata/backups/backup.log 2>&1
   ```

4. **Off-device copy:** see "Part 3: Off-Device Redundancy" below — Cloud Sync to Google Drive supersedes a
   manual `cp` to an external drive.

### Docker Compose (docker-compose.yml)

Mount point is specified in the compose file:
```yaml
volumes:
  - /volume1/docker/ancientdata/media:/app/media:rw
  - /volume1/docker/ancientdata/backup:/backup:rw
```

---

## Recovery Procedures

### Restore Media Files Only

```bash
# Extract media from backup archive
tar -xzf ancientdata-backup-20260611-150000.tar.gz
cp -r var/ancientdata-backup-20260611-150000-media/media/* /volume1/docker/ancientdata/media/
```

### Restore Database Only

```bash
# Extract database dump
tar -xzf ancientdata-backup-20260611-150000.tar.gz

# Restore to PostgreSQL
PGPASSWORD="password" psql -h 84.84.172.110 -p 2665 -U postgres -d webGIS_DB < ancientdata-backup-20260611-150000-db.sql
```

### Restore GeoServer Settings Only

```bash
# Extract GeoServer data_dir from backup archive
tar -xzf ancientdata-backup-20260611-150000.tar.gz
tar -xzf ancientdata-backup-20260611-150000-geoserver.tar.gz

# Restore to the NAS bind-mounted path, then fix ownership/permissions
# (see ancientdataworkspace/docs/deployment-recovery.md §11.F for the full
# rationale — GeoServer's container UID/GID must be able to write here)
sudo rm -rf /volume1/docker/ancientdata/geoserver
sudo cp -r geoserver /volume1/docker/ancientdata/geoserver
sudo chown -R 1000:1000 /volume1/docker/ancientdata/geoserver
sudo find /volume1/docker/ancientdata/geoserver -type d -exec chmod 775 {} \;
sudo find /volume1/docker/ancientdata/geoserver -type f -exec chmod 664 {} \;

cd /volume1/docker/AncientDataWebGIS
docker compose up -d --force-recreate geoserver
```

### Full Restore

1. Restore database (see above)
2. Restore GeoServer settings (see above)
3. Restore media (see above)
4. Restart application

---

## Part 3: Off-Device Redundancy

### Purpose

Parts 1 and 2 both write to `/volume1/docker/ancientdata/backup*` — a *different folder*, but still the
**same physical NAS** as the primary data. That protects against accidental deletion or a bad deploy, but
not against NAS-level failure, theft, or fire. Synology **Cloud Sync** pushes these folders to Google Drive
on a schedule, closing that gap without any extra physical media (see ADR-006, which flagged this as an
open item).

Cloud Sync is a **free** DSM package (Package Center, no Synology license/subscription) and authenticates
via your own Google account's OAuth login — it writes against your normal Google Drive storage quota, the
same as installing Google Drive on a laptop. This is a different mechanism from the original (abandoned)
`GoogleDriveBackupService`, which used a **service account** with no storage quota of its own and hit
`storageQuotaExceeded`; that specific blocker does not apply here. It's also unrelated to Synology C2
(Synology's own paid cloud service) or Active Backup for Google Workspace (backs up *from* a paid Workspace
account, the opposite direction) — no additional paid product is involved.

This is DSM GUI/Package Center configuration on the NAS itself — there is no application code or CLI
script for it, so it isn't executable from a dev machine or CI, though DSM (and Cloud Sync's setup wizard)
is reachable remotely via QuickConnect if you're not on the home LAN. Configure it once, directly on the NAS:

### Setup (Cloud Sync → Google Drive)

1. **Package Center** → install **Cloud Sync** (free, bundled package listing).
2. Open **Cloud Sync** → **+** → select **Google Drive** → sign in with your own Google account (OAuth
   consent screen) → authorize.
3. **Local path**: choose/create a dedicated folder, e.g. `/volume1/docker/ancientdata-cloud-sync/`, and
   copy or symlink in the two folders to protect:
   - `/volume1/docker/ancientdata/backup` (in-app DB dump + media mirror, `NasBackupService`/`DbBackupService`)
   - `/volume1/docker/ancientdata/backups` (the full timestamped archive from `scripts/backup.sh`, now
     including the GeoServer `data_dir` tar — see Part 2)

   (Cloud Sync syncs a single configured local folder tree to the remote; point it at a parent directory
   that contains both, or configure two separate Cloud Sync tasks — one per folder — if you'd rather keep
   them as distinct Drive folders.)
4. **Remote path**: a dedicated Drive folder, e.g. `AncientDataWebGIS-Backups`.
5. **Sync direction**: Upload only (NAS → Google Drive) — this is a one-way backup, not a two-way sync;
   nothing should ever be edited on the Drive side and synced back.
6. **Schedule**: Cloud Sync watches the local folder continuously by default; if you'd rather it only run
   at a fixed time (to avoid syncing partial files mid-write), use its task schedule settings and set it
   for after the 02:00 `backup.sh` cron run and the 03:00 in-app media sync (e.g. 04:00).
7. File versioning: Google Drive keeps its own revision history per file; Cloud Sync itself doesn't need
   extra version-retention config for this use case (unlike Hyper Backup) — `backup.sh`'s own 7-archive
   retention already keeps the working set bounded.

### Verification

- Trigger a manual sync (or wait for the schedule) and confirm the Drive folder contains the latest
  `ancientdata-backup-*.tar.gz` and current `backup/db`, `backup/media` contents.
- Periodically (e.g. after a schema or GeoServer config change) download one of the archived files from
  Drive and verify it actually restores — an unverified backup is not a trustworthy backup.

---

## Monitoring

### Check Backup Health

```bash
# View backup logs
tail -f /volume1/docker/ancientdata/backups/backup.log

# Check sync logs (from app)
docker logs ancientdata | grep "backup sync"

# Verify backed-up files exist
ls -lh /volume1/docker/ancientdata/backup/

# Verify archive backups
ls -lh /volume1/docker/ancientdata/backups/
```

### Alerts (Optional)

Add to backup script for email notifications on failure:
```bash
if [ $? -ne 0 ]; then
    echo "Backup failed" | mail -s "AncientDataWebGIS Backup Failure" admin@example.com
fi
```

---

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| NAS backup disabled | `BACKUP_NAS_ENABLED=false` | Set to `true` in `.env` |
| Mount not found | NAS not accessible | Verify SMB/NFS, check network connectivity |
| DB backup fails | PostgreSQL credentials wrong | Verify `POSTGRES_PASSWORD` in `.env` |
| Old backups not deleted | No write permission | Check directory ownership/permissions |
| Huge archive size | Media directory includes large files | Consider excluding in `backup.sh` tar command |
| GeoServer member missing from archive | `GEOSERVER_DATA_PATH` not found on that run | Check `backup.log` for the `WARNING: GeoServer data_dir not found` line; verify the path/mount |
| "Back up now" always says "Backup triggered" regardless of outcome, or returns a 500 / "Failed to trigger backup" | Old behavior before the outcome-reporting fix (see ADR-006 addendum); a 500 specifically meant `backup_history` was missing on the live DB and an unhandled save failure crashed the request | Redeploy the current backend — the response now reflects real success/failure, and a `backup_history` persistence failure is logged, not thrown |
| Status panel keeps showing "Never"/stale even though "Back up now" reports success | `backup_history` table missing on the live DB (Flyway is disabled — schema is manual, see `DB-MIGRATION-STRATEGY.md`); the backup itself succeeds but its outcome isn't recorded | Apply `docs/architecture/sql/backup_history.sql` via `psql`/pgAdmin against the shared PostGIS DB |

---

## References

- Spring Services: `src/main/java/com/webgis/ancientdata/application/service/NasBackupService.java`, `DbBackupService.java`, `BackupStatusService.java`
- Config: `src/main/java/com/webgis/ancientdata/config/NasBackupConfig.java`, `DbBackupConfig.java`
- Endpoints: `POST /api/backup/sync`, `GET /api/backup/status` (admin only)
- Schema: `docs/architecture/sql/backup_history.sql` (apply manually — see `docs/architecture/DB-MIGRATION-STRATEGY.md`)
- Admin UI: `AncientDataWebGIS_FE/src/pages/AdminPanel.tsx`
- Full backup script (DB + media + GeoServer): `scripts/backup.sh`
- ADR: `docs/architecture/adr/ADR-006-media-backup-nas-sync.md` (media sync decision, DB backup addendum,
  outcome-reporting fix addendum, off-device redundancy)
- ADR: `docs/architecture/adr/ADR-012-raster-publishing-pipeline.md` (rationale for keeping `rastermaps`
  separate from GeoServer's `data_dir`)

