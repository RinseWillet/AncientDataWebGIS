-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through pgAdmin or psql against the PostGIS container.
-- Do NOT use Flyway — schema is externally managed (see DB-MIGRATION-STRATEGY.md).
--
-- E2-BACKUP-NAS-4: backup_history table
-- Records the outcome of each database/media backup attempt (scheduled or manually
-- triggered via POST /api/backup/sync) so the admin UI can show when the last
-- successful backup ran and flag stale/failed backups.

CREATE TABLE IF NOT EXISTS backup_history (
    id           BIGSERIAL       PRIMARY KEY,

    backup_type  VARCHAR(20)     NOT NULL CHECK (backup_type IN ('DATABASE', 'MEDIA')),
    outcome      VARCHAR(20)     NOT NULL CHECK (outcome IN ('SUCCESS', 'FAILURE')),

    started_at   TIMESTAMPTZ     NOT NULL,
    finished_at  TIMESTAMPTZ     NOT NULL,
    message      VARCHAR(1000)
);

-- Fast lookup of the most recent run per backup type
CREATE INDEX IF NOT EXISTS idx_backup_history_type_started
    ON backup_history (backup_type, started_at DESC);

-- Grant access to the application user
GRANT ALL PRIVILEGES ON TABLE backup_history TO webgis_client;
GRANT USAGE, SELECT ON SEQUENCE backup_history_id_seq TO webgis_client;

