package com.webgis.ancientdata.utils;

//Spring

import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;

//class to convert JSON object from repository to GeoJSON
@Service
public class GeoJsonConverter {

    //Logging object
    private final Logger logger = LoggerFactory.getLogger(GeoJsonConverter.class);

    //method to convert individual point to geojson
    public JSONObject convertSite(Optional<Site> siteOptional) {
        JSONObject features = setUpGeoJSON();
        if(siteOptional.isPresent()) {
            Site site = siteOptional.get();

            String [] propertyTypes = {
                    "id", "pleiadesId", "name", "province",
                    "siteType", "status", "references", "description"};

            JSONObject feature = siteParser(site, propertyTypes);
            features.put("features", feature);
            return features;
        } else {
            return null;
        }
    }

    //method to convert points to geojson
    public JSONObject convertSites(Iterable<Site> siteIterable) {

        String [] propertyTypes = {"id", "name", "siteType", "status"};

        JSONObject features = setUpGeoJSON();
        features.put("name", "sites");

        //converting incoming Iterable to Iterator
        Iterator<Site> siteIterator = siteIterable.iterator();

        //JSONArray to store all features
        JSONArray jsonArray = new JSONArray();

        //lambda function to iterate through the sites
        siteIterator.forEachRemaining(element -> {

            JSONObject feature = siteParser(element, propertyTypes);

            //add to JSON Array
            jsonArray.put(feature);
        });

        //loading parsed sites into JSONobject
        features.put("features", jsonArray);

        //returning JSON cf. GeoJSON standard
        return features;
    }

    //method to convert multilinestrings to geojson
    public JSONObject convertRoads(Iterable<Road> roadIterable) {

        String [] propertyTypes = {"id", "name", "type", "date"};

        JSONObject features = setUpGeoJSON();
        features.put("name", "roads");

        //converting incoming Iterable to Iterator
        Iterator<Road> roadIterator = roadIterable.iterator();

        //JSONArray to store all features
        JSONArray jsonArray = new JSONArray();

        //lambda function to iterate through the sites
        roadIterator.forEachRemaining(element -> {
            if(element!=null){
                JSONObject feature = roadParser(element, propertyTypes);
                //add to JSON Array
                jsonArray.put(feature);
            } else {
                logger.info("no elements found in roadsarray");
            }
        });

        //loading parsed sites into JSONobject
        features.put("features", jsonArray);

        //returning JSON cf. GeoJSON standard
        return features;
    }

    public JSONObject convertRoad(Optional<Road> roadOptional){
        JSONObject features = setUpGeoJSON();
        if(roadOptional.isPresent()){
            Road road = roadOptional.get();

            String [] propertyTypes = {
                    "id", "cat_nr", "name", "type", "typeDescription",
                    "location", "description", "date", "references",
                    "historicalReferences"};

            JSONObject feature = roadParser(road, propertyTypes);
            features.put("features", feature);
            return features;
        } else {
            return null;
        }
    }

    private JSONObject setUpGeoJSON () {

        //main object in which all features are stored, object in which projection (crs) information is stored, object
        //in which projection properties are stored;
        JSONObject features = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(features);

        //setting mainobject
        features.put("type", "FeatureCollection");
//        features.put("crs", crs);

        return features;
    }

    //this function sets the HashMap in the main JSONObject to a LinkedHashMap, to ensure
    //the ordered sequence of the JSON object
    private void setLinkedHashMap(JSONObject jsonObject) {
        try {
            Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.info(e.getMessage());
        }
    }

    //method to parse values from Site object to JSON feature
    private JSONObject siteParser (Site site, String [] propertyTypes) throws NullPointerException {

        //JSON objects to store the properties, geometry and the complete feature
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        JSONObject geometry = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(feature);
        setLinkedHashMap(properties);
        setLinkedHashMap(geometry);

        SitePropertiesParser(site, properties, propertyTypes);

        //constructing geometry
        try {
            geometry.put("type", site.getGeom().getGeometryType());
        } catch (Exception e) {
            logger.warn("geometry of " + site.getName() + " not found");
            return feature;
        }

        Double [] coordinates = pointCoordinates(site);

        if (coordinates != null) {
            geometry.put("coordinates", coordinates);
        } else {
            geometry.put("coordinates", 1234);
        }

        //constructing feature
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        //return completed feature
        return feature;
    }

