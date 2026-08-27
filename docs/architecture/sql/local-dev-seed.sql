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
-- data_suggestions, raster_layer) in sync with their canonical single-purpose scripts in
-- this same directory (media_asset.sql, backup_history.sql, data_suggestions.sql,
-- raster_layer.sql) — those remain the source of truth for what gets applied to the real
-- shared database.

CREATE
EXTENSION IF NOT EXISTS postgis;

-- ── QGIS-owned tables (mirrored here only for local-dev parity) ────────────

CREATE TABLE IF NOT EXISTS arch_sites
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    pleiadesid
    INTEGER,
    name
    VARCHAR
(
    255
),
    geom geometry
(
    Point,
    4326
),
    province VARCHAR
(
    255
),
    sitetype VARCHAR
(
    64
),
    status VARCHAR
(
    255
),
    ref VARCHAR
(
    800
),
    description VARCHAR
(
    5000
)
    );

CREATE TABLE IF NOT EXISTS roads
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    cat_nr
    INTEGER
    NOT
    NULL,
    cat_name
    VARCHAR
(
    255
) NOT NULL,
    geom geometry
(
    MultiLineString,
    4326
) NOT NULL,
    type VARCHAR
(
    255
) NOT NULL,
    cat_type_descr VARCHAR
(
    1000
),
    cat_location VARCHAR
(
    1500
),
    cat_description VARCHAR
(
    5000
),
    cat_date VARCHAR
(
    255
),
    cat_ref VARCHAR
(
    800
),
    cat_hist_ref VARCHAR
(
    800
)
    );

CREATE TABLE IF NOT EXISTS modernrefs
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    short_ref
    VARCHAR
(
    255
),
    full_ref VARCHAR
(
    2000
),
    url VARCHAR
(
    1000
)
    );

CREATE TABLE IF NOT EXISTS modernrefs_sites_mapping
(
    site_id
    BIGINT
    NOT
    NULL
    REFERENCES
    arch_sites
(
    id
) ON DELETE CASCADE,
    modernref_id BIGINT NOT NULL REFERENCES modernrefs
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    site_id,
    modernref_id
)
    );

CREATE TABLE IF NOT EXISTS modernrefs_roads_mapping
(
    road_id
    BIGINT
    NOT
    NULL
    REFERENCES
    roads
(
    id
) ON DELETE CASCADE,
    modernref_id BIGINT NOT NULL REFERENCES modernrefs
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    road_id,
    modernref_id
)
    );

-- ── Backend-owned tables (mirrors media_asset.sql / backup_history.sql /
--    data_suggestions.sql — see those files if this ever needs updating) ────

CREATE TABLE IF NOT EXISTS users
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    username
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    password VARCHAR
(
    255
) NOT NULL,
    role VARCHAR
(
    20
) NOT NULL CHECK
(
    role
    IN
(
    'ADMIN',
    'USER'
))
    );

CREATE TABLE IF NOT EXISTS media_asset
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    target_type
    VARCHAR
(
    10
) NOT NULL CHECK
(
    target_type
    IN
(
    'ROAD',
    'SITE'
)),
    target_id BIGINT NOT NULL,
    storage_key VARCHAR
(
    512
) NOT NULL UNIQUE,
    mime_type VARCHAR
(
    64
) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    caption VARCHAR
(
    1000
),
    author VARCHAR
(
    255
),
    source VARCHAR
(
    512
),
    license VARCHAR
(
    255
),
    date_taken DATE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    visibility_status VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDING'
    CHECK
(
    visibility_status
    IN
(
    'PENDING',
    'APPROVED',
    'REJECTED',
    'HIDDEN'
)),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    created_by VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS backup_history
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    backup_type
    VARCHAR
(
    20
) NOT NULL CHECK
(
    backup_type
    IN
(
    'DATABASE',
    'MEDIA'
)),
    outcome VARCHAR
(
    20
) NOT NULL CHECK
(
    outcome
    IN
(
    'SUCCESS',
    'FAILURE'
)),
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ NOT NULL,
    message VARCHAR
(
    1000
)
    );

CREATE TABLE IF NOT EXISTS raster_layer
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    source VARCHAR
(
    255
) NOT NULL UNIQUE,
    bounds_south DOUBLE PRECISION NOT NULL,
    bounds_west DOUBLE PRECISION NOT NULL,
    bounds_north DOUBLE PRECISION NOT NULL,
    bounds_east DOUBLE PRECISION NOT NULL,
    zoom_min INTEGER NOT NULL,
    zoom_max INTEGER NOT NULL,
    attribution VARCHAR
