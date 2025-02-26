package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.repository.RoadRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;


@Service
public class RoadService {

    @Autowired
    private final RoadRepository roadRepository;

    private final Logger logger = LoggerFactory.getLogger(RoadService.class);

    public RoadService(RoadRepository roadRepository) {
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

    public Optional<Road> findById(long id) {
        logger.info("find road id : {}", id);

        Optional<Road> roadOptional = roadRepository.findById(id);
        if (roadOptional.isEmpty()) {
            logger.warn("road wit id {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Road with id " + id + "not found"));
        }
        return roadOptional;
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        try {
            GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
            return geoJsonConverter.convertRoad(findById(id)).toString();
        } catch (Exception e) {
            logger.warn("road {} not found ", id);
            return "not found";
        }
    }

    public Road save(Road road) {
        try {
            logger.info("saving road : {}", road);
            return roadRepository.save(road);
        } catch (NullPointerException e) {
            if (road.getGeom() == null) {
                logger.error("for road: {} - no geometry is present", road);
            }
            if (road.getName() == null) {
                logger.error("for road: {} - no value for name is entered", road);
            }
            if (road.getType() == null) {
                logger.error("for road: {} - no value for type is entered", road);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "values missing", e);
        } catch (Exception e) {
            logger.warn("saving road failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicate value", e);
        }
    }

    public Road update(long roadId, Road roadupdate) throws ResponseStatusException {
        try {

            Optional<Road> roadOptional = findById(roadId);
            if (roadOptional.isPresent()) {
                Road road = roadOptional.get();
                road.setName(roadupdate.getName());
                road.setGeom(roadupdate.getGeom());
                road.setType(roadupdate.getType());
                road.setTypeDescription(roadupdate.getTypeDescription());
                road.setLocation(roadupdate.getLocation());
                road.setDescription(roadupdate.getDescription());
                road.setDate(roadupdate.getDate());
                road.setReferences(roadupdate.getReferences());
                road.setHistoricalReferences(roadupdate.getHistoricalReferences());
                return save(road);
            } else {
                logger.warn("updating road failed");
                return null;
            }
        } catch (Exception e) {
            logger.warn("updating road failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public Road addModernReferenceToRoad(long roadId, ModernReferenceDTO modernReferenceDTO) {
        try {
            Optional<Road> roadOptional = findById(roadId);

            if (roadOptional.isPresent()) {
                Road road = roadOptional.get();

                ModernReference modernReference = new ModernReference(
                        modernReferenceDTO.getShortRef(),
                        modernReferenceDTO.getFullRef(),
                        modernReferenceDTO.getURL());
                road.addModernReference(modernReference);
                return roadRepository.save(road);
            } else {
                logger.warn("updating road failed");
                return null;
            }
        } catch (Exception e) {
            logger.warn("adding Modern Reference to road failed: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    //Parsing into DTO to prevent infinite regressing due to bidirectional many-to-many relationship
    //roads and modernrefs
    public List<ModernReferenceDTO> findModernReferencesByRoadId(long roadId) {
        try {

            Optional<Road> roadOptional = findById(roadId);

            if (roadOptional.isPresent()) {
                Road road = findById(roadId).get();

                List<ModernReference> modernReferenceList = road.getModernReferenceList();

                return getModernReferenceDTOList(modernReferenceList);
            } else {
                logger.warn("updating road failed");
                return null;
            }
        } catch (Exception e) {
            logger.warn("finding Modern References for road with id " + roadId + "failed because {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
        }
    }

    public LinkedHashMap getDashBoardData() throws NullPointerException {
        Iterable<Road> roadIterable = findAll();

        //count the amount of roads in total and per type in the DB
        long total = 0;
        AtomicInteger roadno = new AtomicInteger();
        AtomicInteger possibleno = new AtomicInteger();
        AtomicInteger hypotheticalno = new AtomicInteger();
        AtomicInteger otherno = new AtomicInteger();
        AtomicInteger hist_recno = new AtomicInteger();

        total = StreamSupport.stream(roadIterable.spliterator(), false).count();
        StreamSupport.stream(roadIterable.spliterator(), false).forEach(road -> {
            if (road.getType().toString().equals("road")) {
                roadno.getAndIncrement();
            } else if (road.getType().toString().equals("possible road")) {
                possibleno.getAndIncrement();
            } else if (road.getType().toString().equals("hypothetical route")) {
                hypotheticalno.getAndIncrement();
            } else if (road.getType().toString().equals("hist_rec")) {
                hist_recno.getAndIncrement();
            } else if (road.getType().toString().equals("other")) {
                otherno.getAndIncrement();
            }
        });

        LinkedHashMap hashMap = new LinkedHashMap();
        hashMap.put("total amount of roads", total);
        hashMap.put("roads", roadno);
        hashMap.put("possible roads", possibleno);
        hashMap.put("hypothetical routes", hypotheticalno);
        hashMap.put("hist_rec", hist_recno);
        hashMap.put("others", otherno);

        return hashMap;
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
