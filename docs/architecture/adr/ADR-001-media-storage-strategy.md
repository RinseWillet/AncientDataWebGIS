# ADR-001: Media Storage Strategy

**Status:** Accepted  
**Date:** 2026-05-29  
**Decision makers:** Project owner  
**Epic:** E2 Photo & Media Integration

---

## Context

AncientData is evolving from text/geometry records to media-bearing records (photographs of archaeological sites and roads). This requires a decision on where image binaries are stored, how they are served, and how they are backed up.

### Constraints

- **Hosting:** Home-hosted from a Synology DS 923+ NAS (4 GB RAM, 2× WD Red HDDs in RAID).
- **Internet:** Copper connection (not fibre) — limited upload bandwidth (~10–40 Mbps).
- **Audience:** Small for the foreseeable future.
- **Existing stack:** Docker Compose on NAS, with PostgreSQL/PostGIS, GeoServer, pgAdmin, and the Spring Boot application as containers. All persistent data uses bind mounts under `/volume1/docker/postgis_pgadmin_stack/`.
- **Budget:** Zero additional infrastructure cost preferred.

---

## Decision

**Store media files on the NAS filesystem, served by the Spring Boot application, with Google Drive as an offsite backup destination.**

### Primary storage: NAS filesystem via Docker bind mount

```
/volume1/docker/postgis_pgadmin_stack/ancientdata_media/
    site/{site_id}/{uuid}.jpg
    road/{road_id}/{uuid}.jpg
```

Mounted into the Spring Boot container as `/app/media` (read-write).

### Metadata: PostgreSQL `media_asset` table

All file metadata (caption, author, license, source, date, visibility, cover flag) is stored in a `media_asset` table in the existing PostGIS database. The `storage_key` column links a DB record to its file on disk.

### Serving: Spring Boot static file endpoint

Files are served through `GET /api/media/files/{storage_key}` with a 24-hour cache header. No signed URLs or external CDN at this stage.

### Backup: Google Drive via rclone (weekly)

A scheduled `rclone sync` job copies the media directory to Google Drive as an offsite backup. This uses the existing 1 TB+ of free Google Drive capacity.

---

## Alternatives Considered

### A. Store image binaries in PostgreSQL (bytea/BLOB)

- **Rejected.** Puts pressure on the shared PostGIS database used by QGIS and pgAdmin. Blobs are not optimised for HTTP streaming or caching. Backup becomes more complex (DB dump includes large binaries).

### B. Google Drive as primary storage (serve images from Google)

- **Rejected.** Each image request would require an extra network hop (NAS → Google API → response → user). Adds Google API dependency, OAuth credential management, and rate limit concerns. Since the copper connection is the bottleneck regardless of where files are stored, eliminating the extra round-trip is better for latency.

### C. MinIO / S3-compatible object store

- **Deferred, not rejected.** Adds operational complexity (extra container, credentials, bucket lifecycle) that is unnecessary for a personal NAS deployment with a small audience. The backend uses a `MediaStorageService` interface, so swapping to MinIO/S3 later is a one-file change.

---

## Consequences

### Positive

- Simplest possible setup — aligns with the existing bind-mount pattern for PostGIS and GeoServer.
- No additional containers, services, or credentials.
- Trivially debuggable (files are visible on the NAS filesystem).
- NAS backup (Synology HyperBackup) automatically covers the media directory.
- Google Drive backup provides offsite disaster recovery at zero cost.
- `MediaStorageService` interface allows future migration to S3/MinIO without touching callers.

### Negative

- No CDN — performance depends entirely on the NAS and copper upload speed. Acceptable for a small audience.
- If the NAS is offline (e.g., disconnected during a thunderstorm ⚡), both the application and all media are unavailable. This is inherent to home-hosting.
- Requires lifecycle sync between DB records and filesystem (orphan files if the DB record is deleted but the file remains, or vice versa). Mitigated by the `MediaService.delete()` method which handles both.

### When to revisit

- If the audience grows beyond what the copper connection can serve.
- If a CDN (e.g., Cloudflare) is placed in front of the NAS.
- If the project moves to cloud hosting.

---

## Implementation References

- DDL script: `docs/architecture/sql/media_asset.sql`
- Storage foundation spec: `docs/features/E2.0-media-storage-foundation.md`
- Backup script: `docs/architecture/backup-media-to-gdrive.sh`
- Backend service interface: `application/service/MediaStorageService.java`
- Backend filesystem implementation: `application/service/FileSystemMediaStorageService.java`
- Docker Compose bind mount: `docker-compose.yml` → `ancientdata` service

