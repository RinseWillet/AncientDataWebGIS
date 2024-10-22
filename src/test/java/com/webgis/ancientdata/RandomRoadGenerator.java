package com.webgis.ancientdata;

import com.webgis.ancientdata.road.Road;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class RandomRoadGenerator {

    private void setLinkedHashMap(JSONObject jsonObject) {
        try {
            Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("error");
        }
    }

    public Road generateRandomRoad() {

        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        Integer randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];
        Double[][][] linesDouble = new Double[randomAmountLines][][];

        for (int i = 0; i < randomAmountLines; i++) {
            Integer randomLinePoints = RandomUtils.nextInt(2, 10);

            Coordinate[] points = new Coordinate[randomLinePoints];
            Double[][] pointsDouble = new Double[randomLinePoints][1];

            for (int j = 0; j < randomLinePoints; j++) {
                Double x = RandomUtils.nextDouble(0, 180);
                Double y = RandomUtils.nextDouble(0, 90);
                Double z = RandomUtils.nextDouble(0, 3000);
                Coordinate coordinate = new Coordinate(x, y, z);
                points[j] = coordinate;
            }

            CoordinateSequence coordinateArraySequence = new CoordinateArraySequence(points);
            GeometryFactory geometryFactory_lineString = new GeometryFactory();
            LineString lineString = new LineString(coordinateArraySequence, geometryFactory_lineString);
            lineStringArray[i] = lineString;
        }

        GeometryFactory geometryFactory_multiLineString = new GeometryFactory();
        MultiLineString geom = new MultiLineString(lineStringArray, geometryFactory_multiLineString);

        String type = RandomStringUtils.randomAlphabetic(10);
        String typeDescription = RandomStringUtils.randomAlphabetic(15);
        String location = RandomStringUtils.randomAlphabetic(100);
        String description = RandomStringUtils.randomAlphabetic(100);
        String date = RandomStringUtils.randomAlphabetic(10);
        String references = RandomStringUtils.randomAlphabetic(50);
        String historicalReferences = RandomStringUtils.randomAlphabetic(50);

        return new Road(cat_nr,
                name,
                geom,
                type,
                typeDescription,
                location,
                description,
                date,
                references,
                historicalReferences);
    }

    public JSONObject generateRandomRoadJSON(Road road) {
        JSONObject roadJSON = new JSONObject();
        roadJSON.put("cat_nr", road.getCat_nr());
        roadJSON.put("name", road.getName());
        roadJSON.put("geom", road.getGeom());
        roadJSON.put("type", road.getType());
        roadJSON.put("typeDescription", road.getDescription());
        roadJSON.put("location", road.getLocation());
        roadJSON.put("description", road.getDescription());
        roadJSON.put("date", road.getDate());
        roadJSON.put("references", road.getReferences());
        roadJSON.put("historicalReferences", road.getHistoricalReferences());

        return roadJSON;
    }

    private JSONObject generateRandomRoadGeoJONProperties (Road road) {

        //properties
        JSONObject properties = new JSONObject();
        setLinkedHashMap(properties);
        properties.put("id", road.getId());
        properties.put("cat_nr", road.getCat_nr());
        properties.put("name", road.getName());
        properties.put("type", road.getType());
        properties.put("typeDescription", road.getTypeDescription());
        properties.put("location", road.getLocation());
        properties.put("description", road.getDescription());
        properties.put("date", road.getDate());
        properties.put("references", road.getReferences());
        properties.put("historicalReferences", road.getHistoricalReferences());

        return properties;
    }

    private JSONObject generateRandomRoadGeoJONGeometry (Road road) {
        //setting up geometry for GeoJSON
        JSONObject geometry = new JSONObject();
        setLinkedHashMap(geometry);
        geometry.put("type", "MultiLineString");

        Double[][][] multiLineCoords = new Double[road.getGeom().getNumGeometries()][][];
        int i = 0;

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
        geometry.put("coordinates", multiLineCoords);

        return geometry;
    }


    public JSONObject generateRandomRoadGeoJSON (Road road) {

        //setting up GeoJSON
        //properties
        JSONObject properties = generateRandomRoadGeoJONProperties(road);

        //setting up geometry for GeoJSON
        JSONObject geometry = generateRandomRoadGeoJONGeometry(road);

        //constructing feature for GeoJSON
        JSONObject feature = new JSONObject();
        setLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        //constructing final GeoJSON
        JSONObject roadGeoJSON = new JSONObject();
        setLinkedHashMap(roadGeoJSON);
        roadGeoJSON.put("type", "FeatureCollection");
        roadGeoJSON.put("features", feature);

        return roadGeoJSON;
    }

    public JSONObject generateRandomRoadsGeoJSON (Road road) {
        //setting up simplified properties for roads GeoJSON
        JSONObject properties = new JSONObject();
        setLinkedHashMap(properties);
        properties.put("id", road.getId());
        properties.put("name", road.getName());
        properties.put("type", road.getType());
        properties.put("date", road.getDate());


        //setting up geometry for GeoJSON
        JSONObject geometry = generateRandomRoadGeoJONGeometry(road);

        //setting up features for roads GeoJSON
        JSONObject feature = new JSONObject();
        setLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        JSONObject [] features = new JSONObject[] {feature};

        JSONObject roadsGeoJSON = new JSONObject();
        setLinkedHashMap(roadsGeoJSON);
        roadsGeoJSON.put("type", "FeatureCollection");
        roadsGeoJSON.put("name", "roads");
        roadsGeoJSON.put("features", features);

        return roadsGeoJSON;
    }
}


