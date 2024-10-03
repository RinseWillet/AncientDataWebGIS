package com.webgis.ancientdata.roadtests;

import com.webgis.ancientdata.road.Road;
import com.webgis.ancientdata.road.RoadRepository;
import com.webgis.ancientdata.road.RoadService;

import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoadServiceTests {

    private Road road;
    private List<Road> roadList;
    private JSONObject roadGeoJSON;
    private JSONObject roadsGeoJSON;
    private JSONObject feature;
    private JSONObject feature_2;
    private JSONObject[] features;
    private JSONObject properties;
    private JSONObject properties_2;
    private JSONObject geometry;

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

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private GeoJsonConverter geoJsonConverter;

    @Autowired
    @InjectMocks
    private RoadService roadService;

    @BeforeEach
    public void setUp(){

        roadList = new ArrayList<>();
        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        Integer randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];
        Double[][][] linesDouble = new Double[randomAmountLines][][];

        for (int i = 0; i < randomAmountLines; i++) {
            Integer randomLinePoints = RandomUtils.nextInt(2, 10);

            Coordinate [] points = new Coordinate[randomLinePoints];
            Double [][] pointsDouble = new Double[randomLinePoints][1];

            for (int j = 0; j < randomLinePoints; j++){
                Double x = RandomUtils.nextDouble(0, 180);
                Double y = RandomUtils.nextDouble(0, 90);
                Double z = RandomUtils.nextDouble(0, 3000);
                Coordinate coordinate = new Coordinate(x,y,z);
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

        road = new Road(cat_nr,
                name,
                geom,
                type,
                typeDescription,
                location,
                description,
                date,
                references,
                historicalReferences);

        roadList.add(road);

        //setting up GeoJSON
        //properties
        properties = new JSONObject();
        setLinkedHashMap(properties);
        properties.put("id", road.getId());
        properties.put("cat_nr", cat_nr);
        properties.put("name", name);
        properties.put("type", type);
        properties.put("typeDescription", typeDescription);
        properties.put("location", location);
        properties.put("description", description);
        properties.put("date", date);
        properties.put("references", references);
        properties.put("historicalReferences", historicalReferences);

        //setting up geometry for GeoJSON
        geometry = new JSONObject();
        setLinkedHashMap(geometry);
        geometry.put("type", "MultiLineString");

        Double [][][] multiLineCoords = new Double [road.getGeom().getNumGeometries()][][];
        int i = 0;

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

        //constructing feature for GeoJSON
        feature = new JSONObject();
        setLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        //constructing final GeoJSON
        roadGeoJSON = new JSONObject();
        setLinkedHashMap(roadGeoJSON);
        roadGeoJSON.put("type", "FeatureCollection");
        roadGeoJSON.put("features", feature);

        //setting up roads GeoJSON
        properties_2 = new JSONObject();
        setLinkedHashMap(properties_2);
        properties_2.put("id", road.getId());
        properties_2.put("name", name);
        properties_2.put("type", type);
        properties_2.put("date", date);

        feature_2 = new JSONObject();
        setLinkedHashMap(feature_2);
        feature_2.put("type", "Feature");
        feature_2.put("properties", properties_2);
        feature_2.put("geometry", geometry);

        features = new JSONObject[] {feature_2};

        roadsGeoJSON = new JSONObject();
        setLinkedHashMap(roadsGeoJSON);
        roadsGeoJSON.put("type", "FeatureCollection");
        roadsGeoJSON.put("name", "roads");
        roadsGeoJSON.put("features", features);

    }

    @AfterEach
    public void tearDown() {
        road = null;
        roadList = null;
        roadGeoJSON = null;
        roadsGeoJSON = null;
        feature = null;
        feature_2 = null;
        features = null;
        properties = null;
        properties_2 = null;
        geometry = null;
    }

    @Test
    public void shouldListAllRoads() {
        when(roadRepository.findAll()).thenReturn(roadList);

        List<Road> fetchedRoads = (List<Road>) roadService.findAll();
        assertEquals(fetchedRoads, roadList);

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    public void shouldFindRoadById(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        Optional<Road> optionalRoad = roadService.findById(road.getId());

        optionalRoad.ifPresent(value -> assertThat(optionalRoad.get()).isEqualTo(road));

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    public void shouldFindRoadByIdGeoJSON(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        String fetchedRoad = roadService.findByIdGeoJson(road.getId());

        assertEquals(fetchedRoad, String.valueOf(roadGeoJSON));

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    public void shouldListAllRoadsGeoJSON(){
        when(roadRepository.findAll()).thenReturn(roadList);

        String fetchedRoadsGeoJSON = roadService.findAllGeoJson();

        assertEquals(String.valueOf(fetchedRoadsGeoJSON), String.valueOf(roadsGeoJSON));

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    public void shouldConvertRoadtoGeoJSON(){
        when(geoJsonConverter.convertRoad(Optional.of(road))).thenReturn(roadGeoJSON);

        JSONObject fetchedroadGeoJSON = geoJsonConverter.convertRoad(Optional.of(road));
        assertEquals(fetchedroadGeoJSON, roadGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoad(any());
    }

    @Test
    public void shouldConvertRoadstoGeoJSON(){
        when(geoJsonConverter.convertRoads(roadList)).thenReturn(roadsGeoJSON);

        JSONObject fetchedRoadsGeoJSON = geoJsonConverter.convertRoads(roadList);
        assertEquals(fetchedRoadsGeoJSON, roadsGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoads(any());
    }
}

