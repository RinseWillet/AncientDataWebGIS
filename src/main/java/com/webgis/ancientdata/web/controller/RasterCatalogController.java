package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RasterCatalogService;
import com.webgis.ancientdata.domain.dto.RasterLayerCreateRequest;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public read-only catalog of raster layers published in GeoServer, for
 * frontend discovery (see ADR-012, ADR-013, E3-2, E3-6), plus admin-only
 * write endpoints for managing the underlying {@code raster_layer} table.
 * Distinct from {@link RasterProxyController}, which forwards actual
 * tile/capabilities requests to GeoServer.
 */
@RestController
@RequestMapping("/api/raster")
public class RasterCatalogController {

    private final RasterCatalogService rasterCatalogService;

    public RasterCatalogController(RasterCatalogService rasterCatalogService) {
        this.rasterCatalogService = rasterCatalogService;
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<RasterLayerDTO>> getCatalog() {
        return ResponseEntity.ok(rasterCatalogService.getCatalog());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/catalog/admin")
    public ResponseEntity<List<RasterLayerDTO>> getCatalogForAdmin() {
        return ResponseEntity.ok(rasterCatalogService.getCatalog());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/catalog")
    public ResponseEntity<RasterLayerDTO> create(@Valid @RequestBody RasterLayerCreateRequest request) {
        return ResponseEntity.ok(rasterCatalogService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/catalog/{source}")
    public ResponseEntity<RasterLayerDTO> update(
            @PathVariable String source,
            @RequestBody RasterLayerUpdateRequest request) {
        return ResponseEntity.ok(rasterCatalogService.update(source, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/catalog/{source}")
    public ResponseEntity<Void> delete(@PathVariable String source) {
        rasterCatalogService.delete(source);
        return ResponseEntity.noContent().build();
    }
}
