package com.webgis.ancientdata.domain.dto;

import lombok.Data;

@Data
public class SiteDTO {
    private Long id;
    private Integer pleiadesId;
    private String name;
    private String geom; // WKT Point string
    private String province;
    private String siteType;
    private String status;
    private String references;
    private String description;
}