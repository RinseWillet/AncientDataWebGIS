package com.webgis.ancientdata.domain.dto;

import java.util.List;

public record MetricSummaryDTO(
        long total,
        List<TypeCountDTO> byType
) {}

