package com.webgis.ancientdata;

import com.webgis.ancientdata.site.Site;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class RandomSiteGenerator {

    private void setLinkedHashMap(JSONObject jsonObject) {
        try {
            Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("error");
        }
    }

    public Site generateRandomSite(){
        Integer pleiadesId = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);

        //creating random point
        Double x = RandomUtils.nextDouble(0, 180);
        Double y = RandomUtils.nextDouble(0, 90);
        Double z = RandomUtils.nextDouble(0, 3000);
        Coordinate coordinate = new Coordinate(x,y,z);
        Coordinate [] coordinates = new Coordinate[]{coordinate};
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

    public JSONObject generateRandomSiteJSON (Site site){
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

    private JSONObject generateRandomSiteGeoJONProperties (Site site) {
        JSONObject properties = new JSONObject();
        setLinkedHashMap(properties);
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

    private JSONObject generateRandomSiteGeoJONGeometry (Site site) {
        JSONObject geometry = new JSONObject();
        setLinkedHashMap(geometry);
        geometry.put("type", "Point");
        Double [] coordsGeoJSON = {site.getGeom().getX(), site.getGeom().getY()};
        geometry.put("coordinates", coordsGeoJSON);
        return geometry;
    }

    private JSONObject generateRandomSiteGeoJSONFeature (JSONObject properties, JSONObject geometry) {
        JSONObject feature = new JSONObject();
        setLinkedHashMap(feature);
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
        setLinkedHashMap(siteGeoJSON);
        siteGeoJSON.put("type", "FeatureCollection");
        siteGeoJSON.put("features", feature);

        return siteGeoJSON;
    }

    public JSONObject generateRandomSitesGeoJSON(Site site) {

        //setting up properties (simplified) for sites GeoJSON
        JSONObject properties = new JSONObject();
        setLinkedHashMap(properties);
        properties.put("id", site.getId());
        properties.put("name", site.getName());
        properties.put("siteType", site.getSiteType());
        properties.put("status", site.getStatus());

        JSONObject geometry = generateRandomSiteGeoJONGeometry(site);
        JSONObject feature = generateRandomSiteGeoJSONFeature(properties, geometry);

        JSONObject [] features = new JSONObject[]{feature};

        JSONObject sitesGeoJSON = new JSONObject();
        setLinkedHashMap(sitesGeoJSON);
        sitesGeoJSON.put("type", "FeatureCollection");
        sitesGeoJSON.put("name", "sites");
        sitesGeoJSON.put("features", features);

        return sitesGeoJSON;
    }
}
