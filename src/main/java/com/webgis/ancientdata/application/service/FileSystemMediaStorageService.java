package com.webgis.ancientdata.application.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemMediaStorageService implements MediaStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemMediaStorageService.class);

    private final Path rootLocation;

    public FileSystemMediaStorageService(@Value("${media.storage-path:./media}") String storagePath) {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(rootLocation);
        logger.info("Media storage initialised at {}", rootLocation);
    }

    @Override
    public String store(String targetDir, String filename, MultipartFile file) throws IOException {
        Path dir = rootLocation.resolve(targetDir).normalize();
        if (!dir.startsWith(rootLocation)) {
            throw new IOException("Cannot store file outside media root");
        }
        Files.createDirectories(dir);

        Path destination = dir.resolve(filename).normalize();
        if (!destination.startsWith(rootLocation)) {
            throw new IOException("Cannot store file outside media root");
        }

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Stored media file: {}", destination);

        return targetDir + "/" + filename;
    }

    @Override
    public Resource load(String storageKey) throws IOException {
        Path file = rootLocation.resolve(storageKey).normalize();
        if (!file.startsWith(rootLocation)) {
            throw new IOException("Cannot read file outside media root");
        }

        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found: " + storageKey);
        }
        return resource;
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Path file = rootLocation.resolve(storageKey).normalize();
        if (!file.startsWith(rootLocation)) {
            throw new IOException("Cannot delete file outside media root");
        }
        Files.deleteIfExists(file);
        logger.info("Deleted media file: {}", file);
    }
}