(
    512
) NOT NULL,
    category VARCHAR
(
    20
) NOT NULL CHECK
(
    category
    IN
(
    'HISTORICAL_MAP',
    'DEM'
)),
    collection VARCHAR
(
    255
),
    hillshade BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW
(
)
    );

CREATE TABLE IF NOT EXISTS data_suggestions
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    target_type
    VARCHAR
(
    32
) NOT NULL,
    target_id BIGINT,
    summary VARCHAR
(
    255
) NOT NULL,
    details VARCHAR
(
    5000
) NOT NULL,
    image_url VARCHAR
(
    1000
),
    status VARCHAR
(
    32
) NOT NULL DEFAULT 'PENDING',
    submitter_username VARCHAR
(
    255
) NOT NULL,
    reviewer_notes VARCHAR
(
    2000
),
    reviewed_by VARCHAR
(
    255
),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
    );

-- ── Seed data ────────────────────────────────────────────────────────────
-- A handful of synthetic rows around the Lower Germanic Limes (Netherlands),
-- roughly matching the real dataset's geographic area, purely so the map/
-- dashboard/data-list pages have something non-empty to render locally.

INSERT INTO arch_sites (pleiadesid, name, geom, province, sitetype, status, ref, description)
VALUES (100001, 'Castellum Nigrum Pullum (dev seed)', ST_SetSRID(ST_MakePoint(4.9410, 52.0530), 4326),
        'Germania Inferior', 'castellum', 'municipium', 'Dev-Seed-Ref-1',
        'Synthetic local-dev seed site — not real archaeological data.'),
       (100002, 'Villa Rustica Sample (dev seed)', ST_SetSRID(ST_MakePoint(5.1214, 52.0907), 4326), 'Germania Inferior',
        'villa', NULL, 'Dev-Seed-Ref-2', 'Synthetic local-dev seed site — not real archaeological data.'),
       (100003, 'Sample Watchtower (dev seed)', ST_SetSRID(ST_MakePoint(5.3020, 51.9800), 4326), 'Germania Inferior',
        'watchtower', NULL, NULL, 'Synthetic local-dev seed site — not real archaeological data.'),
       (100004, 'Sample Tumulus (dev seed)', ST_SetSRID(ST_MakePoint(5.5020, 52.0800), 4326), 'Germania Inferior',
        'tum', NULL, NULL, 'Synthetic local-dev seed site — not real archaeological data.'),
       (100003, 'Sample Possible Tumulus (dev seed)', ST_SetSRID(ST_MakePoint(5.4020, 51.1800), 4326), 'Germania Inferior',
        'ptum', NULL, NULL,
        'Synthetic local-dev seed site — not real archaeological data.'),
       (100003, 'Sample Possible Villa (dev seed)', ST_SetSRID(ST_MakePoint(5.2020, 51.0980), 4326), 'Germania Inferior',
        'pvilla', NULL, NULL,
        'Synthetic local-dev seed site — not real archaeological data.') ON CONFLICT DO NOTHING;

INSERT INTO roads (cat_nr, cat_name, geom, type, cat_type_descr, cat_location, cat_description, cat_date, cat_ref,
                   cat_hist_ref)
VALUES (9001, 'Dev Seed Road A',
        ST_SetSRID(ST_GeomFromText('MULTILINESTRING((4.9410 52.0530, 5.0200 52.0700, 5.1214 52.0907))'), 4326),
        'road', 'paved road', 'Between two dev-seed sites',
        'Synthetic local-dev seed road — not real archaeological data.', '2nd century CE', 'Dev-Seed-Ref-3', NULL),
       (9002, 'Dev Seed Road B',
        ST_SetSRID(ST_GeomFromText('MULTILINESTRING((5.1214 52.0907, 5.2100 52.0400, 5.3020 51.9800))'), 4326),
        'possible road', 'unpaved track', 'Between two dev-seed sites',
        'Synthetic local-dev seed road — not real archaeological data.', NULL, NULL, NULL),
       (9002, 'Dev Seed Road C',
        ST_SetSRID(ST_GeomFromText('MULTILINESTRING((4.34920 52.0577, 4.3145 52.03396, 4.25665 52.01647))'), 4326),
        'hypothetical route', 'just a guess of a route', 'Loose in Western Netherlands',
        'Synthetic local-dev seed road — not real archaeological data.', NULL, NULL, NULL) ON CONFLICT DO NOTHING;

