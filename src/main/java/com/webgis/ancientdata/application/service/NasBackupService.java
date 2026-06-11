package com.webgis.ancientdata.application.service;

import com.webgis.ancientdata.config.NasBackupConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * NAS-based media backup service.
 * Syncs local media directory to a mounted NAS folder on schedule or manual trigger.
 */
@Service
public class NasBackupService {

    private static final Logger logger = LoggerFactory.getLogger(NasBackupService.class);

    private final NasBackupConfig config;
    private final Path mediaRoot;
    Path nasBackupRoot;

    public NasBackupService(
            NasBackupConfig config,
            @Value("${media.storage-path:./media}") String storagePath) {
        this.config = config;
        this.mediaRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            logger.info("NAS backup is disabled");
            return;
        }

        this.nasBackupRoot = Paths.get(config.getMountPath()).toAbsolutePath().normalize();

        if (!Files.exists(nasBackupRoot)) {
            logger.warn("NAS backup mount path does not exist: {}", config.getMountPath());
            return;
        }

        if (!Files.isWritable(nasBackupRoot)) {
            logger.warn("NAS backup mount path is not writable: {}", config.getMountPath());
            return;
        }

        logger.info("NAS backup service initialised (mount: {})", config.getMountPath());
    }

    /**
     * Scheduled sync — runs on the configured cron expression.
     * Only executes if backup is enabled and initialised.
     */
    @Scheduled(cron = "${backup.nas.sync-cron:0 0 3 * * SUN}")
    public void scheduledSync() {
        if (!config.isEnabled()) {
            return;
        }
        sync();
    }

    /**
     * Full bidirectional sync: copies new/modified local files to NAS,
     * deletes remote files whose local counterpart no longer exists.
     */
    public void sync() {
        if (nasBackupRoot == null || !Files.exists(nasBackupRoot)) {
            logger.warn("NAS backup mount not available — skipping sync");
            return;
        }

        logger.info("Starting NAS backup sync...");
        try {
            Map<String, Path> remoteFiles = listRemoteFiles();
            Set<String> localKeys = syncLocalFiles(remoteFiles);
            deleteOrphanedRemoteFiles(remoteFiles, localKeys);
            logger.info("NAS backup sync completed");
        } catch (IOException e) {
            logger.error("NAS backup sync failed: {}", e.getMessage());
        }
    }

    /**
     * Walk local media directory, sync each file (copy if new/modified),
     * return set of relative paths for orphan detection.
     */
    private Set<String> syncLocalFiles(Map<String, Path> remoteFiles) throws IOException {
        Set<String> localKeys = new HashSet<>();
        try (Stream<Path> paths = Files.walk(mediaRoot)) {
            paths.filter(Files::isRegularFile).forEach(localFile -> {
                String relativePath = mediaRoot.relativize(localFile).toString();
                localKeys.add(relativePath);
                syncSingleFile(localFile, relativePath, remoteFiles.get(relativePath));
            });
        }
        return localKeys;
    }

    /**
     * Sync a single file: copy if remote doesn't exist or is older than local.
     */
    private void syncSingleFile(Path localFile, String relativePath, Path remote) {
        try {
            if (remote == null) {
                copyFile(localFile, relativePath);
            } else {
                long localModified = Files.getLastModifiedTime(localFile).toMillis();
                long remoteModified = Files.getLastModifiedTime(remote).toMillis();
                if (localModified > remoteModified) {
                    copyFile(localFile, relativePath);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to sync file {}: {}", relativePath, e.getMessage());
        }
    }

    /**
     * Delete remote files that no longer exist locally.
     */
    private void deleteOrphanedRemoteFiles(Map<String, Path> remoteFiles, Set<String> localKeys) {
        for (Map.Entry<String, Path> entry : remoteFiles.entrySet()) {
            if (!localKeys.contains(entry.getKey())) {
                try {
                    Files.delete(entry.getValue());
                    logger.info("Deleted orphaned backup file: {}", entry.getKey());
                } catch (IOException e) {
                    logger.error("Failed to delete backup file {}: {}", entry.getKey(), e.getMessage());
                }
            }
        }
    }

    /**
     * Recursively list all files in the NAS backup directory,
     * returning a map of relative-path → NAS Path.
     */
    private Map<String, Path> listRemoteFiles() throws IOException {
        Map<String, Path> result = new HashMap<>();

        try (Stream<Path> paths = Files.walk(nasBackupRoot)) {
            paths.filter(Files::isRegularFile).forEach(remoteFile -> {
                String relativePath = nasBackupRoot.relativize(remoteFile).toString();
                result.put(relativePath, remoteFile);
            });
        }

        return result;
    }

    /**
     * Copy a file from local media to NAS backup, creating directories as needed.
     */
    private void copyFile(Path localFile, String relativePath) throws IOException {
        Path targetPath = nasBackupRoot.resolve(relativePath);
        Files.createDirectories(targetPath.getParent());

        Files.copy(localFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Backed up to NAS: {}", relativePath);
    }
}

