package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.web.mapper.SiteMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    // Public Endpoints

    @GetMapping("/all")
    public ResponseEntity<String> findAllGeoJson() {
        return ResponseEntity.ok(siteService.findAllGeoJson().toString());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findByIdGeoJson(@PathVariable long id) {
        return ResponseEntity.ok(siteService.findByIdGeoJson(id));
    }

    @GetMapping("/modref/{id}")
    public ResponseEntity<List<ModernReferenceDTO>> findModernReferencesBySiteId(@PathVariable long id) {
        return ResponseEntity.ok(siteService.findModernReferencesBySiteId(id));
    }

    // Protected Endpoints

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SiteDTO> createSite(@Valid @RequestBody SiteDTO siteDTO) {
        Site site = siteService.save(siteDTO);
        return ResponseEntity.ok(SiteMapper.toDto(site));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SiteDTO> updateSite(@PathVariable Long id, @Valid @RequestBody SiteDTO siteDTO) {
        Site site = siteService.update(id, siteDTO);
        return ResponseEntity.ok(SiteMapper.toDto(site));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/modern-reference")
    public ResponseEntity<SiteDTO> addModernReferenceToSite(
            @PathVariable Long id,
            @Valid @RequestBody ModernReferenceDTO referenceDTO
    ) {
        Site updatedSite = siteService.addModernReferenceToSite(id, referenceDTO);
        return ResponseEntity.ok(SiteMapper.toDto(updatedSite));
    }
}