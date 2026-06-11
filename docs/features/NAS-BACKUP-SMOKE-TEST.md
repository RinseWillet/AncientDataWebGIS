# NAS Backup Smoke Test

**Purpose:** Verify the end-to-end flow: upload an image → check it landed on disk → trigger backup → confirm it was copied to the NAS backup directory.

**Prerequisite:** The Spring Boot app is running locally (or via Docker Compose) with a valid `.env`.

---

## 1. Set Up Local Backup Directory

Use a local temp directory as the "NAS mount" so you can verify without a real NAS:

```bash
mkdir -p /tmp/ancientdata-nas-backup
```

Add (or set) these values in your `.env`:

```dotenv
BACKUP_NAS_ENABLED=true
BACKUP_NAS_MOUNT_PATH=/tmp/ancientdata-nas-backup
MEDIA_STORAGE_PATH=./media
```

Restart the app so the new config is picked up:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Watch for this log line on startup — it confirms init succeeded:

```
NAS backup service initialised (mount: /tmp/ancientdata-nas-backup)
```

---

## 2. Log In and Get a JWT Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<your-admin-user>","password":"<your-admin-password>"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "Token: $TOKEN"
```

---

## 3. Upload an Image

Replace `<site_id>` with any existing site ID (e.g. `1`).

```bash
curl -s -X POST http://localhost:8080/api/media \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/test-image.jpg" \
  -F "targetType=SITE" \
  -F "targetId=<site_id>" \
  -F "caption=Smoke test photo" \
  | python3 -m json.tool
```

Note the `storageKey` field in the response (e.g. `site/1/abc123.jpg`).

---

## 4. Confirm the File Is on Disk

```bash
ls -lh ./media/site/<site_id>/
```

You should see the uploaded file. The file path matches the `storageKey` returned above.

---

## 5. Trigger the Backup

```bash
curl -s -X POST http://localhost:8080/api/backup/sync \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Expected response:

```json
{
  "status": "ok",
  "message": "Backup sync triggered"
}
```

If `BACKUP_NAS_ENABLED` is `false`, you will get:

```json
{
  "status": "error",
  "message": "NAS backup is not enabled"
}
```

---

## 6. Verify the File Was Copied to the Backup Directory

```bash
ls -lh /tmp/ancientdata-nas-backup/site/<site_id>/
```

The uploaded file should appear here with the same relative path as in `./media/`.

---

## 7. Verify Orphan Deletion (Optional)

Manually place a dummy file in the backup directory:

```bash
echo "orphan" > /tmp/ancientdata-nas-backup/orphan-test.txt
```

Trigger the backup again:

```bash
curl -s -X POST http://localhost:8080/api/backup/sync \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Confirm the orphan was removed:

```bash
ls /tmp/ancientdata-nas-backup/orphan-test.txt
# Should return: No such file or directory
```

---

## Known Limitations to Observe

1. **`POST /api/backup/sync` returns `{"status":"ok"}` even if the mount path became unavailable after startup** (e.g., you deleted `/tmp/ancientdata-nas-backup`). Watch the application log for:
   ```
   WARN  NasBackupService - NAS backup mount not available — skipping sync
   ```
   This is a known limitation documented in ADR-006.

2. **First sync may be slow** if the media directory is large — the service walks the full tree. Subsequent syncs only copy modified files.

3. **Deletion mirroring:** Deleting a file via `DELETE /api/media/{id}` removes the local file. The next sync will remove its copy from the backup directory. There is no immediate backup-side deletion.

---

## Cleanup

```bash
rm -rf /tmp/ancientdata-nas-backup
```

Remove `BACKUP_NAS_ENABLED=true` from `.env` when done testing.

