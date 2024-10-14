package com.webgis.ancientdata.modernreference;


import com.webgis.ancientdata.road.Road;
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

    @GetMapping("/roads/{id}")
    public Iterable<Road> findRoadsByModernReferenceId(@PathVariable long id) {
        return modernReferenceService.findRoadsByModernReferenceId(id);
    }
}
