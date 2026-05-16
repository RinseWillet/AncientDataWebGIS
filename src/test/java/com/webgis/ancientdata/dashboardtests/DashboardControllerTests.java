package com.webgis.ancientdata.dashboardtests;

import com.webgis.ancientdata.application.service.DashboardService;
import com.webgis.ancientdata.domain.dto.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTests {

    @TestConfiguration
    static class MockServiceConfiguration {
        @Bean
        @Primary
        public DashboardService dashboardService() {
            return Mockito.mock(DashboardService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardService dashboardService;


    @Test
    void testGetDashboardSummary_Success() throws Exception {
        // Arrange
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                1,
                Instant.now(),
                new MetricSummaryDTO(
                        10L,
                        List.of(
                                new TypeCountDTO("city", 5L),
                                new TypeCountDTO("fort", 5L)
                        )
                ),
                new RoadMetricDTO(
                        5L,
                        List.of(
                                new TypeCountDTO("roman road", 3L),
                                new TypeCountDTO("ancient path", 2L)
                        ),
                        1234.56,
                        List.of(
                                new TypeLengthDTO("roman road", 800.0),
                                new TypeLengthDTO("ancient path", 434.56)
                        )
                )
        );

        when(dashboardService.getSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(1))
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.sites.total").value(10))
                .andExpect(jsonPath("$.sites.byType", hasSize(2)))
                .andExpect(jsonPath("$.roads.total").value(5))
                .andExpect(jsonPath("$.roads.lengthKmTotal").value(1234.56))
                .andExpect(jsonPath("$.roads.byType", hasSize(2)))
                .andExpect(jsonPath("$.roads.lengthKmByType", hasSize(2)))
                .andExpect(jsonPath("$.roads.lengthKmByType[0].type").value("roman road"))
                .andExpect(jsonPath("$.roads.lengthKmByType[0].lengthKm").value(800.0));
    }

    @Test
    void testGetDashboardSummary_EmptyData() throws Exception {
        // Arrange
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                1,
                Instant.now(),
                new MetricSummaryDTO(0L, List.of()),
                new RoadMetricDTO(0L, List.of(), 0.0, List.of())
        );

        when(dashboardService.getSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sites.total").value(0))
                .andExpect(jsonPath("$.roads.total").value(0))
                .andExpect(jsonPath("$.roads.lengthKmTotal").value(0.0))
                .andExpect(jsonPath("$.sites.byType", hasSize(0)))
                .andExpect(jsonPath("$.roads.byType", hasSize(0)))
                .andExpect(jsonPath("$.roads.lengthKmByType", hasSize(0)));
    }

    @Test
    void testGetDashboardSummary_PublicReadAccess() throws Exception {
        // Arrange
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                1,
                Instant.now(),
                new MetricSummaryDTO(5L, List.of()),
                new RoadMetricDTO(3L, List.of(), 100.0, List.of())
        );

        when(dashboardService.getSummary()).thenReturn(summary);

        // Act & Assert: verify endpoint is publicly accessible (no auth required)
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDashboardSummary_ContentType() throws Exception {
        // Arrange
        DashboardSummaryDTO summary = new DashboardSummaryDTO(
                1,
                Instant.now(),
                new MetricSummaryDTO(0L, List.of()),
                new RoadMetricDTO(0L, List.of(), 0.0, List.of())
        );

        when(dashboardService.getSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}

