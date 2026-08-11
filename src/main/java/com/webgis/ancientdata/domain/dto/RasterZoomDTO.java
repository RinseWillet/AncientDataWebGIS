package com.webgis.ancientdata.domain.dto;

/** Curated min/max zoom for displaying a raster overlay on a Leaflet map. */
public record RasterZoomDTO(
        int min,
        int max
) {}
