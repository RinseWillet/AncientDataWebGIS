package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.MediaAssetDTO;
import com.webgis.ancientdata.domain.model.MediaAsset;

public class MediaAssetMapper {

    private MediaAssetMapper() {
    }

    public static MediaAssetDTO toDto(MediaAsset entity, String baseUrl) {
        return toDto(entity, baseUrl, false);
    }

    // resized: true only on the immediate upload response for a file that was
    // server-side downscaled/recompressed to fit — a one-time notice, not a
    // persisted/queryable attribute of the asset.
    public static MediaAssetDTO toDto(MediaAsset entity, String baseUrl, boolean resized) {
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
                entity.getLatitude(),
                entity.getLongitude(),
                entity.isCover(),
                entity.getVisibilityStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                resized
        );
    }
}

