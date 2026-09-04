package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.BackupStatusService;
import com.webgis.ancientdata.application.service.DbBackupService;
import com.webgis.ancientdata.application.service.NasBackupService;
import com.webgis.ancientdata.config.DbBackupConfig;
import com.webgis.ancientdata.config.NasBackupConfig;
import com.webgis.ancientdata.domain.dto.BackupStatusDTO;
import com.webgis.ancientdata.domain.model.BackupOutcome;
import com.webgis.ancientdata.domain.model.BackupRunResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final NasBackupService nasBackupService;
    private final NasBackupConfig nasBackupConfig;
    private final DbBackupService dbBackupService;
    private final DbBackupConfig dbBackupConfig;
    private final BackupStatusService backupStatusService;
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";

    public BackupController(
            NasBackupService nasBackupService,
            NasBackupConfig nasBackupConfig,
            DbBackupService dbBackupService,
            DbBackupConfig dbBackupConfig,
            BackupStatusService backupStatusService) {
        this.nasBackupService = nasBackupService;
        this.nasBackupConfig = nasBackupConfig;
        this.dbBackupService = dbBackupService;
        this.dbBackupConfig = dbBackupConfig;
        this.backupStatusService = backupStatusService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Map<String, String>>> triggerSync() {
        Map<String, String> databaseResult = dbBackupConfig.isEnabled()
                ? runAndReport(dbBackupService::run)
                : Map.of(STATUS, "error", MESSAGE, "Database backup is not enabled");

        Map<String, String> mediaResult = nasBackupConfig.isEnabled()
                ? runAndReport(nasBackupService::sync)
                : Map.of(STATUS, "error", MESSAGE, "NAS backup is not enabled");

        return ResponseEntity.ok(Map.of("database", databaseResult, "media", mediaResult));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<BackupStatusDTO> getStatus() {
        return ResponseEntity.ok(backupStatusService.getStatus());
    }

    private Map<String, String> runAndReport(Supplier<BackupRunResult> backupAction) {
        BackupRunResult result = backupAction.get();
        String status = result.outcome() == BackupOutcome.SUCCESS ? "ok" : "error";
        return Map.of(STATUS, status, MESSAGE, result.message());
    }
}


