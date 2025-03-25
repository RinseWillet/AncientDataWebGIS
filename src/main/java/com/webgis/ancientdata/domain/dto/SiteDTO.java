package com.webgis.ancientdata.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SiteDTO {

    private Long id;

    private Integer pleiadesId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Geometry (WKT) is required")
    private String geom;

    @Size(max = 255, message = "Province must be less than 255 characters")
    private String province;

    @NotBlank(message = "Site type is required")
    @Size(max = 255, message = "Site type must be less than 255 characters")
    private String siteType;

    @Size(max = 255, message = "Status must be less than 255 characters")
    private String status;

    @Size(max = 800, message = "References must be less than 800 characters")
    private String references;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;
}