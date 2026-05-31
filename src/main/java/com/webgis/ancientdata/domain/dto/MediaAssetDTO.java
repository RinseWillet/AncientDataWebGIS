package com.webgis.ancientdata.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

public record MediaAssetDTO(
        Long id,
        String targetType,
        Long targetId,
        String fullUrl,
        String caption,
        String author,
        String source,
        String license,
        LocalDate dateTaken,
        boolean isCover,
        String visibilityStatus,
        Instant createdAt,
        Instant updatedAt
) {}

