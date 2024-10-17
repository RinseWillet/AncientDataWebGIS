package com.webgis.ancientdata.modernreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModernReferenceRepository extends JpaRepository<ModernReference, Long> {
}
