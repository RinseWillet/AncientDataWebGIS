package com.webgis.ancientdata.sitetests;

//Model
import com.webgis.ancientdata.site.*;

//Java
import java.lang.reflect.Field;
import java.util.*;

import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;

//Test boilerplate libraries
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SiteServiceTests {
    private Site site;
    private List<Site> siteList;
    private Iterable<Site> siteIterable;
    private JSONObject siteGeoJSON;
    private JSONObject sitesGeoJSON;
    private JSONObject feature;
    private JSONObject feature_2;
    private JSONObject[] features;
    private JSONObject properties;
    private JSONObject properties_2;
    private JSONObject geometry;

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

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private GeoJsonConverter geoJsonConverter;

    @Autowired
    @InjectMocks
    private SiteService siteService;

    @BeforeEach
    public void setUp(){
        siteList = new ArrayList<>();

        //generating values for fields
        Integer pleiadesId = RandomUtils.nextInt();
        String name = RandomStringUtils.randomAlphabetic(10);
        String province = RandomStringUtils.randomAlphabetic(10);
        String siteType = RandomStringUtils.randomAlphabetic(10);
        String status = RandomStringUtils.randomAlphabetic(10);
        String statusReference = RandomStringUtils.randomAlphabetic(10);
        String comment = RandomStringUtils.randomAlphabetic(10);

        //creating random point
        Double x = RandomUtils.nextDouble(0, 180);
        Double y = RandomUtils.nextDouble(0, 90);
        Double z = RandomUtils.nextDouble(0, 3000);
        Coordinate coordinate = new Coordinate(x,y,z);
        Coordinate [] coordinates = new Coordinate[]{coordinate};
        CoordinateArraySequence coordinateArraySequence = new CoordinateArraySequence(coordinates);
        GeometryFactory geometryFactory = new GeometryFactory();
        Point geom = new Point(coordinateArraySequence, geometryFactory);

        //setting up site object
        site = new Site(pleiadesId,
                name,
                geom,
                province,
                siteType,
                status,
                statusReference,
                comment);
        siteList.add(site);

        //setting up site GeoJSON
        geometry = new JSONObject();
        setLinkedHashMap(geometry);
        geometry.put("type", "Point");
        Double [] coordsGeoJSON = {site.getGeom().getX(), site.getGeom().getY()};
        geometry.put("coordinates", coordsGeoJSON);
        properties = new JSONObject();
        setLinkedHashMap(properties);
        properties.put("id", site.getId());
        properties.put("pleiadesId", pleiadesId);
        properties.put("name", name);
        properties.put("province", province);
        properties.put("siteType", siteType);
        properties.put("status", status);
        properties.put("statusReference", statusReference);
        properties.put("comment", comment);

        feature = new JSONObject();
        setLinkedHashMap(feature);
        feature.put("type", "Feature");
        feature.put("properties", properties);
        feature.put("geometry", geometry);

        siteGeoJSON = new JSONObject();
        setLinkedHashMap(siteGeoJSON);
        siteGeoJSON.put("type", "FeatureCollection");
        siteGeoJSON.put("features", feature);

        //setting up sites GeoJSON
        properties_2 = new JSONObject();
        setLinkedHashMap(properties_2);
        properties_2.put("id", site.getId());
        properties_2.put("name", name);
        properties_2.put("siteType", siteType);
        properties_2.put("status", status);

        feature_2 = new JSONObject();
        setLinkedHashMap(feature_2);
        feature_2.put("type", "Feature");
        feature_2.put("properties", properties_2);
        feature_2.put("geometry", geometry);

        features = new JSONObject[]{feature_2};

        sitesGeoJSON = new JSONObject();
        setLinkedHashMap(sitesGeoJSON);
        sitesGeoJSON.put("type", "FeatureCollection");
        sitesGeoJSON.put("name", "sites");
        sitesGeoJSON.put("features", features);
    }

    @AfterEach
    public void tearDown() {
        site = null;
        siteList = null;
        siteGeoJSON = null;
        sitesGeoJSON = null;
        feature = null;
        feature_2 = null;
        features = null;
        properties = null;
        properties_2 = null;
        geometry = null;
    }

    @Test
    public void shouldListAllSites() {
        when(siteRepository.findAll()).thenReturn(siteList);

        List<Site> fetchedSites = (List<Site>) siteService.findAll();
        assertEquals(fetchedSites, siteList);

        verify(siteRepository, times(1)).findAll();
    }

    @Test
    public void shouldFindSiteById(){
        when(siteRepository.findById(site.getId())).thenReturn(Optional.ofNullable(site));

        Optional<Site> optionalSite = siteService.findById(site.getId());

        optionalSite.ifPresent(value -> assertThat(optionalSite.get()).isEqualTo(site));

        verify(siteRepository, times(1)).findById(any());
    }

    @Test
    public void shouldFindSiteByIdGeoJSON(){
        when(siteRepository.findById(site.getId())).thenReturn(Optional.ofNullable(site));

        String fetchedSite = siteService.findByIdGeoJson(site.getId());
        assertEquals(fetchedSite, String.valueOf(siteGeoJSON));

        verify(siteRepository, times(1)).findById(any());
    }

    //    findAllGeoJson()
    @Test
    public void shouldListAllSitesGeoJSON(){
        when(siteRepository.findAll()).thenReturn(siteList);

        JSONObject fetchedSitesGeoJSON = siteService.findAllGeoJson();

        assertEquals(String.valueOf(fetchedSitesGeoJSON), String.valueOf(sitesGeoJSON));

        verify(siteRepository, times(1)).findAll();
    }

    //    convertSitetoGeoJson
    @Test
    public void shouldConvertSitetoGeoJSON(){
        when(geoJsonConverter.convertSite(Optional.of(site))).thenReturn(siteGeoJSON);

        JSONObject fetchedsiteGeoJSON = geoJsonConverter.convertSite(Optional.of(site));
        assertEquals(fetchedsiteGeoJSON, siteGeoJSON);

        verify(geoJsonConverter, times(1)).convertSite(any());
    }

    //    convertSitestoGeoJson
    @Test
    public void shouldConvertSitestoGeoJSON() {
        when(geoJsonConverter.convertSites(siteList)).thenReturn(sitesGeoJSON);

        JSONObject fetchedSitesGeoJSON = geoJsonConverter.convertSites(siteList);
        assertEquals(fetchedSitesGeoJSON, sitesGeoJSON);

        verify(geoJsonConverter, times(1)).convertSites(any());
    }
}
