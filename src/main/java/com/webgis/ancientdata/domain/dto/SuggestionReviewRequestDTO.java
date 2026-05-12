package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuggestionReviewRequestDTO(
        @NotBlank(message = "Decision is required")
        String decision,

        @Size(max = 2000, message = "Reviewer notes must be less than 2000 characters")
        String reviewerNotes
) {
}

