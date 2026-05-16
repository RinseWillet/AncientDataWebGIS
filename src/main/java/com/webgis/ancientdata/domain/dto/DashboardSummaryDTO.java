package com.webgis.ancientdata.domain.dto;

import java.time.Instant;

public record DashboardSummaryDTO(
        int schemaVersion,
        Instant generatedAt,
        MetricSummaryDTO sites,
        RoadMetricDTO roads
) {}



