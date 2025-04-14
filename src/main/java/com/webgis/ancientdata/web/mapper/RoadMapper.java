package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import org.locationtech.jts.io.WKTWriter;

import java.util.Collections;
import java.util.List;

public class RoadMapper {

    public static RoadDTO toDto(Road road) {
        List<Long> referenceIds = road.getModernReferences() != null
                ? road.getModernReferences().stream().map(ModernReference::getId).toList()
                : Collections.emptyList();

        return new RoadDTO(
                road.getId(),
                road.getCat_nr(),
                road.getName(),
                new WKTWriter().write(road.getGeom()),
                road.getType(),
                road.getTypeDescription(),
                road.getLocation(),
                road.getDescription(),
                road.getDate(),
                referenceIds
        );
    }
}