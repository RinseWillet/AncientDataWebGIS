package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

public record SiteDTO(

        Long id,

        Integer pleiadesId,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be less than 255 characters")
        String name,

        @NotBlank(message = "Geometry (WKT) is required")
        String geom,

        @Size(max = 255, message = "Province must be less than 255 characters")
        String province,

        @NotBlank(message = "Site type is required")
        @Size(max = 255, message = "Site type must be less than 255 characters")
        String siteType,

        @Size(max = 255, message = "Status must be less than 255 characters")
        String status,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        String description,

        @Size(max = 800, message = "References must be less than 800 characters")
        String references,

        List<ModernReferenceDTO>modernReferenceList

) implements Serializable {}