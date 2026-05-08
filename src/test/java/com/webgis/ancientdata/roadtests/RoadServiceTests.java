package com.webgis.ancientdata.roadtests;

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.dto.RoadDTO;
import com.webgis.ancientdata.domain.dto.RoadDashboardDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
import com.webgis.ancientdata.domain.repository.ModernReferenceRepository;
import com.webgis.ancientdata.domain.repository.RoadRepository;
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
class RoadServiceTests {

    private Road road;
    private List<Road> roadList;
    private JSONObject roadGeoJSON;
    private JSONObject roadsGeoJSON;
    private ModernReference modernReference;
    private List<ModernReference> modernReferenceList;
    private ModernReferenceDTO modernReferenceDTO;
    private List<ModernReferenceDTO> modernReferenceDTOList;
    private RandomRoadGenerator randomRoadGenerator;

    @Mock
    private ModernReferenceRepository modernReferenceRepository;

    @Mock
    private RoadRepository roadRepository;

    @Mock
    private GeoJsonConverter geoJsonConverter;

    @InjectMocks
    private RoadService roadService;

    @BeforeEach
    void setUp() {

        roadList = new ArrayList<>();
        randomRoadGenerator = new RandomRoadGenerator();

        road = randomRoadGenerator.generateRandomRoad();
        road.setId(RandomUtils.insecure().randomLong(1, 10000));
        roadList.add(road);

        //setting up GeoJSON
        //properties
        roadGeoJSON = randomRoadGenerator.generateRandomRoadGeoJSON(road);

        roadsGeoJSON = randomRoadGenerator.generateRandomRoadsGeoJSON(road);

        modernReferenceDTOList = new ArrayList<>();
        modernReferenceList = new ArrayList<>();

        Long id = RandomUtils.insecure().randomLong();
        String shortRef = RandomStringUtils.insecure().nextAlphabetic(100);
        String fullRef = RandomStringUtils.insecure().nextAlphabetic(100);
        String url = RandomStringUtils.insecure().nextAlphabetic(100);

        modernReference = new ModernReference(shortRef, fullRef, url);
        modernReference.setId(id);
        modernReferenceList.add(modernReference);
        modernReferenceDTO = new ModernReferenceDTO(id, shortRef, fullRef, url);
        modernReferenceDTOList.add(modernReferenceDTO);

        road.setModernReferenceList(modernReferenceList);
    }

    @AfterEach
    void tearDown() {
        road = null;
        roadList = null;
        roadGeoJSON = null;
        roadsGeoJSON = null;
        modernReference = null;
        modernReferenceList = null;
        modernReferenceDTO = null;
        modernReferenceDTOList = null;
        randomRoadGenerator = null;
    }

