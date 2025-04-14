package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import org.locationtech.jts.io.WKTWriter;

public class SiteMapper {
    public static SiteDTO toDto(Site site) {
        return new SiteDTO(
                site.getId(),
                site.getPleiadesId(),
                site.getName(),
                new WKTWriter().write(site.getGeom()),
                site.getProvince(),
                site.getSiteType(),
                site.getStatus(),
                site.getReferences(),
                site.getDescription()
        );
    }
}