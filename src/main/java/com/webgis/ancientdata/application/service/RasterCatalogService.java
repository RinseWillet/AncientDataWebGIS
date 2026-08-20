package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerCategory;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterZoomDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Curated catalog of raster layers published in GeoServer (see ADR-012 /
 * the E3.1 runbook). GeoServer's own WMS capabilities can't supply a
 * layer's display name, attribution, or a sensible zoom range for a Leaflet
 * overlay, so this list is maintained by hand here rather than derived live
 * from GetCapabilities. Adding a newly published layer means adding an
 * entry here.
 */
@Service
public class RasterCatalogService {

    private static final String DE_MAN_1818_COLLECTION = "1818 De Man - Nijmegen";

    private static final String KLEVE_1740_COLLECTION = "1740 - Kleve";

    private static final List<RasterLayerDTO> CATALOG = List.of(
            new RasterLayerDTO(
                    "Sheet A1",
                    "ancientdata:De-Man-1818-A1",
                    new RasterBoundsDTO(51.782659965003994, 5.757907509752199, 51.87017277720497, 5.9033985468639525),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A2",
                    "ancientdata:De-Man-1818-A2",
                    new RasterBoundsDTO(51.79248099648939, 5.757982824283212, 51.825455193924086, 5.83070005750662),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A3",
                    "ancientdata:De-Man-1818-A3",
                    new RasterBoundsDTO(51.80404451531167, 5.830950124353257, 51.84002406292155, 5.892320690155102),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A4",
                    "ancientdata:De-Man-1818-A4",
                    new RasterBoundsDTO(51.81399554398528, 5.781638645840879, 51.84932509161219, 5.84386633466715),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A5",
                    "ancientdata:De-Man-1818-A5",
                    new RasterBoundsDTO(51.830096780009875, 5.843679743944152, 51.86142158497805, 5.90330558027289),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A6",
                    "ancientdata:De-Man-1818-A6",
                    new RasterBoundsDTO(51.84001106233645, 5.795165495731549, 51.87004849240705, 5.855503640022906),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 3",
                    "ancientdata:1740-Kleve-DINA1-03_r",
                    new RasterBoundsDTO(51.73343140813606, 6.104849008894311, 51.768296396569404, 6.172473312074854),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 4",
                    "ancientdata:1740-Kleve-DINA1-04_r",
                    new RasterBoundsDTO(51.714760818201405, 6.098408831298351, 51.75039849774439, 6.167572939469806),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 5",
                    "ancientdata:1740-Kleve-DINA1-05_r",
                    new RasterBoundsDTO(51.724959956235594, 6.059738633657765, 51.759432933524536, 6.126875610219495),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 6",
                    "ancientdata:1740-Kleve-DINA1-06_r",
                    new RasterBoundsDTO(51.74433174217834, 6.0749002252559245, 51.77728077588717, 6.142042710074628),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 7",
                    "ancientdata:1740-Kleve-DINA1-07_r",
                    new RasterBoundsDTO(51.74940470691819, 6.046352191393674, 51.79147680047071, 6.110540343319396),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 8",
                    "ancientdata:1740-Kleve-DINA1-08_r",
                    new RasterBoundsDTO(51.73437245423012, 6.0196045630039565, 51.77533383415638, 6.085694174077287),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 9",
                    "ancientdata:1740-Kleve-DINA1-09_r",
                    new RasterBoundsDTO(51.701978740840865, 6.073059609648879, 51.734918675183586, 6.139548633704987),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 10",
                    "ancientdata:1740-Kleve-DINA1-10_r",
                    new RasterBoundsDTO(51.71190925262841, 6.028074408837049, 51.74909286779242, 6.097061797809015),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 11",
                    "ancientdata:1740-Kleve-DINA1-11_r",
                    new RasterBoundsDTO(51.70378612585145, 6.0202019269466875, 51.738783901985535, 6.086039558873208),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 12",
                    "ancientdata:1740-Kleve-DINA1-12_r",
                    new RasterBoundsDTO(51.720365114334676, 5.984911069366643, 51.75418291977292, 6.054044974780257),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 13",
                    "ancientdata:1740-Kleve-DINA1-13_r",
                    new RasterBoundsDTO(51.74588100091163, 5.970521074183843, 51.77820643078969, 6.041818616786195),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 14",
                    "ancientdata:1740-Kleve-DINA1-14_r",
                    new RasterBoundsDTO(51.73326775071595, 5.957216780600897, 51.76088538697168, 6.018964850841942),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 15",
                    "ancientdata:1740-Kleve-DINA1-15_r",
                    new RasterBoundsDTO(51.732550582035415, 5.92953195601755, 51.760620065038815, 5.988022815568189),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet 16",
                    "ancientdata:1740-Kleve-DINA1-16_r",
                    new RasterBoundsDTO(51.72739911643023, 5.889941922033602, 51.762824002738, 5.955974491305206),
                    new RasterZoomDTO(12, 19),
                    KLEVE_1740_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    KLEVE_1740_COLLECTION,
                    false
            ),
            // DEM layers (E3-4): each is an independent published area, not sheets of one
            // atlas, so `collection` is null for all of them - no nested grouping in the
            // "Physical" LayerPanel section. Zoom ceilings are per-file, based on each COG's
            // measured `gdalinfo` Pixel Size (see the E3.1 runbook's "DEM-specific conversion"
            // section) - most of this batch measured ~1m native resolution (zoom 8-17), except
            // Venlo-Geldern which measured a genuine 0.5m and earns zoom 8-18. Some of these
            // areas geographically overlap each other (confirmed by comparing bounds) - left as
            // independently toggleable entries relying on the Physical group's existing
            // opacity/reorder controls, since all draw from the same underlying AHN3/DGM1
            // source data rather than conflicting datasets.
            //
            // Hillshade layers (E3-5): each elevation DEM above now has a multidirectional
            // hillshade sibling (own COG, own GeoServer layer, same extent/zoom/attribution),
            // listed immediately before it so it defaults to the higher z-index/top row in
            // the Physical group - the frontend blend-multiplies it over whatever's beneath,
            // so it needs to render above its elevation counterpart to have any visible effect.
            new RasterLayerDTO(
                    "DEM of Gelderland-NRW research area - Hillshade",
                    "ancientdata:research_area_Gelderland_NRW_hillshade_cog",
                    new RasterBoundsDTO(51.45783572791835, 5.419587299877188, 52.0690994943719, 6.865869726173805),
                    new RasterZoomDTO(8, 17),
                    "AHN3 (NL) & DGM1 (NRW, DE) LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    true
            ),
            new RasterLayerDTO(
                    "DEM of Gelderland-NRW research area",
                    "ancientdata:research_area_Gelderland_NRW_cog",
                    new RasterBoundsDTO(51.45783572791835, 5.419587299877188, 52.0690994943719, 6.865869726173805),
                    new RasterZoomDTO(8, 17),
                    "AHN3 (NL) & DGM1 (NRW, DE) LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    false
            ),
            new RasterLayerDTO(
                    "DEM of Swalmen area (AHN3 + DGM1) - Hillshade",
                    "ancientdata:merge_swalmen_hillshade_cog",
                    new RasterBoundsDTO(51.14425175970463, 5.965415083893099, 51.35358917449149, 6.409873937995263),
                    new RasterZoomDTO(8, 17),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    true
            ),
            new RasterLayerDTO(
                    "DEM of Swalmen area (AHN3 + DGM1)",
                    "ancientdata:merge_swalmen_cog",
                    new RasterBoundsDTO(51.14425175970463, 5.965415083893099, 51.35358917449149, 6.409873937995263),
                    new RasterZoomDTO(8, 17),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    false
            ),
            new RasterLayerDTO(
                    "DEM of Geldern - Venlo area (DGM1, AHN3) - Hillshade",
                    "ancientdata:merge_venlo_geldern_hillshade_cog",
                    new RasterBoundsDTO(51.3350043816511, 5.966035132096261, 51.54046275728694, 6.416339886112167),
                    new RasterZoomDTO(8, 18),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    true
            ),
            new RasterLayerDTO(
                    "DEM of Geldern - Venlo area (DGM1, AHN3)",
                    "ancientdata:merge_venlo_geldern_cog",
                    new RasterBoundsDTO(51.3350043816511, 5.966035132096261, 51.54046275728694, 6.416339886112167),
                    new RasterZoomDTO(8, 18),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    false
            ),
            new RasterLayerDTO(
                    "DEM of Mönchengladbach - Neuss area (DGM1) - Hillshade",
                    "ancientdata:dem_Mönchengladbach-Neuss-hillshade",
                    new RasterBoundsDTO(51.09919903622249, 6.423118334166201, 51.21238621797372, 6.685970391067374),
                    new RasterZoomDTO(8, 17),
                    "DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    true
            ),
            new RasterLayerDTO(
                    "DEM of Mönchengladbach - Neuss area (DGM1)",
                    "ancientdata:Mönchengladbach-Neuss-merge_cog",
                    new RasterBoundsDTO(51.09919903622249, 6.423118334166201, 51.21238621797372, 6.685970391067374),
                    new RasterZoomDTO(8, 17),
                    "DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    false
            ),
            new RasterLayerDTO(
                    "DEM of area west of Mönchengladbach (DGM1, AHN3) - Hillshade",
                    "ancientdata:Mönchengladbach_west-hillshade",
                    new RasterBoundsDTO(51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674),
                    new RasterZoomDTO(8, 17),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    true
            ),
            new RasterLayerDTO(
                    "DEM of area west of Mönchengladbach (DGM1, AHN3)",
                    "ancientdata:Mönchengladbach_west-merge_cog",
                    new RasterBoundsDTO(51.02210046622095, 6.192158255124815, 51.24292939309103, 6.433099522466674),
                    new RasterZoomDTO(8, 17),
                    "AHN3 & DGM1 LiDAR",
                    RasterLayerCategory.DEM,
                    null,
                    false
            )
    );

    public List<RasterLayerDTO> getCatalog() {
        return CATALOG;
    }
}
