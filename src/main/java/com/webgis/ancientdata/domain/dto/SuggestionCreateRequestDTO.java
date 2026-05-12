package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestionCreateRequestDTO(
        @NotBlank(message = "Target type is required")
        String targetType,

        Long targetId,

        @NotBlank(message = "Summary is required")
        @Size(max = 255, message = "Summary must be less than 255 characters")
        String summary,

        @NotBlank(message = "Details are required")
        @Size(max = 5000, message = "Details must be less than 5000 characters")
        String details,

        @Size(max = 1000, message = "Image URL must be less than 1000 characters")
        String imageUrl
) {
}

