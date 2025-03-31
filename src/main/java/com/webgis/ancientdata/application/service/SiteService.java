package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
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
import java.util.Optional;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final ModernReferenceRepository modernReferenceRepository;
    private final Logger logger = LoggerFactory.getLogger(SiteService.class);
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public SiteService(SiteRepository siteRepository, ModernReferenceRepository modernReferenceRepository) {
        this.siteRepository = siteRepository;
        this.modernReferenceRepository = modernReferenceRepository;
    }

    public Iterable<Site> findAll() {
        logger.info("Finding all sites");
        return siteRepository.findAll();
    }

    public JSONObject findAllGeoJson() {
        return new GeoJsonConverter().convertSites(findAll());
    }

    public Optional<Site> findById(long id) {
        logger.info("Finding site with ID {}", id);
        return siteRepository.findById(id)
                .or(() -> {
                    logger.warn("Site with ID {} not found", id);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
                });
    }

    public String findByIdGeoJson(long id) {
        return new GeoJsonConverter().convertSite(findById(id)).toString();
    }

    public Site save(SiteDTO siteDTO) {
        validateSiteDTO(siteDTO);
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

            logger.info("Saving site: {}", site);
            return siteRepository.save(site);
        } catch (ParseException e) {
            logger.error("Invalid WKT geometry format: {}", siteDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_WKT_FORMAT, e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Saving site failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_SITE, e);
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

                logger.info("Updating site: {}", site);
                return siteRepository.save(site);
            } else {
                logger.warn("Site with ID {} not found for update", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
            }
        } catch (ParseException e) {
            logger.error("Invalid WKT geometry format: {}", siteDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_WKT_FORMAT, e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Updating site failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_UPDATE_SITE, e);
        }
    }

    public void delete(Long id) {
        if (!siteRepository.existsById(id)) {
            logger.warn("Site with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
        }
        siteRepository.deleteById(id);
        logger.info("Deleted site with ID {}", id);
    }

    public Site addModernReferenceToSite(long siteId, ModernReferenceDTO dto) {
        return siteRepository.findById(siteId).map(site -> {
            ModernReference modernReference;

            if (dto.getId() != null) {
                modernReference = modernReferenceRepository.findById(dto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));
            } else {
                modernReference = new ModernReference(dto.getShortRef(), dto.getFullRef(), dto.getUrl());
            }

            site.addModernReference(modernReference);
            logger.info("Added modern reference to site ID {}", siteId);
            return siteRepository.save(site);

        }).orElseThrow(() -> {
            logger.warn("Site with ID {} not found to add a modern reference to", siteId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
        });
    }

    public List<ModernReferenceDTO> findModernReferencesBySiteId(long siteId) {
        return siteRepository.findById(siteId)
                .map(site -> getModernReferenceDTOList(site.getModernReferenceList()))
                .orElseThrow(() -> {
                    logger.warn("Site with ID {} not found", siteId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
                });
    }

    private List<ModernReferenceDTO> getModernReferenceDTOList(List<ModernReference> modernReferenceList) {
        List<ModernReferenceDTO> modernReferenceDTOList = new ArrayList<>();
        for (ModernReference modernReference : modernReferenceList) {
            modernReferenceDTOList.add(new ModernReferenceDTO(
                    modernReference.getId(),
                    modernReference.getShortRef(),
                    modernReference.getFullRef(),
                    modernReference.getUrl()
            ));
        }
        return modernReferenceDTOList;
    }

    private Point convertWktToPoint(String wkt) throws ParseException {
        WKTReader reader = new WKTReader(geometryFactory);
        return (Point) reader.read(wkt);
    }

    private void validateSiteDTO(SiteDTO siteDTO) {
        if (siteDTO.getGeom() == null || siteDTO.getName() == null || siteDTO.getSiteType() == null) {
            logger.error("Invalid site data: Missing required fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_SITE_DATA);
        }
    }
}
