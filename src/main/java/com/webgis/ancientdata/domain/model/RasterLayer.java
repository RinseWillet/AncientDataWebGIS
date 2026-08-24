package com.webgis.ancientdata.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "raster_layer")
public class RasterLayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "source", nullable = false, unique = true)
    private String source;

    @Column(name = "bounds_south", nullable = false)
    private double boundsSouth;

    @Column(name = "bounds_west", nullable = false)
    private double boundsWest;

    @Column(name = "bounds_north", nullable = false)
    private double boundsNorth;

    @Column(name = "bounds_east", nullable = false)
    private double boundsEast;

    @Column(name = "zoom_min", nullable = false)
    private int zoomMin;

    @Column(name = "zoom_max", nullable = false)
    private int zoomMax;

    @Column(name = "attribution", nullable = false)
    private String attribution;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private RasterLayerCategory category;

    @Column(name = "collection")
    private String collection;

    @Column(name = "hillshade", nullable = false)
    private boolean hillshade;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
