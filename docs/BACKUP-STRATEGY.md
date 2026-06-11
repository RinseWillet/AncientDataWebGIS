# AncientDataWebGIS Backup Strategy

## Overview

AncientDataWebGIS uses a **two-tier backup approach**:

1. **Real-time media sync** (`NasBackupService`) — syncs media files to NAS on a schedule
2. **Full backup script** — backs up both database and media as timestamped archives

This ensures both continuous protection and point-in-time recovery.

---

## Part 1: Real-Time Media Sync (Spring Service)

### How It Works

The `NasBackupService` runs on a configurable schedule (default: daily at 3 AM UTC) and:
- Walks the local media directory (`/app/media`)
- Copies new/modified files to the NAS mount (`/backup/media`)
- Deletes orphaned files on NAS (if deleted locally)

### Configuration

Set in `.env`:
```properties
BACKUP_NAS_ENABLED=true
BACKUP_NAS_MOUNT_PATH=/backup/media
BACKUP_NAS_SYNC_CRON=0 0 3 * * SUN
```

### Local Testing Setup

1. **Create NAS backup directory:**
   ```bash
   mkdir -p /backup/media
   chmod 755 /backup/media
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

4. **Trigger manual sync:**
   ```bash
   curl -X POST http://localhost:8080/api/backup/sync \
     -H "Authorization: Bearer <ADMIN_JWT>"
   ```

### Expected Behavior

- **Enabled, mount exists:** Files sync automatically on schedule. Manual trigger works.
- **Enabled, mount missing:** Warns in logs, gracefully skips sync.
- **Disabled:** Service initializes but does nothing (logs message).

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

- Spring Service: `src/main/java/com/webgis/ancientdata/application/service/NasBackupService.java`
- Config: `src/main/java/com/webgis/ancientdata/config/NasBackupConfig.java`
- Endpoint: `POST /api/backup/sync` (admin only)

