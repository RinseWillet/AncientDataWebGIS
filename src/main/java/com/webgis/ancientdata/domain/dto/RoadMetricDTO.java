package com.webgis.ancientdata.domain.dto;

import java.util.List;

public record RoadMetricDTO(
        long total,
        List<TypeCountDTO> byType,
        double lengthKmTotal,
        List<TypeLengthDTO> lengthKmByType
) {}

