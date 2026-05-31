package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.MediaAsset;
import com.webgis.ancientdata.domain.model.TargetType;
import com.webgis.ancientdata.domain.model.VisibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByTargetTypeAndTargetIdAndVisibilityStatus(
            TargetType targetType, Long targetId, VisibilityStatus visibilityStatus);

    List<MediaAsset> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}

