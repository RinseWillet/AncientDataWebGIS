package com.webgis.ancientdata.dashboardtests;

import com.webgis.ancientdata.application.service.DashboardService;
import com.webgis.ancientdata.domain.dto.DashboardSummaryDTO;
import com.webgis.ancientdata.domain.repository.RoadRepository;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private SiteRepository siteRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(roadRepository, siteRepository);
    }

    @Test
    void testGetSummary_WithValidData() {
        // Arrange: road count by type
        when(roadRepository.count()).thenReturn(5L);
        when(roadRepository.countByTypeRaw()).thenReturn(List.of(
                new Object[]{"Roman Road", 3L},
                new Object[]{"Ancient Path", 2L}
        ));
        when(roadRepository.getTotalLengthKm()).thenReturn(1234.56);
        when(roadRepository.getLengthKmByTypeRaw()).thenReturn(List.of(
                new Object[]{"Roman Road", 800.0},
                new Object[]{"Ancient Path", 434.56}
        ));

        // Site count by type
        when(siteRepository.count()).thenReturn(10L);
        when(siteRepository.countByTypeRaw()).thenReturn(List.of(
                new Object[]{"City", 5L},
                new Object[]{"Fort", 5L}
        ));

        // Act
        DashboardSummaryDTO result = dashboardService.getSummary();

        // Assert
        assertEquals(1, result.schemaVersion());
        assertNotNull(result.generatedAt());

        // Check roads
        assertEquals(5L, result.roads().total());
        assertEquals(1234.56, result.roads().lengthKmTotal(), 0.01);
        assertEquals(2, result.roads().byType().size());
        assertEquals("roman road", result.roads().byType().getFirst().type());
        assertEquals(3L, result.roads().byType().getFirst().count());
        assertEquals(2, result.roads().lengthKmByType().size());
        assertEquals("roman road", result.roads().lengthKmByType().getFirst().type());
        assertEquals(800.0, result.roads().lengthKmByType().getFirst().lengthKm(), 0.01);

        // Check sites
        assertEquals(10L, result.sites().total());
        assertEquals(2, result.sites().byType().size());
    }

    @Test
    void testGetSummary_WithNullTypes() {
        // Arrange: null types should be normalized to "unknown"
        when(roadRepository.count()).thenReturn(2L);
        when(roadRepository.countByTypeRaw()).thenReturn(List.of(
                new Object[]{null, 1L},
                new Object[]{"  ", 1L}  // blank string
        ));
        when(roadRepository.getTotalLengthKm()).thenReturn(100.0);
        when(roadRepository.getLengthKmByTypeRaw()).thenReturn(List.of(
                new Object[]{null, 50.0},
                new Object[]{"  ", 50.0}
        ));

        when(siteRepository.count()).thenReturn(0L);
        when(siteRepository.countByTypeRaw()).thenReturn(List.of());

        // Act
        DashboardSummaryDTO result = dashboardService.getSummary();

        // Assert
        assertEquals(1, result.roads().byType().size());
        assertEquals("unknown", result.roads().byType().getFirst().type());
        assertEquals(2L, result.roads().byType().getFirst().count());
        assertEquals(1, result.roads().lengthKmByType().size());
        assertEquals("unknown", result.roads().lengthKmByType().getFirst().type());
        assertEquals(100.0, result.roads().lengthKmByType().getFirst().lengthKm(), 0.01);
    }

    @Test
    void testGetSummary_WithZeroData() {
        // Arrange: empty data
        when(roadRepository.count()).thenReturn(0L);
        when(roadRepository.countByTypeRaw()).thenReturn(List.of());
        when(roadRepository.getTotalLengthKm()).thenReturn(0.0);
        when(roadRepository.getLengthKmByTypeRaw()).thenReturn(List.of());

        when(siteRepository.count()).thenReturn(0L);
        when(siteRepository.countByTypeRaw()).thenReturn(List.of());

        // Act
        DashboardSummaryDTO result = dashboardService.getSummary();

        // Assert
        assertEquals(0L, result.roads().total());
        assertEquals(0.0, result.roads().lengthKmTotal(), 0.01);
        assertTrue(result.roads().byType().isEmpty());
        assertTrue(result.roads().lengthKmByType().isEmpty());
        assertEquals(0L, result.sites().total());
        assertTrue(result.sites().byType().isEmpty());
    }

    @Test
    void testGetSummary_LengthSortedDescending() {
        // Arrange: multiple types sorted by length descending
        when(roadRepository.count()).thenReturn(3L);
        when(roadRepository.countByTypeRaw()).thenReturn(List.of(
                new Object[]{"Type A", 1L},
                new Object[]{"Type B", 1L},
                new Object[]{"Type C", 1L}
        ));
        when(roadRepository.getTotalLengthKm()).thenReturn(1000.0);
        when(roadRepository.getLengthKmByTypeRaw()).thenReturn(List.of(
                new Object[]{"Type A", 300.0},
                new Object[]{"Type B", 500.0},
                new Object[]{"Type C", 200.0}
        ));

        when(siteRepository.count()).thenReturn(0L);
        when(siteRepository.countByTypeRaw()).thenReturn(List.of());

        // Act
        DashboardSummaryDTO result = dashboardService.getSummary();

        // Assert: lengths should be sorted descending
        assertEquals("type b", result.roads().lengthKmByType().getFirst().type());
        assertEquals(500.0, result.roads().lengthKmByType().getFirst().lengthKm(), 0.01);
        assertEquals("type a", result.roads().lengthKmByType().get(1).type());
        assertEquals(300.0, result.roads().lengthKmByType().get(1).lengthKm(), 0.01);
        assertEquals("type c", result.roads().lengthKmByType().get(2).type());
        assertEquals(200.0, result.roads().lengthKmByType().get(2).lengthKm(), 0.01);
    }
}

