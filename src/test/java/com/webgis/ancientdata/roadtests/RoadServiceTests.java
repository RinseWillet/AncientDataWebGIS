package com.webgis.ancientdata.roadtests;

//MVC

import com.webgis.ancientdata.RandomRoadGenerator;
import com.webgis.ancientdata.application.service.RoadService;
import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;
import com.webgis.ancientdata.domain.model.Road;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoadServiceTests {

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
    private RoadRepository roadRepository;

    @Mock
    private GeoJsonConverter geoJsonConverter;

    @Autowired
    @InjectMocks
    private RoadService roadService;

    @BeforeEach
    public void setUp(){

        roadList = new ArrayList<>();
        randomRoadGenerator = new RandomRoadGenerator();

        road = randomRoadGenerator.generateRandomRoad();
        roadList.add(road);

        //setting up GeoJSON
        //properties
        roadGeoJSON = randomRoadGenerator.generateRandomRoadGeoJSON(road);

        roadsGeoJSON = randomRoadGenerator.generateRandomRoadsGeoJSON(road);

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

        road.setModernReferenceList(modernReferenceList);
    }

    @AfterEach
    public void tearDown() {
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
    public void shouldListAllRoads() {
        when(roadRepository.findAll()).thenReturn(roadList);

        List<Road> fetchedRoads = (List<Road>) roadService.findAll();
        assertEquals(fetchedRoads, roadList);

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    public void shouldFindRoadById(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        Optional<Road> optionalRoad = roadService.findById(road.getId());

        optionalRoad.ifPresent(value -> assertThat(optionalRoad.get()).isEqualTo(road));

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    public void shouldFindRoadByIdGeoJSON(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        String fetchedRoad = roadService.findByIdGeoJson(road.getId());

        System.out.println("in de test:");
        System.out.println(fetchedRoad);
        System.out.println("en vanuit de RandomGenerator");
        System.out.println(roadGeoJSON);

        assertEquals(fetchedRoad, String.valueOf(roadGeoJSON));

        verify(roadRepository, times(1)).findById(any());
    }

    @Test
    public void shouldListAllRoadsGeoJSON(){
        when(roadRepository.findAll()).thenReturn(roadList);

        String fetchedRoadsGeoJSON = roadService.findAllGeoJson();

        assertEquals(String.valueOf(fetchedRoadsGeoJSON), String.valueOf(roadsGeoJSON));

        verify(roadRepository, times(1)).findAll();
    }

    @Test
    public void shouldConvertRoadtoGeoJSON(){
        when(geoJsonConverter.convertRoad(Optional.of(road))).thenReturn(roadGeoJSON);

        JSONObject fetchedroadGeoJSON = geoJsonConverter.convertRoad(Optional.of(road));
        assertEquals(fetchedroadGeoJSON, roadGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoad(any());
    }

    @Test
    public void shouldConvertRoadstoGeoJSON(){
        when(geoJsonConverter.convertRoads(roadList)).thenReturn(roadsGeoJSON);

        JSONObject fetchedRoadsGeoJSON = geoJsonConverter.convertRoads(roadList);
        assertEquals(fetchedRoadsGeoJSON, roadsGeoJSON);

        verify(geoJsonConverter, times(1)).convertRoads(any());
    }

    @Test
    public void shouldSaveRoad(){
        when(roadRepository.save(any())).thenReturn(road);

        assertEquals(roadService.save(road), road);

        verify(roadRepository, times(1)).save(any());
    }

    @Test
    public void shouldUpdateRoad(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));
        when(roadRepository.save(any())).thenReturn(road);

        assertEquals(roadService.update(road.getId(), road), road);

        verify(roadRepository, times(1)).save(road);
        verify(roadRepository, times(1)).findById(road.getId());
    }

    @Test
    public void shouldFindModernReferencesByRoadId() {
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));

        assertEquals(roadService.findModernReferencesByRoadId(road.getId()), modernReferenceDTOList);

        verify(roadRepository, times(1)).findById(road.getId());
    }

    @Test
    public void shouldAddModernReferenceToRoad(){
        when(roadRepository.findById(road.getId())).thenReturn(Optional.ofNullable(road));
        when(roadRepository.save(road)).thenReturn(road);

        assertEquals(roadService.addModernReferenceToRoad(road.getId(), modernReferenceDTO), road);

        verify(roadRepository, times(1)).findById(road.getId());
        verify(roadRepository, times(1)).save(road);
    }
}