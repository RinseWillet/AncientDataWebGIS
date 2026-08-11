package com.webgis.ancientdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for proxying raster tile/capabilities requests to the
 * internal GeoServer container. Properties: geoserver.internal-url
 */
@Component
@ConfigurationProperties(prefix = "geoserver")
@Getter
@Setter
public class GeoServerProxyConfig {

    /**
     * Base URL of GeoServer as reached from inside the ancientdata container
     * (Docker service DNS name, not the LAN-published admin port).
     */
    private String internalUrl;
}
