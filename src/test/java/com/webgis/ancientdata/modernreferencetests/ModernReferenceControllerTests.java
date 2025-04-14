package com.webgis.ancientdata.modernreferencetests;

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModernReferenceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.webgis.ancientdata.application.service.ModernReferenceService modernReferenceService;

    private ModernReference modernReference;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private JSONObject modernReferenceJSON;

    private Road road;
    private JSONObject roadJSON;

    private Site site;
    private JSONObject siteJSON;

    @BeforeAll
    public void setup() throws JSONException {
        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String url = RandomStringUtils.randomAlphabetic(100);

        modernReference = new ModernReference(shortRef, fullRef, url);

        modernReference.setId(RandomUtils.nextLong(1, 1000));
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

        road = new RandomRoadGenerator().generateRandomRoad();
        roadJSON = new RandomRoadGenerator().generateRandomRoadJSON(road);

        site = new RandomSiteGenerator().generateRandomSite();
        siteJSON = new RandomSiteGenerator().generateRandomSiteJSON(site);
    }

    @WithMockUser(roles = "USER")
    @Test
    public void shouldFindModernReferenceById() throws Exception {
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
    public void shouldFindAllModernReferences() throws Exception {
        when(modernReferenceService.findAllAsDTOs()).thenReturn(modernReferenceDTOList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService, times(1)).findAllAsDTOs();
    }

    @WithMockUser(roles = "USER")
    @Test
    public void shouldFindRoadsByModernReferenceId() throws Exception {
        when(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId()))
                .thenReturn(roadJSON.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/road/" + modernReference.getId()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService).findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId());
    }

    @WithMockUser(roles = "USER")
    @Test
    public void shouldFindSitesByModernReferenceId() throws Exception {
        when(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(modernReference.getId()))
                .thenReturn(siteJSON.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/modernreferences/site/" + modernReference.getId()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(modernReferenceService).findSitesByModernReferenceIdAsGeoJSON(modernReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldCreateModernReference() throws Exception {
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
    public void shouldUpdateModernReference() throws Exception {
        when(modernReferenceService.update(eq(modernReference.getId()), any())).thenReturn(modernReference);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/modernreferences/" + modernReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(modernReference.getId()))
                .andExpect(jsonPath("$.shortRef").value(modernReference.getShortRef()));
        ;

        verify(modernReferenceService).update(eq(modernReference.getId()), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnBadRequestWhenUpdatingWithInvalidModernReference() throws Exception {
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
    public void shouldDeleteModernReference() throws Exception {
        doNothing().when(modernReferenceService).delete(modernReference.getId());

        mockMvc.perform(delete("/api/modernreferences/" + modernReference.getId()))
                .andExpect(status().isNoContent());

        verify(modernReferenceService).delete(modernReference.getId());
    }

    @Test
    public void shouldRejectUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/modernreferences/" + modernReference.getId()))
                .andExpect(status().isUnauthorized());

        verify(modernReferenceService, never()).delete(modernReference.getId());
    }

    @WithMockUser(roles = "GUEST")
    @Test
    public void shouldDenyCreateModernReferenceForUnauthorizedRole() throws Exception {
        when(modernReferenceService.save(any())).thenReturn(modernReference);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/modernreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isForbidden());

        verify(modernReferenceService, never()).save(any());
    }

    @WithMockUser(roles = "USER")
    @Test
    public void shouldForbidUserFromDeletingReference() throws Exception {
        Long id = modernReference.getId();
        mockMvc.perform(delete("/api/modernreferences/{id}", id))
                .andExpect(status().isForbidden());

        verify(modernReferenceService, never()).delete(modernReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnNotFoundWhenUpdatingNonexistentModernReference() throws Exception {
        when(modernReferenceService.update(eq(999L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/modernreferences/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modernReferenceJSON.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnNotFoundWhenDeletingNonexistentReference() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(modernReferenceService).delete(999L);

        mockMvc.perform(delete("/api/modernreferences/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldReturnBadRequestWhenCreatingInvalidModernReference() throws Exception {
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
    public void shouldFailAtPreAuthorize() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/modernreferences/1"))
                .andExpect(status().isForbidden());
    }
}