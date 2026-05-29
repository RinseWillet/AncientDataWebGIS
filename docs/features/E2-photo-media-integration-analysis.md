# E2 — Photo and Media Integration (Analysis)

**Date:** 2026-05-18  
**Epic:** E2 Photo & Media Integration  
**Priority wave:** P1

## Goal

Enable AncientData to store, manage, and display photographs for roads and sites in:

- `AncientDataWebGIS_FE/src/pages/SiteInfo.tsx`
- `AncientDataWebGIS_FE/src/pages/RoadInfo.tsx`

## Current state

- `Site` and `Road` entities do not include photo/media fields.
- GeoJSON responses for site/road info do not carry media references.
- UI has placeholder area (`.infopage-image`) but no media pipeline.
- Suggestion flow supports optional `imageUrl`, but this is tied to suggestion review, not authoritative site/road media.
- Database schema is externally owned; Flyway is disabled:
  - `docs/architecture/DB-MIGRATION-STRATEGY.md`

## Storage options

### Option A: Store binary image data in DB

**Approach**
- Add bytea/blob columns or a photo table storing binary payloads.

**Pros**
- Single backup domain.
- Strict transactional consistency.

**Cons**
- DB growth and performance pressure.
- Harder CDN/cache optimization.
- Less operational flexibility for larger media collections.

### Option B: Store files in object/filesystem storage + metadata in DB (recommended)

**Approach**
- Store image files in storage backend (NAS path, MinIO/S3 bucket, or web-served media directory).
- Store metadata + linkage in DB (`media_asset` table).

**Pros**
- Better scaling for media-heavy use cases.
- Easier thumbnails, caching, and later CDN support.
- Cleaner separation of concerns.

**Cons**
- Requires lifecycle sync (DB record vs stored file).
- Requires storage policy decisions (public vs signed URLs).

## Recommendation

Use **Option B**:

1. **DB metadata table** for search, attribution, moderation, and linking.
2. **External file storage** for binaries.
3. Stable API contract returning resolved image URLs.

This best fits Epic E2 goals and current architecture constraints.

## Proposed data model (`media_asset`)

Suggested columns:

- `id` (PK)
- `target_type` (`ROAD` | `SITE`)
- `target_id` (FK-like reference to road/site id)
- `storage_key` (internal key/path)
- `public_url` (nullable if generated dynamically)
- `thumbnail_url` (nullable)
- `mime_type`
- `file_size_bytes`
- `caption` (nullable)
- `author` (nullable)
- `source` (nullable)
- `license` (nullable)
- `date_taken` (nullable)
- `is_cover` (boolean)
- `visibility_status` (`PENDING` | `APPROVED` | `REJECTED` | `HIDDEN`)
- `created_at`
- `updated_at`
- `created_by` (nullable)

## Proposed API shape

- `POST /api/media` (multipart upload + metadata) [auth/admin policy to decide]
- `GET /api/media?targetType=SITE&targetId={id}`
- `PATCH /api/media/{id}` (metadata/moderation)
- `DELETE /api/media/{id}`

Response item example fields:

- `id`, `targetType`, `targetId`
- `fullUrl`, `thumbnailUrl`
- `caption`, `author`, `source`, `license`, `dateTaken`
- `isCover`, `visibilityStatus`

## Frontend integration plan

1. Add `MediaService` in frontend service layer.
2. In `SiteInfo` and `RoadInfo`, fetch media by `(targetType, targetId)`.
3. Render image gallery in the existing illustration section (`.infopage-image`).
4. Show caption/attribution and empty-state messaging.
5. Reuse first approved `isCover=true` item later for `MapInfoCard` (E2-4).

## Security and validation notes

- Validate MIME types and max file size at upload.
- Enforce extension/content-type checks server-side.
- Define visibility policy for anonymous users vs authenticated users.
- Sanitize metadata fields and avoid blind trust of client-provided URLs.

## Delivery order (aligned to backlog)

1. **E2-1:** media asset model + list/upload endpoints.
2. **E2-2:** rich metadata round-trip.
3. **E2-3:** galleries in `SiteInfo` and `RoadInfo`.
4. **E2-4:** cover thumbnail in map info card.
5. **E2-5:** admin moderation flow.

## Open decisions

- Storage backend for P1: NAS filesystem or MinIO/S3-compatible bucket?
- Public URLs vs signed URLs?
- Upload permissions: admin-only initially, or role-based broader access?
- Thumbnail generation strategy: synchronous vs async job.

