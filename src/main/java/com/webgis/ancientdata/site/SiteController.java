package com.webgis.ancientdata.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @GetMapping
    public Iterable<Site> findAll() {
        return siteService.findAll();
    }
}
