package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.domain.repository.RoadRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;


@Service
public class RoadService {

    private final RoadRepository roadRepository;
    private final ModernReferenceRepository modernReferenceRepository;
    private final Logger logger = LoggerFactory.getLogger(RoadService.class);
    private final GeometryFactory geometryFactory = new GeometryFactory();


    private static final String ROAD = "road";
    private static final String POS_ROAD = "possible road";
    private static final String HYP_ROUTE = "hypothetical route";
    private static final String HIST_REC = "hist_rec";
    private static final String OTHER = "other";

    public RoadService(RoadRepository roadRepository, ModernReferenceRepository modernReferenceRepository) {
        this.roadRepository = roadRepository;
        this.modernReferenceRepository = modernReferenceRepository;
    }

    //methods accessible for all roles

    public Iterable<Road> findAll() {
        logger.info("Retrieving all roads");
        return roadRepository.findAll();
    }

    public String findAllGeoJson() {
        logger.info("Retrieving all roads and converting to GeoJSON");
        return new GeoJsonConverter().convertRoads(findAll()).toString();
    }

    public Optional<Road> findById(long id) {
        logger.info("find road id : {}", id);
        return roadRepository.findById(id)
                .or(() -> {
                    logger.warn("Road with id {} not found", id);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
                });
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        return new GeoJsonConverter().convertRoad(findById(id)).toString();
    }

    //protected methods

    public Road save(RoadDTO roadDTO) {
        if (roadDTO.getGeom() == null || roadDTO.getName() == null || roadDTO.getType() == null) {
            logger.error("Invalid road data: Missing required fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_ROAD_DATA);
        }
        try {
            Road road = new Road();
            road.setCat_nr(roadDTO.getCat_nr());
            road.setName(roadDTO.getName());
            road.setGeom(convertWktToGeometry(roadDTO.getGeom()));
            road.setType(roadDTO.getType());
            road.setTypeDescription(roadDTO.getTypeDescription());
            road.setLocation(roadDTO.getLocation());
            road.setDescription(roadDTO.getDescription());
            road.setDate(roadDTO.getDate());

            logger.info("Saving road: {}", road);
            return roadRepository.save(road);
        } catch (ParseException e) {
            logger.error("Invalid WKT geometry format: {}", roadDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid WKT format", e);
        } catch (Exception e) {
            logger.warn("Saving road failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_SAVE_ROAD, e);
        }
    }

    public Road update(long roadId, RoadDTO roadDTO) {
        try {
            Optional<Road> roadOptional = findById(roadId);
            if (roadOptional.isPresent()) {
                Road road = roadOptional.get();
                road.setCat_nr(roadDTO.getCat_nr());
                road.setName(roadDTO.getName());
                road.setGeom(convertWktToGeometry(roadDTO.getGeom()));
                road.setType(roadDTO.getType());
                road.setTypeDescription(roadDTO.getTypeDescription());
                road.setLocation(roadDTO.getLocation());
                road.setDescription(roadDTO.getDescription());
                road.setDate(roadDTO.getDate());

                logger.info("Updating road: {}", road);
                return roadRepository.save(road);
            } else {
                logger.warn("Road with ID {} not found for update", roadId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
            }
        } catch (ParseException e) {
            logger.error("Invalid WKT geometry format: {}", roadDTO.getGeom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid WKT format", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Updating road failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.COULD_NOT_UPDATE_ROAD, e);
        }
    }

    //protected - ADMIN ONLY

    public void delete(long roadId) {
        if (!roadRepository.existsById(roadId)) {
            logger.warn("Road with ID {} not found for deletion", roadId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
        }
        roadRepository.deleteById(roadId);
        logger.info("Delete road with ID {}", roadId);
    }

    //protected

    public Road addModernReferenceToRoad(long roadId, ModernReferenceDTO dto) {
        return roadRepository.findById(roadId).map(road -> {
            ModernReference modernReference;

            if (dto.getId() != null) {
                modernReference = modernReferenceRepository.findById(dto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ModernReference not found"));
            } else {
                modernReference = new ModernReference(dto.getShortRef(), dto.getFullRef(), dto.getUrl());
            }

            road.addModernReference(modernReference);
            logger.info("Added modern reference to road ID {}", roadId);
            return roadRepository.save(road);

        }).orElseThrow(() -> {
            logger.warn("Road with ID {} not found to add a modern reference to", roadId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
        });
    }

    //methods accessible for all roles

    //Parsing into DTO to prevent infinite regressing due to bidirectional many-to-many relationship
    //roads and modernrefs
    public List<ModernReferenceDTO> findModernReferencesByRoadId(long roadId) {
        return roadRepository.findById(roadId)
                .map(road -> getModernReferenceDTOList(road.getModernReferenceList()))
                .orElseThrow(() -> {
                    logger.warn("Road with ID {} not found", roadId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Road not found");
                });
    }

    public LinkedHashMap<String, Object> getDashBoardData() throws NullPointerException {
        Iterable<Road> roadIterable = findAll();

        //count the amount of roads in total and per type in the DB
        AtomicInteger roadCount = new AtomicInteger();
        AtomicInteger possibleCount = new AtomicInteger();
        AtomicInteger hypotheticalCount = new AtomicInteger();
        AtomicInteger otherCount = new AtomicInteger();
        AtomicInteger historicalCount = new AtomicInteger();

        long total = StreamSupport.stream(roadIterable.spliterator(), false).count();
        StreamSupport.stream(roadIterable.spliterator(), false).forEach(road -> {
            switch (road.getType()) {
                case ROAD -> roadCount.getAndIncrement();
                case POS_ROAD -> possibleCount.getAndIncrement();
                case HYP_ROUTE -> hypotheticalCount.getAndIncrement();
                case HIST_REC -> historicalCount.getAndIncrement();
                case OTHER -> otherCount.getAndIncrement();
            }
        });

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("total_roads", total);
        data.put("confirmed_roads", roadCount.get());
        data.put("possible_roads", possibleCount.get());
        data.put("hypothetical_routes", hypotheticalCount.get());
        data.put("historical_recorded", historicalCount.get());
        data.put("other", otherCount.get());

        logger.info("Dashboard data retrieved");

        return data;
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

    private MultiLineString convertWktToGeometry(String wkt) throws ParseException {
        WKTReader reader = new WKTReader(geometryFactory);
        return (MultiLineString) reader.read(wkt);
    }
}
