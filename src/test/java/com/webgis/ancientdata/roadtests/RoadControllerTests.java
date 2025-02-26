package com.webgis.ancientdata.roadtests;

//MVC

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.web.controller.RoadController;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoadControllerTests {

    private Road road;
    private List<Road> roadList;
    private JSONObject roadJSON;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private RandomRoadGenerator randomRoadGenerator;


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
        randomRoadGenerator = new RandomRoadGenerator();
        road = randomRoadGenerator.generateRandomRoad();

        roadList.add(road);

        roadJSON = randomRoadGenerator.generateRandomRoadJSON(road);

        modernReferenceDTOList = new ArrayList<>();

        Long id = RandomUtils.nextLong();
        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String URL = RandomStringUtils.randomAlphabetic(100);

        modernReferenceDTO = new ModernReferenceDTO(id, shortRef, fullRef, URL);
        modernReferenceDTOList.add(modernReferenceDTO);
    }

    @AfterEach
    void tearDown() {
        road = null;
        roadList = null;
        roadJSON = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
        randomRoadGenerator = null;
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

    @Test
    public void shouldFindAllRoadsGeoJSON() throws Exception {
        when(roadService.findAllGeoJson()).thenReturn(String.valueOf(roadJSON));

        mockMvc.perform(get("/api/roads/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(roadJSON)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findAllGeoJson();
    }

    @Test
    public void shouldFindModernReferencesByRoadId() throws Exception {
        when(roadService.findModernReferencesByRoadId(road.getId())).thenReturn(modernReferenceDTOList);

        mockMvc.perform(get("/api/roads/modref/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceDTOList.toString()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findModernReferencesByRoadId(road.getId());
    }
}
