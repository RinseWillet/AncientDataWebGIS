-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through pgAdmin or psql against the PostGIS container.
-- Do NOT use Flyway — schema is externally managed (see DB-MIGRATION-STRATEGY.md).
--
-- E2-GEO-1: add optional geotag coordinates to media_asset
-- Populated either automatically from photo EXIF GPS data on upload,
-- or manually via a map pin in the frontend (manual pin always takes
-- precedence over EXIF). Both columns are nullable — most existing
-- rows will have no geotag.

ALTER TABLE media_asset
    ADD COLUMN IF NOT EXISTS latitude  DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Postgres has no "ADD CONSTRAINT IF NOT EXISTS", so guard manually
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_media_asset_latitude'
    ) THEN
        ALTER TABLE media_asset
            ADD CONSTRAINT chk_media_asset_latitude
                CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_media_asset_longitude'
    ) THEN
        ALTER TABLE media_asset
            ADD CONSTRAINT chk_media_asset_longitude
                CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
    END IF;
END $$;

-- Fast lookup of geotagged photos for a given entity (map marker layer)
CREATE INDEX IF NOT EXISTS idx_media_asset_geotag
    ON media_asset (target_type, target_id)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;


