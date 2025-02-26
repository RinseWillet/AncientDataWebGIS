package com.webgis.ancientdata.modernreferencetests;

//MVC

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.ModernReferenceService;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModernReferenceServiceTests {

    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private JSONObject modernReferenceJSON;
    private RandomRoadGenerator randomRoadGenerator;
    private Road road;
    private List<Road> roadList;
    private JSONObject roadsGeoJSON;
    private RandomSiteGenerator randomSiteGenerator;
    private Site site;
    private List<Site> siteList;
    private JSONObject sitesGeoJSON;

    @Mock
    private ModernReferenceRepository modernReferenceRepository;

    @Autowired
    @InjectMocks
    private ModernReferenceService modernReferenceService;

    @BeforeEach
    public void setUp() {

        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String URL = RandomStringUtils.randomAlphabetic(100);

        modernReference = new ModernReference(shortRef, fullRef, URL);

        modernReferenceJSON = new JSONObject();
        modernReferenceJSON.put("shortRef", shortRef);
        modernReferenceJSON.put("fullRef", fullRef);
        modernReferenceJSON.put("URL", URL);

        //roads
        randomRoadGenerator = new RandomRoadGenerator();
        roadList = new ArrayList<>();

        road = randomRoadGenerator.generateRandomRoad();
        roadList.add(road);

        roadsGeoJSON = randomRoadGenerator.generateRandomRoadsGeoJSON(road);

        //sites
        randomSiteGenerator = new RandomSiteGenerator();
        siteList = new ArrayList<>();

        site = randomSiteGenerator.generateRandomSite();
        siteList.add(site);

        sitesGeoJSON = randomSiteGenerator.generateRandomSitesGeoJSON(site);

        modernReferenceList = new ArrayList<>();
        modernReference.setRoads(roadList);
        modernReference.setSites(siteList);
        modernReferenceList.add(modernReference);

    }

    @AfterEach
    public void tearDown() {
        modernReference = null;
        modernReferenceList = null;
        modernReferenceJSON = null;
        randomRoadGenerator = null;
        road = null;
        roadList = null;
        roadsGeoJSON = null;
    }

    @Test
    public void shouldFindAllModernReferences(){
        when(modernReferenceRepository.findAll()).thenReturn(modernReferenceList);

        List<ModernReference> fetchedModernReferences = (List<ModernReference>) modernReferenceService.findAll();

        assertEquals(fetchedModernReferences, modernReferenceList);

        verify(modernReferenceRepository, times(1)).findAll();
    }

    @Test
    public void shouldFindModernReferenceById(){
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        Optional<ModernReference> modernReferenceOptional = modernReferenceService.findById(road.getId());

        modernReferenceOptional.ifPresent(value -> assertThat(modernReferenceOptional.get()).isEqualTo(modernReference));

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldFindRoadsByModernReferenceIdAsGeoJSON(){
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        assertEquals(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId()), String.valueOf(roadsGeoJSON));

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldFindSitessByModernReferenceIdAsGeoJSON(){
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        assertEquals(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(modernReference.getId()), String.valueOf(sitesGeoJSON));

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }
}