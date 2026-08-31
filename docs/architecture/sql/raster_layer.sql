-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through pgAdmin or psql against the PostGIS container.
-- Do NOT use Flyway — schema is externally managed (see DB-MIGRATION-STRATEGY.md).
--
-- E3-6: raster_layer table
-- Replaces RasterCatalogService's static, hand-edited Java list (E3-2) as the
-- backing store for GET /api/raster/catalog. `source` (the GeoServer
-- "workspace:layer" identifier) is the stable public key the frontend and
-- the admin CRUD endpoints key off, not the internal `id`.
--
-- This script must be applied BEFORE deploying the application code that
-- reads from this table (see the E3-6 write-up in
-- docs/features/FEATURE-SPEC-BACKLOG.md for the cutover sequencing). The
-- seed INSERTs below transcribe the 30 entries previously hard-coded in
-- RasterCatalogService.java, so no catalog content is lost during cutover.

CREATE TABLE IF NOT EXISTS raster_layer (
    id                BIGSERIAL       PRIMARY KEY,

    name              VARCHAR(255)    NOT NULL,
    source            VARCHAR(255)    NOT NULL UNIQUE,

    -- WGS84 (EPSG:4326) lat/lon bounding box
    bounds_south      DOUBLE PRECISION NOT NULL,
    bounds_west       DOUBLE PRECISION NOT NULL,
    bounds_north      DOUBLE PRECISION NOT NULL,
    bounds_east       DOUBLE PRECISION NOT NULL,

    -- curated min/max zoom for display
    zoom_min          INTEGER         NOT NULL,
    zoom_max          INTEGER         NOT NULL,

    attribution       VARCHAR(512)    NOT NULL,
    category          VARCHAR(20)     NOT NULL CHECK (category IN ('HISTORICAL_MAP', 'DEM')),
    collection        VARCHAR(255),
    hillshade         BOOLEAN         NOT NULL DEFAULT FALSE,

    -- audit
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Keep updated_at in sync automatically
CREATE OR REPLACE FUNCTION set_raster_layer_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_raster_layer_updated_at ON raster_layer;
CREATE TRIGGER trg_raster_layer_updated_at
    BEFORE UPDATE ON raster_layer
    FOR EACH ROW EXECUTE FUNCTION set_raster_layer_updated_at();

-- Grant access to the application user
GRANT ALL PRIVILEGES ON TABLE raster_layer TO webgis_client;
GRANT USAGE, SELECT ON SEQUENCE raster_layer_id_seq TO webgis_client;

-- Seed data: transcribed verbatim from RasterCatalogService.java's static
-- CATALOG list (E3-2), in the same order, so `id` ordering preserves the
-- existing hillshade-immediately-before-elevation-sibling ordering the
-- frontend's Physical group z-order relies on (see E3-3/E3-4 write-ups).
-- ON CONFLICT (source) DO NOTHING makes this script safe to re-run.

INSERT INTO raster_layer (name, source, bounds_south, bounds_west, bounds_north, bounds_east, zoom_min, zoom_max, attribution, category, collection, hillshade) VALUES
('Sheet A1', 'ancientdata:De-Man-1818-A1', 51.782659965003994, 5.757907509752199, 51.87017277720497, 5.9033985468639525, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet A2', 'ancientdata:De-Man-1818-A2', 51.79248099648939, 5.757982824283212, 51.825455193924086, 5.83070005750662, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet A3', 'ancientdata:De-Man-1818-A3', 51.80404451531167, 5.830950124353257, 51.84002406292155, 5.892320690155102, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet A4', 'ancientdata:De-Man-1818-A4', 51.81399554398528, 5.781638645840879, 51.84932509161219, 5.84386633466715, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet A5', 'ancientdata:De-Man-1818-A5', 51.830096780009875, 5.843679743944152, 51.86142158497805, 5.90330558027289, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet A6', 'ancientdata:De-Man-1818-A6', 51.84001106233645, 5.795165495731549, 51.87004849240705, 5.855503640022906, 12, 19, '1818 De Man - Nijmegen', 'HISTORICAL_MAP', '1818 De Man - Nijmegen', FALSE),
('Sheet 3', 'ancientdata:1740-Kleve-DINA1-03_r', 51.73343140813606, 6.104849008894311, 51.768296396569404, 6.172473312074854, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 4', 'ancientdata:1740-Kleve-DINA1-04_r', 51.714760818201405, 6.098408831298351, 51.75039849774439, 6.167572939469806, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 5', 'ancientdata:1740-Kleve-DINA1-05_r', 51.724959956235594, 6.059738633657765, 51.759432933524536, 6.126875610219495, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 6', 'ancientdata:1740-Kleve-DINA1-06_r', 51.74433174217834, 6.0749002252559245, 51.77728077588717, 6.142042710074628, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 7', 'ancientdata:1740-Kleve-DINA1-07_r', 51.74940470691819, 6.046352191393674, 51.79147680047071, 6.110540343319396, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 8', 'ancientdata:1740-Kleve-DINA1-08_r', 51.73437245423012, 6.0196045630039565, 51.77533383415638, 6.085694174077287, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 9', 'ancientdata:1740-Kleve-DINA1-09_r', 51.701978740840865, 6.073059609648879, 51.734918675183586, 6.139548633704987, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 10', 'ancientdata:1740-Kleve-DINA1-10_r', 51.71190925262841, 6.028074408837049, 51.74909286779242, 6.097061797809015, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 11', 'ancientdata:1740-Kleve-DINA1-11_r', 51.70378612585145, 6.0202019269466875, 51.738783901985535, 6.086039558873208, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 12', 'ancientdata:1740-Kleve-DINA1-12_r', 51.720365114334676, 5.984911069366643, 51.75418291977292, 6.054044974780257, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 13', 'ancientdata:1740-Kleve-DINA1-13_r', 51.74588100091163, 5.970521074183843, 51.77820643078969, 6.041818616786195, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 14', 'ancientdata:1740-Kleve-DINA1-14_r', 51.73326775071595, 5.957216780600897, 51.76088538697168, 6.018964850841942, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 15', 'ancientdata:1740-Kleve-DINA1-15_r', 51.732550582035415, 5.92953195601755, 51.760620065038815, 5.988022815568189, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('Sheet 16', 'ancientdata:1740-Kleve-DINA1-16_r', 51.72739911643023, 5.889941922033602, 51.762824002738, 5.955974491305206, 12, 19, '1740 - Kleve', 'HISTORICAL_MAP', '1740 - Kleve', FALSE),
('DEM of Gelderland-NRW research area - Hillshade', 'ancientdata:research_area_Gelderland_NRW_hillshade_cog', 51.45783572791835, 5.419587299877188, 52.0690994943719, 6.865869726173805, 8, 17, 'AHN3 (NL) & DGM1 (NRW, DE) LiDAR', 'DEM', 'Gelderland-NRW', TRUE),
('DEM of Gelderland-NRW research area', 'ancientdata:research_area_Gelderland_NRW_cog', 51.45783572791835, 5.419587299877188, 52.0690994943719, 6.865869726173805, 8, 17, 'AHN3 (NL) & DGM1 (NRW, DE) LiDAR', 'DEM', 'Gelderland-NRW', FALSE),
('DEM of Swalmen area (AHN3 + DGM1) - Hillshade', 'ancientdata:merge_swalmen_hillshade_cog', 51.14425175970463, 5.965415083893099, 51.35358917449149, 6.409873937995263, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', 'Swalmen', TRUE),
('DEM of Swalmen area (AHN3 + DGM1)', 'ancientdata:merge_swalmen_cog', 51.14425175970463, 5.965415083893099, 51.35358917449149, 6.409873937995263, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', 'Swalmen', FALSE),
('DEM of Geldern - Venlo area (DGM1, AHN3) - Hillshade', 'ancientdata:merge_venlo_geldern_hillshade_cog', 51.3350043816511, 5.966035132096261, 51.54046275728694, 6.416339886112167, 8, 18, 'AHN3 & DGM1 LiDAR', 'DEM', 'Geldern - Venlo', TRUE),
('DEM of Geldern - Venlo area (DGM1, AHN3)', 'ancientdata:merge_venlo_geldern_cog', 51.3350043816511, 5.966035132096261, 51.54046275728694, 6.416339886112167, 8, 18, 'AHN3 & DGM1 LiDAR', 'DEM', 'Geldern - Venlo', FALSE),
('DEM of Mönchengladbach - Neuss area (DGM1) - Hillshade', 'ancientdata:dem_Mönchengladbach-Neuss-hillshade', 51.09919903622249, 6.423118334166201, 51.21238621797372, 6.685970391067374, 8, 17, 'DGM1 LiDAR', 'DEM', 'Mönchengladbach - Neuss', TRUE),
('DEM of Mönchengladbach - Neuss area (DGM1)', 'ancientdata:Mönchengladbach-Neuss-merge_cog', 51.09919903622249, 6.423118334166201, 51.21238621797372, 6.685970391067374, 8, 17, 'DGM1 LiDAR', 'DEM', 'Mönchengladbach - Neuss', FALSE),
('DEM of area west of Mönchengladbach (DGM1, AHN3) - Hillshade', 'ancientdata:Mönchengladbach_west-hillshade', 51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', 'Area west of Mönchengladbach', TRUE),
('DEM of area west of Mönchengladbach (DGM1, AHN3)', 'ancientdata:Mönchengladbach_west-merge_cog', 51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674, 8, 17, 'AHN3 & DGM1 LiDAR', 'DEM', 'Area west of Mönchengladbach', FALSE)
ON CONFLICT (source) DO NOTHING;

-- Backfill for the 10 DEM rows above if they were already inserted before `collection` was
-- populated for DEM entries (i.e. `ON CONFLICT (source) DO NOTHING` above is a no-op against
-- an existing shared DB) - groups each DEM with its hillshade sibling in the frontend's
-- "Physical" LayerPanel section the same way Historical Maps sheets are grouped by atlas.
-- Safe to re-run.
UPDATE raster_layer SET collection = 'Gelderland-NRW' WHERE source IN (
    'ancientdata:research_area_Gelderland_NRW_hillshade_cog', 'ancientdata:research_area_Gelderland_NRW_cog');
UPDATE raster_layer SET collection = 'Swalmen' WHERE source IN (
    'ancientdata:merge_swalmen_hillshade_cog', 'ancientdata:merge_swalmen_cog');
UPDATE raster_layer SET collection = 'Geldern - Venlo' WHERE source IN (
    'ancientdata:merge_venlo_geldern_hillshade_cog', 'ancientdata:merge_venlo_geldern_cog');
UPDATE raster_layer SET collection = 'Mönchengladbach - Neuss' WHERE source IN (
    'ancientdata:dem_Mönchengladbach-Neuss-hillshade', 'ancientdata:Mönchengladbach-Neuss-merge_cog');
UPDATE raster_layer SET collection = 'Area west of Mönchengladbach' WHERE source IN (
    'ancientdata:Mönchengladbach_west-hillshade', 'ancientdata:Mönchengladbach_west-merge_cog');
