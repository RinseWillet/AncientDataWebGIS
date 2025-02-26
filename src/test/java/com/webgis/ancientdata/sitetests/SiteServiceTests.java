package com.webgis.ancientdata.sitetests;

//Model

import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import com.webgis.ancientdata.utils.GeoJsonConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SiteServiceTests {

    private RandomSiteGenerator randomSiteGenerator;
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
    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;

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

        randomSiteGenerator = new RandomSiteGenerator();
        siteList = new ArrayList<>();
        site = randomSiteGenerator.generateRandomSite();
        siteList.add(site);
        siteGeoJSON = randomSiteGenerator.generateRandomSiteGeoJSON(site);
        sitesGeoJSON = randomSiteGenerator.generateRandomSitesGeoJSON(site);

        modernReferenceDTOList = new ArrayList<>();
        modernReferenceList = new ArrayList<>();

        Long id = RandomUtils.nextLong();
        String shortRef = RandomStringUtils.randomAlphabetic(100);
        String fullRef = RandomStringUtils.randomAlphabetic(100);
        String URL = RandomStringUtils.randomAlphabetic(100);

        modernReference = new ModernReference(shortRef, fullRef, URL);
        modernReference.setId(id);
        modernReferenceList.add(modernReference);
        modernReferenceDTO = new ModernReferenceDTO(id, shortRef, fullRef, URL);
        modernReferenceDTOList.add(modernReferenceDTO);

        site.setModernReferenceList(modernReferenceList);
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

        System.out.println("fetched");
        System.out.println(fetchedSitesGeoJSON);
        System.out.println("random");
        System.out.println(sitesGeoJSON);
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

    @Test
    public void shouldSaveSite(){
        when(siteRepository.save(any())).thenReturn(site);

        assertEquals(siteService.save(site), site);

        verify(siteRepository, times(1)).save(any());
    }

    @Test
    public void shouldUpdateSite(){
        when(siteRepository.findById(site.getId())).thenReturn(Optional.ofNullable(site));
        when(siteRepository.save(any())).thenReturn(site);

        assertEquals(siteService.update(site.getId(), site), site);

        verify(siteRepository, times(1)).save(site);
        verify(siteRepository, times(1)).findById(site.getId());
    }

    @Test
    public void shouldFindModernReferencesBySiteId() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.ofNullable(site));

        assertEquals(siteService.findModernReferencesBySiteId(site.getId()), modernReferenceDTOList);

        verify(siteRepository, times(1)).findById(site.getId());
    }

    @Test
    public void shouldAddModernReferenceToRoad(){
        when(siteRepository.findById(site.getId())).thenReturn(Optional.ofNullable(site));
        when(siteRepository.save(site)).thenReturn(site);

        assertEquals(siteService.addModernReferenceToSite(site.getId(), modernReferenceDTO), site);

        verify(siteRepository, times(1)).findById(site.getId());
        verify(siteRepository, times(1)).save(site);
    }
}
