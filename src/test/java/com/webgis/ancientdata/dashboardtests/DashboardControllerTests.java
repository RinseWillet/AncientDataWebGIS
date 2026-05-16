package com.webgis.ancientdata.dashboardtests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;



    @Test
    void testGetDashboardSummary_Success() throws Exception {
        // Act & Assert: endpoint responds with valid schema
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").exists())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.sites").exists())
                .andExpect(jsonPath("$.sites.total").exists())
                .andExpect(jsonPath("$.sites.byType").isArray())
                .andExpect(jsonPath("$.roads").exists())
                .andExpect(jsonPath("$.roads.total").exists())
                .andExpect(jsonPath("$.roads.byType").isArray())
                .andExpect(jsonPath("$.roads.lengthKmTotal").exists())
                .andExpect(jsonPath("$.roads.lengthKmByType").isArray());
    }

    @Test
    void testGetDashboardSummary_PublicReadAccess() throws Exception {

        // Act & Assert: verify endpoint is publicly accessible (no auth required)
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDashboardSummary_ContentType() throws Exception {
        // Act & Assert: verify response is valid JSON
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}

