package com.webgis.ancientdata.domain.dto;

import com.webgis.ancientdata.domain.model.RasterLayerCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Admin-only request to register a newly published raster layer in the
 * catalog (E3-6). Field-level blank/null checks are enforced here; cross-field
 * invariants (bounds south&lt;north, west&lt;east, zoom min&lt;=max) are
 * enforced in {@code RasterCatalogService}.
 */
public record RasterLayerCreateRequest(
        @NotBlank String name,
        @NotBlank String source,
        @NotNull RasterBoundsDTO bounds,
        @NotNull RasterZoomDTO zoom,
        @NotBlank String attribution,
        @NotNull RasterLayerCategory category,
        String collection,
        boolean hillshade
) {}
