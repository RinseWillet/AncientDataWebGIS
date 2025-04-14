package com.webgis.ancientdata.modernreferencetests;

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.ModernReferenceService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        modernReference.setId(RandomUtils.nextLong(1, 1000));

        modernReferenceJSON = new JSONObject();
        modernReferenceJSON.put("shortRef", shortRef);
        modernReferenceJSON.put("fullRef", fullRef);
        modernReferenceJSON.put("URL", URL);

        //roads
        randomRoadGenerator = new RandomRoadGenerator();
        roadList = new ArrayList<>();

        road = randomRoadGenerator.generateRandomRoad();
        road.setId(RandomUtils.nextLong(1, 1000));
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
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.of(modernReference));

        Optional<ModernReference> modernReferenceOptional = modernReferenceService.findById(modernReference.getId());

        modernReferenceOptional.ifPresent(value ->
                assertThat(value).isEqualTo(modernReference)
        );

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldReturnModernReferenceDTOById() {
        when(modernReferenceRepository.findById(1L)).thenReturn(Optional.of(modernReference));

        ModernReferenceDTO result = modernReferenceService.findByIdDTO(1L);

        assertEquals(modernReference.getId(), result.id());
        assertEquals(modernReference.getShortRef(), result.shortRef());
        assertEquals(modernReference.getFullRef(), result.fullRef());
        assertEquals(modernReference.getUrl(), result.url());

        verify(modernReferenceRepository).findById(1L);
    }

    @Test
    public void shouldThrowWhenModernReferenceNotFound() {
        when(modernReferenceRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> modernReferenceService.findByIdDTO(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void shouldFindRoadsByModernReferenceIdAsGeoJSON(){
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        assertEquals(modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(modernReference.getId()), String.valueOf(roadsGeoJSON));

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldFindSitesByModernReferenceIdAsGeoJSON(){
        when(modernReferenceRepository.findById(modernReference.getId())).thenReturn(Optional.ofNullable(modernReference));

        assertEquals(modernReferenceService.findSitesByModernReferenceIdAsGeoJSON(modernReference.getId()), String.valueOf(sitesGeoJSON));

        verify(modernReferenceRepository, times(1)).findById(modernReference.getId());
    }

    @Test
    public void shouldThrowWhenFetchingGeoJSONWithNonexistentId() {
        long nonexistentId = 9999L;
        when(modernReferenceRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                modernReferenceService.findRoadsByModernReferenceIdAsGeoJSON(nonexistentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void shouldSaveModernReference() {
        ModernReferenceDTO dto = new ModernReferenceDTO(
                null,
                modernReference.getShortRef(),
                modernReference.getFullRef(),
                modernReference.getUrl()
        );

        when(modernReferenceRepository.save(any())).thenReturn(modernReference);

        ModernReference saved = modernReferenceService.save(dto);

        assertThat(saved).isNotNull();
        assertEquals(modernReference.getShortRef(), saved.getShortRef());
        assertEquals(modernReference.getFullRef(), saved.getFullRef());
        assertEquals(modernReference.getUrl(), saved.getUrl());

        verify(modernReferenceRepository, times(1)).save(any());
    }

    @Test
    public void shouldUpdateModernReference() {
        Long id = modernReference.getId();

        ModernReferenceDTO dto = new ModernReferenceDTO(
                id,
                "Updated ShortRef",
                "Updated FullRef",
                "https://updated.url"
        );

        // Pretend it already exists in the DB
        when(modernReferenceRepository.findById(id)).thenReturn(Optional.of(modernReference));
        // Assume save returns the updated version
        when(modernReferenceRepository.save(any(ModernReference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModernReference updated = modernReferenceService.update(id, dto);

        assertThat(updated.getShortRef()).isEqualTo("Updated ShortRef");
        assertThat(updated.getFullRef()).isEqualTo("Updated FullRef");
        assertThat(updated.getUrl()).isEqualTo("https://updated.url");

        verify(modernReferenceRepository).findById(id);
        verify(modernReferenceRepository).save(any(ModernReference.class));
    }

    @Test
    public void shouldThrowWhenUpdatingNonexistentModernReference() {
        Long id = 9999L;
        ModernReferenceDTO dto = new ModernReferenceDTO(
                id,
                "Updated ShortRef",
                "Updated FullRef",
                "https://updated.url"
        );

        when(modernReferenceRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> modernReferenceService.update(id, dto)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(modernReferenceRepository).findById(id);
    }

    @Test
    public void shouldDeleteModernReferenceIfExists() {
        Long id = modernReference.getId();

        when(modernReferenceRepository.existsById(id)).thenReturn(true);
        doNothing().when(modernReferenceRepository).deleteById(id);

        modernReferenceService.delete(id);

        verify(modernReferenceRepository, times(1)).existsById(id);
        verify(modernReferenceRepository, times(1)).deleteById(id);
    }

    @Test
    public void shouldThrowWhenDeletingNonexistentModernReference() {
        Long nonexistentId = 9999L;

        when(modernReferenceRepository.existsById(nonexistentId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                modernReferenceService.delete(nonexistentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Modern reference not found", exception.getReason());

        verify(modernReferenceRepository, times(1)).existsById(nonexistentId);
        verify(modernReferenceRepository, never()).deleteById(any());
    }

    @Test
    public void shouldThrowConflictWhenDeletionFailsUnexpectedly() {
        Long id = modernReference.getId();

        when(modernReferenceRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("DB failure")).when(modernReferenceRepository).deleteById(id);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                modernReferenceService.delete(id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Could not delete modern reference", exception.getReason());

        verify(modernReferenceRepository, times(1)).deleteById(id);
    }

    @Test
    public void shouldThrowWhenSavingInvalidDTO() {
        ModernReferenceDTO dto = new ModernReferenceDTO(null, "", "", "");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                modernReferenceService.save(dto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}