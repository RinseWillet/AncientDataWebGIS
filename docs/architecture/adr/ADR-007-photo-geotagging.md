# ADR-007: Photo Geotagging — EXIF Extraction Library and Manual-Pin Precedence

**Status:** Accepted
**Date:** 2026-07-10
**Decision makers:** Backend/Frontend maintainers
**Epic/Story:** E2-GEO-1

---

## Context

E2-GEO-1 required storing optional GPS coordinates on `media_asset` records, either
read automatically from a photo's EXIF metadata or set manually by an admin on a map
(needed especially for road photos, since a single road geometry can span kilometers
and a photo's location can't be inferred from the road's own geometry).

Two decisions needed to be made:

1. Which Java library to use for reading EXIF GPS data.
2. When both an EXIF GPS value and a manual pin exist, which one wins.

---

## Decision

1. Use **`com.drewnoakes:metadata-extractor`** (Apache 2.0) for EXIF GPS extraction.
2. **Manual pin coordinates always take precedence over EXIF-extracted coordinates**,
   both at upload time and on later metadata edits.

---

## Alternatives Considered

### A. Apache Commons Imaging

- Full ASF governance (strong institutional durability), but broader in scope (reads/writes
  many raster formats) than needed for "read GPS off a JPEG."
- Historically slow release cadence (spent over a decade on `1.0-alpha` releases before a
  GA 1.0).
- Lower-level GPS/TIFF field API compared to metadata-extractor's purpose-built
  `GeoLocation` object — more code for the same outcome.
- Rejected: broader surface area and heavier API for a narrowly-scoped need.

### B. EXIF always wins over manual pin

- Would silently discard an admin's deliberate correction whenever EXIF data (possibly
  wrong, e.g. phone GPS drift) is present.
- Rejected: manual input is an explicit user action and should not be silently overridden.

---

## Consequences

### Positive

- Minimal, purpose-built dependency (`metadata-extractor`) for a narrow need — small
  footprint, mature API, proven via embedding in Apache Tika and other large consumers.
- Predictable precedence rule: admins can always correct/override EXIF via the map picker.
- No schema migration surprises: geotag columns are nullable and additive.

### Negative

- `metadata-extractor` is effectively single-maintainer-led (bus-factor risk), mitigated
  by its long history (20+ years) and embedding in major downstream projects.
- The new `latitude`/`longitude` columns require a manual schema change against the
  externally-owned shared PostGIS database (see ADR-002 / `DB-MIGRATION-STRATEGY.md`)
  before this feature can go live in any environment.

### When to Revisit

- If EXIF extraction needs expand beyond GPS (e.g. orientation, camera make/model) or to
  other file formats not well supported by metadata-extractor.

---

## Implementation References

- `docs/architecture/sql/media_asset_add_geotag.sql` — schema change script (must be
  applied externally per `DB-MIGRATION-STRATEGY.md`)
- `src/main/java/com/webgis/ancientdata/utils/ExifGpsExtractor.java`
- `src/main/java/com/webgis/ancientdata/application/service/MediaService.java` (`applyGeotag`)
- `AncientDataWebGIS_FE/src/components/MediaGallery/PhotoLocationPicker.tsx`
- `docs/features/FEATURE-SPEC-BACKLOG.md` — E2-GEO-1 completion summary

