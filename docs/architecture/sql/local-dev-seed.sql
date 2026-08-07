-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
--
-- LOCAL DEV ONLY — schema + seed data for the optional local-dev PostGIS container
-- (see docker-compose.local-dev.yml). This is NOT the production schema-of-record:
-- the real shared PostGIS database is owned/managed externally via QGIS (see
-- docs/architecture/DB-MIGRATION-STRATEGY.md / ADR-002). This script exists purely
-- so a developer without access to the home-LAN/WARP-tunneled NAS database can still
-- run the backend + frontend locally against a throwaway, schema-compatible Postgres
-- instance with a handful of synthetic rows.
--
-- Auto-applied by the official `postgis/postgis` Docker image on first container
-- start (anything mounted under /docker-entrypoint-initdb.d runs once, in filename
-- order, against the POSTGRES_DB database). See docker-compose.local-dev.yml.
--
-- Keep the app-owned table definitions below (media_asset, backup_history,
-- data_suggestions) in sync with their canonical single-purpose scripts in this same
-- directory (media_asset.sql, backup_history.sql, data_suggestions.sql) — those
-- remain the source of truth for what gets applied to the real shared database.

CREATE EXTENSION IF NOT EXISTS postgis;

-- ── QGIS-owned tables (mirrored here only for local-dev parity) ────────────

CREATE TABLE IF NOT EXISTS arch_sites (
    id          BIGSERIAL PRIMARY KEY,
    pleiadesid  INTEGER,
    name        VARCHAR(255),
    geom        geometry(Point, 4326),
    province    VARCHAR(255),
    sitetype    VARCHAR(64),
    status      VARCHAR(255),
    ref         VARCHAR(800),
    description VARCHAR(5000)
);

CREATE TABLE IF NOT EXISTS roads (
    id             BIGSERIAL PRIMARY KEY,
    cat_nr         INTEGER NOT NULL,
    cat_name       VARCHAR(255) NOT NULL,
    geom           geometry(MultiLineString, 4326) NOT NULL,
    type           VARCHAR(255) NOT NULL,
    cat_type_descr VARCHAR(1000),
    cat_location   VARCHAR(1500),
    cat_description VARCHAR(5000),
    cat_date       VARCHAR(255),
    cat_ref        VARCHAR(800),
    cat_hist_ref   VARCHAR(800)
);

