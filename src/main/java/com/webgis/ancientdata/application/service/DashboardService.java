package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.domain.dto.*;
import com.webgis.ancientdata.domain.repository.RoadRepository;
import com.webgis.ancientdata.domain.repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int SCHEMA_VERSION = 1;
    private static final String UNKNOWN_TYPE = "unknown";

    private final RoadRepository roadRepository;
    private final SiteRepository siteRepository;

    public DashboardService(RoadRepository roadRepository, SiteRepository siteRepository) {
        this.roadRepository = roadRepository;
        this.siteRepository = siteRepository;
    }

    public DashboardSummaryDTO getSummary() {
        // Sites: count only
        MetricSummaryDTO sites = new MetricSummaryDTO(
                siteRepository.count(),
                normalizeAndAggregate(siteRepository.countByTypeRaw())
        );

        // Roads: count + length metrics
        RoadMetricDTO roads = new RoadMetricDTO(
                roadRepository.count(),
                normalizeAndAggregate(roadRepository.countByTypeRaw()),
                roadRepository.getTotalLengthKm(),
                normalizeAndAggregateLengths(roadRepository.getLengthKmByTypeRaw())
        );

        return new DashboardSummaryDTO(
                SCHEMA_VERSION,
                Instant.now(),
                sites,
                roads
        );
    }

    private List<TypeCountDTO> normalizeAndAggregate(List<Object[]> rawTypeCounts) {
        Map<String, Long> normalizedCounts = rawTypeCounts.stream()
                .collect(Collectors.groupingBy(
                        row -> normalizeType((String) row[0]),
                        Collectors.summingLong(row -> ((Number) row[1]).longValue())
                ));

        return normalizedCounts.entrySet().stream()
                .map(entry -> new TypeCountDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(TypeCountDTO::count).reversed()
                        .thenComparing(TypeCountDTO::type, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<TypeLengthDTO> normalizeAndAggregateLengths(List<Object[]> rawTypeLengths) {
        Map<String, Double> normalizedLengths = rawTypeLengths.stream()
                .collect(Collectors.groupingBy(
                        row -> normalizeType((String) row[0]),
                        Collectors.summingDouble(row -> ((Number) row[1]).doubleValue())
                ));

        return normalizedLengths.entrySet().stream()
                .map(entry -> new TypeLengthDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(TypeLengthDTO::lengthKm).reversed()
                        .thenComparing(TypeLengthDTO::type, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private String normalizeType(String type) {
        if (type == null) {
            return UNKNOWN_TYPE;
        }

        String normalized = type.trim().toLowerCase();
        return normalized.isEmpty() ? UNKNOWN_TYPE : normalized;
    }
}

