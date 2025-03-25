package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.ModernReferenceService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.web.mapper.ModernReferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/modernreferences")
public class ModernReferenceController {

    @Autowired
    private ModernReferenceService modernReferenceService;

    @GetMapping("/all")
    public Iterable<ModernReference> findAll () {
        return modernReferenceService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ModernReference> findById(@PathVariable long id){
        return modernReferenceService.findById(id);
    }

    @GetMapping("/road/{id}")
    public String findRoadsByModernReferenceId(@PathVariable long id){
        return modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(id);
    }

    @GetMapping("/site/{id}")
    public String findSitesByModernReferenceId(@PathVariable long id){
        return modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(id);
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
