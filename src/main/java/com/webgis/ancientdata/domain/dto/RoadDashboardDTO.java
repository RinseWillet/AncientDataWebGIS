package com.webgis.ancientdata.domain.dto;

public record RoadDashboardDTO(
        long totalRoads,
        long confirmedRoads,
        long possibleRoads,
        long hypotheticalRoutes,
        long historicalRecorded,
        long other
) {}
