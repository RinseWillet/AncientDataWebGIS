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
            byte[] body = ("query=" + exchange.getRequestURI().getQuery()).getBytes(StandardCharsets.UTF_8);
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
}
