package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.config.GeoServerProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Forwards read-only OGC service requests (WMS/WMTS/GWC) to the internal
 * GeoServer container. GeoServer's admin REST API and web admin UI stay
 * LAN/WARP-only — this proxy never forwards to them (see ADR-012).
 */
@Service
public class RasterProxyService {

    private static final Logger logger = LoggerFactory.getLogger(RasterProxyService.class);

    /** Caching-relevant headers worth passing through from GeoServer/GWC as-is. */
    private static final List<String> FORWARDED_HEADER_NAMES = List.of(
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.ETAG,
            HttpHeaders.LAST_MODIFIED,
            HttpHeaders.EXPIRES
    );

    /** GWC's own diagnostic prefix (e.g. "geowebcache-cache-result") - forwarded for
     * observability (confirming whether a tile was a cache HIT/MISS) even though the
     * app itself doesn't act on it. */
    private static final String GEOWEBCACHE_HEADER_PREFIX = "geowebcache-";

    /** Map tiles are public and don't vary per requester, so they're safely cacheable -
     * unlike this app's other endpoints, which correctly default to no-store. Only
     * applied when GeoServer/GWC didn't already specify its own Cache-Control. */
    private static final String DEFAULT_CACHE_CONTROL = "public, max-age=3600";

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
    public ResponseEntity<StreamingResponseBody> forward(String subPath, String queryString) {
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
            // exchange(fn, false) keeps the upstream ClientHttpResponse open past this callback
            // instead of closing it once headers are read, so the StreamingResponseBody below can
            // pipe the body straight to the client in bounded chunks - a large upstream DEM/GeoTIFF
            // response used to be fully buffered into a byte[] here, which was the root cause of
            // the 2026-08-20 host-wide outage (see ADR-016).
            return restClient.get().uri(uri).exchange((_, response) -> {
                HttpHeaders upstreamHeaders = response.getHeaders();

                HttpHeaders headers = new HttpHeaders();
                MediaType contentType = upstreamHeaders.getContentType();
                if (contentType != null) {
                    headers.setContentType(contentType);
                }
                FORWARDED_HEADER_NAMES.forEach(name -> {
                    List<String> values = upstreamHeaders.get(name);
                    if (values != null) {
                        headers.put(name, values);
                    }
                });
                upstreamHeaders.forEach((name, values) -> {
                    if (name.toLowerCase(Locale.ROOT).startsWith(GEOWEBCACHE_HEADER_PREFIX)) {
                        headers.put(name, values);
                    }
                });
                if (headers.getFirst(HttpHeaders.CACHE_CONTROL) == null) {
                    headers.set(HttpHeaders.CACHE_CONTROL, DEFAULT_CACHE_CONTROL);
                }

                HttpStatusCode status = response.getStatusCode();
                StreamingResponseBody body = outputStream -> {
                    // try-with-resources on the upstream response (not just its InputStream) so the
                    // underlying connection is released once streaming finishes, whether it succeeds,
                    // the client disconnects mid-stream, or upstream drops the connection.
                    try (response) {
                        response.getBody().transferTo(outputStream);
                    }
                };

                return new ResponseEntity<>(body, headers, status);
            }, false);
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
