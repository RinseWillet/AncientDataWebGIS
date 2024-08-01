package com.webgis.ancientdata.road;

import com.webgis.ancientdata.site.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/roads")
public class RoadController {

    @Autowired
    private RoadService roadService;

    //endpoint to find all roads and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, type, date, geometrie(s)
    @GetMapping("/geojson")
    public String findAllGeoJson(){
        return roadService.finaAllGeoJson().toString();
    }
}
