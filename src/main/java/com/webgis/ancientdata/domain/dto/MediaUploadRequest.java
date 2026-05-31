package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.TargetType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public record MediaUploadRequest(
        MultipartFile file,
        TargetType targetType,
        Long targetId,
        String caption,
        String author,
        String source,
        String license,
        LocalDate dateTaken,
        boolean isCover,
        String createdBy
) {}

