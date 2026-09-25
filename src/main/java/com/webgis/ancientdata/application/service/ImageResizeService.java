package com.webgis.ancientdata.application.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Downscales/recompresses oversized images so an upload that exceeds the storage
 * size limit can be accepted rather than rejected outright. See
 * docs/features/E15-image-resize-on-upload.md for the design rationale.
 */
@Service
public class ImageResizeService {

    private static final Logger logger = LoggerFactory.getLogger(ImageResizeService.class);

    // Long-edge cap generous enough to print at ~300 DPI (e.g. 4000px ~= 13.3" at 300 DPI).
    private static final int MAX_LONG_EDGE_PX = 4000;
    private static final float STARTING_QUALITY = 0.85f;
    private static final float QUALITY_STEP = 0.05f;
    private static final float MIN_QUALITY = 0.5f;
    private static final int TARGET_DPI = 300;

    public record ResizeResult(byte[] data, String mimeType) {}

    /**
     * Decode, downscale (if needed), and recompress an image as JPEG, searching
     * downward in quality until the output fits under {@code maxBytes} or the
     * minimum quality floor is reached (in which case the smallest attempt is
     * returned rather than looping indefinitely).
     *
     * @throws ImageProcessingException if the input cannot be decoded as an image
     */
    public ResizeResult resize(InputStream input, long maxBytes) {
        byte[] sourceBytes;
        try {
            sourceBytes = input.readAllBytes();
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image data", e);
        }

        BufferedImage original;
        try {
            original = ImageIO.read(new ByteArrayInputStream(sourceBytes));
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image data", e);
        }
        if (original == null) {
            throw new ImageProcessingException("Unsupported or corrupt image data");
        }

        // ImageIO.read() decodes raw sensor-orientation pixels and ignores the EXIF
        // Orientation tag; our re-encode below writes fresh metadata with no
        // orientation hint, so a portrait phone photo must be physically rotated
        // here or it comes out landscape with no way for a viewer to correct it.
        BufferedImage oriented = applyExifOrientation(original, readExifOrientation(sourceBytes));

        BufferedImage scaled = scaleToMaxLongEdge(oriented);

        byte[] bestAttempt = null;
        for (float quality = STARTING_QUALITY; quality >= MIN_QUALITY - 1e-6f; quality -= QUALITY_STEP) {
            bestAttempt = encodeJpeg(scaled, quality);
            if (bestAttempt.length <= maxBytes) {
                return new ResizeResult(bestAttempt, "image/jpeg");
            }
        }

        logger.warn("Resized image still exceeds target size ({} bytes) at minimum quality; " +
                "accepting smallest attempt ({} bytes)", maxBytes, bestAttempt.length);
        return new ResizeResult(bestAttempt, "image/jpeg");
    }

    // Best-effort EXIF orientation read — defaults to 1 (normal) if absent/unreadable,
    // which is a safe no-op in applyExifOrientation below.
    private int readExifOrientation(byte[] sourceBytes) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(sourceBytes));
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception e) {
            logger.debug("Could not read EXIF orientation (defaulting to normal): {}", e.getMessage());
        }
        return 1;
    }

    // Bakes the EXIF-indicated rotation/flip into the pixel data so the output
    // (which carries no orientation tag of its own) already displays correctly.
    // Transform matrices per the standard EXIF Orientation values 1-8.
    private BufferedImage applyExifOrientation(BufferedImage image, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        boolean swapDimensions = orientation >= 5;

        AffineTransform transform = new AffineTransform();
        switch (orientation) {
            case 2 -> { // flip horizontal
                transform.scale(-1.0, 1.0);
                transform.translate(-width, 0);
            }
            case 3 -> { // rotate 180
                transform.translate(width, height);
                transform.rotate(Math.PI);
            }
            case 4 -> { // flip vertical
                transform.scale(1.0, -1.0);
                transform.translate(0, -height);
            }
            case 5 -> { // rotate -90 and flip horizontal
                transform.rotate(-Math.PI / 2);
                transform.scale(-1.0, 1.0);
            }
            case 6 -> { // rotate 90 CW
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
            }
            case 7 -> { // rotate 90 CW and flip horizontal
                transform.scale(-1.0, 1.0);
                transform.translate(-height, 0);
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
            }
            case 8 -> { // rotate 270 CW (90 CCW)
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
            }
            default -> { /* orientation 1 (normal) handled by the early return above */ }
        }

        int newWidth = swapDimensions ? height : width;
        int newHeight = swapDimensions ? width : height;
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rotated.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, transform, null);
        } finally {
            g.dispose();
        }
        return rotated;
    }

    private BufferedImage scaleToMaxLongEdge(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int longEdge = Math.max(width, height);
        if (longEdge <= MAX_LONG_EDGE_PX) {
            return src;
        }

        double scale = (double) MAX_LONG_EDGE_PX / longEdge;
        int newWidth = Math.max(1, (int) Math.round(width * scale));
        int newHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, newWidth, newHeight, null);
        } finally {
            g.dispose();
        }
        return scaled;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new ImageProcessingException("No JPEG writer available");
        }
        ImageWriter writer = writers.next();
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            // Must reflect the actual image being written (not a fixed assumption) —
            // a mismatched type here corrupts the writer's SOF segment and throws NPE.
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, param);
            setJfifDensityBestEffort(metadata);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, metadata), param);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to encode resized image", e);
        } finally {
            writer.dispose();
        }
    }

    // Best-effort JFIF density write (E15-3) — purely cosmetic metadata, so any
    // failure here is logged and swallowed rather than failing the upload.
    //
    // In the javax_imageio_jpeg_image_1.0 tree, app0JFIF lives under the JPEGvariety
    // node, not directly under root — appending it to root instead produces an
    // invalid tree that breaks the writer's SOF segment construction.
    private void setJfifDensityBestEffort(IIOMetadata metadata) {
        try {
            String formatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(formatName);
            IIOMetadataNode jpegVariety = findChild(root, "JPEGvariety");
            IIOMetadataNode jfif = jpegVariety == null ? null : findChild(jpegVariety, "app0JFIF");
            if (jfif == null) {
                return; // not a JFIF-wrapped JPEG (or writer didn't default one in) — skip silently
            }
            jfif.setAttribute("resUnits", "1"); // 1 = dots per inch
            jfif.setAttribute("Xdensity", String.valueOf(TARGET_DPI));
            jfif.setAttribute("Ydensity", String.valueOf(TARGET_DPI));
            metadata.setFromTree(formatName, root);
        } catch (Exception e) {
            logger.debug("Could not set JFIF density metadata (non-fatal): {}", e.getMessage());
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
}
