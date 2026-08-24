package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterZoomDTO;
import com.webgis.ancientdata.domain.model.RasterLayer;

public class RasterLayerMapper {

    private RasterLayerMapper() {
    }

    public static RasterLayerDTO toDto(RasterLayer entity) {
        return new RasterLayerDTO(
                entity.getName(),
                entity.getSource(),
                new RasterBoundsDTO(
                        entity.getBoundsSouth(),
                        entity.getBoundsWest(),
                        entity.getBoundsNorth(),
                        entity.getBoundsEast()
                ),
                new RasterZoomDTO(entity.getZoomMin(), entity.getZoomMax()),
                entity.getAttribution(),
                entity.getCategory(),
                entity.getCollection(),
                entity.isHillshade()
        );
    }
}
