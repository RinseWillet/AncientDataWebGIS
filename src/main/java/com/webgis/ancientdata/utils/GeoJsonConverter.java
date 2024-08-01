package com.webgis.ancientdata.utils;

//Spring
import com.webgis.ancientdata.road.Road;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Coordinates;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.stereotype.Service;

//JSON
import org.json.*;

//Logging
import org.slf4j.*;

//Java
import java.lang.reflect.Field;
import java.util.*;

//project components
import com.webgis.ancientdata.site.Site;

//class to convert JSON object from repository to GeoJSON
@Service
public class GeoJsonConverter {

    //Logging object
    private final Logger logger = LoggerFactory.getLogger(GeoJsonConverter.class);

    //method to convert points to geojson
    public JSONObject convertSite(Iterable<Site> siteIterable) {

        JSONObject features = setUpGeoJSON();
        features.put("name", "sites");

        //converting incoming Iterable to Iterator
        Iterator<Site> siteIterator = siteIterable.iterator();

        //JSONArray to store all features
        JSONArray jsonArray = new JSONArray();

        //lambda function to iterate through the sites
        siteIterator.forEachRemaining(element -> {

            JSONObject feature = sitePropertiesParser(element);

            //add to JSON Array
            jsonArray.put(feature);
        });

        //loading parsed sites into JSONobject
        features.put("features", jsonArray);

        //returning JSON cf. GeoJSON standard
        return features;
    }

    //method to convert multilinestrings to geojson
    public JSONObject convertRoad(Iterable<Road> roadIterable) {

        JSONObject features = setUpGeoJSON();
        features.put("name", "roads");

        //converting incoming Iterable to Iterator
        Iterator<Road> roadIterator = roadIterable.iterator();

        //JSONArray to store all features
        JSONArray jsonArray = new JSONArray();

        //lambda function to iterate through the sites
        roadIterator.forEachRemaining(element -> {
            if(element!=null){
                JSONObject feature = roadPropertiesParser(element);
                //add to JSON Array
                jsonArray.put(feature);
            } else {
                System.out.println("null");
            }
        });

        //loading parsed sites into JSONobject
        features.put("features", jsonArray);

        //returning JSON cf. GeoJSON standard
        return features;
    }

    private JSONObject setUpGeoJSON () {

        //main object in which all features are stored, object in which projection (crs) information is stored, object
        //in which projection properties are stored;
        JSONObject features = new JSONObject();
        JSONObject crs = new JSONObject();
        JSONObject crsProperties = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(features);
        setLinkedHashMap(crs);
        setLinkedHashMap(crsProperties);

        //setting projection properties - standard WGS 84 projection
        crsProperties.put("name", "urn:ogc:def:crs:OGC:1.3:CRS84");

        //setting projection
        crs.put("type", "name");
        crs.put("properties", crsProperties);

        //setting mainobject
        features.put("type", "FeatureCollection");
        features.put("crs", crs);

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
    private JSONObject sitePropertiesParser (Site site) {

        //JSON objects to store the properties, geometry and the complete feature
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        JSONObject geometry = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(feature);
        setLinkedHashMap(properties);
        setLinkedHashMap(geometry);

        //constructing properties
        properties.put("id", site.getId());
        properties.put("name", site.getName());
        properties.put("siteType", site.getSiteType());
        properties.put("comment", site.getComment());
        properties.put("status", site.getStatus());
        properties.put("statusRef", site.getStatusReference());

        //constructing geometry
        if (site.getGeom() != null){
            geometry.put("type", site.getGeom().getGeometryType());

            //making array of X (latitude) and Y (longitude) coordinates
            Double [] coords = {site.getGeom().getX(), site.getGeom().getY()};
            geometry.put("coordinates", coords);
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

    private JSONObject roadPropertiesParser (Road road) {

        //JSON objects to store the properties, geometry and the complete feature
        JSONObject feature = new JSONObject();
        JSONObject properties = new JSONObject();
        JSONObject geometry = new JSONObject();

        //setting HashMap to linked HashMap to keep order in JSON objects
        setLinkedHashMap(feature);
        setLinkedHashMap(properties);
        setLinkedHashMap(geometry);

        //constructing geometry
        if (road.getGeom() != null) {
            geometry.put("type", road.getGeom().getGeometryType());

            //loop over number of geometries (arrays of coordinates of separate line in multilinestring)
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
            geometry.put("coordinates", multiLineCoords);
        } else {
            geometry.put("coordinates", 1234);
        }

        //constructing properties
        properties.put("id", road.getId());
        properties.put("name", road.getName());
        properties.put("type", road.getType());
        properties.put("date", road.getDate());

        //constructing feature
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);
        return feature;
    }
}
