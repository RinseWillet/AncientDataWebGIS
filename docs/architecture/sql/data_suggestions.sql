-- noinspection SqlDialectInspection
-- noinspection SqlNoDataSourceInspection
-- Manual SQL for shared PostGIS database
-- Apply through DBA/QGIS-managed process, not via Flyway in this repository.

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

CREATE INDEX IF NOT EXISTS idx_data_suggestions_status_created
    ON data_suggestions (status, created_at);

CREATE INDEX IF NOT EXISTS idx_data_suggestions_submitter
    ON data_suggestions (submitter_username, created_at DESC);

