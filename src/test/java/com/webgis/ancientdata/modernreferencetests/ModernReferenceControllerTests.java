package com.webgis.ancientdata.modernreferencetests;

import com.webgis.ancientdata.modernreference.ModernReference;
import com.webgis.ancientdata.modernreference.ModernReferenceController;
import com.webgis.ancientdata.modernreference.ModernReferenceService;
import com.webgis.ancientdata.road.Road;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class ModernReferenceControllerTests {

    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private JSONObject modernReferenceJSON;
    private Road road;
    private List<Road> roadList;
    private JSONObject roadJSON;

    @Mock
    private ModernReferenceService modernReferenceService;

    @InjectMocks
    private ModernReferenceController modernReferenceController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws JSONException {
        mockMvc = MockMvcBuilders.standaloneSetup(modernReferenceController).build();

        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String URL = RandomStringUtils.randomAlphabetic(100);

        modernReference = new ModernReference(shortRef, fullRef, URL);
        modernReferenceList = new ArrayList<>();
        modernReferenceList.add(modernReference);

        modernReferenceJSON = new JSONObject();
        modernReferenceJSON.put("shortRef", shortRef);
        modernReferenceJSON.put("fullRef", fullRef);
        modernReferenceJSON.put("URL", URL);

        //roads
        roadList = new ArrayList<>();
        int cat_nr = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random points and line
        Integer randomAmountLines = RandomUtils.nextInt(2, 500);
        LineString[] lineStringArray = new LineString[randomAmountLines];

        for (int i = 0; i < randomAmountLines; i++) {
            Integer randomlinepoints = RandomUtils.nextInt(2, 10);

            Coordinate[] points = new Coordinate[randomlinepoints];

            for (int j = 0; j < randomlinepoints; j++) {
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
        roadJSON.put("id", cat_nr);
        roadJSON.put("name", name);
        roadJSON.put("type", type);
        roadJSON.put("geom", geom);
    }

    @AfterEach
    void tearDown() {
        modernReference = null;
        modernReferenceList = null;
        modernReferenceJSON = null;
        road = null;
        roadList = null;
        roadJSON = null;
    }

    @Test
    public void shouldFindModernReferenceById() throws Exception {
        when(modernReferenceService.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        mockMvc.perform(get("/api/modernreferences/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(modernReferenceJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldFindAllModernReferences() throws Exception {
        when(modernReferenceService.findAll()).thenReturn(modernReferenceList);

        mockMvc.perform(get("/api/modernreferences/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(modernReferenceJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findAll();
    }

    @Test
    public void shouldFindRoadsByModernReferenceId() throws Exception {
        when(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId())).thenReturn(roadJSON.toString());

        mockMvc.perform(get("/api/modernreferences/road/" + modernReference.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(roadJSON.toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId());
    }
}