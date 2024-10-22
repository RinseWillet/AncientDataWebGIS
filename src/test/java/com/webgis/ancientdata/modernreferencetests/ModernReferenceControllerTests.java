package com.webgis.ancientdata.modernreferencetests;

//MVC
import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.modernreference.ModernReference;
import com.webgis.ancientdata.modernreference.ModernReferenceController;
import com.webgis.ancientdata.modernreference.ModernReferenceService;
import com.webgis.ancientdata.road.Road;
import com.webgis.ancientdata.site.Site;

//Java
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

//Testing libraries
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

//static
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ModernReferenceControllerTests {

    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private JSONObject modernReferenceJSON;

    private RandomRoadGenerator randomRoadGenerator;
    private Road road;
    private List<Road> roadList;
    private JSONObject roadJSON;

    private RandomSiteGenerator randomSiteGenerator;
    private Site site;
    private List<Site> siteList;
    private JSONObject siteJSON;


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
        randomRoadGenerator = new RandomRoadGenerator();
        roadList = new ArrayList<>();

        road = randomRoadGenerator.generateRandomRoad();
        roadList.add(road);

        roadJSON = randomRoadGenerator.generateRandomRoadJSON(road);

        // sites
        randomSiteGenerator = new RandomSiteGenerator();
        siteList = new ArrayList<>();

        site = randomSiteGenerator.generateRandomSite();
        siteList.add(site);

        siteJSON = randomSiteGenerator.generateRandomSiteJSON(site);
    }

    @AfterEach
    void tearDown() {
        modernReference = null;
        modernReferenceList = null;
        modernReferenceJSON = null;
        randomRoadGenerator = null;
        road = null;
        roadList = null;
        roadJSON = null;
        site = null;
        siteList = null;
        siteJSON = null;
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

    @Test
    public void shouldFindSitesByModernReferenceId() throws Exception {
        when(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(modernReference.getId())).thenReturn(siteJSON.toString());

        mockMvc.perform(get("/api/modernreferences/site/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(siteJSON.toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findSitesByModernReferenceIdAsGeoJSON(modernReference.getId());
    }
}