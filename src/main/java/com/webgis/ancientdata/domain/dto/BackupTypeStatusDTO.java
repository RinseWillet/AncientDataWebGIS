package com.webgis.ancientdata.domain.dto;

import java.time.Instant;

public record BackupTypeStatusDTO(
        Instant lastRunAt,
        String outcome,
        String message,
        boolean stale
) {
}

