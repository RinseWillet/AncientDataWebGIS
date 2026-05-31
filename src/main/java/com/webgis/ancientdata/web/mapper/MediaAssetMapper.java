package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.model.MediaAsset;

public class MediaAssetMapper {

    private MediaAssetMapper() {
    }

    public static MediaAssetDTO toDto(MediaAsset entity, String baseUrl) {
        String fullUrl = baseUrl + "/" + entity.getStorageKey();
        return new MediaAssetDTO(
                entity.getId(),
                entity.getTargetType().name(),
                entity.getTargetId(),
                fullUrl,
                entity.getCaption(),
                entity.getAuthor(),
                entity.getSource(),
                entity.getLicense(),
                entity.getDateTaken(),
                entity.isCover(),
                entity.getVisibilityStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

