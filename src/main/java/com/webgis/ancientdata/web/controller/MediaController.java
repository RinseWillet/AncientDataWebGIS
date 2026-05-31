package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.MediaService;
import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.dto.MediaUpdateRequest;
import com.webgis.ancientdata.domain.dto.MediaUploadRequest;
import com.webgis.ancientdata.domain.model.TargetType;
import com.webgis.ancientdata.domain.model.VisibilityStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaAssetDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetType") TargetType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "license", required = false) String license,
            @RequestParam(value = "dateTaken", required = false) LocalDate dateTaken,
            @RequestParam(value = "isCover", defaultValue = "false") boolean isCover,
            Authentication authentication) {

        String createdBy = authentication != null ? authentication.getName() : null;

        MediaAssetDTO dto = mediaService.upload(new MediaUploadRequest(
                file, targetType, targetId,
                caption, author, source, license, dateTaken, isCover,
                createdBy));

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<MediaAssetDTO>> findByTarget(
            @RequestParam("targetType") TargetType targetType,
            @RequestParam("targetId") Long targetId) {
        List<MediaAssetDTO> assets = mediaService.findByTarget(targetType, targetId, true);
        return ResponseEntity.ok(assets);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<MediaAssetDTO>> findByTargetAdmin(
            @RequestParam("targetType") TargetType targetType,
            @RequestParam("targetId") Long targetId) {
        List<MediaAssetDTO> assets = mediaService.findByTarget(targetType, targetId, false);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile(jakarta.servlet.http.HttpServletRequest request) {

        // Extract the storage key from the path after /api/media/files/
        String fullPath = request.getRequestURI();
        String storageKey = fullPath.substring(fullPath.indexOf("/files/") + "/files/".length());

        Resource resource = mediaService.loadFile(storageKey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(MediaType.parseMediaType(detectContentType(storageKey)))
                .body(resource);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MediaAssetDTO> updateMetadata(
            @PathVariable Long id,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "license", required = false) String license,
            @RequestParam(value = "dateTaken", required = false) LocalDate dateTaken,
            @RequestParam(value = "isCover", required = false) Boolean isCover,
            @RequestParam(value = "visibilityStatus", required = false) VisibilityStatus visibilityStatus) {

        MediaAssetDTO dto = mediaService.updateMetadata(new MediaUpdateRequest(
                id, caption, author, source, license, dateTaken, isCover, visibilityStatus));
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String detectContentType(String storageKey) {
        String lower = storageKey.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