INSERT INTO modernrefs (short_ref, full_ref, url)
VALUES ('DevSeed 2026', 'Dev Seed Author 2026, "Synthetic Reference for Local Development", Nowhere Press.',
        NULL) ON CONFLICT DO NOTHING;

INSERT INTO modernrefs_sites_mapping (site_id, modernref_id)
SELECT s.id, m.id
FROM arch_sites s,
     modernrefs m
WHERE s.pleiadesid = 100001
  AND m.short_ref = 'DevSeed 2026' ON CONFLICT DO NOTHING;

INSERT INTO modernrefs_roads_mapping (road_id, modernref_id)
SELECT r.id, m.id
FROM roads r,
     modernrefs m
WHERE r.cat_nr = 9001
  AND m.short_ref = 'DevSeed 2026' ON CONFLICT DO NOTHING;

-- Mirrors raster_layer.sql's full seed set exactly (real GeoServer `source`
-- identifiers, real bounds/zoom/attribution) rather than a "handful of synthetic
-- rows" like the other tables above. Unlike site/road data, this is just public
-- catalog display metadata (already exposed unauthenticated via the real
-- GET /api/raster/catalog), so there's no confidentiality reason to fake it here —
-- and keeping the real `source` values means the local-dev backend's raster proxy
-- (GEOSERVER_INTERNAL_URL, typically pointed at the real GeoServer even when running
-- against this throwaway DB) can still serve real map tiles for local testing of
-- viewport gating, hillshade z-order, and collection grouping (E3-3/E3-4/E3-7/E3-8).
INSERT INTO raster_layer (name, source, bounds_south, bounds_west, bounds_north, bounds_east, zoom_min, zoom_max,
                          attribution, category, collection, hillshade)
