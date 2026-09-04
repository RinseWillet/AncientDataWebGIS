package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.application.service.support.ProcessExecutor;
import com.webgis.ancientdata.config.DbBackupConfig;
import com.webgis.ancientdata.domain.model.BackupHistory;
import com.webgis.ancientdata.domain.model.BackupOutcome;
import com.webgis.ancientdata.domain.model.BackupRunResult;
import com.webgis.ancientdata.domain.model.BackupType;
import com.webgis.ancientdata.domain.repository.BackupHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Database backup service. Runs pg_dump against the configured PostgreSQL
 * database and records the outcome in backup_history.
 */
@Service
public class DbBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DbBackupService.class);
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DbBackupConfig config;
    private final BackupHistoryRepository backupHistoryRepository;
    private final ProcessExecutor processExecutor;

    public DbBackupService(
            DbBackupConfig config,
            BackupHistoryRepository backupHistoryRepository,
            ProcessExecutor processExecutor) {
        this.config = config;
        this.backupHistoryRepository = backupHistoryRepository;
        this.processExecutor = processExecutor;
    }

    /**
     * Runs pg_dump and records the outcome. Never throws — failures are captured
     * as a FAILURE row in backup_history (best-effort; see {@link #recordHistory}).
     *
     * @return the actual outcome of this run, for callers (e.g. the admin "back up
     *         now" endpoint) that need to report real success/failure rather than
     *         assuming the call succeeded just because it returned.
     */
    public BackupRunResult run() {
        if (!config.isEnabled()) {
            logger.info("Database backup is disabled");
            return BackupRunResult.failure("Database backup is not enabled");
        }

        Instant startedAt = Instant.now();
        BackupOutcome outcome;
        String message;

        try {
            Path outputDir = Paths.get(config.getOutputPath()).toAbsolutePath().normalize();
            Files.createDirectories(outputDir);
            Path dumpFile = outputDir.resolve("ancientdata-db-" + FILE_TIMESTAMP.format(startedAt.atZone(java.time.ZoneOffset.UTC)) + ".sql");

            List<String> command = List.of(
                    "pg_dump",
                    "-h", config.getHost(),
                    "-p", String.valueOf(config.getPort()),
                    "-U", config.getUser(),
                    "-d", config.getName(),
                    "-F", "plain"
            );

            int exitCode = processExecutor.runToFile(command, Map.of("PGPASSWORD", config.getPassword()), dumpFile);

            if (exitCode == 0) {
                outcome = BackupOutcome.SUCCESS;
                message = "Database backup written to " + dumpFile;
                logger.info("Database backup succeeded: {}", dumpFile);
            } else {
                outcome = BackupOutcome.FAILURE;
                message = "pg_dump exited with code " + exitCode;
                logger.error("Database backup failed: {}", message);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            outcome = BackupOutcome.FAILURE;
            message = "Database backup failed: " + e.getMessage();
            logger.error(message, e);
        }

        recordHistory(startedAt, outcome, message);
        return new BackupRunResult(outcome, message);
    }

    /**
     * Persists the outcome for audit/status purposes. This is best-effort: a
     * persistence failure here (e.g. the backup_history table missing on the
     * shared, manually-migrated production database — see DB-MIGRATION-STRATEGY.md)
     * must not mask the real pg_dump outcome from the caller by throwing.
     */
    private void recordHistory(Instant startedAt, BackupOutcome outcome, String message) {
        BackupHistory history = new BackupHistory();
        history.setBackupType(BackupType.DATABASE);
        history.setOutcome(outcome);
        history.setStartedAt(startedAt);
        history.setFinishedAt(Instant.now());
        history.setMessage(message);
        try {
            backupHistoryRepository.save(history);
        } catch (RuntimeException e) {
            logger.error("Failed to persist backup_history row for database backup: {}", e.getMessage(), e);
        }
    }
}

