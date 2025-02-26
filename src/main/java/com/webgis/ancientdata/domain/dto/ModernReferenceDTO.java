package com.webgis.ancientdata.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModernReferenceDTO {

    private long id;

    private String shortRef;

    private String fullRef;

    private String URL;
}
