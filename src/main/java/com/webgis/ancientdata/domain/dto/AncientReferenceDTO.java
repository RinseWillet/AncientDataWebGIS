package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AncientReferenceDTO(
        Long id,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be less than 255 characters")
        String name,

        @Size(max = 255, message = "Author must be less than 255 characters")
        String author,

        @Size(max = 255, message = "Work must be less than 255 characters")
        String work,

        @Size(max = 64, message = "Book must be less than 64 characters")
        String book,

        Integer page
) {}
