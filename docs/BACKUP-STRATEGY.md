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

Backs up both **PostgreSQL database** and **media files** as a single timestamped archive for disaster recovery.

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
├── ancientdata-backup-20260611-150000-db.sql      # PostgreSQL dump
└── ancientdata-backup-20260611-150000-media.tar.gz # Media files
```

### Configuration

Script reads from environment:
- `DATABASE_HOST` — PostgreSQL host (default: 84.84.172.110)
- `DATABASE_PORT` — PostgreSQL port (default: 2665)
- `POSTGRES_USER` — DB user (default: postgres)
- `POSTGRES_PASSWORD` — DB password (required, use .env)
- `POSTGRES_DB` — database name (default: webGIS_DB)
- `MEDIA_STORAGE_PATH` — media directory (default: /volume1/docker/ancientdata/media)

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

4. **Copy external backups (e.g., USB):**
   ```bash
   # Weekly or monthly
   cp /volume1/docker/ancientdata/backups/ancientdata-backup-*.tar.gz /mnt/external-drive/
   ```

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

### Full Restore

1. Restore database (see above)
2. Restore media (see above)
3. Restart application

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

---

## References

- Spring Services: `src/main/java/com/webgis/ancientdata/application/service/NasBackupService.java`, `DbBackupService.java`, `BackupStatusService.java`
- Config: `src/main/java/com/webgis/ancientdata/config/NasBackupConfig.java`, `DbBackupConfig.java`
- Endpoints: `POST /api/backup/sync`, `GET /api/backup/status` (admin only)
- Schema: `docs/architecture/sql/backup_history.sql` (apply manually — see `docs/architecture/DB-MIGRATION-STRATEGY.md`)
- Admin UI: `AncientDataWebGIS_FE/src/pages/AdminPanel.tsx`

