package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import com.webgis.ancientdata.web.mapper.SiteMapper;
import org.json.JSONObject;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return new GeoJsonConverter().convertSite(findById(id).orElse(null)).toString();
    }

    public SiteDTO save(SiteDTO siteDTO) {
        validateSiteDTO(siteDTO);
        try {
            List<ModernReference> modernReferenceList = buildModernReferenceList(siteDTO);
            Site site = SiteMapper.toEntity(siteDTO, modernReferenceList);
            Site saved = siteRepository.save(site);
            logger.info("Saving site: {}", saved);
            return SiteMapper.toDto(saved);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Saving site failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_SITE, e);
        }
    }

    public SiteDTO update(Long id, SiteDTO siteDTO) {
        validateSiteDTO(siteDTO);
        try {
            Site site = siteRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Site with ID {} not found for update", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
                    });
            List<ModernReference> modernReferenceList = buildModernReferenceList(siteDTO);
            Site updateSite = SiteMapper.toEntity(siteDTO, modernReferenceList);
            site.setPleiadesId(updateSite.getPleiadesId());
            site.setName(updateSite.getName());
            site.setGeom(updateSite.getGeom());
            site.setSiteType(updateSite.getSiteType());
            site.setStatus(updateSite.getStatus());
            site.setProvince(updateSite.getProvince());
            site.setReferences(updateSite.getReferences());
            site.setDescription(updateSite.getDescription());
            site.setModernReferenceList(updateSite.getModernReferenceList());

            Site updatedSite = siteRepository.save(site);
            logger.info("Updating site: {}", updatedSite);
            return SiteMapper.toDto(updatedSite);

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

    @Transactional
    public SiteDTO addModernReferenceToSite(long siteId, long refId) {
        Site site = siteRepository.findById(siteId).orElseThrow(() -> {
            logger.warn("Site with ID {} not found to add a modern reference", siteId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND);
        });

        ModernReference modernReference = modernReferenceRepository.findById(refId).orElseThrow(() -> {
            logger.warn("ModernReference with ID {} not found", refId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND);
        });

        if (!site.getModernReferenceList().contains(modernReference)) {
            site.addModernReference(modernReference);
            logger.info("Linked ModernReference ID {} to Site ID {}", refId, siteId);
            siteRepository.save(site);
        } else {
            logger.info("ModernReference ID {} already linked to Site ID {}", refId, siteId);
        }

        return SiteMapper.toDto(site);
    }

    public SiteDTO removeModernReferenceFromSite(long siteId, long refId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.SITE_NOT_FOUND));
        ModernReference ref = modernReferenceRepository.findById(refId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));

        site.getModernReferenceList().remove(ref);
        logger.info("Removed ModernReference ID {} from Site ID {}", refId, siteId);
        return SiteMapper.toDto(siteRepository.save(site));
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
        return modernReferenceList.stream().map(modernReference -> new ModernReferenceDTO(
                modernReference.getId(),
                modernReference.getShortRef(),
                modernReference.getFullRef(),
                modernReference.getUrl())).toList();
    }

    private void validateSiteDTO(SiteDTO siteDTO) {
        if (siteDTO.geom() == null || siteDTO.name() == null || siteDTO.siteType() == null) {
            logger.error("Invalid site data: Missing required fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_SITE_DATA);
        }
    }

    private List<ModernReference> buildModernReferenceList (SiteDTO siteDTO) {
        List<ModernReference> modernReferenceList = new ArrayList<>();
        for (Long refId : siteDTO.referenceIds()) {
            modernReferenceList.add(modernReferenceRepository.getReferenceById(refId));
        }
        return modernReferenceList;
    }
}