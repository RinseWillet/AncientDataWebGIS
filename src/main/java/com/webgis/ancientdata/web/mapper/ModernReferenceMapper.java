package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.ModernReferenceDTO;
import com.webgis.ancientdata.domain.model.ModernReference;

public class ModernReferenceMapper {

    public static ModernReferenceDTO toDto(ModernReference entity) {
        return new ModernReferenceDTO(
                entity.getId(),
                entity.getShortRef(),
                entity.getFullRef(),
                entity.getUrl()
        );
    }

    public static ModernReference toEntity(ModernReferenceDTO dto) {
        if (dto.shortRef() == null || dto.shortRef().trim().isEmpty() ||
                dto.fullRef() == null || dto.fullRef().trim().isEmpty() ||
                dto.url() == null || dto.url().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid data");
        }

        ModernReference entity = new ModernReference();
        entity.setShortRef(dto.shortRef());
        entity.setFullRef(dto.fullRef());
        entity.setUrl(dto.url());
        return entity;
    }
}