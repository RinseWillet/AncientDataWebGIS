package com.webgis.ancientdata.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RoadDTO {
    private long id;

    @NotNull(message = "Catalog number is required")
    private Integer cat_nr;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Geometry (WKT) is required")
    private String geom;

    @NotBlank(message = "Type is required")
    @Size(max = 255, message = "Type must be less than 255 characters")
    private String type;

    @Size(max = 1000, message = "Type description must be less than 1000 characters")
    private String typeDescription;

    @Size(max = 1500, message = "Location must be less than 1500 characters")
    private String location;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    private String date;
}