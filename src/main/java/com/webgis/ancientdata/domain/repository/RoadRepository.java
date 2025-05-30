package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadRepository extends JpaRepository<Road, Long> {

}
