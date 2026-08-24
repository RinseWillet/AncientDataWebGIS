package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.RasterLayerCategory;

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
 * @param category    distinguishes elevation data from other raster overlays
 * @param collection  optional shared name grouping multiple sheets of the same
 *                    published atlas/series (e.g. "1818 De Man - Nijmegen") so
 *                    the frontend can nest them under one collapsible group
 *                    instead of listing every sheet flat. Null for a
 *                    standalone entry that isn't part of a multi-sheet series.
 * @param hillshade   true for a multidirectional-hillshade derivative of a DEM
 *                    layer (own COG, own GeoServer layer, same extent/zoom as
 *                    its elevation counterpart). The frontend renders these
 *                    with a multiply blend over whatever's beneath them rather
 *                    than as an independent tile layer, and excludes them from
 *                    "active DEM for the Elevation legend" - a hillshade has no
 *                    colour ramp of its own to explain. False for every other
 *                    entry (including plain elevation DEM layers).
 */
public record RasterLayerDTO(
        String name,
        String source,
        RasterBoundsDTO bounds,
        RasterZoomDTO zoom,
        String attribution,
        RasterLayerCategory category,
        String collection,
        boolean hillshade
) {}
