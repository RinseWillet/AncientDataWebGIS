package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import org.locationtech.jts.io.WKTWriter;

public class SiteMapper {

    public static SiteDTO toDto(Site site) {
        SiteDTO dto = new SiteDTO();
        dto.setId(site.getId());
        dto.setPleiadesId(site.getPleiadesId());
        dto.setName(site.getName());
        dto.setGeom(new WKTWriter().write(site.getGeom()));
        dto.setProvince(site.getProvince());
        dto.setSiteType(site.getSiteType());
        dto.setStatus(site.getStatus());
        dto.setReferences(site.getReferences());
        dto.setDescription(site.getDescription());
        return dto;
    }
}