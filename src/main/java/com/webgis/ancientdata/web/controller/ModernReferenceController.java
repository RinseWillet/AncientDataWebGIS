package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.ModernReferenceService;
import com.webgis.ancientdata.domain.model.ModernReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
