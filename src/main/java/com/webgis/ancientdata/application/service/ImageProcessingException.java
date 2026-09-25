package com.webgis.ancientdata.application.service;

/** Thrown when {@link ImageResizeService} cannot decode or re-encode an image. */
public class ImageProcessingException extends RuntimeException {

    public ImageProcessingException(String message) {
        super(message);
    }

    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