CREATE TABLE IF NOT EXISTS modernrefs (
    id        BIGSERIAL PRIMARY KEY,
    short_ref VARCHAR(255),
    full_ref  VARCHAR(2000),
    url       VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS modernrefs_sites_mapping (
    site_id      BIGINT NOT NULL REFERENCES arch_sites(id) ON DELETE CASCADE,
    modernref_id BIGINT NOT NULL REFERENCES modernrefs(id) ON DELETE CASCADE,
    PRIMARY KEY (site_id, modernref_id)
);

CREATE TABLE IF NOT EXISTS modernrefs_roads_mapping (
    road_id      BIGINT NOT NULL REFERENCES roads(id) ON DELETE CASCADE,
    modernref_id BIGINT NOT NULL REFERENCES modernrefs(id) ON DELETE CASCADE,
    PRIMARY KEY (road_id, modernref_id)
);

-- ── Backend-owned tables (mirrors media_asset.sql / backup_history.sql /
--    data_suggestions.sql — see those files if this ever needs updating) ────

CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE IF NOT EXISTS media_asset (
    id                BIGSERIAL       PRIMARY KEY,
    target_type       VARCHAR(10)     NOT NULL CHECK (target_type IN ('ROAD', 'SITE')),
    target_id         BIGINT          NOT NULL,
    storage_key       VARCHAR(512)    NOT NULL UNIQUE,
    mime_type         VARCHAR(64)     NOT NULL,
    file_size_bytes   BIGINT          NOT NULL,
    caption           VARCHAR(1000),
    author            VARCHAR(255),
    source            VARCHAR(512),
    license           VARCHAR(255),
    date_taken        DATE,
    latitude          DOUBLE PRECISION,
    longitude         DOUBLE PRECISION,
    is_cover          BOOLEAN         NOT NULL DEFAULT FALSE,
    visibility_status VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                      CHECK (visibility_status IN ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN')),
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS backup_history (
    id           BIGSERIAL       PRIMARY KEY,
    backup_type  VARCHAR(20)     NOT NULL CHECK (backup_type IN ('DATABASE', 'MEDIA')),
    outcome      VARCHAR(20)     NOT NULL CHECK (outcome IN ('SUCCESS', 'FAILURE')),
    started_at   TIMESTAMPTZ     NOT NULL,
    finished_at  TIMESTAMPTZ     NOT NULL,
    message      VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS data_suggestions (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT,
    summary VARCHAR(255) NOT NULL,
    details VARCHAR(5000) NOT NULL,
    image_url VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    submitter_username VARCHAR(255) NOT NULL,
    reviewer_notes VARCHAR(2000),
    reviewed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
);

-- ── Seed data ────────────────────────────────────────────────────────────
-- A handful of synthetic rows around the Lower Germanic Limes (Netherlands),
-- roughly matching the real dataset's geographic area, purely so the map/
-- dashboard/data-list pages have something non-empty to render locally.

INSERT INTO arch_sites (pleiadesid, name, geom, province, sitetype, status, ref, description) VALUES
    (100001, 'Castellum Nigrum Pullum (dev seed)', ST_SetSRID(ST_MakePoint(4.9410, 52.0530), 4326), 'Germania Inferior', 'castellum', 'municipium', 'Dev-Seed-Ref-1', 'Synthetic local-dev seed site — not real archaeological data.'),
    (100002, 'Villa Rustica Sample (dev seed)',      ST_SetSRID(ST_MakePoint(5.1214, 52.0907), 4326), 'Germania Inferior', 'villa',      NULL,           'Dev-Seed-Ref-2', 'Synthetic local-dev seed site — not real archaeological data.'),
    (100003, 'Sample Watchtower (dev seed)',         ST_SetSRID(ST_MakePoint(5.3020, 51.9800), 4326), 'Germania Inferior', 'watchtower', NULL,           NULL,             'Synthetic local-dev seed site — not real archaeological data.')
ON CONFLICT DO NOTHING;

INSERT INTO roads (cat_nr, cat_name, geom, type, cat_type_descr, cat_location, cat_description, cat_date, cat_ref, cat_hist_ref) VALUES
    (9001, 'Dev Seed Road A', ST_SetSRID(ST_GeomFromText('MULTILINESTRING((4.9410 52.0530, 5.0200 52.0700, 5.1214 52.0907))'), 4326),
     'road', 'paved road', 'Between two dev-seed sites', 'Synthetic local-dev seed road — not real archaeological data.', '2nd century CE', 'Dev-Seed-Ref-3', NULL),
    (9002, 'Dev Seed Road B', ST_SetSRID(ST_GeomFromText('MULTILINESTRING((5.1214 52.0907, 5.2100 52.0400, 5.3020 51.9800))'), 4326),
     'possible road', 'unpaved track', 'Between two dev-seed sites', 'Synthetic local-dev seed road — not real archaeological data.', NULL, NULL, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO modernrefs (short_ref, full_ref, url) VALUES
    ('DevSeed 2026', 'Dev Seed Author 2026, "Synthetic Reference for Local Development", Nowhere Press.', NULL)
ON CONFLICT DO NOTHING;

INSERT INTO modernrefs_sites_mapping (site_id, modernref_id)
    SELECT s.id, m.id FROM arch_sites s, modernrefs m
    WHERE s.pleiadesid = 100001 AND m.short_ref = 'DevSeed 2026'
ON CONFLICT DO NOTHING;

INSERT INTO modernrefs_roads_mapping (road_id, modernref_id)
    SELECT r.id, m.id FROM roads r, modernrefs m
    WHERE r.cat_nr = 9001 AND m.short_ref = 'DevSeed 2026'
ON CONFLICT DO NOTHING;

-- Dev-only login: username "devadmin", password "DevAdmin123!" (bcrypt hash below).
-- Never use this hash/credential pair outside a throwaway local container.
INSERT INTO users (username, password, role) VALUES
    ('devadmin', '$2b$10$Q.kfGT8wu6.vu3UZvrcte.vRLpgBXWhitWtoloReUqO8Y9ov68JC2', 'ADMIN')
ON CONFLICT DO NOTHING;

-- Grant access to the application user (matches the real deployment's role name;
-- harmless no-op if this user doesn't exist in a given local container since we
-- create it below as part of this same script for local-dev convenience).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'webgis_client') THEN
        CREATE ROLE webgis_client LOGIN PASSWORD 'local_dev_only';
    END IF;
END
$$;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO webgis_client;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO webgis_client;

