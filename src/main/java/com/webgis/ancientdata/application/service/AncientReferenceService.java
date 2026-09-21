package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.AncientReferenceDTO;
import com.webgis.ancientdata.domain.model.AncientReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.AncientReferenceRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import com.webgis.ancientdata.web.mapper.AncientReferenceMapper;
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
public class AncientReferenceService {

    @Autowired
    private final AncientReferenceRepository ancientReferenceRepository;

    private final Logger logger = LoggerFactory.getLogger(AncientReferenceService.class);

    public AncientReferenceService(AncientReferenceRepository ancientReferenceRepository) {
        this.ancientReferenceRepository = ancientReferenceRepository;
    }

    public List<AncientReferenceDTO> findAllAsDTOs() {
        try {
            return StreamSupport
                    .stream(findAll().spliterator(), false)
                    .map(AncientReferenceMapper::toDto)
                    .filter(dto -> dto.name() != null)
                    .sorted(Comparator.comparing(AncientReferenceDTO::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to fetch or sort ancient references: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load references", e);
        }
    }

    public Iterable<AncientReference> findAll() {
        logger.info("Finding all ancient references");
        return ancientReferenceRepository.findAll();
    }

    public AncientReferenceDTO findByIdDTO(long id) {
        return findById(id)
                .map(AncientReferenceMapper::toDto)
                .orElseThrow(() -> {
                    logger.warn("AncientReference with ID {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ancient reference not found");
                });
    }

    public Optional<AncientReference> findById(long id) {
        Optional<AncientReference> found = ancientReferenceRepository.findById(id);
        if (found.isEmpty()) {
            logger.warn("ancient reference with id: {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ANCIENT_REFERENCE_NOT_FOUND);
        }
        return found;
    }

    public String findSitesByAncientReferenceIdAsGeoJSON(long id) {
        logger.info("finding all sites connected to ancient reference id : {}", id);
        AncientReference ancientReference = findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ANCIENT_REFERENCE_NOT_FOUND));
        List<Site> siteList = ancientReference.getSites();
        return new GeoJsonConverter().convertSites(siteList).toString();
    }

    public AncientReference save(AncientReferenceDTO dto) {
        try {
            AncientReference ancientReference = AncientReferenceMapper.toEntity(dto);
            logger.info("Saving ancient reference: {}", ancientReference);
            return ancientReferenceRepository.save(ancientReference);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid ancient reference data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_ANCIENT_REFERENCE, e);
        } catch (Exception e) {
            logger.error("Saving ancient reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_ANCIENT_REFERENCE, e);
        }
    }

    public AncientReference update(Long id, AncientReferenceDTO dto) {
        try {
            AncientReference reference = ancientReferenceRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ANCIENT_REFERENCE_NOT_FOUND));

            reference.setName(dto.name());
            reference.setAuthor(dto.author());
            reference.setWork(dto.work());
            reference.setBook(dto.book());
            reference.setPage(dto.page());

            logger.info("Updating ancient reference: {}", reference);
            return ancientReferenceRepository.save(reference);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Updating ancient reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_UPDATE_ANCIENT_REFERENCE, e);
        }
    }

    public void delete(Long id) {
        if (!ancientReferenceRepository.existsById(id)) {
            logger.warn("Ancient reference with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ANCIENT_REFERENCE_NOT_FOUND);
        }

        try {
            ancientReferenceRepository.deleteById(id);
            logger.info("Deleted ancient reference with ID {}", id);
        } catch (Exception e) {
            logger.error("Deleting ancient reference failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_DELETE_ANCIENT_REFERENCE, e);
        }
    }
}
