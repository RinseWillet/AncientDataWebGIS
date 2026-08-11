package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RasterProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public read-only proxy to the internal GeoServer container, so raster
 * tile/capabilities requests can reach GeoServer without giving it a public
 * port or exposing its admin surface (see ADR-012).
 */
@RestController
@RequestMapping("/api/raster")
public class RasterProxyController {

    private final RasterProxyService rasterProxyService;

    public RasterProxyController(RasterProxyService rasterProxyService) {
        this.rasterProxyService = rasterProxyService;
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String subPath = fullPath.substring(fullPath.indexOf("/api/raster") + "/api/raster".length());
        return rasterProxyService.forward(subPath, request.getQueryString());
    }
}
