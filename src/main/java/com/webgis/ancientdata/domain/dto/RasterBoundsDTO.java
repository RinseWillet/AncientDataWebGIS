package com.webgis.ancientdata.domain.dto;

/**
 * WGS84 (EPSG:4326) lat/lon bounding box, as read from GeoServer's layer
 * "Lat/Lon Bounding Box" (already WGS84 regardless of the layer's native SRS).
 */
public record RasterBoundsDTO(
        double south,
        double west,
        double north,
        double east
) {}
