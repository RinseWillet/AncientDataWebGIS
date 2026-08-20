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
    void getCatalog_ContainsBothPublishedDeManSheets() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog)
                .extracting(RasterLayerDTO::source)
                .contains("ancientdata:De-Man-1818-A2", "ancientdata:De-Man-1818-A3");
    }

    @Test
    void getCatalog_ContainsAllFivePublishedDemLayers() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog)
                .extracting(RasterLayerDTO::source)
                .contains(
                        "ancientdata:research_area_Gelderland_NRW_cog",
                        "ancientdata:merge_swalmen_cog",
                        "ancientdata:merge_venlo_geldern_cog",
                        "ancientdata:Mönchengladbach-Neuss-merge_cog",
                        "ancientdata:Mönchengladbach_west-merge_cog"
                );
    }

    @Test
    void getCatalog_ContainsAllFivePublishedHillshadeLayers() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        assertThat(catalog)
                .extracting(RasterLayerDTO::source)
                .contains(
                        "ancientdata:research_area_Gelderland_NRW_hillshade_cog",
                        "ancientdata:merge_swalmen_hillshade_cog",
                        "ancientdata:merge_venlo_geldern_hillshade_cog",
                        "ancientdata:dem_Mönchengladbach-Neuss-hillshade",
                        "ancientdata:Mönchengladbach_west-hillshade"
                );
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
    void getCatalog_DeManSheetsAreCategorizedAsHistoricalMapsAndShareACollection() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        List<RasterLayerDTO> deManSheets = catalog.stream()
                .filter(entry -> entry.source().startsWith("ancientdata:De-Man-1818"))
                .toList();

        assertThat(deManSheets).hasSize(6);
        assertThat(deManSheets).extracting(RasterLayerDTO::category).containsOnly(RasterLayerCategory.HISTORICAL_MAP);
        assertThat(deManSheets).extracting(RasterLayerDTO::collection).containsOnly("1818 De Man - Nijmegen");
        assertThat(deManSheets).extracting(RasterLayerDTO::hillshade).containsOnly(false);
    }

    @Test
    void getCatalog_DemLayersAreCategorizedAsDemWithNoCollection() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        List<RasterLayerDTO> demLayers = catalog.stream()
                .filter(entry -> entry.category() == RasterLayerCategory.DEM)
                .toList();

        // 5 elevation + 5 hillshade siblings.
        assertThat(demLayers).hasSize(10);
        assertThat(demLayers).extracting(RasterLayerDTO::collection).containsOnlyNulls();
    }

    @Test
    void getCatalog_HillshadeLayersAreFlaggedAndEachHasAMatchingElevationSibling() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        List<RasterLayerDTO> hillshadeLayers = catalog.stream()
                .filter(RasterLayerDTO::hillshade)
                .toList();
        List<RasterLayerDTO> elevationLayers = catalog.stream()
                .filter(entry -> entry.category() == RasterLayerCategory.DEM && !entry.hillshade())
                .toList();

        assertThat(hillshadeLayers).hasSize(5);
        assertThat(elevationLayers).hasSize(5);
        // Every hillshade shares its exact extent with exactly one non-hillshade DEM entry -
        // proves each was published for a real elevation counterpart, not orphaned.
        assertThat(hillshadeLayers).allSatisfy(hillshade ->
                assertThat(elevationLayers)
                        .anySatisfy(elevation -> assertThat(elevation.bounds()).isEqualTo(hillshade.bounds())));
    }

    @Test
    void getCatalog_EachHillshadeIsListedImmediatelyBeforeItsElevationCounterpart() {
        // Physical layer z-order/row-order defaults to catalog list order (E3-3) - a
        // hillshade needs to render *above* its elevation sibling for the frontend's
        // multiply blend to have any visible effect, so it must come first in the list.
        List<RasterLayerDTO> catalog = service.getCatalog();

        for (int i = 0; i < catalog.size() - 1; i++) {
            RasterLayerDTO entry = catalog.get(i);
            if (entry.hillshade()) {
                RasterLayerDTO next = catalog.get(i + 1);
                assertThat(next.category()).isEqualTo(RasterLayerCategory.DEM);
                assertThat(next.hillshade()).isFalse();
                assertThat(next.bounds()).isEqualTo(entry.bounds());
            }
        }
    }

    @Test
    void getCatalog_DemZoomCeilingMatchesEachLayersMeasuredNativeResolution() {
        List<RasterLayerDTO> catalog = service.getCatalog();

        // Venlo-Geldern (both the elevation layer and its hillshade sibling, which shares
        // its native resolution) measured a genuine 0.5m native pixel size (gdalinfo) and
        // earns zoom 18; the rest of this batch measured ~1m and cap at 17 - see the E3.1
        // runbook's "DEM-specific conversion" section for the derivation.
        List<RasterLayerDTO> venloGeldern = catalog.stream()
                .filter(entry -> entry.source().contains("venlo_geldern"))
                .toList();
        assertThat(venloGeldern).hasSize(2);
        assertThat(venloGeldern).extracting(entry -> entry.zoom().max()).containsOnly(18);

        List<RasterLayerDTO> oneMeterDemLayers = catalog.stream()
                .filter(entry -> entry.category() == RasterLayerCategory.DEM)
                .filter(entry -> !entry.source().contains("venlo_geldern"))
                .toList();
        assertThat(oneMeterDemLayers).hasSize(8);
        assertThat(oneMeterDemLayers).extracting(entry -> entry.zoom().max()).containsOnly(17);
    }
}
