package com.webgis.ancientdata.roadtests;

//MVC

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        mockMvc = MockMvcBuilders
                .standaloneSetup(roadController)
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        roadList = new ArrayList<>();
        randomRoadGenerator = new RandomRoadGenerator();
        road = randomRoadGenerator.generateRandomRoad();
        road.setId(RandomUtils.nextLong(1, 1000));
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

    @Test
    public void shouldCreateRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.save(any(RoadDTO.class))).thenReturn(road);

        mockMvc.perform(post("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).save(any(RoadDTO.class));
    }

    @Test
    public void shouldReturnBadRequestWhenMissingRequiredFields() throws Exception {
        String invalidJson = """
    {
        "cat_nr": 12345,
        "geom": "MULTILINESTRING ((12.4924 41.8902, 12.4964 41.9028))"
    }
    """;

        mockMvc.perform(post("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(org.springframework.web.bind.MethodArgumentNotValidException.class))
                .andExpect(result -> assertThat(result.getResolvedException().getMessage())
                        .contains("Type is required", "Name is required"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldUpdateRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.update(eq(road.getId()), any(RoadDTO.class))).thenReturn(road);

        mockMvc.perform(put("/api/roads/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).update(eq(road.getId()), any(RoadDTO.class));
    }

    @Test
    public void shouldReturnNotFoundWhenUpdatingNonexistentRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.update(eq(9999L), any(RoadDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Road not found"));

        mockMvc.perform(put("/api/roads/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldDeleteRoad() throws Exception {
        doNothing().when(roadService).delete(road.getId());

        mockMvc.perform(delete("/api/roads/" + road.getId()))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).delete(road.getId());
    }

    @Test
    public void shouldReturnNotFoundWhenDeletingNonexistentRoad() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Road not found"))
                .when(roadService).delete(9999L);

        mockMvc.perform(delete("/api/roads/9999"))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldAddModernReferenceToRoad() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.addModernReferenceToRoad(eq(road.getId()), any(ModernReferenceDTO.class)))
                .thenReturn(road);

        mockMvc.perform(post("/api/roads/" + road.getId() + "/modern-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modernReferenceDTO)))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).addModernReferenceToRoad(eq(road.getId()), any(ModernReferenceDTO.class));
    }

    @Test
    public void shouldReturnNotFoundWhenAddingReferenceToNonexistentRoad() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.addModernReferenceToRoad(eq(9999L), any(ModernReferenceDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Road not found"));

        mockMvc.perform(post("/api/roads/9999/modern-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modernReferenceDTO)))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
}
