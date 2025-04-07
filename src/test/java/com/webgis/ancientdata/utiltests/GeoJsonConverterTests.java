package com.webgis.ancientdata.utiltests;

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GeoJsonConverterTests {

    private RandomRoadGenerator roadGenerator;
    private RandomSiteGenerator siteGenerator;

    @InjectMocks
    private GeoJsonConverter geoJsonConverter;

    private Road testRoad;
    private Site testSite;

    @BeforeEach
    public void setup() {
        roadGenerator = new RandomRoadGenerator();
        siteGenerator = new RandomSiteGenerator();

        testRoad = roadGenerator.generateRandomRoad();
        testRoad.setId(1L);

        testSite = siteGenerator.generateRandomSite();
        testSite.setId(1L);
    }

    @Test
    public void shouldConvertSingleRoadToGeoJSON() {
        JSONObject expected = roadGenerator.generateRandomRoadGeoJSON(testRoad);
        JSONObject actual = geoJsonConverter.convertRoad(testRoad);

        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void shouldConvertSingleSiteToGeoJSON() {
        JSONObject expected = siteGenerator.generateRandomSiteGeoJSON(testSite);
        JSONObject actual = geoJsonConverter.convertSite(testSite);

        assertNotNull(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void shouldReturnNullWhenRoadIsNull() {
        assertNull(geoJsonConverter.convertRoad(null));
    }

    @Test
    public void shouldReturnNullWhenSiteIsNull() {
        assertNull(geoJsonConverter.convertSite(null));
    }

    @Test
    public void shouldConvertMultipleSitesToGeoJSON() {
        List<Site> siteList = List.of(testSite);

        JSONObject result = geoJsonConverter.convertSites(siteList);
        JSONArray features = result.getJSONArray("features");

        assertThat(result.getString("type")).isEqualTo("FeatureCollection");
        assertThat(result.getString("name")).isEqualTo("sites");
        assertThat(features.length()).isEqualTo(1);

        JSONObject feature = features.getJSONObject(0);
        assertThat(feature.getString("type")).isEqualTo("Feature");
        assertThat(feature.getJSONObject("properties").getLong("id")).isEqualTo(testSite.getId());

        JSONObject geometry = feature.getJSONObject("geometry");
        assertThat(geometry.getString("type")).isEqualTo("Point");

        Object coordsObj = geometry.get("coordinates");
        assertThat(coordsObj).isInstanceOf(Double[].class);

        Double[] coords = (Double[]) coordsObj;
        assertThat(coords.length).isEqualTo(2);
        assertThat(coords[0]).isEqualTo(testSite.getGeom().getX());
        assertThat(coords[1]).isEqualTo(testSite.getGeom().getY());
    }

    @Test
    public void shouldConvertMultipleRoadsToGeoJSON() {
        List<Road> roadList = List.of(testRoad);

        JSONObject result = geoJsonConverter.convertRoads(roadList);
        JSONArray features = result.getJSONArray("features");

        assertThat(result.getString("type")).isEqualTo("FeatureCollection");
        assertThat(result.getString("name")).isEqualTo("roads");
        assertThat(features.length()).isEqualTo(1);

        JSONObject feature = features.getJSONObject(0);
        assertThat(feature.getString("type")).isEqualTo("Feature");
        assertThat(feature.getJSONObject("properties").getLong("id")).isEqualTo(testRoad.getId());

        JSONObject geometry = feature.getJSONObject("geometry");
        assertThat(geometry.getString("type")).isEqualTo("MultiLineString");

        Object linesObj = geometry.get("coordinates");
        assertThat(linesObj).isInstanceOf(Double[][][].class);

        Double[][][] coords = (Double[][][]) linesObj;
        assertThat(coords.length).isEqualTo(testRoad.getGeom().getNumGeometries());
    }

    @Test
    public void shouldSkipNullRoadsInList() {
        List<Road> roadList = new java.util.ArrayList<>(java.util.Arrays.asList(null, testRoad, null));
        JSONObject result = geoJsonConverter.convertRoads(roadList);
        JSONArray features = result.getJSONArray("features");

        assertThat(features.length()).isEqualTo(1);
        assertThat(features.getJSONObject(0).getJSONObject("properties").getLong("id")).isEqualTo(testRoad.getId());
    }

    @Test
    public void shouldHandleEmptySiteList() {
        JSONObject result = geoJsonConverter.convertSites(List.of());
        JSONArray features = result.getJSONArray("features");

        assertThat(features.length()).isZero();
    }
}
