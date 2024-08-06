package com.webgis.ancientdata.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    //endpoint to find all sites and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, siteType, status
    @GetMapping("/geojson")
    public String findAllGeoJson() {
        return siteService.findAllGeoJson().toString();
    }

    //find road by id - info passed as Site object (when present):
    //id, name, siteType, comment, status, statusref
    @GetMapping("/{id}")
    public String findByIdGeoJson(@PathVariable long id){
        return siteService.findByIdGeoJson(id);
    }
}

