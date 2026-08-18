package com.webgis.ancientdata.rastertests;

import com.sun.net.httpserver.HttpServer;
import com.webgis.ancientdata.application.service.RasterProxyService;
import com.webgis.ancientdata.config.GeoServerProxyConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RasterProxyServiceTests {

    private HttpServer stubGeoServer;
    private RasterProxyService service;

    @BeforeEach
    void setUp() throws IOException {
        stubGeoServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        stubGeoServer.createContext("/geoserver/ancientdata/wms", exchange -> {
            // getRawQuery() (not getQuery()) — asserts on the exact bytes received on the
            // wire, so encoding/double-encoding bugs are visible rather than silently
            // decoded away by URI's own query accessor.
            byte[] body = ("query=" + exchange.getRequestURI().getRawQuery()).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        stubGeoServer.createContext("/geoserver/missing", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        stubGeoServer.createContext("/geoserver/cached", exchange -> {
            byte[] body = "tile".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.getResponseHeaders().add("Cache-Control", "max-age=86400");
            exchange.getResponseHeaders().add("geowebcache-cache-result", "HIT");
            exchange.getResponseHeaders().add("ETag", "\"abc123\"");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        stubGeoServer.start();

        GeoServerProxyConfig config = new GeoServerProxyConfig();
        config.setInternalUrl("http://localhost:" + stubGeoServer.getAddress().getPort() + "/geoserver");

        service = new RasterProxyService(config);
    }

    @AfterEach
    void tearDown() {
        stubGeoServer.stop(0);
    }

    @Test
    void forward_ForwardsAllowedPathWithQueryString_AndPropagatesResponse() {
        ResponseEntity<byte[]> response = service.forward("/ancientdata/wms", "service=WMS&request=GetMap");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
        assertNotNull(response.getBody());
        assertEquals("query=service=WMS&request=GetMap", new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    void forward_DoesNotDoubleEncodeAlreadyEncodedQueryParams() {
        // bbox commas arrive pre-encoded as %2C (e.g. from a browser-built WMS GetMap URL).
        // A naive uri(String) call would re-encode the literal "%" into "%25", corrupting it
        // into "%252C" — GeoServer would then see one coordinate instead of four.
        ResponseEntity<byte[]> response = service.forward("/ancientdata/wms", "bbox=1%2C2%2C3%2C4");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("query=bbox=1%2C2%2C3%2C4", new String(response.getBody(), StandardCharsets.UTF_8));
    }

    @Test
    void forward_BlocksRestAdminPaths() {
        ResponseEntity<byte[]> response = service.forward("/rest/workspaces", null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void forward_BlocksWebAdminPaths() {
        ResponseEntity<byte[]> response = service.forward("/web/", null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void forward_PropagatesUpstreamErrorStatus() {
        ResponseEntity<byte[]> response = service.forward("/missing", null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void forward_PropagatesUpstreamCachingAndGwcDiagnosticHeaders() {
        ResponseEntity<byte[]> response = service.forward("/cached", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("max-age=86400", response.getHeaders().getFirst("Cache-Control"));
        assertEquals("HIT", response.getHeaders().getFirst("geowebcache-cache-result"));
        assertEquals("\"abc123\"", response.getHeaders().getFirst("ETag"));
    }

    @Test
    void forward_DefaultsToPubliclyCacheableWhenUpstreamSetsNoCacheControl() {
        // The plain /ancientdata/wms stub context (unlike /cached above) never sets its
        // own Cache-Control - map tiles are public and requester-independent, so the
        // proxy should still make them cacheable rather than leaving Spring Security's
        // app-wide no-store default in place for this specific, safe-to-cache endpoint.
        ResponseEntity<byte[]> response = service.forward("/ancientdata/wms", "service=WMS");

        assertEquals("public, max-age=3600", response.getHeaders().getFirst("Cache-Control"));
    }
}
