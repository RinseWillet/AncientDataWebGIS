# E15: Automatic Image Resizing for Oversized Media Uploads

**Backlog entries:** `docs/features/FEATURE-SPEC-BACKLOG.md` § P1.7 (`E15-1` .. `E15-6`)
**Decision record (to write at implementation time):** `docs/architecture/adr/ADR-015-oversized-media-resize-strategy.md`

---

## Problem

`MediaService.validateFile()` (`MediaService.java:159-169`) rejects any upload over
`MAX_FILE_SIZE` (10 MB, `MediaService.java:38`) with `MEDIA_FILE_TOO_LARGE`. A modern
phone/camera JPEG routinely exceeds 10 MB, so an admin's only recourse today is to
resize the photo themselves in some other tool before retrying. That's a dead end for
a feature whose whole point is making it easy to attach field photos to sites/roads.

This was found while investigating an unrelated "upload stuck on 'Uploading…'" report —
that report turned out to trace to an nginx `client_max_body_size` deployment/sync
issue, not a bug in the current upload code path (`MediaController` →
`MediaService.upload()` → `FileSystemMediaStorageService.store()` all work correctly
end-to-end, confirmed via a local repro). The 10 MB reject-outright behavior surfaced
as a separate, real UX gap during that investigation.

## Goal

Instead of rejecting an oversized-but-otherwise-valid image, downscale/recompress it
server-side to fit under the limit, preserving as much visual quality as reasonable —
targeting ~300 DPI (a common "print quality" reference point) where that's a
meaningful concept for the output format.

## Non-goals

- Client-side resizing/compression before upload (server-side is the single source of
  truth for what gets stored; keeps the admin upload form simple).
- Resizing/recompressing images that are already under the 10 MB limit — no forced
  re-encode of files that already fit.
- A hard ceiling above which nothing is accepted at all is still needed (can't resize
  a corrupt file, and shouldn't spend CPU trying to resize something absurd) — see
  "Hard reject ceiling" below.
- Video or non-image media — out of scope; `ALLOWED_MIME_TYPES` stays JPEG/PNG/WebP.

## On "300 DPI"

DPI (dots per inch) is a print-density metadata tag, not a property of the pixel data
itself — the same 4000×3000px image can claim 300 DPI or 72 DPI in its metadata
without changing a single pixel. So "targeting 300 DPI" means two separate things:

1. **Pixel dimensions generous enough to print reasonably at 300 DPI** — e.g. a
   4000×3000px image prints at ~13.3"×10" at 300 DPI. The resize step should cap the
   long edge at something in that neighborhood (proposed: 4000px) rather than
   aggressively downscaling, so quality loss comes primarily from JPEG recompression,
   not from throwing away pixels.
2. **Writing `300` into the output file's density metadata** (JFIF APP0 segment for
   JPEG, via `ImageIO`'s `IIOMetadata` for the `javax_imageio_jpeg_image_1.0` format).
   This is a pure metadata write — cosmetic for on-screen display, relevant only if
   someone later prints the photo or opens it in software that reads/displays DPI.
   Best-effort only (`E15-3`): if writing the JFIF density segment turns out to be
   awkward for a given case (e.g. PNG/WebP output, which don't have JFIF), skip it
   silently rather than failing the upload over a metadata nicety.

## Decision: plain `ImageIO`, no new dependency

The backend already reads image bytes for two purposes (`ExifGpsExtractor` for GPS,
implicit content-type sniffing for validation) but has no resize/recompress capability
yet. Options considered:

| Option | Verdict |
|---|---|
| Java's built-in `javax.imageio` (`ImageIO.read`/`write`, `BufferedImage.getScaledInstance`/`Graphics2D.drawImage`) | **Chosen.** Zero new dependencies; sufficient for scale + JPEG quality control (`ImageWriteParam.setCompressionQuality`); `IIOMetadata` can set JFIF density for the DPI nicety. |
| A resize library (e.g. Thumbnailator, imgscalr) | Rejected for now — nicer API, but adds a dependency for something `ImageIO` already covers; revisit only if the plain-`ImageIO` approach proves awkward (e.g. quality/anti-aliasing complaints). |
| Apache Commons Imaging | Rejected — better metadata support than plain `ImageIO`, but this project doesn't need general-purpose metadata read/write, just JFIF density on the resize output path. |

## Approach

New `ImageResizeService` (`application/service`), called from `MediaService.upload()`
in place of the current unconditional reject:

1. If `file.getSize() <= MAX_FILE_SIZE`, store as-is (current behavior, unchanged).
2. If `file.getSize() > MAX_FILE_SIZE` and `<=` the hard reject ceiling (below):
   - Decode via `ImageIO.read`.
   - If the long edge exceeds ~4000px, scale down to that cap (aspect-ratio preserved).
   - Re-encode as JPEG, searching downward from a starting quality (e.g. 0.85) in
     fixed steps until the output fits under `MAX_FILE_SIZE`, or a minimum quality
     floor (e.g. 0.5) is hit — if still over the floor, fall through to the
     already-downscaled dimensions at minimum quality (accept the result rather than
     looping indefinitely).
   - Best-effort: write `300` into the output JPEG's JFIF density metadata (`E15-3`).
3. If decoding fails (corrupt/unsupported image) or the file exceeds the hard reject
   ceiling, reject as today (`MEDIA_FILE_TOO_LARGE` / a corrupt-file equivalent).

### Hard reject ceiling

Keep a much higher outright-reject threshold — proposed **50 MB**, matching the
existing `spring.servlet.multipart.max-file-size`/`max-request-size` (`application.properties`)
and comfortably above nginx's `client_max_body_size 25m` (so nginx would 413 first
in practice; the Spring-side ceiling exists as a defensive backstop, e.g. if nginx's
limit is ever raised or a request arrives via a path that bypasses it). This bounds
worst-case resize CPU/memory work — no unbounded "resize a 500 MB image" risk.

### Response contract change

`MediaAssetDTO` gains a `resized: boolean` field (`E15-4`) so the frontend can tell
the admin their photo was automatically resized, rather than silently storing a
different file than the one they picked. `MediaUploadForm` shows this as an inline
note (not an error) on successful upload.

## Stories

See `FEATURE-SPEC-BACKLOG.md` § P1.7 for the authoritative list with
priority/size/dependencies. Summary:

- **E15-1** — `ImageResizeService` core (decode, scale, iterative quality search).
- **E15-2** — Wire into `MediaService.upload()`; add the 50 MB hard reject ceiling.
- **E15-3** — Best-effort 300 DPI JFIF density write on resized JPEG output.
- **E15-4** — `resized` flag on `MediaAssetDTO`/upload response; FE notice.
- **E15-5** — Tests (threshold behavior, ceiling under `MAX_FILE_SIZE`, aspect ratio,
  DPI tag presence, hard-reject-ceiling and corrupt-file cases still rejected).
- **E15-6** — ADR documenting the `ImageIO`-vs-library choice, quality-search
  strategy, and ceiling rationale.

## Open questions (resolve during `E15-1` implementation, not before)

- Exact starting quality / step size / floor for the iterative JPEG quality search —
  needs a couple of real oversized field photos to tune against, not a guess.
- Whether PNG/WebP inputs over 10 MB should be re-encoded as JPEG (smaller, lossy) or
  kept in their original format at reduced dimensions only (lossless, may still not
  fit) — affects whether format conversion needs to be called out as a behavior
  change to admins beyond the "resized" note.
