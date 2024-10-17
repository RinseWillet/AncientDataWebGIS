package com.webgis.ancientdata.road;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.MultiLineString;

@Data
@AllArgsConstructor
public class RoadDTO {

    private long id;

    private String name;

    private String type;

    private MultiLineString geom;
}
