package com.webgis.ancientdata.domain.dto;

public record BackupStatusDTO(
        BackupTypeStatusDTO database,
        BackupTypeStatusDTO media
) {
}

