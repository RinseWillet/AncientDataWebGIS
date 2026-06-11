package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.NasBackupService;
import com.webgis.ancientdata.config.NasBackupConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final NasBackupService backupService;
    private final NasBackupConfig backupConfig;

    public BackupController(NasBackupService backupService, NasBackupConfig backupConfig) {
        this.backupService = backupService;
        this.backupConfig = backupConfig;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        if (!backupConfig.isEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "NAS backup is not enabled"));
        }
        backupService.sync();
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Backup sync triggered"));
    }
}

