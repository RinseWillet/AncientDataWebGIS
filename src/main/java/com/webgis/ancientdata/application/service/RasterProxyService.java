package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.config.GeoServerProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

/**
 * Forwards read-only OGC service requests (WMS/WMTS/GWC) to the internal
 * GeoServer container. GeoServer's admin REST API and web admin UI stay
 * LAN/WARP-only — this proxy never forwards to them (see ADR-012).
 */
@Service
public class RasterProxyService {

    private static final Logger logger = LoggerFactory.getLogger(RasterProxyService.class);

    private final GeoServerProxyConfig config;
    private final RestClient restClient;

    public RasterProxyService(GeoServerProxyConfig config) {
        this.config = config;
        this.restClient = RestClient.create();
    }

    /**
     * @param subPath     the request path after "/api/raster", e.g. "/ancientdata/wms"
     * @param queryString raw query string (may be null)
     */
    public ResponseEntity<byte[]> forward(String subPath, String queryString) {
        if (isDeniedPath(subPath)) {
            logger.warn("Blocked raster proxy request to admin path: {}", subPath);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String targetUrl = config.getInternalUrl() + subPath + (queryString != null ? "?" + queryString : "");

        try {
            // subPath/queryString are already percent-encoded as received from the client.
            // uri(URI) is used instead of uri(String) so RestClient treats it as a fully
            // composed URI and forwards it verbatim, rather than as a template it re-encodes
            // (which would double-encode e.g. "%2C" into "%252C" and corrupt query params).
            URI uri = URI.create(targetUrl);
            return restClient.get().uri(uri).exchange((_, response) -> {
                byte[] body = response.getBody().readAllBytes();

                HttpHeaders headers = new HttpHeaders();
                MediaType contentType = response.getHeaders().getContentType();
                if (contentType != null) {
                    headers.setContentType(contentType);
                }

                return new ResponseEntity<>(body, headers, response.getStatusCode());
            });
        } catch (IllegalArgumentException e) {
            logger.warn("Malformed raster proxy request {}: {}", subPath, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RestClientException e) {
            logger.error("GeoServer unreachable for {}: {}", subPath, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    private boolean isDeniedPath(String subPath) {
        String lower = subPath.toLowerCase();
        return lower.contains("/rest/") || lower.contains("/web/");
    }
}
