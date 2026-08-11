package com.webgis.ancientdata.rastertests;

import com.webgis.ancientdata.application.service.RasterProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RasterProxyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RasterProxyService rasterProxyService;

    @Test
    void proxy_ExtractsSubPathAndQueryString_AndReturnsServiceResponse() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        when(rasterProxyService.forward("/ancientdata/wms", "service=WMS&request=GetMap"))
                .thenReturn(new ResponseEntity<>("tile".getBytes(), headers, HttpStatus.OK));

        mockMvc.perform(get("/api/raster/ancientdata/wms?service=WMS&request=GetMap"))
                .andExpect(status().isOk());

        verify(rasterProxyService).forward("/ancientdata/wms", "service=WMS&request=GetMap");
    }

    @Test
    void proxy_IsReachableWithoutAuthentication() throws Exception {
        when(rasterProxyService.forward(eq("/ancientdata/wms"), isNull()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(get("/api/raster/ancientdata/wms"))
                .andExpect(status().isOk());
    }
}
