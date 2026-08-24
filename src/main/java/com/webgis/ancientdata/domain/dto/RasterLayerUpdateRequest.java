package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.RasterLayerCategory;

/**
 * Admin-only partial update for an existing catalog entry, addressed by its
 * {@code source} (path parameter, not part of this body — see E3-6). Every
 * field is optional; only non-null fields overwrite the existing entry.
 * Resulting cross-field invariants (bounds south&lt;north, west&lt;east,
 * zoom min&lt;=max) are re-checked against the merged entity in
 * {@code RasterCatalogService}.
 */
public record RasterLayerUpdateRequest(
        String name,
        RasterBoundsDTO bounds,
        RasterZoomDTO zoom,
        String attribution,
        RasterLayerCategory category,
        String collection,
        Boolean hillshade
) {}
