package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.Site;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Long> {

}
