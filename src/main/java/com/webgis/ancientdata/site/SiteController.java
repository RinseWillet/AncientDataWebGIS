package com.webgis.ancientdata.site;

import com.fasterxml.jackson.core.StreamWriteConstraints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    //endpoint to find all sites and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, siteType, comment, status, statusref
    @GetMapping("/geojson")
    public String findAllGeoJson() {
        return siteService.findAllGeoJson().toString();
    }
    }
}
