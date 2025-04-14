package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModernReferenceDTO(
        Long id,

        @NotBlank(message = "Short reference is required")
        @Size(max = 255, message = "Short reference must be less than 255 characters")
        String shortRef,

        @NotBlank(message = "Full reference is required")
        @Size(max = 1000, message = "Full reference must be less than 1000 characters")
        String fullRef,

        @Size(max = 1000, message = "URL must be less than 1000 characters")
        String url
) {}