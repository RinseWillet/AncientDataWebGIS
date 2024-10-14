package com.webgis.ancientdata.road;

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
        return roadService.findAllGeoJson();
    }

    //find road by id - info passed as geojson String object containing (when present):
    //id, name, type, typeDescription, location, description, date, references, historical references
    @GetMapping("/{id}")
    public String findByIdGeoJson(@PathVariable long id){
        return roadService.findByIdGeoJson(id);
    }

    @GetMapping("/all")
    public Iterable<Road> findAll () {
        return roadService.findAll();
    }


}
