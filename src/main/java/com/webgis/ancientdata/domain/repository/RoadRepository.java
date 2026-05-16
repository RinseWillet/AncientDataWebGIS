package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadRepository extends JpaRepository<Road, Long> {

    @Query("SELECT r.type, COUNT(r) FROM Road r GROUP BY r.type")
    List<Object[]> countByTypeRaw();

    // PostGIS native query: aggregates total road length using ST_Length on WGS84 geometry, converted to km
    // noinspection SqlDialectInspection
    @Query(value = "SELECT COALESCE(SUM(ST_Length(geom::geography)) / 1000.0, 0) FROM roads", nativeQuery = true)
    Double getTotalLengthKm();

    // PostGIS native query: aggregates road length by type using ST_Length on WGS84 geometry, converted to km
    // noinspection SqlDialectInspection
    @Query(value = "SELECT type, COALESCE(SUM(ST_Length(geom::geography)) / 1000.0, 0) FROM roads GROUP BY type", nativeQuery = true)
    List<Object[]> getLengthKmByTypeRaw();
}


