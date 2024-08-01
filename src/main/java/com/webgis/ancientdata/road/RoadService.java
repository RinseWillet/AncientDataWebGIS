package com.webgis.ancientdata.road;

import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public JSONObject finaAllGeoJson() {
        GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
        return geoJsonConverter.convertRoad(findAll());
    }
}
