package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.RasterLayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RasterLayerRepository extends JpaRepository<RasterLayer, Long> {

    List<RasterLayer> findAllByOrderByIdAsc();

    Optional<RasterLayer> findBySource(String source);

    boolean existsBySource(String source);

    void deleteBySource(String source);
}
