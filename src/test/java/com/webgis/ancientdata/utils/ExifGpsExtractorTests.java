package com.webgis.ancientdata.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExifGpsExtractorTests {

    private InputStream resource(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("media/" + name);
        assertNotNull(stream, "Test fixture not found: " + name);
        return stream;
    }

    @Test
    void extract_photoWithGps_returnsCoordinates() throws IOException {
        try (InputStream in = resource("photo-with-gps.jpg")) {
            Optional<ExifGpsExtractor.GeoPoint> result = ExifGpsExtractor.extract(in);

            assertTrue(result.isPresent());
            assertEquals(52.0907, result.get().latitude(), 0.001);
            assertEquals(5.1214, result.get().longitude(), 0.001);
        }
    }

    @Test
    void extract_photoWithoutGps_returnsEmpty() throws IOException {
        try (InputStream in = resource("photo-without-gps.jpg")) {
            Optional<ExifGpsExtractor.GeoPoint> result = ExifGpsExtractor.extract(in);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void extract_notAnImage_returnsEmpty() {
        InputStream in = new java.io.ByteArrayInputStream(new byte[]{1, 2, 3});
        Optional<ExifGpsExtractor.GeoPoint> result = ExifGpsExtractor.extract(in);

        assertTrue(result.isEmpty());
    }
}