VALUES ('Sheet A1', 'ancientdata:De-Man-1818-A1', 51.782659965003994, 5.757907509752199, 51.87017277720497,
        5.9033985468639525, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet A2', 'ancientdata:De-Man-1818-A2', 51.79248099648939, 5.757982824283212, 51.825455193924086,
        5.83070005750662, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet A3', 'ancientdata:De-Man-1818-A3', 51.80404451531167, 5.830950124353257, 51.84002406292155,
        5.892320690155102, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet A4', 'ancientdata:De-Man-1818-A4', 51.81399554398528, 5.781638645840879, 51.84932509161219,
        5.84386633466715, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet A5', 'ancientdata:De-Man-1818-A5', 51.830096780009875, 5.843679743944152, 51.86142158497805,
        5.90330558027289, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet A6', 'ancientdata:De-Man-1818-A6', 51.84001106233645, 5.795165495731549, 51.87004849240705,
        5.855503640022906, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
       ('Sheet 3', 'ancientdata:1740-Kleve-DINA1-03_r', 51.73343140813606, 6.104849008894311, 51.768296396569404,
        6.172473312074854, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 4', 'ancientdata:1740-Kleve-DINA1-04_r', 51.714760818201405, 6.098408831298351, 51.75039849774439,
        6.167572939469806, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 5', 'ancientdata:1740-Kleve-DINA1-05_r', 51.724959956235594, 6.059738633657765, 51.759432933524536,
        6.126875610219495, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 6', 'ancientdata:1740-Kleve-DINA1-06_r', 51.74433174217834, 6.0749002252559245, 51.77728077588717,
        6.142042710074628, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 7', 'ancientdata:1740-Kleve-DINA1-07_r', 51.74940470691819, 6.046352191393674, 51.79147680047071,
        6.110540343319396, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 8', 'ancientdata:1740-Kleve-DINA1-08_r', 51.73437245423012, 6.0196045630039565, 51.77533383415638,
        6.085694174077287, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 9', 'ancientdata:1740-Kleve-DINA1-09_r', 51.701978740840865, 6.073059609648879, 51.734918675183586,
        6.139548633704987, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 10', 'ancientdata:1740-Kleve-DINA1-10_r', 51.71190925262841, 6.028074408837049, 51.74909286779242,
        6.097061797809015, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 11', 'ancientdata:1740-Kleve-DINA1-11_r', 51.70378612585145, 6.0202019269466875, 51.738783901985535,
        6.086039558873208, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 12', 'ancientdata:1740-Kleve-DINA1-12_r', 51.720365114334676, 5.984911069366643, 51.75418291977292,
        6.054044974780257, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 13', 'ancientdata:1740-Kleve-DINA1-13_r', 51.74588100091163, 5.970521074183843, 51.77820643078969,
        6.041818616786195, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 14', 'ancientdata:1740-Kleve-DINA1-14_r', 51.73326775071595, 5.957216780600897, 51.76088538697168,
        6.018964850841942, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 15', 'ancientdata:1740-Kleve-DINA1-15_r', 51.732550582035415, 5.92953195601755, 51.760620065038815,
        5.988022815568189, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('Sheet 16', 'ancientdata:1740-Kleve-DINA1-16_r', 51.72739911643023, 5.889941922033602, 51.762824002738,
        5.955974491305206, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
       ('DEM of Gelderland-NRW research area - Hillshade', 'ancientdata:research_area_Gelderland_NRW_hillshade_cog',
        51.45783572791835, 5.419587299877188, 52.0690994943719, 6.865869726173805, 8, 17,
        'AHN3 (NL) & DGM1 (NRW, DE) LiDAR', 'DEM', NULL, TRUE),
       ('DEM of Gelderland-NRW research area', 'ancientdata:research_area_Gelderland_NRW_cog', 51.45783572791835,
        5.419587299877188, 52.0690994943719, 6.865869726173805, 8, 17, 'AHN3 (NL) & DGM1 (NRW, DE) LiDAR', 'DEM', NULL,
        FALSE),
       ('DEM of Swalmen area (AHN3 + DGM1) - Hillshade', 'ancientdata:merge_swalmen_hillshade_cog', 51.14425175970463,
        5.965415083893099, 51.35358917449149, 6.409873937995263, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', NULL, TRUE),
       ('DEM of Swalmen area (AHN3 + DGM1)', 'ancientdata:merge_swalmen_cog', 51.14425175970463, 5.965415083893099,
        51.35358917449149, 6.409873937995263, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', NULL, FALSE),
       ('DEM of Geldern - Venlo area (DGM1, AHN3) - Hillshade', 'ancientdata:merge_venlo_geldern_hillshade_cog',
        51.3350043816511, 5.966035132096261, 51.54046275728694, 6.416339886112167, 8, 18, 'AHN3 & DGM1 LiDAR', 'DEM',
        NULL, TRUE),
       ('DEM of Geldern - Venlo area (DGM1, AHN3)', 'ancientdata:merge_venlo_geldern_cog', 51.3350043816511,
        5.966035132096261, 51.54046275728694, 6.416339886112167, 8, 18, 'AHN3 & DGM1 LiDAR', 'DEM', NULL, FALSE),
       ('DEM of Mönchengladbach - Neuss area (DGM1) - Hillshade', 'ancientdata:dem_Mönchengladbach-Neuss-hillshade',
        51.09919903622249, 6.423118334166201, 51.21238621797372, 6.685970391067374, 8, 17, 'DGM1 LiDAR', 'DEM', NULL,
        TRUE),
       ('DEM of Mönchengladbach - Neuss area (DGM1)', 'ancientdata:Mönchengladbach-Neuss-merge_cog', 51.09919903622249,
        6.423118334166201, 51.21238621797372, 6.685970391067374, 8, 17, 'DGM1 LiDAR', 'DEM', NULL, FALSE),
       ('DEM of area west of Mönchengladbach (DGM1, AHN3) - Hillshade', 'ancientdata:Mönchengladbach_west-hillshade',
        51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM',
        NULL, TRUE),
       ('DEM of area west of Mönchengladbach (DGM1, AHN3)', 'ancientdata:Mönchengladbach_west-merge_cog',
        51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM',
        NULL, FALSE) ON CONFLICT DO NOTHING;

-- Dev-only login: username "devadmin", password "DevAdmin123!" (bcrypt hash below).
-- Never use this hash/credential pair outside a throwaway local container.
INSERT INTO users (username, password, role)
VALUES ('devadmin', '$2b$10$Q.kfGT8wu6.vu3UZvrcte.vRLpgBXWhitWtoloReUqO8Y9ov68JC2', 'ADMIN') ON CONFLICT DO NOTHING;

-- Grant access to the application user (matches the real deployment's role name;
-- harmless no-op if this user doesn't exist in a given local container since we
-- create it below as part of this same script for local-dev convenience).
DO
$$
BEGIN
    IF
NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'webgis_client') THEN
CREATE ROLE webgis_client LOGIN PASSWORD 'local_dev_only';
END IF;
END
$$;

GRANT ALL PRIVILEGES ON ALL
TABLES IN SCHEMA public TO webgis_client;
GRANT ALL PRIVILEGES ON ALL
SEQUENCES IN SCHEMA public TO webgis_client;
