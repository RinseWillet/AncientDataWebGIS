package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RasterCatalogService;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public read-only catalog of raster layers published in GeoServer, for
 * frontend discovery (see ADR-012, E3-2). Distinct from
 * {@link RasterProxyController}, which forwards actual tile/capabilities
 * requests to GeoServer.
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
}
