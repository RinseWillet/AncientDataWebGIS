package com.webgis.ancientdata.rastertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.application.service.RasterCatalogService;
import com.webgis.ancientdata.application.service.RasterProxyService;
import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerCreateRequest;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerUpdateRequest;
import com.webgis.ancientdata.domain.dto.RasterZoomDTO;
import com.webgis.ancientdata.domain.model.RasterLayerCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RasterCatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RasterCatalogService rasterCatalogService;

    // Mocked (and asserted un-touched) to prove /api/raster/catalog resolves to this
    // controller rather than falling through to RasterProxyController's /api/raster/**
    // catch-all, which would otherwise try to forward it to GeoServer.
    @MockitoBean
    private RasterProxyService rasterProxyService;

    private static final RasterLayerDTO SAMPLE_DTO = new RasterLayerDTO(
            "Sheet A2",
            "ancientdata:De-Man-1818-A2",
            new RasterBoundsDTO(51.79, 5.75, 51.82, 5.83),
            new RasterZoomDTO(12, 19),
            "1818 De Man - Nijmegen",
            RasterLayerCategory.HISTORICAL_MAP,
            "1818 De Man - Nijmegen",
            false
    );

    private RasterLayerCreateRequest validCreateRequest() {
        return new RasterLayerCreateRequest(
                "New Sheet", "ancientdata:new-sheet",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(12, 19),
                "Attribution", RasterLayerCategory.HISTORICAL_MAP, null, false);
    }

    // --- Public GET /catalog ---

    @Test
    void getCatalog_ReturnsPublishedLayers_WithoutHittingTheProxy() throws Exception {
        when(rasterCatalogService.getCatalog()).thenReturn(List.of(SAMPLE_DTO));

        mockMvc.perform(get("/api/raster/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Sheet A2"))
                .andExpect(jsonPath("$[0].source").value("ancientdata:De-Man-1818-A2"))
                .andExpect(jsonPath("$[0].bounds.south").isNumber())
                .andExpect(jsonPath("$[0].bounds.west").isNumber())
                .andExpect(jsonPath("$[0].bounds.north").isNumber())
                .andExpect(jsonPath("$[0].bounds.east").isNumber())
                .andExpect(jsonPath("$[0].zoom.min").isNumber())
                .andExpect(jsonPath("$[0].zoom.max").isNumber())
                .andExpect(jsonPath("$[0].category").value("HISTORICAL_MAP"))
                .andExpect(jsonPath("$[0].collection").value("1818 De Man - Nijmegen"))
                .andExpect(jsonPath("$[0].id").doesNotExist());

        verifyNoInteractions(rasterProxyService);
    }

    @Test
    void getCatalog_IsReachableWithoutAuthentication() throws Exception {
        when(rasterCatalogService.getCatalog()).thenReturn(List.of(SAMPLE_DTO));

        mockMvc.perform(get("/api/raster/catalog"))
                .andExpect(status().isOk());
    }

    // --- Admin GET /catalog/admin ---

    @Test
    void adminList_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/raster/catalog/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminList_withUserRole_returns403() throws Exception {
        mockMvc.perform(get("/api/raster/catalog/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminList_withAdminRole_returns200() throws Exception {
        when(rasterCatalogService.getCatalog()).thenReturn(List.of(SAMPLE_DTO));

        mockMvc.perform(get("/api/raster/catalog/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // --- Admin POST /catalog ---

    @Test
    void create_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/raster/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_withUserRole_returns403() throws Exception {
        mockMvc.perform(post("/api/raster/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_withAdminRole_returns200() throws Exception {
        when(rasterCatalogService.create(any(RasterLayerCreateRequest.class))).thenReturn(SAMPLE_DTO);

        mockMvc.perform(post("/api/raster/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("ancientdata:De-Man-1818-A2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankName_returns400() throws Exception {
        RasterLayerCreateRequest invalid = new RasterLayerCreateRequest(
                "", "ancientdata:blank-name",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(12, 19),
                "Attribution", RasterLayerCategory.HISTORICAL_MAP, null, false);

        mockMvc.perform(post("/api/raster/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rasterCatalogService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_duplicateSource_returns409() throws Exception {
        when(rasterCatalogService.create(any(RasterLayerCreateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "A raster layer with this source already exists"));

        mockMvc.perform(post("/api/raster/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict());
    }

    // --- Admin PATCH /catalog/{source} ---

    @Test
    void update_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/raster/catalog/ancientdata:De-Man-1818-A2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void update_withUserRole_returns403() throws Exception {
        mockMvc.perform(patch("/api/raster/catalog/ancientdata:De-Man-1818-A2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_withAdminRole_returns200() throws Exception {
        when(rasterCatalogService.update(eq("ancientdata:De-Man-1818-A2"), any(RasterLayerUpdateRequest.class)))
                .thenReturn(SAMPLE_DTO);

        mockMvc.perform(patch("/api/raster/catalog/ancientdata:De-Man-1818-A2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("ancientdata:De-Man-1818-A2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_unknownSource_returns404() throws Exception {
        when(rasterCatalogService.update(eq("ancientdata:missing"), any(RasterLayerUpdateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Raster layer not found"));

        mockMvc.perform(patch("/api/raster/catalog/ancientdata:missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Updated name\"}"))
                .andExpect(status().isNotFound());
    }

    // --- Admin DELETE /catalog/{source} ---

    @Test
    void delete_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/raster/catalog/ancientdata:De-Man-1818-A2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_withUserRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/raster/catalog/ancientdata:De-Man-1818-A2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_withAdminRole_returns204() throws Exception {
        doNothing().when(rasterCatalogService).delete("ancientdata:De-Man-1818-A2");

        mockMvc.perform(delete("/api/raster/catalog/ancientdata:De-Man-1818-A2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_unknownSource_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Raster layer not found"))
                .when(rasterCatalogService).delete("ancientdata:missing");

        mockMvc.perform(delete("/api/raster/catalog/ancientdata:missing"))
                .andExpect(status().isNotFound());
    }
}
