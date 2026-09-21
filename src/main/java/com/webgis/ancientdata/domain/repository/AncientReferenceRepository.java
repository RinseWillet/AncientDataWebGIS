package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.AncientReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AncientReferenceRepository extends JpaRepository<AncientReference, Long> {
}
