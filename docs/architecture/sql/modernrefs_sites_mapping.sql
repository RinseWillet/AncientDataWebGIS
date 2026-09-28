-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through pgAdmin or psql against the PostGIS container.
-- Do NOT use Flyway — schema is externally managed (see DB-MIGRATION-STRATEGY.md).
--
-- modernrefs_sites_mapping: many-to-many join table between arch_sites and
-- modernrefs. Backs Site.modernReferenceList (@JoinTable in Site.java) and
-- ModernReferenceService's findSitesByModernReferenceId on the app side; on
-- the QGIS side it's the layer curators use to link sites to modern-source
-- references.
--
-- If applying this against a DB where the table already exists without a
-- primary key, dedupe first — duplicate (site_id, modernref_id) rows make
-- ADD PRIMARY KEY fail:
--   DELETE FROM modernrefs_sites_mapping a USING modernrefs_sites_mapping b
--   WHERE a.ctid < b.ctid AND a.site_id = b.site_id AND a.modernref_id = b.modernref_id;
--   ALTER TABLE modernrefs_sites_mapping ADD PRIMARY KEY (site_id, modernref_id);

CREATE TABLE IF NOT EXISTS modernrefs_sites_mapping (
    site_id      BIGINT NOT NULL REFERENCES arch_sites (id) ON DELETE CASCADE,
    modernref_id BIGINT NOT NULL REFERENCES modernrefs (id) ON DELETE CASCADE,
    PRIMARY KEY (site_id, modernref_id)
);

-- Grant access to the application service account
GRANT ALL PRIVILEGES ON TABLE modernrefs_sites_mapping TO webgis_client;

-- Grant access to the QGIS curator role (roads/fieldsystems/modernrefs/
-- unidentified_linear_objects owner) — this table was missing from its
-- grants, which is what caused QGIS's "This user has no privileges" warning.
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE modernrefs_sites_mapping TO qgis_user;
