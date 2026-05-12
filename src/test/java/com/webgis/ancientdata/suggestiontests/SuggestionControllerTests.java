package com.webgis.ancientdata.suggestiontests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.ancientdata.application.service.SuggestionService;
import com.webgis.ancientdata.domain.dto.SuggestionCreateRequestDTO;
import com.webgis.ancientdata.domain.dto.SuggestionResponseDTO;
import com.webgis.ancientdata.domain.dto.SuggestionReviewRequestDTO;
import com.webgis.ancientdata.domain.model.SuggestionStatus;
import com.webgis.ancientdata.domain.model.SuggestionTargetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SuggestionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuggestionService suggestionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "researcher", roles = "USER")
    void shouldSubmitSuggestionForAuthenticatedUser() throws Exception {
        SuggestionResponseDTO response = new SuggestionResponseDTO(
                1L,
                SuggestionTargetType.ROAD,
                10L,
                "Possible branch",
                "I suspect a branch near milestone X.",
                null,
                SuggestionStatus.PENDING,
                "researcher",
                null,
                null,
                LocalDateTime.now(),
                null
        );

        when(suggestionService.submitSuggestion(eq("researcher"), any(SuggestionCreateRequestDTO.class))).thenReturn(response);

        SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO(
                "ROAD",
                10L,
                "Possible branch",
                "I suspect a branch near milestone X.",
                null
        );

        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.submitterUsername").value("researcher"));

        verify(suggestionService).submitSuggestion(eq("researcher"), any(SuggestionCreateRequestDTO.class));
    }

    @Test
    void shouldRejectUnauthenticatedSuggestionSubmit() throws Exception {
        SuggestionCreateRequestDTO request = new SuggestionCreateRequestDTO(
                "SITE",
                2L,
                "Possible sanctuary",
                "Potential sanctuary remains mentioned in local survey.",
                null
        );

        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(suggestionService, never()).submitSuggestion(eq("researcher"), any(SuggestionCreateRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAdminToReviewSuggestion() throws Exception {
        SuggestionResponseDTO reviewed = new SuggestionResponseDTO(
                2L,
                SuggestionTargetType.SITE,
                3L,
                "Settlement correction",
                "Needs review",
                null,
                SuggestionStatus.APPROVED,
                "researcher",
                "Looks valid",
                "admin",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(suggestionService.reviewSuggestion(eq(2L), eq("admin"), any(SuggestionReviewRequestDTO.class))).thenReturn(reviewed);

        SuggestionReviewRequestDTO request = new SuggestionReviewRequestDTO("APPROVED", "Looks valid");

        mockMvc.perform(patch("/api/suggestions/2/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedBy").value("admin"));

        verify(suggestionService).reviewSuggestion(eq(2L), eq("admin"), any(SuggestionReviewRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "researcher", roles = "USER")
    void shouldForbidUserFromReviewingSuggestion() throws Exception {
        SuggestionReviewRequestDTO request = new SuggestionReviewRequestDTO("REJECTED", "Insufficient evidence");

        mockMvc.perform(patch("/api/suggestions/2/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(suggestionService, never()).reviewSuggestion(eq(2L), eq("researcher"), any(SuggestionReviewRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldListPendingSuggestionsForAdmin() throws Exception {
        when(suggestionService.listPendingSuggestions()).thenReturn(List.of());

        mockMvc.perform(get("/api/suggestions/pending"))
                .andExpect(status().isOk());

        verify(suggestionService).listPendingSuggestions();
    }
}

