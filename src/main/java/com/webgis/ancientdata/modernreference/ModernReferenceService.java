package com.webgis.ancientdata.modernreference;

import com.webgis.ancientdata.road.Road;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ModernReferenceService {

//    private final ModernReferenceRepository modernReferenceRepository;
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

    public Optional<ModernReference> findById(long id){
        logger.info("find modern reference id : {}", id);
        return modernReferenceRepository.findById(id);
    }

    public Iterable<Road> findRoadsByModernReferenceId(long id) throws NoSuchElementException {
        logger.info("finding all roads connected to modern reference id : {}", id);
        try{
            return modernReferenceRepository.findById(id).get().getRoads();
        } catch (Exception e) {
            logger.warn("road " + id + " not found ");
            return null;
        }
    }
}
