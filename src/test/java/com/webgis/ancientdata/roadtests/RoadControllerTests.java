package com.webgis.ancientdata.roadtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoadControllerTests {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockBean
    private RoadService roadService;

    private Road road;
    private JSONObject roadJSON;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private RandomRoadGenerator randomRoadGenerator;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        randomRoadGenerator = new RandomRoadGenerator();
        objectMapper = new ObjectMapper();

        road = randomRoadGenerator.generateRandomRoad();
        road.setId(RandomUtils.nextLong(1, 1000));
        roadJSON = randomRoadGenerator.generateRandomRoadJSON(road);

        modernReferenceDTO = new ModernReferenceDTO(
                RandomUtils.nextLong(1, 1000),
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(20),
                RandomStringUtils.randomAlphabetic(15)
        );
        modernReferenceDTOList = List.of(modernReferenceDTO);
    }

    @AfterEach
    public void tearDown() {
        road = null;
        roadJSON = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
        randomRoadGenerator = null;
        objectMapper = null;
    }

    @Test
    public void shouldFindRoadByIdGeoJSON() throws Exception {
        when(roadService.findByIdGeoJson(road.getId())).thenReturn(roadJSON.toString());

        mockMvc.perform(get("/api/roads/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(roadJSON.toString()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findByIdGeoJson(road.getId());
    }

    @Test
    public void shouldFindAllRoadsGeoJSON() throws Exception {
        when(roadService.findAllGeoJson()).thenReturn(roadJSON.toString());

        mockMvc.perform(get("/api/roads/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(roadJSON.toString()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findAllGeoJson();
    }

    @Test
    public void shouldFindModernReferencesByRoadId() throws Exception {
        when(roadService.findModernReferencesByRoadId(road.getId())).thenReturn(modernReferenceDTOList);

        mockMvc.perform(get("/api/roads/modref/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(modernReferenceDTOList.size()))
                .andExpect(jsonPath("$[0].id").value(modernReferenceDTO.getId()))
                .andExpect(jsonPath("$[0].shortRef").value(modernReferenceDTO.getShortRef()))
                .andExpect(jsonPath("$[0].fullRef").value(modernReferenceDTO.getFullRef()))
                .andExpect(jsonPath("$[0].url").value(modernReferenceDTO.getUrl()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).findModernReferencesByRoadId(road.getId());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldCreateRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        when(roadService.save(any(RoadDTO.class))).thenReturn(road);

        mockMvc.perform(post("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(road.getId()))
                .andExpect(jsonPath("$.name").value(road.getName()))
                .andExpect(jsonPath("$.cat_nr").value(road.getCat_nr()))
                .andExpect(jsonPath("$.type").value(road.getType()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).save(any(RoadDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
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
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(0)).save(any(RoadDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
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
    @WithMockUser(roles = {"USER"})
    public void shouldReturnNotFoundWhenUpdatingNonexistentRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        ObjectMapper objectMapper = new ObjectMapper();

        when(roadService.update(eq(9999L), any(RoadDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND));

        mockMvc.perform(put("/api/roads/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void shouldDeleteRoad() throws Exception {
        doNothing().when(roadService).delete(road.getId());

        mockMvc.perform(delete("/api/roads/" + road.getId()))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).delete(road.getId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void shouldReturnNotFoundWhenDeletingNonexistentRoad() throws Exception {
        long nonexistentId = 9999L;
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND))
                .when(roadService).delete(nonexistentId);

        mockMvc.perform(delete("/api/roads/" + nonexistentId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class)
                        .hasMessageContaining(ErrorMessages.ROAD_NOT_FOUND))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldAddModernReferenceToRoad() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RoadDTO expectedDto = randomRoadGenerator.toDTO(road);

        when(roadService.addModernReferenceToRoad(eq(road.getId()), any(ModernReferenceDTO.class)))
                .thenReturn(road);

        mockMvc.perform(post("/api/roads/" + road.getId() + "/modern-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modernReferenceDTO)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    RoadDTO responseDto = objectMapper.readValue(json, RoadDTO.class);
                    assertThat(responseDto.getName()).isEqualTo(expectedDto.getName());
                    assertThat(responseDto.getType()).isEqualTo(expectedDto.getType());
                })
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).addModernReferenceToRoad(eq(road.getId()), any(ModernReferenceDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void shouldReturnNotFoundWhenAddingReferenceToNonexistentRoad() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        long nonexistentId = 9999L;

        when(roadService.addModernReferenceToRoad(eq(nonexistentId), any(ModernReferenceDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.ROAD_NOT_FOUND));

        mockMvc.perform(post("/api/roads/" + nonexistentId + "/modern-reference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modernReferenceDTO)))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertThat(result.getResolvedException())
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining(ErrorMessages.ROAD_NOT_FOUND)
                )
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, times(1)).addModernReferenceToRoad(eq(nonexistentId), any(ModernReferenceDTO.class));
    }
}
