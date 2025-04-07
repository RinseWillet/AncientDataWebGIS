package com.webgis.ancientdata.utils;

import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

//class to convert JSON object from repository to GeoJSON
@Service
public class GeoJsonConverter {

    private final Logger logger = LoggerFactory.getLogger(GeoJsonConverter.class);

    //method to convert individual Point to geojson
    public JSONObject convertSite(Optional<Site> siteOptional) {
        return siteOptional.map(site -> {
            JSONObject features = setUpGeoJSON();
            String[] propertyTypes = {
                    "id", "pleiadesId", "name", "province",
                    "siteType", "status", "references", "description"};
            JSONObject feature = siteParser(site, propertyTypes);
            features.put("features", wrapInArray(feature));
            return features;
        }).orElse(null);
    }

    public JSONObject convertSites(Iterable<Site> siteIterable) {

        String[] propertyTypes = {"id", "name", "siteType", "status"};

        JSONObject features = setUpGeoJSON();
        features.put("name", "sites");

        JSONArray jsonArray = new JSONArray();

        for (Site site : siteIterable) {
            jsonArray.put(siteParser(site, propertyTypes));
        }

        features.put("features", jsonArray);
        return features;
    }

    //method to convert MultiLineString to geojson
    public JSONObject convertRoad(Optional<Road> roadOptional) {
        return roadOptional.map(road -> {
            JSONObject features = setUpGeoJSON();
            String[] propertyTypes = {
                    "id", "cat_nr", "name", "type", "typeDescription",
                    "location", "description", "date", "references",
                    "historicalReferences"};
            JSONObject feature = roadParser(road, propertyTypes);
            features.put("features", wrapInArray(feature));
            return features;
        }).orElse(null);
    }

    public JSONObject convertRoads(Iterable<Road> roadIterable) {
        String[] propertyTypes = {"id", "name", "type", "date"};

        JSONObject features = setUpGeoJSON();
        features.put("name", "roads");

        JSONArray jsonArray = new JSONArray();

        for (Road road : roadIterable) {
            if (road != null) {
                jsonArray.put(roadParser(road, propertyTypes));
            } else {
                logger.debug("[GeoJsonConverter] Skipping null road entry.");
            }
        }

        features.put("features", jsonArray);
        return features;
    }

    private JSONObject setUpGeoJSON() {
        JSONObject features = new JSONObject();

        JsonUtils.enforceLinkedHashMap(features);

        features.put("type", "FeatureCollection");
        return features;
    }

    //method to parse values from Site object to JSON feature
    private JSONObject siteParser(Site site, String[] propertyTypes) throws NullPointerException {

        //JSON objects to store the properties, geometry and the complete feature
        Map<String, JSONObject> structure = createFeatureStructure();
        JSONObject feature = structure.get("feature");
        JSONObject geometry = structure.get("geometry");

        JSONObject extractedProps = extractProperties(site, propertyTypes, siteExtractors);

        //constructing geometry
        if (site.getGeom() == null) {
            logger.warn("[GeoJsonConverter] Skipping Site '{}' (ID: {}) due to missing geometry.",
                    site.getName(), site.getId());
            return feature;
        }
        geometry.put("type", site.getGeom().getGeometryType());

        Double[] coordinates = pointCoordinates(site);

        if (coordinates != null) {
            geometry.put("coordinates", coordinates);
        } else {
            geometry.put("coordinates", JSONObject.NULL);
        }

        //constructing feature
        feature.put("type", "Feature");
        feature.put("properties", extractedProps);
        feature.put("geometry", geometry);

        //return completed feature
        return feature;
    }

