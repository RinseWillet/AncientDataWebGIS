package com.webgis.ancientdata.web.mapper;

import com.webgis.ancientdata.domain.dto.AncientReferenceDTO;
import com.webgis.ancientdata.domain.model.AncientReference;

public class AncientReferenceMapper {

    public static AncientReferenceDTO toDto(AncientReference entity) {
        return new AncientReferenceDTO(
                entity.getId(),
                entity.getName(),
                entity.getAuthor(),
                entity.getWork(),
                entity.getBook(),
                entity.getPage()
        );
    }

    public static AncientReference toEntity(AncientReferenceDTO dto) {
        if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid data");
        }

        AncientReference entity = new AncientReference();
        entity.setName(dto.name());
        entity.setAuthor(dto.author());
        entity.setWork(dto.work());
        entity.setBook(dto.book());
        entity.setPage(dto.page());
        return entity;
    }
}
