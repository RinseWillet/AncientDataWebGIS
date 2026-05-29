package com.webgis.ancientdata.application.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction for binary media storage.
 * Default implementation uses the local filesystem (NAS bind mount).
 * Can be swapped for S3/MinIO later without touching callers.
 */
public interface MediaStorageService {

    /**
     * Store a file and return the storage key (relative path from media root).
     */
    String store(String targetDir, String filename, MultipartFile file) throws IOException;

    /**
     * Load a file as a Spring Resource for serving.
     */
    Resource load(String storageKey) throws IOException;

    /**
     * Delete a file by storage key.
     */
    void delete(String storageKey) throws IOException;
}

