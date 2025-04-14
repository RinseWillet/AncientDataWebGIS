package com.webgis.ancientdata;

import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.utils.JsonUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

public class RandomSiteGenerator {

    public Site generateRandomSite() {
        Integer pleiadesId = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random point
        double x = RandomUtils.nextDouble(0, 180);
        double y = RandomUtils.nextDouble(0, 90);
        double z = RandomUtils.nextDouble(0, 3000);
        Coordinate coordinate = new Coordinate(x, y, z);
        Coordinate[] coordinates = new Coordinate[]{coordinate};
        CoordinateArraySequence coordinateArraySequence = new CoordinateArraySequence(coordinates);
        GeometryFactory geometryFactory = new GeometryFactory();
        Point geom = new Point(coordinateArraySequence, geometryFactory);

        String province = RandomStringUtils.randomAlphabetic(10);
        String siteType = RandomStringUtils.randomAlphabetic(10);
        String status = RandomStringUtils.randomAlphabetic(10);
        String references = RandomStringUtils.randomAlphabetic(10);
        String description = RandomStringUtils.randomAlphabetic(10);

        return new Site(pleiadesId,
                name,
                geom,
                province,
                siteType,
                status,
                references,
                description);
    }

    public JSONObject generateRandomSiteJSON(Site site) {
        JSONObject siteJSON = new JSONObject();
        siteJSON.put("pleiadesId", site.getPleiadesId());
        siteJSON.put("name", site.getName());
        siteJSON.put("geom", site.getGeom());
        siteJSON.put("province", site.getProvince());
        siteJSON.put("siteType", site.getSiteType());
        siteJSON.put("status", site.getStatus());
        siteJSON.put("references", site.getReferences());
        siteJSON.put("description", site.getDescription());

        return siteJSON;
    }

    private JSONObject generateRandomSiteGeoJONProperties(Site site) {
        JSONObject properties = new JSONObject();
        JsonUtils.enforceLinkedHashMap(properties);
        properties.put("id", site.getId());
        properties.put("pleiadesId", site.getPleiadesId());
        properties.put("name", site.getName());
        properties.put("province", site.getProvince());
        properties.put("siteType", site.getSiteType());
        properties.put("status", site.getStatus());
        properties.put("references", site.getReferences());
        properties.put("description", site.getDescription());
        return properties;
    }

    private JSONObject generateRandomSiteGeoJONGeometry(Site site) {
        JSONObject geometry = new JSONObject();
        JsonUtils.enforceLinkedHashMap(geometry);
        geometry.put("type", "Point");
        Double[] coordsGeoJSON = {site.getGeom().getX(), site.getGeom().getY()};
        geometry.put("coordinates", coordsGeoJSON);
        return geometry;
    }

    private JSONObject generateRandomSiteGeoJSONFeature(JSONObject properties, JSONObject geometry) {
        JSONObject feature = new JSONObject();
        JsonUtils.enforceLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        return feature;
    }

    public JSONObject generateRandomSiteGeoJSON(Site site) {
        //setting up site GeoJSON
        JSONObject geometry = generateRandomSiteGeoJONGeometry(site);
        JSONObject properties = generateRandomSiteGeoJONProperties(site);
        JSONObject feature = generateRandomSiteGeoJSONFeature(properties, geometry);

        JSONObject siteGeoJSON = new JSONObject();
        JsonUtils.enforceLinkedHashMap(siteGeoJSON);

        JSONArray featureArray = new JSONArray();
        featureArray.put(feature);

        siteGeoJSON.put("type", "FeatureCollection");

        siteGeoJSON.put("features", featureArray);

        return siteGeoJSON;
    }

    public JSONObject generateRandomSitesGeoJSON(Site site) {

        //setting up properties (simplified) for sites GeoJSON
        JSONObject properties = new JSONObject();
        JsonUtils.enforceLinkedHashMap(properties);
        properties.put("id", site.getId());
        properties.put("name", site.getName());
        properties.put("siteType", site.getSiteType());
        properties.put("status", site.getStatus());

        JSONObject geometry = generateRandomSiteGeoJONGeometry(site);
        JSONObject feature = generateRandomSiteGeoJSONFeature(properties, geometry);

        JSONObject[] features = new JSONObject[]{feature};

        JSONObject sitesGeoJSON = new JSONObject();
        JsonUtils.enforceLinkedHashMap(sitesGeoJSON);
        sitesGeoJSON.put("type", "FeatureCollection");
        sitesGeoJSON.put("name", "sites");
        sitesGeoJSON.put("features", features);

        return sitesGeoJSON;
    }

    public SiteDTO toDTO(Site site) {
        return new SiteDTO(
                site.getId(),
                site.getPleiadesId(),
                site.getName(),
                site.getGeom().toString(),
                site.getProvince(),
                site.getSiteType(),
                site.getStatus(),
                site.getReferences(),
                site.getDescription());
    }
}
