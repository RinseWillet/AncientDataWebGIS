package com.webgis.ancientdata.mediatests;

import com.webgis.ancientdata.application.service.MediaService;
import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.dto.MediaUpdateRequest;
import com.webgis.ancientdata.domain.dto.MediaUploadRequest;
import com.webgis.ancientdata.domain.model.TargetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    private static final MediaAssetDTO SAMPLE_DTO = new MediaAssetDTO(
            1L, "SITE", 42L,
            "http://localhost:8081/api/media/files/site/42/abc.jpg",
            "A Roman temple", "John", "fieldwork", "CC-BY-4.0",
            LocalDate.of(2025, 6, 15), true, "APPROVED",
            Instant.now(), Instant.now()
    );

    // --- Public GET ---

    @Test
    void listApprovedMedia_publicAccess_returns200() throws Exception {
        when(mediaService.findByTarget(TargetType.SITE, 42L, true))
                .thenReturn(List.of(SAMPLE_DTO));

        mockMvc.perform(get("/api/media")
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].targetType").value("SITE"))
                .andExpect(jsonPath("$[0].fullUrl").exists());
    }

    @Test
    void serveFile_publicAccess_returns200() throws Exception {
        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(mediaService.loadFile("site/42/abc.jpg")).thenReturn(resource);

        mockMvc.perform(get("/api/media/files/site/42/abc.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=86400"));
    }

    @Test
    void serveFile_notFound_returns404() throws Exception {
        when(mediaService.loadFile(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Media asset not found"));

        mockMvc.perform(get("/api/media/files/site/99/missing.jpg"))
                .andExpect(status().isNotFound());
    }

    // --- Admin upload ---

    @Test
    void upload_withoutAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void upload_withUserRole_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void upload_withAdminRole_returns200() throws Exception {
        when(mediaService.upload(any(MediaUploadRequest.class)))
                .thenReturn(SAMPLE_DTO);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/media")
                        .file(file)
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- Admin list (all statuses) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminList_withAdminRole_returns200() throws Exception {
        when(mediaService.findByTarget(TargetType.SITE, 42L, false))
                .thenReturn(List.of(SAMPLE_DTO));

        mockMvc.perform(get("/api/media/admin")
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void adminList_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/media/admin")
                        .param("targetType", "SITE")
                        .param("targetId", "42"))
                .andExpect(status().isUnauthorized());
    }

    // --- Admin PATCH ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMetadata_withAdminRole_returns200() throws Exception {
        MediaAssetDTO updated = new MediaAssetDTO(
                1L, "SITE", 42L,
                "http://localhost:8081/api/media/files/site/42/abc.jpg",
                "Updated caption", "John", "fieldwork", "CC-BY-4.0",
                LocalDate.of(2025, 6, 15), true, "APPROVED",
                Instant.now(), Instant.now()
        );

        when(mediaService.updateMetadata(any(MediaUpdateRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/media/1")
                        .param("caption", "Updated caption")
                        .param("visibilityStatus", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caption").value("Updated caption"));
    }

    // --- Admin DELETE ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_withAdminRole_returns204() throws Exception {
        doNothing().when(mediaService).delete(1L);

        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isUnauthorized());
    }
}

