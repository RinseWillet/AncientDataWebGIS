package com.webgis.ancientdata.domain.model;

/**
 * Outcome of a single backup run, returned by the backup services to their caller
 * (e.g. {@code BackupController}) so the HTTP response can reflect what actually
 * happened instead of an unconditional "ok".
 */
public record BackupRunResult(BackupOutcome outcome, String message) {

    public static BackupRunResult success(String message) {
        return new BackupRunResult(BackupOutcome.SUCCESS, message);
    }

    public static BackupRunResult failure(String message) {
        return new BackupRunResult(BackupOutcome.FAILURE, message);
    }
}
