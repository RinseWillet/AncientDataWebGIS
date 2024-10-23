package com.webgis.ancientdata.site;

import com.webgis.ancientdata.modernreference.ModernReferenceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<ModernReferenceDTO> findModernReferencesByRoadId(@PathVariable long id) {
        return siteService.findModernReferencesBySiteId(id);
    }
}

