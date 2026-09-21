package com.webgis.ancientdata.ancientreferencetests;

import com.webgis.ancientdata.RandomSiteGenerator;
import com.webgis.ancientdata.application.service.AncientReferenceService;
import com.webgis.ancientdata.domain.dto.AncientReferenceDTO;
import com.webgis.ancientdata.domain.model.AncientReference;
import com.webgis.ancientdata.domain.model.Site;
import com.webgis.ancientdata.domain.repository.AncientReferenceRepository;
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
class AncientReferenceServiceTests {

    private AncientReference ancientReference;
    private List<AncientReference> ancientReferenceList;
    private JSONObject sitesGeoJSON;

    @Mock
    private AncientReferenceRepository ancientReferenceRepository;

    @InjectMocks
    private AncientReferenceService ancientReferenceService;

    @BeforeEach
    void setUp() {
        String name = RandomStringUtils.insecure().nextAlphabetic(20);
        String author = RandomStringUtils.insecure().nextAlphabetic(20);
        String work = RandomStringUtils.insecure().nextAlphabetic(20);
        String book = RandomStringUtils.insecure().nextAlphabetic(5);

        ancientReference = new AncientReference(name, author, work, book, null);
        ancientReference.setId(RandomUtils.insecure().randomLong(1, 1000));

        RandomSiteGenerator randomSiteGenerator = new RandomSiteGenerator();
        List<Site> siteList = new ArrayList<>();

        Site site = randomSiteGenerator.generateRandomSite();
        siteList.add(site);

        sitesGeoJSON = randomSiteGenerator.generateRandomSitesGeoJSON(site);

        ancientReferenceList = new ArrayList<>();
        ancientReference.setSites(siteList);
        ancientReferenceList.add(ancientReference);
    }

    @AfterEach
    void tearDown() {
        ancientReference = null;
        ancientReferenceList = null;
        sitesGeoJSON = null;
    }

    @Test
    void shouldFindAllAncientReferences() {
        when(ancientReferenceRepository.findAll()).thenReturn(ancientReferenceList);

        List<AncientReference> fetched = (List<AncientReference>) ancientReferenceService.findAll();

        assertEquals(fetched, ancientReferenceList);

        verify(ancientReferenceRepository, times(1)).findAll();
    }

    @Test
    void shouldFindAncientReferenceById() {
        when(ancientReferenceRepository.findById(ancientReference.getId())).thenReturn(Optional.of(ancientReference));

        Optional<AncientReference> found = ancientReferenceService.findById(ancientReference.getId());

        found.ifPresent(value -> assertThat(value).isEqualTo(ancientReference));

        verify(ancientReferenceRepository, times(1)).findById(ancientReference.getId());
    }

    @Test
    void shouldReturnAncientReferenceDTOById() {
        when(ancientReferenceRepository.findById(1L)).thenReturn(Optional.of(ancientReference));

        AncientReferenceDTO result = ancientReferenceService.findByIdDTO(1L);

        assertEquals(ancientReference.getId(), result.id());
        assertEquals(ancientReference.getName(), result.name());
        assertEquals(ancientReference.getAuthor(), result.author());
        assertEquals(ancientReference.getWork(), result.work());
        assertEquals(ancientReference.getBook(), result.book());

        verify(ancientReferenceRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenAncientReferenceNotFound() {
        when(ancientReferenceRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ancientReferenceService.findByIdDTO(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldFindSitesByAncientReferenceIdAsGeoJSON() {
        when(ancientReferenceRepository.findById(ancientReference.getId())).thenReturn(Optional.ofNullable(ancientReference));

        assertEquals(ancientReferenceService.findSitesByAncientReferenceIdAsGeoJSON(ancientReference.getId()), String.valueOf(sitesGeoJSON));

        verify(ancientReferenceRepository, times(1)).findById(ancientReference.getId());
    }

    @Test
    void shouldThrowWhenFetchingGeoJSONWithNonexistentId() {
        long nonexistentId = 9999L;
        when(ancientReferenceRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ancientReferenceService.findSitesByAncientReferenceIdAsGeoJSON(nonexistentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldSaveAncientReference() {
        AncientReferenceDTO dto = new AncientReferenceDTO(
                null,
                ancientReference.getName(),
                ancientReference.getAuthor(),
                ancientReference.getWork(),
                ancientReference.getBook(),
                ancientReference.getPage()
        );

        when(ancientReferenceRepository.save(any())).thenReturn(ancientReference);

        AncientReference saved = ancientReferenceService.save(dto);

        assertThat(saved).isNotNull();
        assertEquals(ancientReference.getName(), saved.getName());
        assertEquals(ancientReference.getAuthor(), saved.getAuthor());
        assertEquals(ancientReference.getWork(), saved.getWork());

        verify(ancientReferenceRepository, times(1)).save(any());
    }

    @Test
    void shouldUpdateAncientReference() {
        Long id = ancientReference.getId();

        AncientReferenceDTO dto = new AncientReferenceDTO(
                id,
                "Updated Name",
                "Updated Author",
                "Updated Work",
                "II",
                null
        );

        when(ancientReferenceRepository.findById(id)).thenReturn(Optional.of(ancientReference));
        when(ancientReferenceRepository.save(any(AncientReference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AncientReference updated = ancientReferenceService.update(id, dto);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getAuthor()).isEqualTo("Updated Author");
        assertThat(updated.getWork()).isEqualTo("Updated Work");

        verify(ancientReferenceRepository).findById(id);
        verify(ancientReferenceRepository).save(any(AncientReference.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentAncientReference() {
        Long id = 9999L;
        AncientReferenceDTO dto = new AncientReferenceDTO(id, "Updated Name", "Updated Author", "Updated Work", "II", null);

        when(ancientReferenceRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ancientReferenceService.update(id, dto)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(ancientReferenceRepository).findById(id);
    }

    @Test
    void shouldDeleteAncientReferenceIfExists() {
        Long id = ancientReference.getId();

        when(ancientReferenceRepository.existsById(id)).thenReturn(true);
        doNothing().when(ancientReferenceRepository).deleteById(id);

        ancientReferenceService.delete(id);

        verify(ancientReferenceRepository, times(1)).existsById(id);
        verify(ancientReferenceRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowWhenDeletingNonexistentAncientReference() {
        Long nonexistentId = 9999L;

        when(ancientReferenceRepository.existsById(nonexistentId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ancientReferenceService.delete(nonexistentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Ancient reference not found", exception.getReason());

        verify(ancientReferenceRepository, times(1)).existsById(nonexistentId);
        verify(ancientReferenceRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowConflictWhenDeletionFailsUnexpectedly() {
        Long id = ancientReference.getId();

        when(ancientReferenceRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("DB failure")).when(ancientReferenceRepository).deleteById(id);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ancientReferenceService.delete(id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Could not delete ancient reference", exception.getReason());

        verify(ancientReferenceRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowWhenSavingInvalidDTO() {
        AncientReferenceDTO dto = new AncientReferenceDTO(null, "", "", "", "", null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                ancientReferenceService.save(dto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
