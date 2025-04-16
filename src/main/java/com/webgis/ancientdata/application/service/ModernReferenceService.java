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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;


@Service
public class ModernReferenceService {

    @Autowired
    private final ModernReferenceRepository modernReferenceRepository;

    private final Logger logger = LoggerFactory.getLogger(ModernReferenceService.class);

    public ModernReferenceService(ModernReferenceRepository modernReferenceRepository) {
        this.modernReferenceRepository = modernReferenceRepository;
    }

    public List<ModernReferenceDTO> findAllAsDTOs() {
        try {
            return StreamSupport
                    .stream(findAll().spliterator(), false)
                    .map(ModernReferenceMapper::toDto)
                    .filter(dto -> dto.shortRef() != null)
                    .sorted(Comparator.comparing(ModernReferenceDTO::shortRef, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to fetch or sort modern references: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load references", e);
        }
    }

    public Iterable<ModernReference> findAll() {
        logger.info("Finding all modern references");
        return modernReferenceRepository.findAll();
    }

    public ModernReferenceDTO findByIdDTO(long id) {
        return findById(id)
                .map(ModernReferenceMapper::toDto)
                .orElseThrow(() -> {
                    logger.warn("ModernReference with ID {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Modern reference not found");
                });
    }

    public Optional<ModernReference> findById(long id) {
        Optional<ModernReference> found = modernReferenceRepository.findById(id);
        if (found.isEmpty()) {
            logger.warn("modern reference with id: {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND);
        }
        return found;
    }

    public String findRoadsByModernReferenceIdAsGeoJSON(long id) {
        logger.info("finding all roads connected to modern reference id : {}", id);
        ModernReference modernReference = findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));
        List<Road> roadList = modernReference.getRoadList();
        return new GeoJsonConverter().convertRoads(roadList).toString();
    }

    public String findSitesByModernReferenceIdAsGeoJSON(long id) {
        logger.info("finding all sites connected to modern reference id : {}", id);
        ModernReference modernReference = findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));
        List<Site> siteList = modernReference.getSiteList();
        return new GeoJsonConverter().convertSites(siteList).toString();
    }

    public ModernReferenceDTO save(ModernReferenceDTO dto) {
        try {
            ModernReference modernReference = ModernReferenceMapper.toEntity(dto);
            ModernReference saved  = modernReferenceRepository.save(modernReference);
            logger.info("Saved modern reference: {}", modernReference);
            return ModernReferenceMapper.toDto(saved);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid modern reference data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_MODERN_REFERENCE, e);
        } catch (Exception e) {
            logger.error("Saving modern reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_MODERN_REFERENCE, e);
        }
    }

    public ModernReferenceDTO update(Long id, ModernReferenceDTO dto) {
        try {
            ModernReference reference = modernReferenceRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));

            reference.setShortRef(dto.shortRef());
            reference.setFullRef(dto.fullRef());
            reference.setUrl(dto.url());
            ModernReference updated = modernReferenceRepository.save(reference);
            logger.info("Updated modern reference: {}", reference);
            return ModernReferenceMapper.toDto(updated);
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
