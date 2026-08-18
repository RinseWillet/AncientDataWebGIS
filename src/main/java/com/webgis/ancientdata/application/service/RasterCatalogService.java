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

    private static final List<RasterLayerDTO> CATALOG = List.of(
            new RasterLayerDTO(
                    "Sheet A2",
                    "ancientdata:1818-de-man-a2",
                    new RasterBoundsDTO(51.78955278371998, 5.753779989397209, 51.82809566498777, 5.8357101348181875),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
                    false
            ),
            new RasterLayerDTO(
                    "Sheet A3",
                    "ancientdata:1818-de-man-a3",
                    new RasterBoundsDTO(51.80092236750916, 5.825842130337765, 51.84283475071846, 5.896962019563531),
                    new RasterZoomDTO(12, 19),
                    DE_MAN_1818_COLLECTION,
                    RasterLayerCategory.HISTORICAL_MAP,
                    DE_MAN_1818_COLLECTION,
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
