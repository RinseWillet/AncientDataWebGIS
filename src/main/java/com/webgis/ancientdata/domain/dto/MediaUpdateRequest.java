package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.VisibilityStatus;

import java.time.LocalDate;

public record MediaUpdateRequest(
        Long id,
        String caption,
        String author,
        String source,
        String license,
        LocalDate dateTaken,
        Boolean isCover,
        VisibilityStatus visibilityStatus
) {}

