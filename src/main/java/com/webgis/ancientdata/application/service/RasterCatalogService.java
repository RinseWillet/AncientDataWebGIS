package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
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

    private static final List<RasterLayerDTO> CATALOG = List.of(
            new RasterLayerDTO(
                    "1818 De Man - Nijmegen, Sheet A2",
                    "ancientdata:1818-de-man-a2",
                    new RasterBoundsDTO(51.78955278371998, 5.753779989397209, 51.82809566498777, 5.8357101348181875),
                    new RasterZoomDTO(12, 19),
                    "1818 De Man - Nijmegen"
            ),
            new RasterLayerDTO(
                    "1818 De Man - Nijmegen, Sheet A3",
                    "ancientdata:1818-de-man-a3",
                    new RasterBoundsDTO(51.80092236750916, 5.825842130337765, 51.84283475071846, 5.896962019563531),
                    new RasterZoomDTO(12, 19),
                    "1818 De Man - Nijmegen"
            )
    );

    public List<RasterLayerDTO> getCatalog() {
        return CATALOG;
    }
}
