package com.webgis.ancientdata.web.controller;

import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/roads")
public class RoadController {

    @Autowired
    private RoadService roadService;

    //endpoint to find all roads and return GeoJSON String for mapping in the front-end
    //info passed (when present): id, name, type, date, geometrie(s)
    @GetMapping("/all")
    public String findAllGeoJson(){
        return roadService.findAllGeoJson();
    }

    //find road by id - info passed as geojson String object containing (when present):
    //id, name, type, typeDescription, location, description, date, references, historical references
    @GetMapping("/{id}")
    public String findByIdGeoJson(@PathVariable long id){
        return roadService.findByIdGeoJson(id);
    }

    @GetMapping("/modref/{id}")
    public List<ModernReferenceDTO> findModernReferencesByRoadId(@PathVariable long id) {
        return roadService.findModernReferencesByRoadId(id);
    }

    //this endpoint provides all the basic data on Roads in the database: The number of roads (total and per category),
    //the amount of roads with ditches, the width of roads (min-max), TODO: length of roads per category
    @GetMapping("/data/")
    public LinkedHashMap getDashboardData(){
        return roadService.getDashBoardData();
    }
}
