package com.webgis.ancientdata.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoadDTO(

        long id,

        @NotNull(message = "Catalog number is required")
        Integer cat_nr,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be less than 255 characters")
        String name,

        @NotBlank(message = "Geometry (WKT) is required")
        String geom,

        @NotBlank(message = "Type is required")
        @Size(max = 255, message = "Type must be less than 255 characters")
        String type,

        @Size(max = 1000, message = "Type description must be less than 1000 characters")
        String typeDescription,

        @Size(max = 1500, message = "Location must be less than 1500 characters")
        String location,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        String description,

        String date,

        @JsonProperty("referenceIds")
        List<Long> referenceIds
) {}