package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.ModernReferenceService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.web.mapper.ModernReferenceMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/modernreferences")
public class ModernReferenceController {

    @Autowired
    private ModernReferenceService modernReferenceService;

    @GetMapping("/all")
    public ResponseEntity<List<ModernReferenceDTO>> findAll() {
        return ResponseEntity.ok(modernReferenceService.findAllAsDTOs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModernReferenceDTO> findById(@PathVariable long id) {
        ModernReferenceDTO dto = modernReferenceService.findByIdDTO(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/road/{id}")
    public ResponseEntity<String> findRoadsByModernReferenceId(@PathVariable long id) {
        return ResponseEntity.ok(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(id));
    }

    @GetMapping("/site/{id}")
    public ResponseEntity<String> findSitesByModernReferenceId(@PathVariable long id) {
        return ResponseEntity.ok(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(id));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ModernReferenceDTO> createReference(@Valid @RequestBody ModernReferenceDTO dto) {
        ModernReference created = modernReferenceService.save(dto);
        return ResponseEntity.ok(ModernReferenceMapper.toDto(created));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ModernReferenceDTO> updateReference(@PathVariable Long id, @Valid @RequestBody ModernReferenceDTO dto) {
        ModernReference updated = modernReferenceService.update(id, dto);
        return ResponseEntity.ok(ModernReferenceMapper.toDto(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReference(@PathVariable Long id) {
        modernReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
