package com.webgis.ancientdata;

import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.utils.JsonUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

public class RandomRoadGenerator {

    public Road generateRandomRoad() {

        int catNr = RandomUtils.insecure().randomInt();
        String name = RandomStringUtils.insecure().nextAlphabetic(10);

        //creating random points and line
        int randomAmountLines = RandomUtils.insecure().randomInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];

        for (int i = 0; i < randomAmountLines; i++) {
            int randomLinePoints = RandomUtils.insecure().randomInt(2, 10);

            Coordinate[] points = new Coordinate[randomLinePoints];

            for (int j = 0; j < randomLinePoints; j++) {
                double x = RandomUtils.insecure().randomDouble(0, 180);
                double y = RandomUtils.insecure().randomDouble(0, 90);
                double z = RandomUtils.insecure().randomDouble(0, 3000);
                Coordinate coordinate = new Coordinate(x, y, z);
                points[j] = coordinate;
            }

            CoordinateSequence coordinateArraySequence = new CoordinateArraySequence(points);
            GeometryFactory geometryFactoryLineString = new GeometryFactory();
            LineString lineString = new LineString(coordinateArraySequence, geometryFactoryLineString);
            lineStringArray[i] = lineString;
        }

        GeometryFactory geometryFactoryMultiLineString = new GeometryFactory();
        MultiLineString geom = new MultiLineString(lineStringArray, geometryFactoryMultiLineString);

        String type = RandomStringUtils.insecure().nextAlphabetic(10);
        String typeDescription = RandomStringUtils.insecure().nextAlphabetic(15);
        String location = RandomStringUtils.insecure().nextAlphabetic(100);
        String description = RandomStringUtils.insecure().nextAlphabetic(100);
        String date = RandomStringUtils.insecure().nextAlphabetic(10);
        String references = RandomStringUtils.insecure().nextAlphabetic(50);
        String historicalReferences = RandomStringUtils.insecure().nextAlphabetic(50);

        return new Road(catNr,
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
        roadJSON.put("typeDescription", road.getTypeDescription());
        roadJSON.put("location", road.getLocation());
        roadJSON.put("description", road.getDescription());
        roadJSON.put("date", road.getDate());
        roadJSON.put("references", road.getReferences());
        roadJSON.put("historicalReferences", road.getHistoricalReferences());

        return roadJSON;
    }

    private JSONObject generateRandomRoadGeoJONProperties(Road road) {

        //properties
        JSONObject properties = new JSONObject();
        JsonUtils.enforceLinkedHashMap(properties);
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

    private JSONObject generateRandomRoadGeoJONGeometry(Road road) {
        //setting up geometry for GeoJSON
        JSONObject geometry = new JSONObject();
        JsonUtils.enforceLinkedHashMap(geometry);
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


    public JSONObject generateRandomRoadGeoJSON(Road road) {

        //setting up GeoJSON
        //properties
        JSONObject properties = generateRandomRoadGeoJONProperties(road);

        //setting up geometry for GeoJSON
        JSONObject geometry = generateRandomRoadGeoJONGeometry(road);

        //constructing feature for GeoJSON
        JSONObject feature = new JSONObject();
        JsonUtils.enforceLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        //constructing final GeoJSON
        JSONObject roadGeoJSON = new JSONObject();
        JsonUtils.enforceLinkedHashMap(roadGeoJSON);

        JSONArray featureArray = new JSONArray();
        featureArray.put(feature);
        roadGeoJSON.put("type", "FeatureCollection");
        roadGeoJSON.put("features", featureArray);

        return roadGeoJSON;
    }

    public JSONObject generateRandomRoadsGeoJSON(Road road) {
        //setting up simplified properties for roads GeoJSON
        JSONObject properties = new JSONObject();
        JsonUtils.enforceLinkedHashMap(properties);
        properties.put("id", road.getId());
        properties.put("name", road.getName());
        properties.put("type", road.getType());
        properties.put("date", road.getDate());


        //setting up geometry for GeoJSON
        JSONObject geometry = generateRandomRoadGeoJONGeometry(road);

        //setting up features for roads GeoJSON
        JSONObject feature = new JSONObject();
        JsonUtils.enforceLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        JSONObject[] features = new JSONObject[]{feature};

        JSONObject roadsGeoJSON = new JSONObject();
        JsonUtils.enforceLinkedHashMap(roadsGeoJSON);
        roadsGeoJSON.put("type", "FeatureCollection");
        roadsGeoJSON.put("name", "roads");
        roadsGeoJSON.put("features", features);

        return roadsGeoJSON;
    }

    public RoadDTO toDTO(Road road) {
        return new RoadDTO(
                road.getId(),
                road.getCat_nr(),
                road.getName(),

                road.getGeom().toText(), // WKT
                road.getType(),
                road.getTypeDescription(),
                road.getLocation(),
                road.getDescription(),
                road.getDate(),
                null
        );
    }
}
