package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
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

    @GetMapping("/{id}/modern-references")
    public ResponseEntity<List<ModernReferenceDTO>> findModernReferencesBySiteId(@PathVariable long id) {
        return ResponseEntity.ok(siteService.findModernReferencesBySiteId(id));
    }

    // Protected Endpoints

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SiteDTO> createSite(@Valid @RequestBody SiteDTO siteDTO) {
        SiteDTO createdSite = siteService.save(siteDTO);
        return ResponseEntity.ok(createdSite);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SiteDTO> updateSite(@PathVariable Long id, @Valid @RequestBody SiteDTO siteDTO) {
        SiteDTO updatedSite = siteService.update(id, siteDTO);
        return ResponseEntity.ok(updatedSite);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        siteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/modern-references/{refId}")
    public ResponseEntity<SiteDTO> addModernReferenceToSite(
            @PathVariable Long id,
            @PathVariable Long refId
    ) {
        SiteDTO updatedSite = siteService.addModernReferenceToSite(id, refId);
        return ResponseEntity.ok(updatedSite);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/modern-references/{refId}")
    public ResponseEntity<SiteDTO> removeModernReferenceFromSite(
            @PathVariable Long id,
            @PathVariable Long refId
    ) {
        SiteDTO updatedSite = siteService.removeModernReferenceFromSite(id, refId);
        return ResponseEntity.ok(updatedSite);
    }
}