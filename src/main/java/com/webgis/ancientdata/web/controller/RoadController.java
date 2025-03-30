package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.web.mapper.RoadMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/roads")
public class RoadController {

    private final RoadService roadService;

    public RoadController(RoadService roadService) {
        this.roadService = roadService;
    }

    // --- Public endpoints ---

    @GetMapping("/all")
    public ResponseEntity<String> findAllGeoJson() {
        return ResponseEntity.ok(roadService.findAllGeoJson());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findByIdGeoJson(@PathVariable long id) {
        return ResponseEntity.ok(roadService.findByIdGeoJson(id));
    }

    @GetMapping("/modref/{id}")
    public ResponseEntity<List<ModernReferenceDTO>> findModernReferencesByRoadId(@PathVariable long id) {
        return ResponseEntity.ok(roadService.findModernReferencesByRoadId(id));
    }

    @GetMapping("/data/")
    public ResponseEntity<LinkedHashMap<String, Object>> getDashboardData() {
        return ResponseEntity.ok(roadService.getDashBoardData());
    }

    // --- Protected endpoints (USER / ADMIN) ---

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RoadDTO> createRoad(@Valid @RequestBody RoadDTO roadDTO) {
        Road savedRoad = roadService.save(roadDTO);
        return ResponseEntity.ok(RoadMapper.toDto(savedRoad));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoadDTO> updateRoad(@PathVariable long id, @Valid @RequestBody RoadDTO roadDTO) {
        Road updatedRoad = roadService.update(id, roadDTO);
        return ResponseEntity.ok(RoadMapper.toDto(updatedRoad));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoad(@PathVariable long id) {
        roadService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/modern-reference")
    public ResponseEntity<RoadDTO> addModernReferenceToRoad(
            @PathVariable long id,
            @Valid @RequestBody ModernReferenceDTO referenceDTO) {
        Road updatedRoad = roadService.addModernReferenceToRoad(id, referenceDTO);
        return ResponseEntity.ok(RoadMapper.toDto(updatedRoad));
    }
}