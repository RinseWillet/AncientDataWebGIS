package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.AncientReferenceService;
import com.webgis.ancientdata.domain.dto.AncientReferenceDTO;
import com.webgis.ancientdata.domain.model.AncientReference;
import com.webgis.ancientdata.web.mapper.AncientReferenceMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/ancientreferences")
public class AncientReferenceController {

    private final AncientReferenceService ancientReferenceService;

    public AncientReferenceController(AncientReferenceService ancientReferenceService) {
        this.ancientReferenceService = ancientReferenceService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AncientReferenceDTO>> findAll() {
        return ResponseEntity.ok(ancientReferenceService.findAllAsDTOs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AncientReferenceDTO> findById(@PathVariable long id) {
        AncientReferenceDTO dto = ancientReferenceService.findByIdDTO(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/site/{id}")
    public ResponseEntity<String> findSitesByAncientReferenceId(@PathVariable long id) {
        return ResponseEntity.ok(ancientReferenceService.findSitesByAncientReferenceIdAsGeoJSON(id));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AncientReferenceDTO> createReference(@Valid @RequestBody AncientReferenceDTO dto) {
        AncientReference created = ancientReferenceService.save(dto);
        return ResponseEntity.ok(AncientReferenceMapper.toDto(created));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AncientReferenceDTO> updateReference(@PathVariable Long id, @Valid @RequestBody AncientReferenceDTO dto) {
        AncientReference updated = ancientReferenceService.update(id, dto);
        return ResponseEntity.ok(AncientReferenceMapper.toDto(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReference(@PathVariable Long id) {
        ancientReferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
