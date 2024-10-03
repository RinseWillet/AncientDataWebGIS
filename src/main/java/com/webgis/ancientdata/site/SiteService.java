package com.webgis.ancientdata.site;

import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final Logger logger = LoggerFactory.getLogger(SiteService.class);

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public Iterable<Site> findAll() {
        logger.info("Finding all sites");
        return siteRepository.findAll();
    }

    public Optional<Site> findById(long id) {
        logger.info("find site id : {}", id);
        return siteRepository.findById(id);
    }

    public String findByIdGeoJson(long id) throws NoSuchElementException {
        try {
            GeoJsonConverter geoJsonConverter = new GeoJsonConverter();
            return geoJsonConverter.convertSite(findById(id)).toString();
        } catch (Exception e) {
            logger.warn("site " + id + " not found ");
            return "not found";
        }
    }

    public JSONObject findAllGeoJson() {

        //create GeoJsonBuilderService object to convert incoming Iterable to GeoJson
        GeoJsonConverter geoJsonConverter = new GeoJsonConverter();

        return geoJsonConverter.convertSites(findAll());
    }

    //    public Site addSite (Site site){
//        try {
//            logger.info("new site registered : {}", site.getName());
//            return siteRepository.save(site);
//        } catch (Exception e) {
//            logger.info(String.valueOf(e));
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
//        }
//    }
//
//    public Optional<Site> changeSite (Site site) {
//        Optional<Site> siteOptional = findById(site.getId());
//        try {
//            if(siteOptional.isPresent()){
//                logger.debug("updated site {}", site.getName());
//                siteRepository.save(site);
//                return Optional.of(site);
//            } else {
//                logger.debug("site not found");
//                return Optional.empty();
//            }
//        } catch (NullPointerException e) {
//            if (siteOptional.isEmpty()){
//                logger.info("settlement not found");
//            }
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "settlement not found", e);
//        } catch (Exception e) {
//            logger.info(String.valueOf(e));
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "error", e);
//        }
//    }

//    public ArrayList<SiteMapDTO> overviewMapping() {
//        ArrayList<SiteMapDTO> siteMapDTOArrayList = new ArrayList<>();
//
//        Iterable<Site> siteIterable = findAll();
//
//        for (Site site : siteIterable) {
//            SiteMapDTO siteMapDTO = convertSite(site);
//            siteMapDTOArrayList.add(siteMapDTO);
//        }
//        // logging info finding by id
//        logger.info("mapping settlements");
//
//        return siteMapDTOArrayList;
//    }
//
//    private SiteMapDTO convertSite(Site site) {
//        long id = site.getId();
//        String name = site.getName();
//        Point geom = site.getGeom();
//
//        return new SiteMapDTO(id, name, geom);
//    }

}
