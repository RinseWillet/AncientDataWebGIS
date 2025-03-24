package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import org.locationtech.jts.io.WKTWriter;

public class RoadMapper {

    public static RoadDTO toDto(Road road) {
        RoadDTO dto = new RoadDTO();
        dto.setId(road.getId());
        dto.setCat_nr(road.getCat_nr());
        dto.setName(road.getName());
        dto.setGeom(new WKTWriter().write(road.getGeom())); // Convert geometry to WKT
        dto.setType(road.getType());
        dto.setTypeDescription(road.getTypeDescription());
        dto.setLocation(road.getLocation());
        dto.setDescription(road.getDescription());
        dto.setDate(road.getDate());
        return dto;
    }
}