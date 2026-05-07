package com.webgis.ancientdata.modernreferencetests;

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ModernReferenceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.webgis.ancientdata.application.service.ModernReferenceService modernReferenceService;

    private ModernReference modernReference;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private JSONObject modernReferenceJSON;
    private JSONObject roadJSON;
    private JSONObject siteJSON;


    @BeforeAll
    void setup() throws JSONException {
        Random random = new Random();
        String shortRef = UUID.randomUUID().toString();
        String fullRef = UUID.randomUUID().toString();
        String url = UUID.randomUUID().toString();

        modernReference = new ModernReference(shortRef, fullRef, url);

        modernReference.setId(1L + random.nextInt(999));
        modernReferenceDTO = new ModernReferenceDTO(
                modernReference.getId(),
                modernReference.getShortRef(),
                modernReference.getFullRef(),
                modernReference.getUrl()
        );

        modernReferenceJSON = new JSONObject();
        modernReferenceJSON.put("shortRef", shortRef);
        modernReferenceJSON.put("fullRef", fullRef);
        modernReferenceJSON.put("URL", url);

        modernReferenceDTOList = Collections.singletonList(modernReferenceDTO);

        Road road = new RandomRoadGenerator().generateRandomRoad();
        roadJSON = new RandomRoadGenerator().generateRandomRoadJSON(road);

        Site site = new RandomSiteGenerator().generateRandomSite();
        siteJSON = new RandomSiteGenerator().generateRandomSiteJSON(site);
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindModernReferenceById() throws Exception {
        when(modernReferenceService.findByIdDTO(modernReference.getId())).thenReturn(modernReferenceDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modernReference.getId()))
                .andExpect(jsonPath("$.shortRef").value(modernReference.getShortRef()))
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService).findByIdDTO(modernReference.getId());
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindAllModernReferences() throws Exception {
        when(modernReferenceService.findAllAsDTOs()).thenReturn(modernReferenceDTOList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findAllAsDTOs();
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindRoadsByModernReferenceId() throws Exception {
        when(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId()))
                .thenReturn(roadJSON.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/road/" + modernReference.getId()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService).findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId());
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindSitesByModernReferenceId() throws Exception {
        when(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(modernReference.getId()))
                .thenReturn(siteJSON.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/site/" + modernReference.getId()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService).findSitesByModernReferenceIdAsGeoJSON(modernReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateModernReference() throws Exception {
        when(modernReferenceService.save(any())).thenReturn(modernReference);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/modernreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modernReference.getId()))
                .andExpect(jsonPath("$.shortRef").value(modernReference.getShortRef()));

        verify(modernReferenceService).save(any());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateModernReference() throws Exception {
        when(modernReferenceService.update(eq(modernReference.getId()), any())).thenReturn(modernReference);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/modernreferences/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modernReference.getId()))
                .andExpect(jsonPath("$.shortRef").value(modernReference.getShortRef()));

        verify(modernReferenceService).update(eq(modernReference.getId()), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenUpdatingWithInvalidModernReference() throws Exception {
        String invalidJson = """
                    {
                      "shortRef": "",
                      "fullRef": "",
                      "URL": ""
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/modernreferences/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteModernReference() throws Exception {
        doNothing().when(modernReferenceService).delete(modernReference.getId());

        mockMvc.perform(delete("/api/modernreferences/" + modernReference.getId()))
                .andExpect(status().isNoContent());

        verify(modernReferenceService).delete(modernReference.getId());
    }

    @Test
    void shouldRejectUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/modernreferences/" + modernReference.getId()))
                .andExpect(status().isUnauthorized());

        verify(modernReferenceService, never()).delete(modernReference.getId());
    }

    @WithMockUser(roles = "GUEST")
    @Test
    void shouldDenyCreateModernReferenceForUnauthorizedRole() throws Exception {
        when(modernReferenceService.save(any())).thenReturn(modernReference);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/modernreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isForbidden());

        verify(modernReferenceService, never()).save(any());
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromDeletingReference() throws Exception {
        Long id = modernReference.getId();
        mockMvc.perform(delete("/api/modernreferences/{id}", id))
                .andExpect(status().isForbidden());

        verify(modernReferenceService, never()).delete(modernReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonexistentModernReference() throws Exception {
        when(modernReferenceService.update(eq(999L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/modernreferences/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonexistentReference() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(modernReferenceService).delete(999L);

        mockMvc.perform(delete("/api/modernreferences/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenCreatingInvalidModernReference() throws Exception {
        String invalidJson = """
                {
                  "shortRef": "",
                  "fullRef": "",
                  "URL": ""
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/modernreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "GUEST")
    @Test
    void shouldFailAtPreAuthorize() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/modernreferences/1"))
                .andExpect(status().isForbidden());
    }
}
