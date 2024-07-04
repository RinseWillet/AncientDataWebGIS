package com.webgis.ancientdata.site;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final Logger logger = LoggerFactory.getLogger(SiteService.class);

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Iterable<Site> findAll() {
        logger.info("Finding all settlements");
        return siteRepository.findAll();
    }
}
