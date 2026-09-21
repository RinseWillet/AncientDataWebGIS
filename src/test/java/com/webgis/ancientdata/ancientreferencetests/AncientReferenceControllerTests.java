package com.webgis.ancientdata.ancientreferencetests;

import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.domain.dto.AncientReferenceDTO;
import com.webgis.ancientdata.domain.model.AncientReference;
import com.webgis.ancientdata.domain.model.Site;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class AncientReferenceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.webgis.ancientdata.application.service.AncientReferenceService ancientReferenceService;

    private AncientReference ancientReference;
    private AncientReferenceDTO ancientReferenceDTO;
    private List<AncientReferenceDTO> ancientReferenceDTOList;
    private JSONObject ancientReferenceJSON;
    private JSONObject siteJSON;

    @BeforeAll
    void setup() throws JSONException {
        Random random = new Random();
        String name = UUID.randomUUID().toString();
        String author = "Strabo";
        String work = "Geographica";
        String book = "IV";

        ancientReference = new AncientReference(name, author, work, book, null);
        ancientReference.setId(1L + random.nextInt(999));

        ancientReferenceDTO = new AncientReferenceDTO(
                ancientReference.getId(),
                ancientReference.getName(),
                ancientReference.getAuthor(),
                ancientReference.getWork(),
                ancientReference.getBook(),
                ancientReference.getPage()
        );

        ancientReferenceJSON = new JSONObject();
        ancientReferenceJSON.put("name", name);
        ancientReferenceJSON.put("author", author);
        ancientReferenceJSON.put("work", work);
        ancientReferenceJSON.put("book", book);

        ancientReferenceDTOList = Collections.singletonList(ancientReferenceDTO);

        Site site = new RandomSiteGenerator().generateRandomSite();
        siteJSON = new RandomSiteGenerator().generateRandomSiteJSON(site);
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindAncientReferenceById() throws Exception {
        when(ancientReferenceService.findByIdDTO(ancientReference.getId())).thenReturn(ancientReferenceDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ancientreferences/" + ancientReference.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ancientReference.getId()))
                .andExpect(jsonPath("$.name").value(ancientReference.getName()))
                .andDo(MockMvcResultHandlers.print());

        verify(ancientReferenceService).findByIdDTO(ancientReference.getId());
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindAllAncientReferences() throws Exception {
        when(ancientReferenceService.findAllAsDTOs()).thenReturn(ancientReferenceDTOList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ancientreferences/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(ancientReferenceService, times(1)).findAllAsDTOs();
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldFindSitesByAncientReferenceId() throws Exception {
        when(ancientReferenceService.findSitesByAncientReferenceIdAsGeoJSON(ancientReference.getId()))
                .thenReturn(siteJSON.toString());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ancientreferences/site/" + ancientReference.getId()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        verify(ancientReferenceService).findSitesByAncientReferenceIdAsGeoJSON(ancientReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAncientReference() throws Exception {
        when(ancientReferenceService.save(any())).thenReturn(ancientReference);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ancientreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ancientReferenceJSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ancientReference.getId()))
                .andExpect(jsonPath("$.name").value(ancientReference.getName()));

        verify(ancientReferenceService).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAncientReference() throws Exception {
        when(ancientReferenceService.update(eq(ancientReference.getId()), any())).thenReturn(ancientReference);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/ancientreferences/" + ancientReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ancientReferenceJSON.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ancientReference.getId()))
                .andExpect(jsonPath("$.name").value(ancientReference.getName()));

        verify(ancientReferenceService).update(eq(ancientReference.getId()), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenUpdatingWithInvalidAncientReference() throws Exception {
        String invalidJson = """
                    {
                      "name": "",
                      "author": "",
                      "work": ""
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/ancientreferences/" + ancientReference.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAncientReference() throws Exception {
        doNothing().when(ancientReferenceService).delete(ancientReference.getId());

        mockMvc.perform(delete("/api/ancientreferences/" + ancientReference.getId()))
                .andExpect(status().isNoContent());

        verify(ancientReferenceService).delete(ancientReference.getId());
    }

    @Test
    void shouldRejectUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/ancientreferences/" + ancientReference.getId()))
                .andExpect(status().isUnauthorized());

        verify(ancientReferenceService, never()).delete(ancientReference.getId());
    }

    @WithMockUser(roles = "GUEST")
    @Test
    void shouldDenyCreateAncientReferenceForUnauthorizedRole() throws Exception {
        when(ancientReferenceService.save(any())).thenReturn(ancientReference);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ancientreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ancientReferenceJSON.toString()))
                .andExpect(status().isForbidden());

        verify(ancientReferenceService, never()).save(any());
    }

    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromDeletingReference() throws Exception {
        Long id = ancientReference.getId();
        mockMvc.perform(delete("/api/ancientreferences/{id}", id))
                .andExpect(status().isForbidden());

        verify(ancientReferenceService, never()).delete(ancientReference.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonexistentAncientReference() throws Exception {
        when(ancientReferenceService.update(eq(999L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/ancientreferences/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ancientReferenceJSON.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingNonexistentReference() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(ancientReferenceService).delete(999L);

        mockMvc.perform(delete("/api/ancientreferences/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestWhenCreatingInvalidAncientReference() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "author": "",
                  "work": ""
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ancientreferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(roles = "GUEST")
    @Test
    void shouldFailAtPreAuthorize() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/ancientreferences/1"))
                .andExpect(status().isForbidden());
    }
}
