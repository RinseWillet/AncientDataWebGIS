package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.constants.ErrorMessages;
import com.webgis.ancientdata.domain.dto.RasterBoundsDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerCreateRequest;
import com.webgis.ancientdata.domain.dto.RasterLayerDTO;
import com.webgis.ancientdata.domain.dto.RasterLayerUpdateRequest;
import com.webgis.ancientdata.domain.dto.RasterZoomDTO;
import com.webgis.ancientdata.domain.model.RasterLayer;
import com.webgis.ancientdata.domain.repository.RasterLayerRepository;
import com.webgis.ancientdata.web.mapper.RasterLayerMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Catalog of raster layers published in GeoServer (see ADR-012 / ADR-013 /
 * the E3.1 runbook), backed by the {@code raster_layer} table (E3-6). Entries
 * are keyed by {@code source} (the GeoServer "workspace:layer" identifier),
 * which is also what admin write operations address - the public
 * {@link RasterLayerDTO} shape stays id-free.
 */
@Service
@Transactional
public class RasterCatalogService {

    private final RasterLayerRepository rasterLayerRepository;

    public RasterCatalogService(RasterLayerRepository rasterLayerRepository) {
        this.rasterLayerRepository = rasterLayerRepository;
    }

    @Transactional(readOnly = true)
    public List<RasterLayerDTO> getCatalog() {
        return rasterLayerRepository.findAllByOrderByIdAsc().stream()
                .map(RasterLayerMapper::toDto)
                .toList();
    }

    public RasterLayerDTO create(RasterLayerCreateRequest request) {
        validate(request.name(), request.source(), request.bounds(), request.zoom(), request.attribution());

        if (rasterLayerRepository.existsBySource(request.source())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessages.RASTER_LAYER_SOURCE_EXISTS);
        }

        RasterLayer entity = new RasterLayer();
        entity.setName(request.name());
        entity.setSource(request.source());
        applyBounds(entity, request.bounds());
        applyZoom(entity, request.zoom());
        entity.setAttribution(request.attribution());
        entity.setCategory(request.category());
        entity.setCollection(request.collection());
        entity.setHillshade(request.hillshade());

        RasterLayer saved = rasterLayerRepository.save(entity);
        return RasterLayerMapper.toDto(saved);
    }

    public RasterLayerDTO update(String source, RasterLayerUpdateRequest request) {
        RasterLayer entity = rasterLayerRepository.findBySource(source)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.RASTER_LAYER_NOT_FOUND));

        if (request.name() != null) entity.setName(request.name());
        if (request.bounds() != null) applyBounds(entity, request.bounds());
        if (request.zoom() != null) applyZoom(entity, request.zoom());
        if (request.attribution() != null) entity.setAttribution(request.attribution());
        if (request.category() != null) entity.setCategory(request.category());
        if (request.collection() != null) entity.setCollection(request.collection());
        if (request.hillshade() != null) entity.setHillshade(request.hillshade());

        validate(entity.getName(), entity.getSource(), currentBounds(entity), currentZoom(entity), entity.getAttribution());

        RasterLayer saved = rasterLayerRepository.save(entity);
        return RasterLayerMapper.toDto(saved);
    }

    public void delete(String source) {
        if (!rasterLayerRepository.existsBySource(source)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.RASTER_LAYER_NOT_FOUND);
        }
        rasterLayerRepository.deleteBySource(source);
    }

    private void applyBounds(RasterLayer entity, RasterBoundsDTO bounds) {
        entity.setBoundsSouth(bounds.south());
        entity.setBoundsWest(bounds.west());
        entity.setBoundsNorth(bounds.north());
        entity.setBoundsEast(bounds.east());
    }

    private void applyZoom(RasterLayer entity, RasterZoomDTO zoom) {
        entity.setZoomMin(zoom.min());
        entity.setZoomMax(zoom.max());
    }

    private RasterBoundsDTO currentBounds(RasterLayer entity) {
        return new RasterBoundsDTO(entity.getBoundsSouth(), entity.getBoundsWest(), entity.getBoundsNorth(), entity.getBoundsEast());
    }

    private RasterZoomDTO currentZoom(RasterLayer entity) {
        return new RasterZoomDTO(entity.getZoomMin(), entity.getZoomMax());
    }

    private void validate(String name, String source, RasterBoundsDTO bounds, RasterZoomDTO zoom, String attribution) {
        if (name == null || name.isBlank()
                || source == null || source.isBlank()
                || attribution == null || attribution.isBlank()
                || bounds == null || bounds.south() >= bounds.north() || bounds.west() >= bounds.east()
                || zoom == null || zoom.min() > zoom.max()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_RASTER_LAYER_DATA);
        }
    }
}
