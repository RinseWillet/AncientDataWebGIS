-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through pgAdmin or psql against the PostGIS container.
-- Do NOT use Flyway — schema is externally managed (see DB-MIGRATION-STRATEGY.md).
--
-- E2-0: media_asset table
-- Stores metadata for photo/image files linked to roads and sites.
-- The actual image binary is stored on the NAS filesystem, not in this table.
-- The storage_key column holds the relative path from the media root,
-- e.g. 'site/42/f7a3c1b2-....jpg'
-- The full URL is constructed by the backend as: {MEDIA_BASE_URL}/{storage_key}

CREATE TABLE IF NOT EXISTS media_asset (
    id                BIGSERIAL       PRIMARY KEY,

    -- which entity this photo belongs to
    target_type       VARCHAR(10)     NOT NULL CHECK (target_type IN ('ROAD', 'SITE')),
    target_id         BIGINT          NOT NULL,

    -- file location on NAS/storage
    storage_key       VARCHAR(512)    NOT NULL UNIQUE,
    mime_type         VARCHAR(64)     NOT NULL,
    file_size_bytes   BIGINT          NOT NULL,

    -- descriptive metadata (all nullable — can be filled in later)
    caption           VARCHAR(1000),
    author            VARCHAR(255),
    source            VARCHAR(512),
    license           VARCHAR(255),
    date_taken        DATE,

    -- display flags
    is_cover          BOOLEAN         NOT NULL DEFAULT FALSE,

    -- moderation
    visibility_status VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                      CHECK (visibility_status IN ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN')),

    -- audit
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(255)
);

-- Fast lookup of all media for a given entity (the most common query)
CREATE INDEX IF NOT EXISTS idx_media_asset_target
    ON media_asset (target_type, target_id, visibility_status);

-- Fast lookup of cover image per entity
CREATE INDEX IF NOT EXISTS idx_media_asset_cover
    ON media_asset (target_type, target_id, is_cover)
    WHERE is_cover = TRUE;

-- Keep updated_at in sync automatically
CREATE OR REPLACE FUNCTION set_media_asset_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_media_asset_updated_at ON media_asset;
CREATE TRIGGER trg_media_asset_updated_at
    BEFORE UPDATE ON media_asset
    FOR EACH ROW EXECUTE FUNCTION set_media_asset_updated_at();

-- Grant access to the application user
GRANT ALL PRIVILEGES ON TABLE media_asset TO webgis_client;
GRANT USAGE, SELECT ON SEQUENCE media_asset_id_seq TO webgis_client;

