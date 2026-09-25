# ADR-015: Oversized Media Resize Strategy

**Status:** Accepted
**Date:** 2026-09-24
**Decision makers:** Project maintainer
**Epic/Story:** E15 (`E15-1` .. `E15-6`), `docs/features/E15-image-resize-on-upload.md`

---

## Context

`MediaService.validateFile()` rejected any upload over `MAX_FILE_SIZE` (10 MB) with
`MEDIA_FILE_TOO_LARGE`. A modern phone/camera JPEG routinely exceeds 10 MB, so an
admin's only recourse was to resize the photo themselves before retrying — a dead
end for a feature whose whole point is making it easy to attach field photos to
sites/roads. This was found while investigating an unrelated "upload stuck on
'Uploading…'" report that turned out to be an nginx `client_max_body_size`
deployment issue, not a code bug; the 10 MB reject-outright behavior was a separate,
real UX gap surfaced during that investigation.

The goal is to downscale/recompress an oversized-but-otherwise-valid image server-side
to fit under the limit instead of rejecting it, preserving as much visual quality as
reasonable and targeting ~300 DPI (a common "print quality" reference point) where
that's a meaningful concept for the output format.

## Decision

Added an `ImageResizeService` (`application/service`, plain `javax.imageio` —
no new runtime dependency), wired into `MediaService.upload()`:

1. Files at or under 10 MB (`MediaService.MAX_FILE_SIZE`) are stored as-is — unchanged
   behavior, no forced re-encode of files that already fit.
2. Files over 10 MB and at or under a 50 MB hard-reject ceiling are decoded via
   `ImageIO.read`, downscaled if their long edge exceeds 4000px (aspect ratio
   preserved), and re-encoded as JPEG, searching quality downward from 0.85 in 0.05
   steps until the output fits under 10 MB or a 0.5 quality floor is hit (the
   floor's output is accepted rather than looping indefinitely). The output JPEG's
   JFIF density metadata is set to 300 DPI on a best-effort basis — a pure metadata
   write, skipped silently on failure since it's cosmetic.
3. Files over the 50 MB hard ceiling are rejected outright (`MEDIA_FILE_TOO_LARGE`),
   as are files that fail to decode as an image even under the ceiling
   (`MEDIA_FILE_CORRUPT`) — resizing a corrupt file isn't possible, and an unbounded
   "resize a 500 MB image" isn't worth the CPU/memory risk.

All resize output is normalized to JPEG regardless of input format (JPEG/PNG/WebP
are all accepted uploads), since JPEG's tunable compression quality is what makes
hitting an arbitrary byte target reliable — a large PNG re-encoded losslessly at
reduced dimensions alone often still won't fit under 10 MB.

`MediaAssetDTO` gained a `resized: boolean` field, populated only on the immediate
upload response (not persisted, not returned on subsequent list/gallery fetches) so
`MediaUploadForm` can show a one-time inline notice: "your photo was larger than our
size limit, so it was automatically resized to fit."

`MediaStorageService.store()` was changed to take an `InputStream` instead of a
`MultipartFile`, since the resize path produces in-memory bytes rather than a
multipart upload — this lets both the as-is and resized paths share one storage
call.

The 50 MB hard-reject ceiling sits a few MB below `spring.servlet.multipart.max-file-size`/
`max-request-size` (`application.properties`, 55 MB — deliberately set *above* the
ceiling, not equal to it; see "Found during local testing" below) and comfortably
below nginx's `client_max_body_size` — nginx would 413 first in practice; the
Spring-side ceiling is a defensive backstop for a path that bypasses nginx.

`ImageResizeService` also reads the source image's EXIF `Orientation` tag (via the
`metadata-extractor` library already used by `ExifGpsExtractor`) and bakes the
corresponding rotation/flip into the pixel data before scaling. `ImageIO.read()`
ignores that tag when decoding, and the re-encoded output carries no orientation
metadata of its own — without this step, a portrait phone photo above the resize
threshold would come out landscape with no way for a viewer to correct it.

## Alternatives Considered

### A. A resize library (Thumbnailator, imgscalr)

- Nicer scaling API than raw `Graphics2D.drawImage`, but adds a dependency for
  something plain `ImageIO` already covers (scale + JPEG quality control via
  `ImageWriteParam.setCompressionQuality`). Rejected for now — revisit only if the
  plain-`ImageIO` approach proves awkward (e.g. quality/anti-aliasing complaints in
  practice).

### B. Apache Commons Imaging

- Better general-purpose metadata read/write support than plain `ImageIO`, but this
  feature only needs one narrow thing — writing JFIF density on the resize output —
  which `ImageIO`'s `IIOMetadata` already does. Rejected as unnecessary surface area.

### C. Client-side resizing/compression before upload

- Would reduce upload time for large files, but moves the single source of truth for
  "what gets stored" into the browser and complicates the simple admin upload form.
  Rejected — server-side is the single place this logic needs to live.

### D. Keep original format on resize (no forced JPEG conversion)

