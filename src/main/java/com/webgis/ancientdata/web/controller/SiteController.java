package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.web.mapper.SiteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    //endpoint to find all sites and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, siteType, status
    @GetMapping("/all")
    public String findAllGeoJson() {
        return siteService.findAllGeoJson().toString();
    }

    //find road by id - info passed as Site object (when present):
    //id, name, siteType, description, status, reference
    @GetMapping("/{id}")
    public String findByIdGeoJson(@PathVariable long id){
        return siteService.findByIdGeoJson(id);
    }

    @GetMapping("/modref/{id}")
    public List<ModernReferenceDTO> findModernReferencesBySiteId(@PathVariable long id) {
        return siteService.findModernReferencesBySiteId(id);
    }

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
}

