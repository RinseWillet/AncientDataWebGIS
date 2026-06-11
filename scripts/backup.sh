#!/bin/bash
# Backup script for AncientDataWebGIS
# Backs up PostgreSQL database and media files to a timestamped archive
# Usage: ./backup.sh [backup-dir]
# Example: ./backup.sh /volume1/docker/ancientdata/backups

set -e

BACKUP_DIR="${1:-.}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
BACKUP_NAME="ancientdata-backup-${TIMESTAMP}"
DB_DUMP="${BACKUP_DIR}/${BACKUP_NAME}-db.sql"
MEDIA_TAR="${BACKUP_DIR}/${BACKUP_NAME}-media.tar.gz"
ARCHIVE="${BACKUP_DIR}/${BACKUP_NAME}.tar.gz"
LOG_FILE="${BACKUP_DIR}/backup.log"

# Database configuration (adjust as needed)
DB_HOST="${DATABASE_HOST:-84.84.172.110}"
DB_PORT="${DATABASE_PORT:-2665}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_NAME="${POSTGRES_DB:-webGIS_DB}"
MEDIA_PATH="${MEDIA_STORAGE_PATH:-/volume1/docker/ancientdata/media}"

echo "[$(date)] Starting AncientDataWebGIS backup..." >> "$LOG_FILE"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# 1. Backup PostgreSQL database
echo "[$(date)] Backing up database: $DB_NAME..." >> "$LOG_FILE"
if PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -F plain > "$DB_DUMP" 2>> "$LOG_FILE"; then
    DB_SIZE=$(du -h "$DB_DUMP" | cut -f1)
    echo "[$(date)] Database backup successful: $DB_SIZE" >> "$LOG_FILE"
else
    echo "[$(date)] ERROR: Database backup failed" >> "$LOG_FILE"
    exit 1
fi

# 2. Backup media files
echo "[$(date)] Backing up media files from: $MEDIA_PATH..." >> "$LOG_FILE"
if [ -d "$MEDIA_PATH" ]; then
    if tar -czf "$MEDIA_TAR" -C "$(dirname "$MEDIA_PATH")" "$(basename "$MEDIA_PATH")" 2>> "$LOG_FILE"; then
        MEDIA_SIZE=$(du -h "$MEDIA_TAR" | cut -f1)
        echo "[$(date)] Media backup successful: $MEDIA_SIZE" >> "$LOG_FILE"
    else
        echo "[$(date)] ERROR: Media backup failed" >> "$LOG_FILE"
        exit 1
    fi
else
    echo "[$(date)] WARNING: Media directory not found: $MEDIA_PATH" >> "$LOG_FILE"
fi

# 3. Create combined archive
echo "[$(date)] Creating combined backup archive..." >> "$LOG_FILE"
if tar -czf "$ARCHIVE" -C "$BACKUP_DIR" "$(basename "$DB_DUMP")" "$(basename "$MEDIA_TAR")" 2>> "$LOG_FILE"; then
    ARCHIVE_SIZE=$(du -h "$ARCHIVE" | cut -f1)
    echo "[$(date)] Archive created successfully: $ARCHIVE_SIZE" >> "$LOG_FILE"

    # Optional: clean up intermediate files (keep only archive)
    # rm "$DB_DUMP" "$MEDIA_TAR"
else
    echo "[$(date)] ERROR: Archive creation failed" >> "$LOG_FILE"
    exit 1
fi

# 4. Retention policy: keep only last 7 backups (optional)
echo "[$(date)] Cleaning up old backups (keeping last 7)..." >> "$LOG_FILE"
ls -t "$BACKUP_DIR"/ancientdata-backup-*.tar.gz 2>/dev/null | tail -n +8 | xargs -r rm -v >> "$LOG_FILE" 2>&1

echo "[$(date)] Backup completed successfully: $ARCHIVE" >> "$LOG_FILE"
echo "✓ Backup complete: $ARCHIVE ($ARCHIVE_SIZE)"

