package com.webgis.ancientdata.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModernReferenceDTO {
    private Long id;
    private String shortRef;
    private String fullRef;

    private String url;
}