    @Test
    void shouldListAllRoads() {
        when(roadRepository.findAll()).thenReturn(roadList);

        List<Road> fetchedRoads = (List<Road>) roadService.findAll();
        assertEquals(fetchedRoads, roadList);

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    void shouldFindRoadById() {
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        Optional<Road> optionalRoad = roadService.findById(road.getId());

        assertThat(optionalRoad).contains(road);

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    void shouldFindRoadByIdGeoJSON() {
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        String fetchedRoad = roadService.findByIdGeoJson(road.getId());

        assertEquals(fetchedRoad, String.valueOf(roadGeoJSON));

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    void shouldListAllRoadsGeoJSON() {
        when(roadRepository.findAll()).thenReturn(roadList);

        String fetchedRoadsGeoJSON = roadService.findAllGeoJson();

        assertEquals(String.valueOf(fetchedRoadsGeoJSON), String.valueOf(roadsGeoJSON));

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    void shouldConvertRoadtoGeoJSON() {
        when(geoJsonConverter.convertRoad(road)).thenReturn(roadGeoJSON);

        JSONObject fetchedroadGeoJSON = geoJsonConverter.convertRoad(road);
        assertEquals(fetchedroadGeoJSON, roadGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoad(any());
    }

    @Test
    void shouldConvertRoadstoGeoJSON() {
        when(geoJsonConverter.convertRoads(roadList)).thenReturn(roadsGeoJSON);

        JSONObject fetchedRoadsGeoJSON = geoJsonConverter.convertRoads(roadList);
        assertEquals(fetchedRoadsGeoJSON, roadsGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoads(any());
    }

    @Test
    void shouldSaveRoad() {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        when(roadRepository.save(any())).thenReturn(road);

        Road saved = roadService.save(roadDTO);

        assertEquals(road.getName(), saved.getName());
        assertEquals(road.getType(), saved.getType());

        verify(roadRepository, times(1)).save(any());
    }

    @Test
    void shouldUpdateRoad() {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        when(roadRepository.findById(road.getId())).thenReturn(Optional.of(road));
        when(roadRepository.save(any())).thenReturn(road);

        Road updated = roadService.update(road.getId(), roadDTO);

        assertEquals(road.getName(), updated.getName());

        verify(roadRepository, times(1)).save(road);
        verify(roadRepository, times(1)).findById(road.getId());
    }

    @Test
    void shouldFindModernReferencesByRoadId() {
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        assertEquals(roadService.findModernReferencesByRoadId(road.getId()), modernReferenceDTOList);

        verify(roadRepository, times(1)).findById(road.getId());
    }

    @Test
    void shouldAddModernReferenceToRoad() {
        when(roadRepository.findById(road.getId())).thenReturn(Optional.of(road));
        when(modernReferenceRepository.findById(modernReferenceDTO.id())).thenReturn(Optional.of(modernReference));
        when(roadRepository.save(road)).thenReturn(road);

        assertEquals(roadService.addModernReferenceToRoad(road.getId(), modernReferenceDTO), road);

        verify(roadRepository, times(1)).findById(road.getId());
        verify(modernReferenceRepository, times(1)).findById(modernReferenceDTO.id());
        verify(roadRepository, times(1)).save(road);
    }

    @Test
    void shouldThrowBadRequestWhenSavingRoadWithMissingFields() {
        RoadDTO invalidDTO = new RoadDTO(
                0L,
                123,               // cat_nr
                "Via Error",       // name
                null,              // geom (missing on purpose)
                null,              // type (missing on purpose)
                null,
                null,
                null,
                null,
                null
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> roadService.save(invalidDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertThat(exception.getReason()).isEqualTo(ErrorMessages.INVALID_ROAD_DATA);
    }

    @Test
    void shouldThrowOnInvalidGeometry() {
        RoadDTO invalidDto = copyWithInvalidGeom(randomRoadGenerator.toDTO(road));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> roadService.save(invalidDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.INVALID_WKT_FORMAT, exception.getReason());
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentRoad() {
        RoadDTO roadDTO = randomRoadGenerator.toDTO(road);
        when(roadRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> roadService.update(9999L, roadDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.ROAD_NOT_FOUND, exception.getReason());
    }

    @Test
    void shouldThrowWhenDeletingNonexistentRoad() {
        when(roadRepository.existsById(anyLong())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> roadService.delete(9999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.ROAD_NOT_FOUND, exception.getReason());
    }

    @Test
    void shouldReturnDashboardData() {
        when(roadRepository.findAll()).thenReturn(roadList);
        road.setType("road");

        RoadDashboardDTO result = roadService.getDashBoardData();

        assertEquals(1L, result.totalRoads());
        assertEquals(1L, result.confirmedRoads());
        assertEquals(0L, result.possibleRoads());
        assertEquals(0L, result.hypotheticalRoutes());
        assertEquals(0L, result.historicalRecorded());
        assertEquals(0L, result.other());
    }

    private RoadDTO copyWithInvalidGeom(RoadDTO original) {
        return new RoadDTO(
                original.id(),
                original.cat_nr(),
                original.name(),
                "INVALID_WKT",
                original.type(),
                original.typeDescription(),
                original.location(),
                original.description(),
                original.date(),
                original.referenceIds()
        );
    }
}
