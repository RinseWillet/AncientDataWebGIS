package com.webgis.ancientdata.domain.dto;

/**
 * Distinguishes a raster layer's kind so consumers (e.g. the frontend's
 * DEM color-ramp legend) can react only to elevation data, not any raster
 * overlay. See E3-3.
 */
public enum RasterLayerCategory {
    HISTORICAL_MAP,
    DEM
}
