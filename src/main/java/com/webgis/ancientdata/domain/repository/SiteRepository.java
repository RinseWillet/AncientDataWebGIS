package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.Site;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Long> {

    @Query("SELECT s.siteType, COUNT(s) FROM Site s GROUP BY s.siteType")
    List<Object[]> countByTypeRaw();
}
