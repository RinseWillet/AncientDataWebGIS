package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.ModernReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModernReferenceRepository extends JpaRepository<ModernReference, Long> {
}
