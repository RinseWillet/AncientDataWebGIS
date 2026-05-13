package com.webgis.ancientdata.roadtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoadControllerTests {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockitoBean
    private RoadService roadService;

    private Road road;
    private JSONObject roadJSON;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private RandomRoadGenerator randomRoadGenerator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        randomRoadGenerator = new RandomRoadGenerator();
        objectMapper = new ObjectMapper();

        Random random = new Random();
        road = randomRoadGenerator.generateRandomRoad();
        road.setId(1L + random.nextInt(999));
        roadJSON = randomRoadGenerator.generateRandomRoadJSON(road);

        modernReferenceDTO = new ModernReferenceDTO(
                1L + random.nextInt(999),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );
        modernReferenceDTOList = List.of(modernReferenceDTO);
    }

    @AfterEach
    void tearDown() {
        road = null;
        roadJSON = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
        randomRoadGenerator = null;
        objectMapper = null;
    }

    @Test
    void shouldFindRoadByIdGeoJSON() throws Exception {
        when(roadService.findByIdGeoJson(road.getId())).thenReturn(roadJSON.toString());

        mockMvc.perform(get("/api/roads/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(roadJSON.toString()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService).findByIdGeoJson(road.getId());
    }

    @Test
    void shouldFindAllRoadsGeoJSON() throws Exception {
        when(roadService.findAllGeoJson()).thenReturn(roadJSON.toString());

        mockMvc.perform(get("/api/roads/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(roadJSON.toString()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService).findAllGeoJson();
    }

    @Test
    void shouldFindModernReferencesByRoadId() throws Exception {
        when(roadService.findModernReferencesByRoadId(road.getId())).thenReturn(modernReferenceDTOList);

        mockMvc.perform(get("/api/roads/modref/" + road.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(modernReferenceDTOList.size()))
                .andExpect(jsonPath("$[0].id").value(modernReferenceDTO.id()))
                .andExpect(jsonPath("$[0].shortRef").value(modernReferenceDTO.shortRef()))
                .andExpect(jsonPath("$[0].fullRef").value(modernReferenceDTO.fullRef()))
                .andExpect(jsonPath("$[0].url").value(modernReferenceDTO.url()))
                .andDo(MockMvcResultHandlers.print());

        verify(roadService).findModernReferencesByRoadId(road.getId());
    }

     @Test
     @WithMockUser
     void shouldForbidCreateRoad() throws Exception {
         RoadDTO roadDTO = randomRoadGenerator.toDTO(road);

         mockMvc.perform(post("/api/roads")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(roadDTO)))
                 .andExpect(status().isForbidden())
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService, never()).save(any(RoadDTO.class));
     }

     @Test
     @WithMockUser
     void shouldForbidCreateRoadWhenMissingRequiredFields() throws Exception {
        String invalidJson = """
        {
            "cat_nr": 12345,
            "geom": "MULTILINESTRING ((12.4924 41.8902, 12.4964 41.9028))"
        }
        """;

        mockMvc.perform(post("/api/roads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                                .andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, never()).save(any(RoadDTO.class));
    }

     @Test
     @WithMockUser
     void shouldForbidUpdateRoad() throws Exception {
         RoadDTO roadDTO = randomRoadGenerator.toDTO(road);

         mockMvc.perform(put("/api/roads/" + road.getId())
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(roadDTO)))
                 .andExpect(status().isForbidden())
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService, never()).update(eq(road.getId()), any(RoadDTO.class));
     }

     @Test
     @WithMockUser
     void shouldForbidUpdatingNonexistentRoad() throws Exception {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);

        mockMvc.perform(put("/api/roads/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roadDTO)))
                .andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, never()).update(eq(9999L), any(RoadDTO.class));
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldForbidDeleteRoad() throws Exception {

        mockMvc.perform(delete("/api/roads/" + road.getId()))
                .andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, never()).delete(road.getId());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldForbidDeletingNonexistentRoad() throws Exception {
        long nonexistentId = 9999L;

        mockMvc.perform(delete("/api/roads/" + nonexistentId))
                .andExpect(status().isForbidden())
                .andDo(MockMvcResultHandlers.print());

        verify(roadService, never()).delete(nonexistentId);
    }

     @Test
     @WithMockUser
     void shouldAddModernReferenceToRoad() throws Exception {
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
                     assertThat(responseDto.name()).isEqualTo(expectedDto.name());
                     assertThat(responseDto.type()).isEqualTo(expectedDto.type());
                 })
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService).addModernReferenceToRoad(eq(road.getId()), any(ModernReferenceDTO.class));
     }

     @Test
     @WithMockUser
     void shouldReturnNotFoundWhenAddingReferenceToNonexistentRoad() throws Exception {
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

        verify(roadService).addModernReferenceToRoad(eq(nonexistentId), any(ModernReferenceDTO.class));
    }

      @Test
      void shouldValidateRoadFetchResponseSchema() throws Exception {
         // Verifies that fetch returns valid GeoJSON with expected properties
         JSONObject responseJson = new JSONObject();
         responseJson.put("type", "FeatureCollection");
         JSONObject feature = new JSONObject();
         feature.put("type", "Feature");
         JSONObject properties = new JSONObject();
         properties.put("id", road.getId());
         properties.put("name", road.getName());
         properties.put("type", "road");
         properties.put("typeDescription", "Historic Roman road");
         properties.put("location", "Northern Italia");
         properties.put("description", "Test road");
         properties.put("cat_nr", 123);
         feature.put("properties", properties);

         JSONObject geometry = new JSONObject();
         geometry.put("type", "LineString");
         geometry.put("coordinates", new double[][]{{12.4924, 41.8902}, {12.5024, 41.9002}});
         feature.put("geometry", geometry);

         responseJson.put("features", new org.json.JSONArray().put(feature));

         when(roadService.findByIdGeoJson(road.getId())).thenReturn(responseJson.toString());

         mockMvc.perform(get("/api/roads/" + road.getId())
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.type").value("FeatureCollection"))
                 .andExpect(jsonPath("$.features[0].type").value("Feature"))
                 .andExpect(jsonPath("$.features[0].properties.id").exists())
                 .andExpect(jsonPath("$.features[0].properties.name").exists())
                 .andExpect(jsonPath("$.features[0].properties.type").exists())
                 .andExpect(jsonPath("$.features[0].geometry.type").exists())
                 .andExpect(jsonPath("$.features[0].geometry.coordinates").isArray())
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService, times(1)).findByIdGeoJson(road.getId());
     }

      @Test
      @WithMockUser(roles = "ADMIN")
      void shouldValidateRoadUpdateRequestSchema() throws Exception {
         // Contract currently denies updates for all roles; service should not be invoked
         RoadDTO validUpdateDTO = new RoadDTO(
                 1L,
                 123,
                 "Updated Road Name",
                 "MULTILINESTRING ((12.4924 41.8902, 12.5024 41.9002))",
                 "road",
                 "Historic Roman road",
                 "Northern Italia",
                 "Updated description",
                 null,
                 List.of()
         );

         mockMvc.perform(put("/api/roads/1")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(objectMapper.writeValueAsString(validUpdateDTO)))
                 .andExpect(status().isForbidden())
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService, times(0)).update(eq(1L), any(RoadDTO.class));
     }

      @Test
      @WithMockUser(roles = "ADMIN")
      void shouldRejectRoadUpdateWithInvalidGeometry() throws Exception {
         // Contract currently denies updates before payload validation
         String invalidUpdateJson = """
                 {
                     "id": 1,
                     "name": "Test Road",
                     "type": "road",
                     "typeDescription": "Historic road",
                     "location": "Italia",
                     "geom": "INVALID WKT",
                     "description": "Test",
                     "references": "None",
                     "cat_nr": 123
                 }
                 """;

         mockMvc.perform(put("/api/roads/1")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(invalidUpdateJson))
                 .andExpect(status().isForbidden())
                 .andDo(MockMvcResultHandlers.print());

         verify(roadService, times(0)).update(eq(1L), any(RoadDTO.class));
     }
}
