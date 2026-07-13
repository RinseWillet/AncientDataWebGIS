package com.webgis.ancientdata.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Reads GPS coordinates from a photo's EXIF metadata, when present.
 * Used to auto-populate media_asset geotag columns on upload (E2-GEO-1).
 */
public class ExifGpsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ExifGpsExtractor.class);

    private ExifGpsExtractor() {
    }

    public record GeoPoint(double latitude, double longitude) {
    }

    public static Optional<GeoPoint> extract(InputStream imageStream) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageStream);
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory == null) {
                return Optional.empty();
            }
            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation == null || geoLocation.isZero()) {
                return Optional.empty();
            }
            return Optional.of(new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude()));
        } catch (ImageProcessingException | IOException e) {
            logger.warn("Could not read EXIF GPS data: {}", e.getMessage());
            return Optional.empty();
        }
    }
}

