package com.webgis.ancientdata.rastertests;

import com.webgis.ancientdata.application.service.RasterCatalogService;
import com.webgis.ancientdata.domain.dto.RasterLayerCategory;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RasterCatalogServiceTests {

    private final RasterCatalogService service = new RasterCatalogService();

    @Test
    void getCatalog_ReturnsBothPublishedDeManSheets() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog)
                .extracting(RasterLayerDTO::source)
                .containsExactlyInAnyOrder("ancientdata:1818-de-man-a2", "ancientdata:1818-de-man-a3");
    }

    @Test
    void getCatalog_EntriesHaveCompleteMetadata() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog).isNotEmpty().allSatisfy(entry -> {
            assertThat(entry.name()).isNotBlank();
            assertThat(entry.source()).isNotBlank();
            assertThat(entry.attribution()).isNotBlank();
            assertThat(entry.bounds()).isNotNull();
            assertThat(entry.bounds().south()).isLessThan(entry.bounds().north());
            assertThat(entry.bounds().west()).isLessThan(entry.bounds().east());
            assertThat(entry.zoom()).isNotNull();
            assertThat(entry.zoom().min()).isLessThanOrEqualTo(entry.zoom().max());
            assertThat(entry.category()).isNotNull();
        });
    }

    @Test
    void getCatalog_DeManSheetsAreCategorizedAsHistoricalMaps() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog)
                .extracting(RasterLayerDTO::category)
                .containsOnly(RasterLayerCategory.HISTORICAL_MAP);
    }
}
