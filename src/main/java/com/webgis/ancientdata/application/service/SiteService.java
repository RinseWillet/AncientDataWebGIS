package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONObject;
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

    public Site save(Site site) {
        try {
            logger.info("saving site : {}", site);
            return siteRepository.save(site);
        } catch (NullPointerException e) {
            if (site.getGeom() == null) {
                logger.error("for site: {} - no geometry is present", site);
            }
            if (site.getName() == null) {
                logger.error("for site: {} - no value for name is entered", site);
            }
            if (site.getSiteType() == null) {
                logger.error("for site: {} - no value for type is entered", site);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "values missing", e);
        } catch (Exception e) {
            logger.warn("saving site failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicate value", e);
        }
    }

    public Site update(long siteId, Site siteupdate) throws ResponseStatusException {
        try {
            Optional<Site> siteOptional = findById(siteId);
            if (siteOptional.isPresent()) {
                Site site = siteOptional.get();
                site.setProvince(siteupdate.getProvince());
                site.setName(siteupdate.getName());
                site.setGeom(siteupdate.getGeom());
                site.setSiteType(siteupdate.getSiteType());
                site.setStatus(siteupdate.getStatus());
                site.setReferences(siteupdate.getReferences());
                site.setDescription(siteupdate.getDescription());
                return save(site);
            } else {
                logger.warn("site was not found");
                return null;
            }

        } catch (Exception e) {
            logger.warn("updating site failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public Site addModernReferenceToSite(long siteId, ModernReferenceDTO modernReferenceDTO) {
        try {
            Optional<Site> siteOptional = findById(siteId);
            if (siteOptional.isPresent()) {
                Site site = siteOptional.get();

                ModernReference modernReference = new ModernReference(
                        modernReferenceDTO.getShortRef(),
                        modernReferenceDTO.getFullRef(),
                        modernReferenceDTO.getURL());
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
                    modernReference.getURL()
            );

            modernReferenceDTOList.add(modernReferenceDTO);
        }
        return modernReferenceDTOList;
    }
}
