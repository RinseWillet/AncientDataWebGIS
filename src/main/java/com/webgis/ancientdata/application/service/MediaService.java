package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.dto.MediaUpdateRequest;
import com.webgis.ancientdata.domain.dto.MediaUploadRequest;
import com.webgis.ancientdata.domain.model.MediaAsset;
import com.webgis.ancientdata.domain.model.TargetType;
import com.webgis.ancientdata.domain.model.VisibilityStatus;
import com.webgis.ancientdata.domain.repository.MediaAssetRepository;
import com.webgis.ancientdata.web.mapper.MediaAssetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class MediaService {

    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaStorageService mediaStorageService;
    private final String mediaBaseUrl;

    public MediaService(
            MediaAssetRepository mediaAssetRepository,
            MediaStorageService mediaStorageService,
            @Value("${media.base-url}") String mediaBaseUrl) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaStorageService = mediaStorageService;
        this.mediaBaseUrl = mediaBaseUrl;
    }

    public MediaAssetDTO upload(MediaUploadRequest request) {

        validateFile(request.file());

        String contentType = request.file().getContentType();
        // contentType is guaranteed non-null here — validateFile rejects null
        assert contentType != null;
        String extension = extensionFromMimeType(contentType);
        String filename = UUID.randomUUID() + extension;
        String targetDir = request.targetType().name().toLowerCase() + "/" + request.targetId();

        String storageKey;
        try {
            storageKey = mediaStorageService.store(targetDir, filename, request.file());
        } catch (IOException e) {
            logger.error("Failed to store media file for {} {}: {}", request.targetType(), request.targetId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.MEDIA_STORAGE_FAILED);
        }

        MediaAsset asset = new MediaAsset();
        asset.setTargetType(request.targetType());
        asset.setTargetId(request.targetId());
        asset.setStorageKey(storageKey);
        asset.setMimeType(contentType);
        asset.setFileSizeBytes(request.file().getSize());
        asset.setCaption(request.caption());
        asset.setAuthor(request.author());
        asset.setSource(request.source());
        asset.setLicense(request.license());
        asset.setDateTaken(request.dateTaken());
        asset.setCover(request.isCover());
        asset.setVisibilityStatus(VisibilityStatus.PENDING);
        asset.setCreatedBy(request.createdBy());

        MediaAsset saved = mediaAssetRepository.save(asset);
        logger.info("Uploaded media asset {} for {} {}", saved.getId(), request.targetType(), request.targetId());

        return MediaAssetMapper.toDto(saved, mediaBaseUrl);
    }

    @Transactional(readOnly = true)
    public List<MediaAssetDTO> findByTarget(TargetType targetType, Long targetId, boolean approvedOnly) {
        List<MediaAsset> assets;
        if (approvedOnly) {
            assets = mediaAssetRepository.findByTargetTypeAndTargetIdAndVisibilityStatus(
                    targetType, targetId, VisibilityStatus.APPROVED);
        } else {
            assets = mediaAssetRepository.findByTargetTypeAndTargetId(targetType, targetId);
        }
        return assets.stream()
                .map(a -> MediaAssetMapper.toDto(a, mediaBaseUrl))
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource loadFile(String storageKey) {
        try {
            return mediaStorageService.load(storageKey);
        } catch (IOException e) {
            logger.warn("Media file not found: {}", storageKey);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MEDIA_NOT_FOUND);
        }
    }

    public MediaAssetDTO updateMetadata(MediaUpdateRequest request) {
        MediaAsset asset = mediaAssetRepository.findById(request.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MEDIA_NOT_FOUND));

        if (request.caption() != null) asset.setCaption(request.caption());
        if (request.author() != null) asset.setAuthor(request.author());
        if (request.source() != null) asset.setSource(request.source());
        if (request.license() != null) asset.setLicense(request.license());
        if (request.dateTaken() != null) asset.setDateTaken(request.dateTaken());
        if (request.isCover() != null) asset.setCover(request.isCover());
        if (request.visibilityStatus() != null) asset.setVisibilityStatus(request.visibilityStatus());

        MediaAsset saved = mediaAssetRepository.save(asset);
        logger.info("Updated media asset {}", request.id());
        return MediaAssetMapper.toDto(saved, mediaBaseUrl);
    }

    public void delete(Long id) {
        MediaAsset asset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.MEDIA_NOT_FOUND));

        try {
            mediaStorageService.delete(asset.getStorageKey());
        } catch (IOException e) {
            logger.warn("Failed to delete media file {}: {}", asset.getStorageKey(), e.getMessage());
        }

        mediaAssetRepository.deleteById(id);
        logger.info("Deleted media asset {}", id);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.MEDIA_FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.MEDIA_FILE_TOO_LARGE);
        }
        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.MEDIA_INVALID_TYPE);
        }
    }

    private String extensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
