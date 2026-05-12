package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.SuggestionStatus;
import com.webgis.ancientdata.domain.model.SuggestionTargetType;

import java.time.LocalDateTime;

public record SuggestionResponseDTO(
        Long id,
        SuggestionTargetType targetType,
        Long targetId,
        String summary,
        String details,
        String imageUrl,
        SuggestionStatus status,
        String submitterUsername,
        String reviewerNotes,
        String reviewedBy,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}

