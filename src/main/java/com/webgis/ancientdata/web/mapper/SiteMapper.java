package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
                site.getDescription(),
                site.getModernReferenceList().stream().map(ModernReferenceMapper::toDto).collect(Collectors.toList())
        );
    }

    public static Site toEntity(SiteDTO siteDTO) {
        Site site = new Site();
        site.setId(siteDTO.id());
        site.setPleiadesId(siteDTO.pleiadesId());
        site.setName(siteDTO.name());
        try {
            Geometry geometry = new WKTReader().read(siteDTO.geom());
            if (!(geometry instanceof Point)) {
                throw new IllegalArgumentException("Geometry must be a Point");
            }
            site.setGeom((Point) geometry);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT geometry: " + siteDTO.geom(), e);
        }
        site.setProvince(siteDTO.province());
        site.setSiteType(siteDTO.siteType());
        site.setStatus(siteDTO.status());
        site.setReferences(siteDTO.references());
        site.setDescription(siteDTO.description());
        site.setModernReferenceList(siteDTO.modernReferenceList() != null
                ? siteDTO.modernReferenceList().stream()
                .map(ModernReferenceMapper::toEntity)
                .collect(Collectors.toList())
                : new ArrayList<>()
        );
        return site;
    }
}