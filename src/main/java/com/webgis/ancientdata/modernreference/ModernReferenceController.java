package com.webgis.ancientdata.modernreference;


import com.webgis.ancientdata.road.Road;
import com.webgis.ancientdata.road.RoadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api/modernreferences")
public class ModernReferenceController {

    @Autowired
    private ModernReferenceService modernReferenceService;

    @Autowired
    private RoadService roadService;

    @GetMapping("/all")
    public Iterable<ModernReference> findAll () {
        return modernReferenceService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ModernReference> findById(@PathVariable long id){
        return modernReferenceService.findById(id);
    }

    @GetMapping("/roads/{id}")
    public Iterable<Road> findRoadsByModernReferenceId(@PathVariable long id) {
        return modernReferenceService.findRoadsByModernReferenceId(id);
    }

    @GetMapping("/getModernReference/{modrefId}")
    public String getModernReference(@PathVariable long modrefId) {
        System.out.println("\nFetch Employee and Project details.");

        // get Employee details
        ModernReference modernReference = this.modernReferenceService.findById(modrefId).get();

        System.out.println("\nModernReference details :: " + modernReference.toString() + "\n");
        System.out.println("\nRoad details :: " + modernReference.getRoadSet() + "\n");

        System.out.println("Done!!!\n");

        return "ModernRef fetched successfully!!!";
    }
}
