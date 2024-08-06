package com.webgis.ancientdata.road;

import com.webgis.ancientdata.site.Site;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoadService {

    private final RoadRepository roadRepository;
    private final Logger logger = LoggerFactory.getLogger(RoadService.class);

    public RoadService (RoadRepository roadRepository) {
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

    public String findByIdGeoJson(long id) {
        GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
        return geoJsonConverter.convertRoad(findById(id)).toString();
    }

    public Optional<Road> findById(long id) {
        logger.info("find road id : {}", id);
        return roadRepository.findById(id);
    }
}
