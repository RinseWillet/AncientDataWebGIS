package com.webgis.ancientdata.roadtests;

//MVC
import com.webgis.ancientdata.road.Road;
import com.webgis.ancientdata.road.RoadController;
import com.webgis.ancientdata.road.RoadService;

//Java
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.locationtech.jts.geom.*;

//Spring
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;

//Test boilerplate libraries
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

@ExtendWith(MockitoExtension.class)
public class RoadControllerTests {

    private Road road;
    private List<Road> roadList;
    private JSONObject roadJSON;

    @Mock
    private RoadService roadService;

    @InjectMocks
    private RoadController roadController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws JSONException {
        mockMvc = MockMvcBuilders.standaloneSetup(roadController).build();

        roadList = new ArrayList<>();
        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        Integer randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];

        for (int i = 0; i < randomAmountLines; i++) {
            Integer randomlinepoints = RandomUtils.nextInt(2, 10);

            Coordinate[] points = new Coordinate[randomlinepoints];

            for (int j = 0; j < randomlinepoints; j++){
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

        roadJSON = new JSONObject();
        roadJSON.put("cat_nr", cat_nr);
        roadJSON.put("name", name);
        roadJSON.put("geom", geom);
        roadJSON.put("type", type);
        roadJSON.put("typeDescription", typeDescription);
        roadJSON.put("location", location);
        roadJSON.put("description", description);
        roadJSON.put("date", date);
        roadJSON.put("references", references);
        roadJSON.put("historicalReferences", historicalReferences);

    }

    @AfterEach
    void tearDown() {
        road = null;
        roadList = null;
        roadJSON = null;
    }

    @Test
    public void shouldFindAllRoads() throws Exception {
        when(roadService.findAll()).thenReturn(roadList);

        mockMvc.perform(get("/api/roads/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(roadJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findAll();
    }

    @Test
    public void shouldFindRoadByIdGeoJSON() throws Exception {
        when(roadService.findByIdGeoJson(road.getId())).thenReturn(String.valueOf(roadJSON));

        mockMvc.perform(get("/api/roads/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(roadJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findByIdGeoJson(road.getId());
    }
}
