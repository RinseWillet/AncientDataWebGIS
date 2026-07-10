package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.BackupStatusDTO;
import com.webgis.ancientdata.domain.dto.BackupTypeStatusDTO;
import com.webgis.ancientdata.domain.model.BackupHistory;
import com.webgis.ancientdata.domain.model.BackupType;
import com.webgis.ancientdata.domain.repository.BackupHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Reports the latest database/media backup outcome and whether it is stale
 * (older than the configured threshold, or has never run).
 */
@Service
public class BackupStatusService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final long stalenessThresholdHours;

    public BackupStatusService(
            BackupHistoryRepository backupHistoryRepository,
            @Value("${backup.staleness-threshold-hours:192}") long stalenessThresholdHours) {
        this.backupHistoryRepository = backupHistoryRepository;
        this.stalenessThresholdHours = stalenessThresholdHours;
    }

    public BackupStatusDTO getStatus() {
        return new BackupStatusDTO(
                toTypeStatus(BackupType.DATABASE),
                toTypeStatus(BackupType.MEDIA)
        );
    }

    private BackupTypeStatusDTO toTypeStatus(BackupType type) {
        Optional<BackupHistory> latest = backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(type);

        if (latest.isEmpty()) {
            // Never backed up — always stale.
            return new BackupTypeStatusDTO(null, null, null, true);
        }

        BackupHistory history = latest.get();
        boolean stale = Duration.between(history.getStartedAt(), Instant.now()).toHours() >= stalenessThresholdHours;

        return new BackupTypeStatusDTO(
                history.getStartedAt(),
                history.getOutcome().name(),
                history.getMessage(),
                stale
        );
    }
}

