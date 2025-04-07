package com.webgis.ancientdata.sitetests;

import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.SiteService;
import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.SiteDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SiteServiceTests {

    private RandomSiteGenerator randomSiteGenerator;
    private Site site;
    private List<Site> siteList;
    private JSONObject siteGeoJSON;
    private JSONObject sitesGeoJSON;
    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ModernReferenceRepository modernReferenceRepository;

    @Mock
    private GeoJsonConverter geoJsonConverter;

    @Autowired
    @InjectMocks
    private SiteService siteService;

    @BeforeEach
    public void setUp() {
        randomSiteGenerator = new RandomSiteGenerator();
        site = randomSiteGenerator.generateRandomSite();
        site.setId(RandomUtils.nextLong(1, 10000));

        siteList = new ArrayList<>();
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
        modernReference = null;
        modernReferenceList = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
        randomSiteGenerator = null;
    }

    @Test
    public void shouldListAllSites() {
        when(siteRepository.findAll()).thenReturn(siteList);

        List<Site> fetchedSites = (List<Site>) siteService.findAll();
        assertEquals(fetchedSites, siteList);

        verify(siteRepository, times(1)).findAll();
    }

    @Test
    public void shouldFindSiteById() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        Optional<Site> optionalSite = siteService.findById(site.getId());

        optionalSite.ifPresent(value -> assertThat(value).isEqualTo(site));

        verify(siteRepository, times(1)).findById(any());
    }

    @Test
    public void shouldFindSiteByIdGeoJSON() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        String fetchedSite = siteService.findByIdGeoJson(site.getId());

        assertEquals(fetchedSite, siteGeoJSON.toString());

        verify(siteRepository, times(1)).findById(any());
    }

    @Test
    public void shouldListAllSitesGeoJSON() {
        when(siteRepository.findAll()).thenReturn(siteList);

        JSONObject fetchedSitesGeoJSON = siteService.findAllGeoJson();

        assertEquals(sitesGeoJSON.toString(), fetchedSitesGeoJSON.toString());

        verify(siteRepository, times(1)).findAll();
    }

    @Test
    public void shouldConvertSiteToGeoJSON() {
        when(geoJsonConverter.convertSite(site)).thenReturn(siteGeoJSON);

        JSONObject fetchedGeoJSON = geoJsonConverter.convertSite(site);
        assertEquals(fetchedGeoJSON, siteGeoJSON);

        verify(geoJsonConverter, times(1)).convertSite(any());
    }

    @Test
    public void shouldConvertSitesToGeoJSON() {
        when(geoJsonConverter.convertSites(siteList)).thenReturn(sitesGeoJSON);

        JSONObject fetchedGeoJSON = geoJsonConverter.convertSites(siteList);
        assertEquals(fetchedGeoJSON, sitesGeoJSON);

        verify(geoJsonConverter, times(1)).convertSites(any());
    }

    @Test
    public void shouldSaveSite() {
        SiteDTO siteDTO = randomSiteGenerator.toDTO(site);
        when(siteRepository.save(any())).thenReturn(site);

        Site saved = siteService.save(siteDTO);

        assertEquals(site.getName(), saved.getName());
        assertEquals(site.getSiteType(), saved.getSiteType());

        verify(siteRepository, times(1)).save(any());
    }

    @Test
    public void shouldUpdateSite() {
        SiteDTO siteDTO = randomSiteGenerator.toDTO(site);
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(siteRepository.save(any())).thenReturn(site);

        Site updated = siteService.update(site.getId(), siteDTO);

        assertEquals(site.getName(), updated.getName());

        verify(siteRepository, times(1)).save(site);
        verify(siteRepository, times(1)).findById(site.getId());
    }

    @Test
    public void shouldFindModernReferencesBySiteId() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));

        assertEquals(siteService.findModernReferencesBySiteId(site.getId()), modernReferenceDTOList);

        verify(siteRepository, times(1)).findById(site.getId());
    }

    @Test
    public void shouldAddModernReferenceToSite() {
        when(siteRepository.findById(site.getId())).thenReturn(Optional.of(site));
        when(modernReferenceRepository.findById(modernReferenceDTO.getId())).thenReturn(Optional.of(modernReference));
        when(siteRepository.save(site)).thenReturn(site);

        Site result = siteService.addModernReferenceToSite(site.getId(), modernReferenceDTO);
        assertEquals(result, site);

        verify(siteRepository, times(1)).findById(site.getId());
        verify(modernReferenceRepository, times(1)).findById(modernReferenceDTO.getId());
        verify(siteRepository, times(1)).save(site);
    }

    @Test
    public void shouldThrowWhenSavingInvalidSite() {
        SiteDTO invalidDTO = new SiteDTO(); // All nulls
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> siteService.save(invalidDTO)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.INVALID_SITE_DATA, exception.getReason());
    }

    @Test
    public void shouldThrowWhenSavingSiteWithInvalidWKT() {
        SiteDTO invalidDTO = randomSiteGenerator.toDTO(site);
        invalidDTO.setGeom("INVALID_WKT");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> siteService.save(invalidDTO)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.INVALID_WKT_FORMAT, exception.getReason());
    }

    @Test
    public void shouldThrowWhenUpdatingNonexistentSite() {
        Long fakeId = 9999L;
        SiteDTO siteDTO = randomSiteGenerator.toDTO(site);

        when(siteRepository.findById(fakeId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> siteService.update(fakeId, siteDTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.SITE_NOT_FOUND, exception.getReason());
    }

    @Test
    public void shouldThrowWhenDeletingNonexistentSite() {
        when(siteRepository.existsById(anyLong())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> siteService.delete(9999L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.SITE_NOT_FOUND, exception.getReason());
    }
}
