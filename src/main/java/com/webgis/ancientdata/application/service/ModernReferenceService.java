package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import com.webgis.ancientdata.web.mapper.ModernReferenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@Service
public class ModernReferenceService {

    @Autowired
    private final ModernReferenceRepository modernReferenceRepository;

    private final Logger logger = LoggerFactory.getLogger(ModernReferenceService.class);

    public ModernReferenceService(ModernReferenceRepository modernReferenceRepository) {
        this.modernReferenceRepository = modernReferenceRepository;
    }

    public Iterable<ModernReference> findAll() {
        logger.info("Finding all modern references");
        return modernReferenceRepository.findAll();
    }

    public Optional<ModernReference> findById(long id) {
        logger.info("find modern reference id : {}", id);

        return modernReferenceRepository.findById(id).map(Optional::of).orElseThrow(() -> {
            logger.warn("modern reference with id: {} not found", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND);
        });
    }

    public String findRoadsByModernReferenceIdAsGeoJSON(long id) {
        logger.info("finding all roads connected to modern reference id : {}", id);
        try {
            ModernReference modernReference = findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));
            List<Road> roadList = modernReference.getRoadList();
            return new GeoJsonConverter().convertRoads(roadList).toString();
        } catch (Exception e) {
            logger.warn("finding roads for modern reference {} failed", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public String findSitesByModernReferenceIdAsGeoJSON(long id) {
        logger.info("finding all sites connected to modern reference id : {}", id);
        try {
            ModernReference modernReference = findById(id).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));
            List<Site> siteList = modernReference.getSiteList();
            return new GeoJsonConverter().convertSites(siteList).toString();
        } catch (Exception e) {
            logger.warn("finding sites for modern reference {} failed", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public ModernReference save(ModernReferenceDTO dto) {
        try {
            ModernReference modernReference = ModernReferenceMapper.toEntity(dto);
            logger.info("Saving modern reference: {}", modernReference);
            return modernReferenceRepository.save(modernReference);
        } catch (Exception e) {
            logger.error("Saving modern reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_MODERN_REFERENCE, e);
        }
    }

    public ModernReference update(Long id, ModernReferenceDTO dto) {
        try {
            ModernReference reference = modernReferenceRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));

            reference.setShortRef(dto.getShortRef());
            reference.setFullRef(dto.getFullRef());
            reference.setUrl(dto.getUrl());

            logger.info("Updating modern reference: {}", reference);
            return modernReferenceRepository.save(reference);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Updating modern reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_UPDATE_MODERN_REFERENCE, e);
        }
    }

    public void delete(Long id) {
        if (!modernReferenceRepository.existsById(id)) {
            logger.warn("Modern reference with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND);
        }

        try {
            modernReferenceRepository.deleteById(id);
            logger.info("Deleted modern reference with ID {}", id);
        } catch (Exception e) {
            logger.error("Deleting modern reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_DELETE_MODERN_REFERENCE, e);
        }
    }
}