    private JSONObject SitePropertiesParser (Site site, JSONObject properties, String [] propertytypes) throws NullPointerException {
        try{
            for (int i = 0; i < propertytypes.length; i++) {
                switch (propertytypes[i]) {
                    case "id":
                        properties.put(propertytypes[i], site.getId());
                        break;
                    case "pleiadesId":
                        properties.put(propertytypes[i], site.getPleiadesId());
                        break;
                    case "name":
                        properties.put(propertytypes[i], site.getName());
                        break;
                    case "province":
                        properties.put(propertytypes[i], site.getProvince());
                        break;
                    case "siteType":
                        properties.put(propertytypes[i], site.getSiteType());
                        break;
                    case "status":
                        properties.put(propertytypes[i], site.getStatus());
                        break;
                    case "references":
                        properties.put(propertytypes[i], site.getReferences());
                        break;
                    case "description":
                        properties.put(propertytypes[i], site.getDescription());
                        break;
                }
            }
        } catch (Exception E) {
            logger.warn("property not found in site " + site.getName());
        }
        return properties;
    }

    private JSONObject roadParser (Road road, String [] propertytypes) throws NullPointerException {

        //JSON objects to store the properties, geometry and the complete feature
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        JSONObject geometry = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(feature);
        setLinkedHashMap(properties);
        setLinkedHashMap(geometry);

        RoadPropertiesParser(road, properties, propertytypes);

       //constructing geometry
        try {
            geometry.put("type", road.getGeom().getGeometryType());
        } catch (Exception e) {
            logger.warn("geometry of " + road.getName() + " not found");
            geometry.put("type", "not_found");
        }

        Double [][][] coordinates = lineCoordinates(road);

        if (coordinates != null) {
            geometry.put("coordinates", coordinates);
        } else {
            geometry.put("coordinates", 1234);
        }

        //constructing feature
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);
        return feature;
    }

    private JSONObject RoadPropertiesParser (Road road, JSONObject properties, String [] propertytypes) throws NullPointerException {
        try{
            for (int i = 0; i < propertytypes.length; i++) {
                switch (propertytypes[i]) {
                    case "id":
                        properties.put(propertytypes[i], road.getId());
                        break;
                    case "cat_nr":
                        properties.put(propertytypes[i], road.getCat_nr());
                        break;
                    case "name":
                        properties.put(propertytypes[i], road.getName());
                        break;
                    case "type":
                        properties.put(propertytypes[i], road.getType());
                        break;
                    case "typeDescription":
                        properties.put(propertytypes[i], road.getTypeDescription());
                        break;
                    case "location":
                        properties.put(propertytypes[i], road.getLocation());
                        break;
                    case "description":
                        properties.put(propertytypes[i], road.getDescription());
                        break;
                    case "date":
                        properties.put(propertytypes[i], road.getDate());
                        break;
                    case "references":
                        properties.put(propertytypes[i], road.getReferences());
                        break;
                    case "historicalReferences":
                        properties.put(propertytypes[i], road.getHistoricalReferences());
                        break;
                }
            }

        } catch (Exception E) {
            logger.warn("property not found in site " + road.getName());
        }
        return properties;
    }

    //constructing coordinates for JSON
    Double [] pointCoordinates (Site site) {

        if (site.getGeom() != null){
            //making array of X (latitude) and Y (longitude) coordinates
            Double [] coordinates = {site.getGeom().getX(), site.getGeom().getY()};
            return coordinates;
        } else {
            return null;
        }
    }

    //loop over number of geometries (arrays of coordinates of separate line in multilinestring)
    Double [][][] lineCoordinates (Road road) {
        if (road.getGeom() != null) {

            int i = 0;
            Double [][][] multiLineCoords = new Double [road.getGeom().getNumGeometries()][][];

            while (i < road.getGeom().getNumGeometries()) {
                Coordinate[] coord = road.getGeom().getGeometryN(i).getCoordinates();

                int j = 0;
                Double [][] line = new Double[coord.length][];

                while (j<coord.length){
                    Double [] point = {coord[j].getX(), coord[j].getY()};
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
}