    private JSONObject roadParser(Road road, String[] propertyTypes) throws NullPointerException {

        //JSON objects to store the properties, geometry and the complete feature
        Map<String, JSONObject> structure = createFeatureStructure();
        JSONObject feature = structure.get("feature");
        JSONObject geometry = structure.get("geometry");

        JSONObject extractedProps = extractProperties(road, propertyTypes, roadExtractors);

        //constructing geometry
        if (road.getGeom() == null) {
            logger.warn("[GeoJsonConverter] Skipping Road '{}' (ID: {}) due to missing geometry.",
                    road.getName(), road.getId());
            geometry.put("type", "not_found");
        }
        geometry.put("type", road.getGeom().getGeometryType());

        Double[][][] coordinates = lineCoordinates(road);

        if (coordinates != null) {
            geometry.put("coordinates", coordinates);
        } else {
            geometry.put("coordinates", JSONObject.NULL);
        }

        //constructing feature
        feature.put("type", "Feature");
        feature.put("properties", extractedProps);
        feature.put("geometry", geometry);
        return feature;
    }

    //constructing coordinates for JSON
    Double[] pointCoordinates(Site site) {

        if (site.getGeom() != null) {
            //making array of X (latitude) and Y (longitude) coordinates
            return new Double[]{site.getGeom().getX(), site.getGeom().getY()};
        } else {
            return null;
        }
    }

    //loop over number of geometries (arrays of coordinates of separate line in multilinestring)
    Double[][][] lineCoordinates(Road road) {
        if (road.getGeom() != null) {

            int i = 0;
            Double[][][] multiLineCoords = new Double[road.getGeom().getNumGeometries()][][];

            while (i < road.getGeom().getNumGeometries()) {
                Coordinate[] coord = road.getGeom().getGeometryN(i).getCoordinates();

                int j = 0;
                Double[][] line = new Double[coord.length][];

                while (j < coord.length) {
                    Double[] point = {coord[j].getX(), coord[j].getY()};
                    line[j] = point;
                    j++;
                }
                multiLineCoords[i] = line;
                i++;
            }
            return multiLineCoords;
        } else {
            return null;
        }
    }

    private JSONArray wrapInArray(JSONObject feature) {
        JSONArray array = new JSONArray();
        array.put(feature);
        return array;
    }

    private Map<String, JSONObject> createFeatureStructure() {
        JSONObject feature = new JSONObject();
        JSONObject geometry = new JSONObject();

        JsonUtils.enforceLinkedHashMap(feature);
        JsonUtils.enforceLinkedHashMap(geometry);

        Map<String, JSONObject> map = new HashMap<>();
        map.put("feature", feature);
        map.put("geometry", geometry);
        return map;
    }

    private <T> JSONObject extractProperties(
            T obj,
            String[] propertyTypes,
            Map<String, Function<T, Object>> extractorMap
    ) {
        JSONObject properties = new JSONObject();
        JsonUtils.enforceLinkedHashMap(properties);

        boolean hadFailure = false;

        for (String type : propertyTypes) {
            try {
                Object value = extractorMap.getOrDefault(type, o -> null).apply(obj);
                properties.put(type, value);
            } catch (Exception e) {
                hadFailure = true;
                logger.debug("Extractor for '{}' failed on object of type {}", type, obj.getClass().getSimpleName());
            }
        }

        if (hadFailure) {
            logger.warn("Some properties failed to extract for {} (ID unknown or missing)", obj.getClass().getSimpleName());
        }

        return properties;
    }

    private static final Map<String, Function<Site, Object>> siteExtractors = Map.of(
            "id", Site::getId,
            "pleiadesId", Site::getPleiadesId,
            "name", Site::getName,
            "province", Site::getProvince,
            "siteType", Site::getSiteType,
            "status", Site::getStatus,
            "references", Site::getReferences,
            "description", Site::getDescription
    );

    private static final Map<String, Function<Road, Object>> roadExtractors = Map.of(
            "id", Road::getId,
            "cat_nr", Road::getCat_nr,
            "name", Road::getName,
            "type", Road::getType,
            "typeDescription", Road::getTypeDescription,
            "location", Road::getLocation,
            "description", Road::getDescription,
            "date", Road::getDate,
            "references", Road::getReferences,
            "historicalReferences", Road::getHistoricalReferences
    );
}