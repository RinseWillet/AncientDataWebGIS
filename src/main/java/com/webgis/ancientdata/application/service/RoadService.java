package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.dto.RoadDashboardDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.domain.repository.RoadRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import com.webgis.ancientdata.web.mapper.RoadMapper;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static com.webgis.ancientdata.constants.ErrorMessages.INVALID_WKT_FORMAT;


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

    public RoadDTO findByIdDTO(long id) {
        return RoadMapper.toDto(findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND)));
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        return new GeoJsonConverter().convertRoad(findById(id).orElse(null)).toString();
    }

    //protected methods

    public Road save(RoadDTO roadDTO) {
        if (roadDTO.geom() == null || roadDTO.name() == null || roadDTO.type() == null) {
            logger.error("Invalid road data: Missing required fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_ROAD_DATA);
        }
        try {
            Road road = new Road();
            road.setCat_nr(roadDTO.cat_nr());
            road.setName(roadDTO.name());
            road.setGeom(convertWktToGeometry(roadDTO.geom()));
            road.setType(roadDTO.type());
            road.setTypeDescription(roadDTO.typeDescription());
            road.setLocation(roadDTO.location());
            road.setDescription(roadDTO.description());
            road.setDate(roadDTO.date());

            logger.info("Saving road: {}", road);
            return roadRepository.save(road);
        } catch (ParseException e) {
            logger.error(INVALID_WKT_FORMAT + ": {} ", roadDTO.geom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_WKT_FORMAT, e);
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
                road.setCat_nr(roadDTO.cat_nr());
                road.setName(roadDTO.name());
                road.setGeom(convertWktToGeometry(roadDTO.geom()));
                road.setType(roadDTO.type());
                road.setTypeDescription(roadDTO.typeDescription());
                road.setLocation(roadDTO.location());
                road.setDescription(roadDTO.description());
                road.setDate(roadDTO.date());


                if (roadDTO.referenceIds() != null) {
                    List<ModernReference> references = modernReferenceRepository.findAllById(roadDTO.referenceIds());
                    StringBuilder shortRefsBuilder = new StringBuilder();
                    for (ModernReference ref : references) {
                        if (!shortRefsBuilder.isEmpty()) {
                            shortRefsBuilder.append(", ");
                        }
                        shortRefsBuilder.append(ref.getShortRef());
                    }
                    road.setReferences(shortRefsBuilder.toString());
                    road.setModernReferenceList(references);
                    logger.info("Set {} modern references on road ID {}", references.size(), road.getId());
                }

                logger.info("Updating road: {}", road);
                return roadRepository.save(road);
            } else {
                logger.warn("Road with ID {} not found for update", roadId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
            }
        } catch (ParseException e) {
            logger.error(ErrorMessages.INVALID_WKT_FORMAT + ": {}", roadDTO.geom());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_WKT_FORMAT, e);
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

    @Transactional
    public RoadDTO addModernReferenceToRoad(long roadId, long refId) {
        Road road = roadRepository.findById(roadId).orElseThrow(() -> {
            logger.warn("Road with ID {} not found to add a modern reference", roadId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND);
        });

        ModernReference modernReference = modernReferenceRepository.findById(refId).orElseThrow(() -> {
            logger.warn("ModernReference with ID {} not found", refId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND);
        });

        if (!road.getModernReferenceList().contains(modernReference)) {
            road.addModernReference(modernReference);
            logger.info("Linked ModernReference ID {} to Site ID {}", refId, roadId);
            roadRepository.save(road);
        } else {
            logger.info("ModernReference ID {} already linked to Site ID {}", refId, roadId);
        }

        return RoadMapper.toDto(road);
    }

    public RoadDTO removeModernReferenceFromRoad(long roadId, long refId) {
        Road road = roadRepository.findById(roadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND));
        ModernReference ref = modernReferenceRepository.findById(refId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MODERN_REFERENCE_NOT_FOUND));

        road.getModernReferenceList().remove(ref);
        logger.info("Removed ModernReference ID {} from Road ID {}", refId, roadId);
        return RoadMapper.toDto(roadRepository.save(road));
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

    public RoadDashboardDTO getDashBoardData() {
        Iterable<Road> roadIterable = findAll();

        AtomicInteger roadCount = new AtomicInteger();
        AtomicInteger possibleCount = new AtomicInteger();
        AtomicInteger hypotheticalCount = new AtomicInteger();
        AtomicInteger otherCount = new AtomicInteger();
        AtomicInteger historicalCount = new AtomicInteger();

        long total = StreamSupport.stream(roadIterable.spliterator(), false).count();

        StreamSupport.stream(roadIterable.spliterator(), false).forEach(road -> {
            switch (road.getType()) {
                case ROAD -> roadCount.incrementAndGet();
                case POS_ROAD -> possibleCount.incrementAndGet();
                case HYP_ROUTE -> hypotheticalCount.incrementAndGet();
                case HIST_REC -> historicalCount.incrementAndGet();
                case OTHER -> otherCount.incrementAndGet();
            }
        });

        logger.info("Dashboard data retrieved");

        return new RoadDashboardDTO(
                total,
                roadCount.get(),
                possibleCount.get(),
                hypotheticalCount.get(),
                historicalCount.get(),
                otherCount.get()
        );
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