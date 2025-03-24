package com.webgis.ancientdata.domain.dto;

import lombok.Data;

@Data
public class RoadDTO {
    private long id;
    private int cat_nr;
    private String name;
    private String geom; // WKT String
    private String type;
    private String typeDescription;
    private String location;
    private String description;
    private String date;
}