package com.webgis.ancientdata.domain.dto;

/**
 * A single discoverable raster layer, published through GeoServer and served
 * via the read-only {@code /api/raster/**} proxy (see ADR-012).
 *
 * @param name        display name for the layer
 * @param source      GeoServer "workspace:layer" identifier, usable as the
 *                    {@code layers} parameter of a WMS request against
 *                    {@code /api/raster/<workspace>/wms}
 * @param bounds      WGS84 lat/lon extent
 * @param zoom        curated min/max zoom for display
 * @param attribution copyright/source text
 */
public record RasterLayerDTO(
        String name,
        String source,
        RasterBoundsDTO bounds,
        RasterZoomDTO zoom,
        String attribution
) {}
