package com.webgis.ancientdata.application.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.webgis.ancientdata.config.GoogleDriveBackupConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class GoogleDriveBackupService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveBackupService.class);
    private static final String APPLICATION_NAME = "AncientDataWebGIS-Backup";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private static final String PATH_SEPARATOR = "/";

    private final GoogleDriveBackupConfig config;
    private final Path mediaRoot;
    private Drive driveService;

    public GoogleDriveBackupService(
            GoogleDriveBackupConfig config,
            @Value("${media.storage-path:./media}") String storagePath) {
        this.config = config;
        this.mediaRoot = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            logger.info("Google Drive backup is disabled");
            return;
        }
        try {
            this.driveService = buildDriveService();
            logger.info("Google Drive backup initialised (folder: {})", config.getFolderId());
        } catch (Exception e) {
            logger.error("Failed to initialise Google Drive backup: {}", e.getMessage());
        }
    }

    private Drive buildDriveService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(config.getServiceAccountKeyPath()))
                .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Scheduled sync — runs on the configured cron expression.
     * Only executes if backup is enabled.
     */
    @Scheduled(cron = "${backup.gdrive.sync-cron:0 0 3 * * SUN}")
    public void scheduledSync() {
        if (!config.isEnabled()) {
            return;
        }
        sync();
    }

    /**
     * Full bidirectional sync: uploads new/modified local files,
     * deletes remote files whose local counterpart no longer exists.
     */
    public void sync() {
        if (driveService == null) {
            logger.warn("Google Drive backup not initialised — skipping sync");
            return;
        }

        logger.info("Starting Google Drive backup sync...");
        try {
            Map<String, File> remoteFiles = listRemoteFiles(config.getFolderId(), "");
            Set<String> localKeys = syncLocalFiles(remoteFiles);
            deleteOrphanedRemoteFiles(remoteFiles, localKeys);
            logger.info("Google Drive backup sync completed");
        } catch (IOException e) {
            logger.error("Google Drive backup sync failed: {}", e.getMessage());
        }
    }

    private Set<String> syncLocalFiles(Map<String, File> remoteFiles) throws IOException {
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

    private void syncSingleFile(Path localFile, String relativePath, File remote) {
        try {
            if (remote == null) {
                uploadFile(localFile, relativePath);
            } else {
                long localModified = Files.getLastModifiedTime(localFile).toMillis();
                long remoteModified = remote.getModifiedTime() != null
                        ? remote.getModifiedTime().getValue() : 0;
                if (localModified > remoteModified) {
                    updateFile(remote.getId(), localFile, relativePath);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to sync file {}: {}", relativePath, e.getMessage());
        }
    }

    private void deleteOrphanedRemoteFiles(Map<String, File> remoteFiles, Set<String> localKeys) {
        for (Map.Entry<String, File> entry : remoteFiles.entrySet()) {
            if (!localKeys.contains(entry.getKey())) {
                try {
                    driveService.files().delete(entry.getValue().getId()).execute();
                    logger.info("Deleted remote backup file: {}", entry.getKey());
                } catch (IOException e) {
                    logger.error("Failed to delete remote file {}: {}", entry.getKey(), e.getMessage());
                }
            }
        }
    }

    /**
     * Recursively lists all files in the given Drive folder,
     * returning a map of relative-path → Drive File.
     */
    private Map<String, File> listRemoteFiles(String folderId, String pathPrefix) throws IOException {
        Map<String, File> result = new HashMap<>();
        String pageToken = null;

        do {
            FileList fileList = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name, mimeType, modifiedTime)")
                    .setPageSize(1000)
                    .setPageToken(pageToken)
                    .execute();

            for (File file : fileList.getFiles()) {
                String fullPath = pathPrefix.isEmpty() ? file.getName() : pathPrefix + PATH_SEPARATOR + file.getName();
                if (FOLDER_MIME_TYPE.equals(file.getMimeType())) {
                    result.putAll(listRemoteFiles(file.getId(), fullPath));
                } else {
                    result.put(fullPath, file);
                }
            }
            pageToken = fileList.getNextPageToken();
        } while (pageToken != null);

        return result;
    }

    private void uploadFile(Path localFile, String relativePath) throws IOException {
        String parentFolderId = ensureRemoteDirectories(relativePath);

        File fileMetadata = new File();
        fileMetadata.setName(localFile.getFileName().toString());
        fileMetadata.setParents(Collections.singletonList(parentFolderId));

        String mimeType = Files.probeContentType(localFile);
        if (mimeType == null) mimeType = "application/octet-stream";

        FileContent mediaContent = new FileContent(mimeType, localFile.toFile());
        driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        logger.info("Uploaded to Google Drive: {}", relativePath);
    }

    private void updateFile(String fileId, Path localFile, String relativePath) throws IOException {
        String mimeType = Files.probeContentType(localFile);
        if (mimeType == null) mimeType = "application/octet-stream";

        FileContent mediaContent = new FileContent(mimeType, localFile.toFile());
        driveService.files().update(fileId, null, mediaContent).execute();

        logger.info("Updated on Google Drive: {}", relativePath);
    }

    /**
     * Ensures the directory structure exists in Google Drive for a given relative path.
     * Returns the Drive folder ID of the parent directory.
     */
    private String ensureRemoteDirectories(String relativePath) throws IOException {
        String[] parts = relativePath.split("/");
        String currentParentId = config.getFolderId();

        // Navigate/create all directories except the filename (last element)
        for (int i = 0; i < parts.length - 1; i++) {
            currentParentId = findOrCreateFolder(currentParentId, parts[i]);
        }
        return currentParentId;
    }

    private String findOrCreateFolder(String parentId, String folderName) throws IOException {
        FileList result = driveService.files().list()
                .setQ("'" + parentId + "' in parents and name = '" + folderName
                        + "' and mimeType = '" + FOLDER_MIME_TYPE + "' and trashed = false")
                .setFields("files(id)")
                .setPageSize(1)
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().getFirst().getId();
        }

        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType(FOLDER_MIME_TYPE);
        folderMetadata.setParents(Collections.singletonList(parentId));

        File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        logger.info("Created Google Drive folder: {}", folderName);
        return folder.getId();
    }
}
