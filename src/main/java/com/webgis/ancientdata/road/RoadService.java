package com.webgis.ancientdata.road;

import com.webgis.ancientdata.Pojos.RoadRequest;
import com.webgis.ancientdata.modernreference.ModernReference;
import com.webgis.ancientdata.modernreference.ModernReferenceDTO;
import com.webgis.ancientdata.modernreference.ModernReferenceRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.locationtech.jts.geom.MultiLineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoadService {

    @Autowired
    private final RoadRepository roadRepository;

    private final Logger logger = LoggerFactory.getLogger(RoadService.class);

    public RoadService (RoadRepository roadRepository, ModernReferenceRepository modernReferenceRepository) {
        this.roadRepository = roadRepository;
    }

    public Iterable<Road> findAll() {
        logger.info("Finding all roads");
        return roadRepository.findAll();
    }

    public String findAllGeoJson() {
        GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
        return geoJsonConverter.convertRoads(findAll()).toString();
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        try {
            GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
            return geoJsonConverter.convertRoad(findById(id)).toString();
        } catch (Exception e) {
            logger.warn("road " + id + " not found ");
            return "not found";
        }
    }

    public Optional<Road> findById(long id) {
        logger.info("find road id : {}", id);
        return roadRepository.findById(id);
    }

    public Road save (Road road) {
        try {
            logger.info("saving road : {}", road);
            return roadRepository.save(road);
        } catch (Exception e) {
            logger.warn("saving road failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicate value", e);
        }
    }

    public List<ModernReferenceDTO> findModernReferencesByRoadId(long id) {
        Road road = findById(id).get();
        List<ModernReference> modernReferenceSet = road.getModernReferenceSet();

        //Parsing into DTO to prevent infinite regressing due to bi-directional many-to-many relationship
        //roads and modernrefs
        List<ModernReferenceDTO> modernReferenceDTOList = new ArrayList<>();

        for(int i = 0; i < modernReferenceSet.size(); i++){
            ModernReferenceDTO modernReferenceDTO = new ModernReferenceDTO(
                    modernReferenceSet.get(i).getId(),
                    modernReferenceSet.get(i).getShortRef(),
                    modernReferenceSet.get(i).getFullRef(),
                    modernReferenceSet.get(i).getURL()
            );

            modernReferenceDTOList.add(modernReferenceDTO);
        }
        return modernReferenceDTOList;
    }
}
