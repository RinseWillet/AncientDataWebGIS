#!/bin/bash
# E2.0 Media Backup Helper
#
# Purpose: sync media_asset files from NAS to Google Drive for offsite backup
# Usage: ./backup-media-to-gdrive.sh
#
# Prerequisites:
# 1. rclone installed and configured with a 'gdrive' remote
#    (see docs/features/E2.0-media-storage-foundation.md for setup)
# 2. Executable permissions: chmod +x backup-media-to-gdrive.sh
# 3. Optional: add to cron for periodic backup

set -e

# Configuration
MEDIA_SRC="/volume1/docker/postgis_pgadmin_stack/ancientdata_media"
GDRIVE_DEST="gdrive:/AncientDataMediaBackup"
LOG_FILE="/var/log/ancientdata_media_backup.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $*" >> "$LOG_FILE"
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $*"
}

error() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $*" >> "$LOG_FILE"
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $*"
    exit 1
}

log "Starting media backup to Google Drive..."

# Check if source directory exists
if [ ! -d "$MEDIA_SRC" ]; then
    error "Media source directory not found: $MEDIA_SRC"
fi

# Check if rclone is installed
if ! command -v rclone &> /dev/null; then
    error "rclone is not installed. See: https://rclone.org/install/"
fi

# Check if gdrive remote is configured
if ! rclone listremotes | grep -q "^gdrive:$"; then
    error "Google Drive remote 'gdrive' not configured. Run: rclone config"
fi

# Run the sync
log "Syncing from: $MEDIA_SRC"
log "Syncing to:   $GDRIVE_DEST"

if rclone sync "$MEDIA_SRC" "$GDRIVE_DEST" \
    --exclude-if-modified-before 24h \
    --progress \
    --log-level INFO \
    >> "$LOG_FILE" 2>&1; then
    log "✓ Media backup completed successfully"
else
    error "✗ Media backup failed. Check $LOG_FILE for details."
fi

# Quick stats
FILE_COUNT=$(find "$MEDIA_SRC" -type f | wc -l)
TOTAL_SIZE=$(du -sh "$MEDIA_SRC" | cut -f1)
log "Current stats: $FILE_COUNT files, $TOTAL_SIZE"

