# Plan: Replace Google Drive Backup with NAS-Based Backup

## Context
E2-BACKUP initially implemented Google Drive API v3 backup, but encountered quota/permissions limitations:
- Service account on personal Google Drive hits `storageQuotaExceeded`
- Shared Drives unavailable (no Google Workspace)
- Solution: pivot to NAS backup (owned infrastructure, zero marginal cost)

## Current State
- Google Drive backup service exists: `GoogleDriveBackupService.java`
- Service account key configured, but unused (API quota issue)
- Media files stored locally: `/home/r-willet/IdeaProjects/AncientDataWebGIS/media`
- NAS infrastructure hinted by docker-compose: `/volume1/docker/ancientdata/`

## Proposed Approach: NAS Backup via SMB/NFS Mount

### Architecture
1. **Local testing:** Mount NAS backup folder to `/backup/media` (via SMB)
2. **Docker (dev/prod):** Bind mount NAS path inside container
3. **Sync service:** Walk local media dir → copy to NAS mount (simpler than Cloud API)
4. **Scheduled task:** Run on cron (same pattern as Google Drive)
5. **Manual trigger:** `POST /api/backup/sync` (existing endpoint reused)

### Implementation Steps

#### 1. Replace GoogleDriveBackupService
- Delete `GoogleDriveBackupService.java`
- Delete `GoogleDriveBackupConfig.java`
- Delete backup deps from `build.gradle` (google-api-client, etc.)

#### 2. Create NasBackupService
**File:** `src/main/java/com/webgis/ancientdata/application/service/NasBackupService.java`

```java
@Service
public class NasBackupService {
    // Config: backup.nas.enabled, backup.nas.mount-path, backup.nas.sync-cron
    // Methods:
    // - init(): validate mount exists and is writable
    // - sync(): walk mediaRoot → copy new/modified files to NAS
    // - cleanup orphaned files on NAS
}
```

#### 3. Create NasBackupConfig
**File:** `src/main/java/com/webgis/ancientdata/config/NasBackupConfig.java`

```java
@ConfigurationProperties(prefix = "backup.nas")
public class NasBackupConfig {
    private boolean enabled;
    private String mountPath; // e.g., /backup/media
    private String syncCron;
}
```

#### 4. Update application.properties
```properties
backup.nas.enabled=true
backup.nas.mount-path=/backup/media
backup.nas.sync-cron=0 0 3 * * SUN
```

#### 5. Update docker-compose.yml
```yaml
volumes:
  # ... existing ...
  - /volume1/docker/ancientdata/backup:/backup:rw
```

#### 6. Reuse BackupController
- Keep existing `POST /api/backup/sync`
- Inject `NasBackupService` instead of `GoogleDriveBackupService`

### Local Testing Setup

#### Prerequisites
1. **Share NAS folder via SMB:**
   - On NAS: create `/volume1/docker/ancientdata/backup`
   - Share via SMB to local machine

2. **Mount NAS locally:**
   ```bash
   mkdir -p /backup/media
   sudo mount -t cifs //nas-ip-or-hostname/backup /backup/media \
     -o username=,password=,uid=1000,gid=1000,file_mode=0755,dir_mode=0755
   ```

3. **Update .env:**
   ```properties
   BACKUP_TYPE=nas
   BACKUP_NAS_MOUNT_PATH=/backup/media
   ```

### Verification
1. **Build:** `./gradlew clean build`
2. **Tests:** `./gradlew test`
3. **Manual:**
   - Upload media → watch `/backup/media` for new files
   - `POST /api/backup/sync` → verify sync completes
   - Delete local file → verify orphan deletion on NAS

### Future Scalability
- **Cloud deployment:** mount S3 bucket locally (s3fs) or use S3 SDK directly
- **Hybrid:** Primary = NAS; secondary = cloud (rsync to B2)
- Switch tested; zero code changes needed

### Dependencies to Remove
From `build.gradle`:
- `com.google.api-client:google-api-client`
- `com.google.apis:google-api-services-drive`
- `com.google.auth:google-auth-library-oauth2-http`

### Files to Delete
- `src/main/java/com/webgis/ancientdata/application/service/GoogleDriveBackupService.java`
- `src/main/java/com/webgis/ancientdata/config/GoogleDriveBackupConfig.java`
- `src/test/java/com/webgis/ancientdata/backuptests/GoogleDriveBackupServiceTests.java`

### Questions Before Implementation
1. **Confirm:** Is `/volume1/docker/ancientdata/` your Synology NAS path?
2. **Network:** Can local dev machine reach NAS via SMB/NFS?
3. **Credentials:** Do we need SMB username/password, or is it on local network (no auth)?
4. **Storage:** How much backup capacity available on NAS (disk space check)?

---

**Status:** Awaiting infrastructure confirmation → proceed to implementation

