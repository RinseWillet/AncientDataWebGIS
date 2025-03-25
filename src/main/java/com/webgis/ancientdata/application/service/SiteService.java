package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONObject;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final Logger logger = LoggerFactory.getLogger(SiteService.class);
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Iterable<Site> findAll() {
        logger.info("Finding all sites");
        return siteRepository.findAll();
    }

    public JSONObject findAllGeoJson() {

        //create GeoJsonBuilderService object to convert incoming Iterable to GeoJson
        GeoJsonConverter geoJsonConverter = new GeoJsonConverter();

        return geoJsonConverter.convertSites(findAll());
    }

    public Optional<Site> findById(long id) {
        logger.info("find site id : {}", id);
        return siteRepository.findById(id);
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        try {
            GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
            return geoJsonConverter.convertSite(findById(id)).toString();
        } catch (Exception e) {
            logger.warn("site " + id + " not found ");
            return "not found";
        }
    }

    public Site save(SiteDTO siteDTO) {
        try {
            Site site = new Site();
            site.setPleiadesId(siteDTO.getPleiadesId());
            site.setName(siteDTO.getName());
            site.setGeom(convertWktToPoint(siteDTO.getGeom()));
            site.setProvince(siteDTO.getProvince());
            site.setSiteType(siteDTO.getSiteType());
            site.setStatus(siteDTO.getStatus());
            site.setReferences(siteDTO.getReferences());
            site.setDescription(siteDTO.getDescription());

            logger.info("saving site (DTO): {}", site);
            return siteRepository.save(site);
        } catch (NullPointerException e) {
            logger.error("Missing required field in siteDTO: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields", e);
        } catch (ParseException e) {
            logger.error("Invalid WKT: {}", siteDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid geometry", e);
        } catch (Exception e) {
            logger.warn("saving site (DTO) failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate value", e);
        }
    }

    public Site update(Long id, SiteDTO siteDTO) {
        try {
            Optional<Site> siteOptional = findById(id);
            if (siteOptional.isPresent()) {
                Site site = siteOptional.get();
                site.setPleiadesId(siteDTO.getPleiadesId());
                site.setName(siteDTO.getName());
                site.setGeom(convertWktToPoint(siteDTO.getGeom()));
                site.setProvince(siteDTO.getProvince());
                site.setSiteType(siteDTO.getSiteType());
                site.setStatus(siteDTO.getStatus());
                site.setReferences(siteDTO.getReferences());
                site.setDescription(siteDTO.getDescription());

                logger.info("updating site (DTO): {}", site);
                return siteRepository.save(site);
            } else {
                logger.warn("site with ID {} not found for update", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found");
            }
        } catch (ParseException e) {
            logger.error("Invalid WKT geometry format: {}", siteDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid WKT format", e);
        } catch (Exception e) {
            logger.warn("updating site failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error updating site", e);
        }
    }

    public void delete(Long id) {
        if (!siteRepository.existsById(id)) {
            logger.warn("Site with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found");
        }
        siteRepository.deleteById(id);
        logger.info("Deleted site with ID {}", id);
    }

    public Site addModernReferenceToSite(long siteId, ModernReferenceDTO modernReferenceDTO) {
        try {
            Optional<Site> siteOptional = findById(siteId);
            if (siteOptional.isPresent()) {
                Site site = siteOptional.get();

                ModernReference modernReference = new ModernReference(
                        modernReferenceDTO.getShortRef(),
                        modernReferenceDTO.getFullRef(),
                        modernReferenceDTO.getUrl());
                site.addModernReference(modernReference);
                return siteRepository.save(site);
            } else {
                logger.warn("site was not found");
                return null;
            }
        } catch (Exception e) {
            logger.warn("adding Modern Reference to site failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    //Parsing into DTO to prevent infinite regressing due to bidirectional many-to-many relationship
    //roads and modernrefs
    public List<ModernReferenceDTO> findModernReferencesBySiteId(long siteId) {
        try {
            Optional<Site> siteOptional = findById(siteId);
            if (siteOptional.isPresent()) {
                Site site = siteOptional.get();

                List<ModernReference> modernReferenceList = site.getModernReferenceList();

                return getModernReferenceDTOList(modernReferenceList);
            } else {
                logger.warn("site was not found");
                return null;
            }
        } catch (Exception e) {
            logger.warn("finding Modern References for site with id " + siteId + "failed because {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    private List<ModernReferenceDTO> getModernReferenceDTOList(List<ModernReference> modernReferenceList) {

        List<ModernReferenceDTO> modernReferenceDTOList = new ArrayList<>();

        for (ModernReference modernReference : modernReferenceList) {
            ModernReferenceDTO modernReferenceDTO = new ModernReferenceDTO(
                    modernReference.getId(),
                    modernReference.getShortRef(),
                    modernReference.getFullRef(),
                    modernReference.getUrl()
            );

            modernReferenceDTOList.add(modernReferenceDTO);
        }
        return modernReferenceDTOList;
    }

    private Point convertWktToPoint(String wkt) throws ParseException {
        WKTReader reader = new WKTReader(geometryFactory);
        return (Point) reader.read(wkt);
    }
}
