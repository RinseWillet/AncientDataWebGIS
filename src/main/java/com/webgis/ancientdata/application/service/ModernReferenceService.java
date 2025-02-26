package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ModernReferenceService {

    @Autowired
    private final ModernReferenceRepository modernReferenceRepository;

    private final Logger logger = LoggerFactory.getLogger(ModernReferenceService.class);

    public ModernReferenceService (ModernReferenceRepository modernReferenceRepository) {
        this.modernReferenceRepository = modernReferenceRepository;
    }

    public Iterable<ModernReference> findAll() {
        logger.info("Finding all modern references");
        return modernReferenceRepository.findAll();
    }

    public Optional<ModernReference> findById(long id) throws NoSuchElementException{
        logger.info("find modern reference id : {}", id);

        Optional<ModernReference> modernReferenceOptional = modernReferenceRepository.findById(id);
        if(modernReferenceOptional.isEmpty()) {
            logger.warn("modern reference with id: {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("modern reference with id " + id + "not found"));
        }
        return modernReferenceOptional;
    }

    public String findRoadsByModernReferenceIdAsGeoJSON(long id)  {
        logger.info("finding all roads connected to modern reference id : {}", id);
        try{
            Optional<ModernReference> modernReferenceOptional = findById(id);
            if(modernReferenceOptional.isPresent()) {
                ModernReference modernReference = modernReferenceOptional.get();

                List<Road> roadList = modernReference.getRoadList();

                GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
                return geoJsonConverter.convertRoads(roadList).toString();
            } else {
                logger.warn("modern reference not found");
                return null;
            }
        } catch (Exception e) {
            logger.warn("finding roads for modern reference {} failed", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public String findSitesByModernReferenceIdAsGeoJSON(long id)  {
        logger.info("finding all sites connected to modern reference id : {}", id);
        try{
            Optional<ModernReference> modernReferenceOptional = findById(id);
            if(modernReferenceOptional.isPresent()) {
                ModernReference modernReference = modernReferenceOptional.get();

                List<Site> siteList = modernReference.getSiteList();

                GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
                return geoJsonConverter.convertSites(siteList).toString();
            } else {
                logger.warn("modern reference not found");
                return null;
            }
        } catch (Exception e) {
            logger.warn("finding sites for modern reference {} failed", id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }
}
