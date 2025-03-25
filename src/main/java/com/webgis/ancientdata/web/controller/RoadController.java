package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.web.mapper.RoadMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/roads")
public class RoadController {

    @Autowired
    private RoadService roadService;


    //public endpoints

    //endpoint to find all roads and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, type, date, geometrie(s)
    @GetMapping("/all")
    public ResponseEntity<String> findAllGeoJson(){
        return ResponseEntity.ok(roadService.findAllGeoJson());
    }

    //find road by id - info passed as geojson String object containing (when present):
    //id, name, type, typeDescription, location, description, date, references, historical references
    @GetMapping("/{id}")
    public ResponseEntity<String> findByIdGeoJson(@PathVariable long id){
        return ResponseEntity.ok(roadService.findByIdGeoJson(id));
    }

    @GetMapping("/modref/{id}")
    public ResponseEntity<List<ModernReferenceDTO>> findModernReferencesByRoadId(@PathVariable long id) {
        return ResponseEntity.ok(roadService.findModernReferencesByRoadId(id));
    }

    //this endpoint provides all the basic data on Roads in the database: The number of roads (total and per category),
    //the amount of roads with ditches, the width of roads (min-max), TODO: length of roads per category
    @GetMapping("/data/")
    public ResponseEntity<LinkedHashMap> getDashboardData(){
        return ResponseEntity.ok(roadService.getDashBoardData());
    }

    //protected endpoints - USER / ADMIN roles only
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RoadDTO> createRoad(@RequestBody RoadDTO roadDTO) {
        Road road = roadService.save(roadDTO);
        return ResponseEntity.ok(RoadMapper.toDto(road));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoadDTO> updateRoad(@PathVariable long id, @RequestBody RoadDTO roadDTO) {
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
    public ResponseEntity<Road> addModernReference(@PathVariable long id, @RequestBody ModernReferenceDTO referenceDTO) {
        Road updatedRoad = roadService.addModernReferenceToRoad(id, referenceDTO);
        return ResponseEntity.ok(updatedRoad);
    }
}
