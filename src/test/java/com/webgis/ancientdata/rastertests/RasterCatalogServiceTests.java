package com.webgis.ancientdata.rastertests;

import com.webgis.ancientdata.application.service.RasterCatalogService;
import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerCreateRequest;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerUpdateRequest;
import com.webgis.ancientdata.domain.dto.RasterZoomDTO;
import com.webgis.ancientdata.domain.model.RasterLayer;
import com.webgis.ancientdata.domain.model.RasterLayerCategory;
import com.webgis.ancientdata.domain.repository.RasterLayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RasterCatalogServiceTests {

    @Mock
    private RasterLayerRepository rasterLayerRepository;

    @InjectMocks
    private RasterCatalogService service;

    private RasterLayer sampleEntity() {
        RasterLayer entity = new RasterLayer();
        entity.setId(1L);
        entity.setName("Sample");
        entity.setSource("ancientdata:test-1");
        entity.setBoundsSouth(51.0);
        entity.setBoundsWest(5.0);
        entity.setBoundsNorth(52.0);
        entity.setBoundsEast(6.0);
        entity.setZoomMin(8);
        entity.setZoomMax(17);
        entity.setAttribution("Test Attribution");
        entity.setCategory(RasterLayerCategory.HISTORICAL_MAP);
        entity.setCollection(null);
        entity.setHillshade(false);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    // --- getCatalog ---

    @Test
    void getCatalog_MapsRepositoryEntriesToDtosInOrder() {
        RasterLayer entity = sampleEntity();
        when(rasterLayerRepository.findAllByOrderByIdAsc()).thenReturn(List.of(entity));

        List<RasterLayerDTO> catalog = service.getCatalog();

        assertEquals(1, catalog.size());
        RasterLayerDTO dto = catalog.getFirst();
        assertEquals("Sample", dto.name());
        assertEquals("ancientdata:test-1", dto.source());
        assertEquals(51.0, dto.bounds().south());
        assertEquals(5.0, dto.bounds().west());
        assertEquals(52.0, dto.bounds().north());
        assertEquals(6.0, dto.bounds().east());
        assertEquals(8, dto.zoom().min());
        assertEquals(17, dto.zoom().max());
        assertEquals("Test Attribution", dto.attribution());
        assertEquals(RasterLayerCategory.HISTORICAL_MAP, dto.category());
        assertNull(dto.collection());
        assertFalse(dto.hillshade());
    }

    // --- create ---

    @Test
    void create_validRequest_savesAndReturnsDto() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "New Layer", "ancientdata:new-layer",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(8, 17),
                "Attribution", RasterLayerCategory.DEM, null, false);

        when(rasterLayerRepository.existsBySource("ancientdata:new-layer")).thenReturn(false);
        when(rasterLayerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RasterLayerDTO result = service.create(request);

        assertEquals("New Layer", result.name());
        assertEquals("ancientdata:new-layer", result.source());
        assertEquals(RasterLayerCategory.DEM, result.category());

        ArgumentCaptor<RasterLayer> captor = ArgumentCaptor.forClass(RasterLayer.class);
        verify(rasterLayerRepository).save(captor.capture());
        assertEquals("New Layer", captor.getValue().getName());
        assertEquals(RasterLayerCategory.DEM, captor.getValue().getCategory());
    }

    @Test
    void create_duplicateSource_throwsConflict() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "New Layer", "ancientdata:existing",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(8, 17),
                "Attribution", RasterLayerCategory.DEM, null, false);

        when(rasterLayerRepository.existsBySource("ancientdata:existing")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verify(rasterLayerRepository, never()).save(any());
    }

    @Test
    void create_boundsSouthNotLessThanNorth_throwsBadRequest() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "Bad Layer", "ancientdata:bad-bounds",
                new RasterBoundsDTO(52.0, 5.0, 51.0, 6.0), // south >= north
                new RasterZoomDTO(8, 17),
                "Attribution", RasterLayerCategory.DEM, null, false);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verifyNoInteractions(rasterLayerRepository);
    }

    @Test
    void create_boundsWestNotLessThanEast_throwsBadRequest() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "Bad Layer", "ancientdata:bad-bounds-2",
                new RasterBoundsDTO(51.0, 6.0, 52.0, 5.0), // west >= east
                new RasterZoomDTO(8, 17),
                "Attribution", RasterLayerCategory.DEM, null, false);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verifyNoInteractions(rasterLayerRepository);
    }

    @Test
    void create_zoomMinAboveMax_throwsBadRequest() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "Bad Layer", "ancientdata:bad-zoom",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(17, 8), // min > max
                "Attribution", RasterLayerCategory.DEM, null, false);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verifyNoInteractions(rasterLayerRepository);
    }

    @Test
    void create_blankName_throwsBadRequest() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "  ", "ancientdata:blank-name",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(8, 17),
                "Attribution", RasterLayerCategory.DEM, null, false);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verifyNoInteractions(rasterLayerRepository);
    }

    @Test
    void create_blankAttribution_throwsBadRequest() {
        RasterLayerCreateRequest request = new RasterLayerCreateRequest(
                "Layer", "ancientdata:blank-attribution",
                new RasterBoundsDTO(51.0, 5.0, 52.0, 6.0),
                new RasterZoomDTO(8, 17),
                "  ", RasterLayerCategory.DEM, null, false);

        assertThrows(ResponseStatusException.class, () -> service.create(request));
        verifyNoInteractions(rasterLayerRepository);
    }

    // --- update ---

    @Test
    void update_existingSource_appliesPartialChanges() {
        RasterLayer existing = sampleEntity();
        when(rasterLayerRepository.findBySource("ancientdata:test-1")).thenReturn(Optional.of(existing));
        when(rasterLayerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RasterLayerUpdateRequest request = new RasterLayerUpdateRequest(
                "Renamed", null, null, null, null, null, null);

        RasterLayerDTO result = service.update("ancientdata:test-1", request);

        assertEquals("Renamed", result.name());
        assertEquals(51.0, result.bounds().south()); // unchanged fields preserved
        assertEquals(8, result.zoom().min());
    }

    @Test
    void update_notFound_throws404() {
        when(rasterLayerRepository.findBySource("ancientdata:missing")).thenReturn(Optional.empty());

        RasterLayerUpdateRequest request = new RasterLayerUpdateRequest(
                "Renamed", null, null, null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> service.update("ancientdata:missing", request));
    }

    @Test
    void update_resultingInvalidBounds_throwsBadRequestAndDoesNotSave() {
        RasterLayer existing = sampleEntity();
        when(rasterLayerRepository.findBySource("ancientdata:test-1")).thenReturn(Optional.of(existing));

        RasterLayerUpdateRequest request = new RasterLayerUpdateRequest(
                null, new RasterBoundsDTO(52.0, 5.0, 51.0, 6.0), null, null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> service.update("ancientdata:test-1", request));
        verify(rasterLayerRepository, never()).save(any());
    }

    // --- delete ---

    @Test
    void delete_existingSource_deletes() {
        when(rasterLayerRepository.existsBySource("ancientdata:test-1")).thenReturn(true);

        service.delete("ancientdata:test-1");

        verify(rasterLayerRepository).deleteBySource("ancientdata:test-1");
    }

    @Test
    void delete_notFound_throws404() {
        when(rasterLayerRepository.existsBySource("ancientdata:missing")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.delete("ancientdata:missing"));
        verify(rasterLayerRepository, never()).deleteBySource(any());
    }
}