- Considered re-encoding oversized PNG/WebP inputs losslessly at reduced dimensions
  only, rather than converting everything to JPEG. Rejected: lossless recompression
  at reduced dimensions frequently still won't fit an oversized PNG under 10 MB,
  whereas JPEG's quality parameter gives a reliable way to hit an arbitrary byte
  target. The `resized` flag already tells the admin the stored file differs from
  what they picked, so a format change alongside a size change isn't a bigger
  surprise than the resize itself.

## Consequences

### Positive

- An admin's oversized field photo is no longer a dead end — it's automatically
  fitted to the storage limit instead of rejected.
- No new runtime dependency; the JVM's built-in `ImageIO` covers decode, scale,
  quality-tunable JPEG encode, and JFIF metadata writes.
- The 4000px long-edge cap keeps resized images print-quality-generous (e.g.
  ~13.3"×10" at 300 DPI) rather than aggressively downscaling — quality loss comes
  primarily from JPEG recompression, not from throwing away pixels.

### Negative

- An oversized PNG/WebP upload is silently converted to JPEG on resize, which is a
  lossy format change beyond just a size change — mitigated by the `resized` FE
  notice, but the notice doesn't spell out the format change explicitly.
- The iterative quality search (up to 8 encode passes per oversized upload) adds
  CPU cost to the upload request; bounded by the 50 MB hard ceiling, but not
  benchmarked against real field-photo file sizes yet.
- `MediaStorageService.store()`'s signature change (`MultipartFile` → `InputStream`)
  is a breaking change to that interface — fine today since `FileSystemMediaStorageService`
  is the only implementation, but a future S3/MinIO implementation (mentioned in the
  interface's own Javadoc) needs to account for it.

### Found during local testing (fixed before merge)

- **EXIF orientation stripped on resize.** Confirmed via a real uploaded field photo:
  a portrait phone photo above the resize threshold came out landscape, because
  `ImageIO.read()`/`write()` don't touch EXIF orientation at all. Fixed by baking the
  orientation into the pixel data (see Decision above); covered by a test that
  hand-crafts a JPEG with `Orientation=6` and asserts the output is both
  dimension-swapped and rotated the correct direction (not just resized).
- **Hard-reject ceiling was unreachable.** `MediaService.HARD_REJECT_CEILING` (50 MB)
  and `spring.servlet.multipart.max-file-size` (originally also 50 MB) were equal, so
  any file large enough to trip our ceiling check always tripped Spring's own
  multipart parser *first* — `MediaService.validateFile()` never got a chance to run,
  and the client saw a raw `MaxUploadSizeExceededException` (uncaught, falling
  through to a generic 500) instead of the intended `MEDIA_FILE_TOO_LARGE` message.
  Reproduced locally with a ~79 MB test file: the frontend showed a generic "Upload
  failed" instead of the specific message a slightly-smaller oversized file got.
  Fixed two ways: raised the Spring-side limit to 55 MB (`application.properties`,
  `application-dev.properties`, `application-local-dev.properties`,
  `application-test.properties`) so our own check is reachable for anything between
  50–55 MB, and added a `GlobalExceptionHandler` handler for
  `MaxUploadSizeExceededException` as a defensive backstop so *any* multipart-size
  failure — regardless of which layer catches it — returns the same friendly,
  on-brand JSON error instead of a generic 500.

### When to Revisit

- If real field photos show the 0.85→0.5 quality search routinely bottoms out at the
  floor without fitting under 10 MB, the starting quality/step/floor values need
  tuning against actual oversized samples rather than the current estimate.
- If resize CPU cost under concurrent uploads becomes a problem, consider bounding
  concurrent resize operations or moving the work off the request thread.
- If PNG uploads with transparency turn out to matter for this project (current
  resize path flattens alpha via JPEG conversion), revisit Alternative D.

---

## Implementation References

- `src/main/java/com/webgis/ancientdata/application/service/ImageResizeService.java`
- `src/main/java/com/webgis/ancientdata/application/service/ImageProcessingException.java`
- `src/main/java/com/webgis/ancientdata/application/service/MediaService.java`
- `src/main/java/com/webgis/ancientdata/application/service/MediaStorageService.java`
- `src/main/java/com/webgis/ancientdata/application/service/FileSystemMediaStorageService.java`
- `src/main/java/com/webgis/ancientdata/domain/dto/MediaAssetDTO.java`
- `src/main/java/com/webgis/ancientdata/web/mapper/MediaAssetMapper.java`
- `src/main/java/com/webgis/ancientdata/web/exception/GlobalExceptionHandler.java`
- `src/main/resources/application.properties`, `application-dev.properties`,
  `application-local-dev.properties` (`spring.servlet.multipart.max-file-size`/`max-request-size`)
- `src/test/java/com/webgis/ancientdata/mediatests/ImageResizeServiceTests.java`
- `src/test/java/com/webgis/ancientdata/mediatests/MediaServiceTests.java`
- `src/test/java/com/webgis/ancientdata/rastertests/GlobalExceptionHandlerTests.java`
- `AncientDataWebGIS_FE/src/components/MediaGallery/MediaUploadForm.tsx`
- `docs/features/E15-image-resize-on-upload.md`
