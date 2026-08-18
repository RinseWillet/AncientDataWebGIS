package com.webgis.ancientdata.rastertests;

import com.webgis.ancientdata.application.service.RasterProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RasterCatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // Mocked (and asserted un-touched) to prove /api/raster/catalog resolves to this
    // controller rather than falling through to RasterProxyController's /api/raster/**
    // catch-all, which would otherwise try to forward it to GeoServer.
    @MockitoBean
    private RasterProxyService rasterProxyService;

    @Test
    void getCatalog_ReturnsPublishedLayers_WithoutHittingTheProxy() throws Exception {
        mockMvc.perform(get("/api/raster/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].source").isNotEmpty())
                .andExpect(jsonPath("$[0].attribution").isNotEmpty())
                .andExpect(jsonPath("$[0].bounds.south").isNumber())
                .andExpect(jsonPath("$[0].bounds.west").isNumber())
                .andExpect(jsonPath("$[0].bounds.north").isNumber())
                .andExpect(jsonPath("$[0].bounds.east").isNumber())
                .andExpect(jsonPath("$[0].zoom.min").isNumber())
                .andExpect(jsonPath("$[0].zoom.max").isNumber())
                .andExpect(jsonPath("$[0].category").value("HISTORICAL_MAP"))
                .andExpect(jsonPath("$[0].collection").value("1818 De Man - Nijmegen"));

        verifyNoInteractions(rasterProxyService);
    }

    @Test
    void getCatalog_IsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/raster/catalog"))
                .andExpect(status().isOk());
    }
}
