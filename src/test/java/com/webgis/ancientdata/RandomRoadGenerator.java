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

        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        int randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];

        for (int i = 0; i < randomAmountLines; i++) {
            int randomLinePoints = RandomUtils.nextInt(2, 10);

            Coordinate[] points = new Coordinate[randomLinePoints];

            for (int j = 0; j < randomLinePoints; j++) {
                double x = RandomUtils.nextDouble(0, 180);
                double y = RandomUtils.nextDouble(0, 90);
                double z = RandomUtils.nextDouble(0, 3000);
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
        RoadDTO dto = new RoadDTO();
        dto.setCat_nr(road.getCat_nr());
        dto.setName(road.getName());
        dto.setGeom(road.getGeom().toText()); // WKT
        dto.setType(road.getType());
        dto.setTypeDescription(road.getTypeDescription());
        dto.setLocation(road.getLocation());
        dto.setDescription(road.getDescription());
        dto.setDate(road.getDate());
        return dto;
    }
}


