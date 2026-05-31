package com.webgis.ancientdata.mediatests;

import com.webgis.ancientdata.application.service.MediaService;
import com.webgis.ancientdata.application.service.MediaStorageService;
import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.dto.MediaUpdateRequest;
import com.webgis.ancientdata.domain.dto.MediaUploadRequest;
import com.webgis.ancientdata.domain.model.MediaAsset;
import com.webgis.ancientdata.domain.model.TargetType;
import com.webgis.ancientdata.domain.model.VisibilityStatus;
import com.webgis.ancientdata.domain.repository.MediaAssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTests {

    @Mock
    private MediaAssetRepository mediaAssetRepository;

    @Mock
    private MediaStorageService mediaStorageService;

    @InjectMocks
    private MediaService mediaService;

    private void setBaseUrl() {
        ReflectionTestUtils.setField(mediaService, "mediaBaseUrl", "http://localhost:8081/api/media/files");
    }

    @Test
    void upload_validFile_savesAssetAndReturnsDto() throws IOException {
        setBaseUrl();
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(mediaStorageService.store(anyString(), anyString(), any())).thenReturn("site/42/uuid.jpg");

        MediaAsset saved = new MediaAsset();
        saved.setId(1L);
        saved.setTargetType(TargetType.SITE);
        saved.setTargetId(42L);
        saved.setStorageKey("site/42/uuid.jpg");
        saved.setMimeType("image/jpeg");
        saved.setFileSizeBytes(3L);
        saved.setVisibilityStatus(VisibilityStatus.APPROVED);
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        when(mediaAssetRepository.save(any())).thenReturn(saved);

        MediaAssetDTO result = mediaService.upload(new MediaUploadRequest(
                file, TargetType.SITE, 42L,
                null, null, null, null, null, false, "admin"));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("SITE", result.targetType());
        assertTrue(result.fullUrl().contains("site/42/uuid.jpg"));

        ArgumentCaptor<MediaAsset> captor = ArgumentCaptor.forClass(MediaAsset.class);
        verify(mediaAssetRepository).save(captor.capture());
        assertEquals(VisibilityStatus.APPROVED, captor.getValue().getVisibilityStatus());
    }

    @Test
    void upload_emptyFile_throwsBadRequest() {
        setBaseUrl();
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{});
        MediaUploadRequest request = new MediaUploadRequest(file, TargetType.SITE, 42L,
                null, null, null, null, null, false, "admin");

        assertThrows(ResponseStatusException.class, () -> mediaService.upload(request));
    }

    @Test
    void upload_invalidMimeType_throwsBadRequest() {
        setBaseUrl();
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});
        MediaUploadRequest request = new MediaUploadRequest(file, TargetType.SITE, 42L,
                null, null, null, null, null, false, "admin");

        assertThrows(ResponseStatusException.class, () -> mediaService.upload(request));
    }

    @Test
    void findByTarget_approvedOnly_filtersCorrectly() {
        setBaseUrl();
        MediaAsset asset = new MediaAsset();
        asset.setId(1L);
        asset.setTargetType(TargetType.SITE);
        asset.setTargetId(42L);
        asset.setStorageKey("site/42/a.jpg");
        asset.setMimeType("image/jpeg");
        asset.setFileSizeBytes(100L);
        asset.setVisibilityStatus(VisibilityStatus.APPROVED);
        asset.setCreatedAt(Instant.now());
        asset.setUpdatedAt(Instant.now());

        when(mediaAssetRepository.findByTargetTypeAndTargetIdAndVisibilityStatus(
                TargetType.SITE, 42L, VisibilityStatus.APPROVED))
                .thenReturn(List.of(asset));

        List<MediaAssetDTO> result = mediaService.findByTarget(TargetType.SITE, 42L, true);
        assertEquals(1, result.size());
        assertEquals("APPROVED", result.getFirst().visibilityStatus());
    }

    @Test
    void delete_existingAsset_removesFileAndRecord() throws IOException {
        setBaseUrl();
        MediaAsset asset = new MediaAsset();
        asset.setId(1L);
        asset.setStorageKey("site/42/a.jpg");

        when(mediaAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mediaService.delete(1L);

        verify(mediaStorageService).delete("site/42/a.jpg");
        verify(mediaAssetRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws404() {
        setBaseUrl();
        when(mediaAssetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> mediaService.delete(99L));
    }

    @Test
    void updateMetadata_existingAsset_updatesFields() {
        setBaseUrl();
        MediaAsset asset = new MediaAsset();
        asset.setId(1L);
        asset.setTargetType(TargetType.SITE);
        asset.setTargetId(42L);
        asset.setStorageKey("site/42/a.jpg");
        asset.setMimeType("image/jpeg");
        asset.setFileSizeBytes(100L);
        asset.setVisibilityStatus(VisibilityStatus.PENDING);
        asset.setCreatedAt(Instant.now());
        asset.setUpdatedAt(Instant.now());

        when(mediaAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(mediaAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MediaAssetDTO result = mediaService.updateMetadata(new MediaUpdateRequest(
                1L, "New caption", null, null, null, null, null, VisibilityStatus.APPROVED));

        assertEquals("New caption", result.caption());
        assertEquals("APPROVED", result.visibilityStatus());
    }
}
