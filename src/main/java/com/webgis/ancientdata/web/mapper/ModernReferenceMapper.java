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
        ModernReference entity = new ModernReference();
        entity.setShortRef(dto.getShortRef());
        entity.setFullRef(dto.getFullRef());
        entity.setUrl(dto.getUrl());
        return entity;
    }
}