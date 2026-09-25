package com.webgis.ancientdata.mediatests;

import com.webgis.ancientdata.application.service.ImageProcessingException;
import com.webgis.ancientdata.application.service.ImageResizeService;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ImageResizeServiceTests {

    private final ImageResizeService service = new ImageResizeService();

    @Test
    void resize_capsLongEdgeAt4000_preservingAspectRatio() throws Exception {
        byte[] source = encodeAsJpeg(solidColorImage(6000, 4000));

        ImageResizeService.ResizeResult result = service.resize(new ByteArrayInputStream(source), 2L * 1024 * 1024);

        BufferedImage output = ImageIO.read(new ByteArrayInputStream(result.data()));
        assertEquals(4000, output.getWidth());
        assertEquals(2667, output.getHeight());
        assertEquals("image/jpeg", result.mimeType());
    }

    @Test
    void resize_imageAlreadyUnderCap_doesNotUpscale() throws Exception {
        byte[] source = encodeAsJpeg(solidColorImage(800, 600));

        ImageResizeService.ResizeResult result = service.resize(new ByteArrayInputStream(source), 2L * 1024 * 1024);

        BufferedImage output = ImageIO.read(new ByteArrayInputStream(result.data()));
        assertEquals(800, output.getWidth());
        assertEquals(600, output.getHeight());
    }

    @Test
    void resize_outputFitsUnderTargetByteCeiling() throws Exception {
        byte[] source = encodeAsJpeg(solidColorImage(5000, 3000));
        long maxBytes = 500L * 1024;

        ImageResizeService.ResizeResult result = service.resize(new ByteArrayInputStream(source), maxBytes);

        assertTrue(result.data().length <= maxBytes,
                "expected resized output <= " + maxBytes + " bytes but was " + result.data().length);
    }

    @Test
    void resize_setsJfifDensityTo300Dpi() throws Exception {
        byte[] source = encodeAsJpeg(solidColorImage(5000, 3000));

        ImageResizeService.ResizeResult result = service.resize(new ByteArrayInputStream(source), 2L * 1024 * 1024);

        assertEquals("300", readJfifXDensity(result.data()));
    }

    @Test
    void resize_exifOrientationSix_bakesInRotationSoOutputDisplaysUpright() throws Exception {
        // Simulates a phone held normally: the sensor captures a landscape-shaped
        // raw frame and tags it EXIF Orientation=6 ("rotate 90 CW to display
        // correctly") — the exact real-world case that showed up as a portrait
        // photo coming out landscape after going through the resize path.
        int rawWidth = 800;
        int rawHeight = 600;
        BufferedImage raw = new BufferedImage(rawWidth, rawHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = raw.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, rawWidth, rawHeight);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100); // marker at the raw frame's top-left corner
        g.dispose();

        byte[] jpegWithOrientation = withExifOrientation(encodeAsJpeg(raw), 6);

        ImageResizeService.ResizeResult result =
                service.resize(new ByteArrayInputStream(jpegWithOrientation), 2L * 1024 * 1024);
        BufferedImage output = ImageIO.read(new ByteArrayInputStream(result.data()));

        // A 90-degree correction swaps the dimensions...
        assertEquals(rawHeight, output.getWidth());
        assertEquals(rawWidth, output.getHeight());
        // ...and a 90 CW rotation moves the raw frame's top-left corner to the
        // output's top-right corner.
        assertEquals(Color.WHITE.getRGB(), output.getRGB(output.getWidth() - 10, 10));
        assertEquals(Color.BLACK.getRGB(), output.getRGB(10, 10));
    }

    @Test
    void resize_corruptData_throwsImageProcessingException() {
        byte[] garbage = new byte[]{1, 2, 3, 4, 5};

        assertThrows(ImageProcessingException.class,
                () -> service.resize(new ByteArrayInputStream(garbage), 2L * 1024 * 1024));
    }

    private BufferedImage solidColorImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private byte[] encodeAsJpeg(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    private String readJfifXDensity(byte[] jpegBytes) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(jpegBytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            assertTrue(readers.hasNext(), "No JPEG reader available");
            ImageReader reader = readers.next();
            reader.setInput(iis);
            IIOMetadata metadata = reader.getImageMetadata(0);
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
            IIOMetadataNode jpegVariety = findChild(root, "JPEGvariety");
            IIOMetadataNode jfif = jpegVariety == null ? null : findChild(jpegVariety, "app0JFIF");
            return jfif == null ? null : jfif.getAttribute("Xdensity");
        }
    }

    private IIOMetadataNode findChild(IIOMetadataNode parent, String name) {
        for (int i = 0; i < parent.getLength(); i++) {
            if (name.equals(parent.item(i).getNodeName())) {
                return (IIOMetadataNode) parent.item(i);
            }
        }
        return null;
    }

    // Splices a minimal hand-built APP1/Exif segment (containing only the
    // Orientation tag) right after the SOI marker of an already-valid JPEG —
    // ImageIO's writer has no support for writing EXIF, so this is the most
    // direct way to produce a real EXIF-tagged JPEG fixture for the test above.
    private byte[] withExifOrientation(byte[] jpegBytes, int orientation) throws IOException {
        ByteArrayOutputStream tiff = new ByteArrayOutputStream();
        tiff.write(new byte[]{0x49, 0x49});             // "II" little-endian byte order
        tiff.write(new byte[]{0x2A, 0x00});              // TIFF magic number (42)
        tiff.write(new byte[]{0x08, 0x00, 0x00, 0x00});  // offset to IFD0
        tiff.write(new byte[]{0x01, 0x00});              // IFD0 entry count = 1
        tiff.write(new byte[]{0x12, 0x01});              // tag 0x0112 = Orientation (274)
        tiff.write(new byte[]{0x03, 0x00});              // type 3 = SHORT
        tiff.write(new byte[]{0x01, 0x00, 0x00, 0x00});  // component count = 1
        tiff.write(new byte[]{(byte) orientation, 0x00, 0x00, 0x00}); // value, padded to 4 bytes
        tiff.write(new byte[]{0x00, 0x00, 0x00, 0x00});  // next IFD offset = 0 (none)

        byte[] tiffBytes = tiff.toByteArray();
        byte[] exifHeader = "Exif\0\0".getBytes(StandardCharsets.US_ASCII);
        int app1Length = 2 + exifHeader.length + tiffBytes.length; // length field includes itself

        ByteArrayOutputStream app1 = new ByteArrayOutputStream();
        app1.write(0xFF);
        app1.write(0xE1);
        app1.write((app1Length >> 8) & 0xFF);
        app1.write(app1Length & 0xFF);
        app1.write(exifHeader);
        app1.write(tiffBytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpegBytes, 0, 2); // SOI marker (0xFFD8)
        out.write(app1.toByteArray());
        out.write(jpegBytes, 2, jpegBytes.length - 2);
        return out.toByteArray();
    }
}
