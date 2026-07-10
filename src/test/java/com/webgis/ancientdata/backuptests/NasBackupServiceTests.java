package com.webgis.ancientdata.backuptests;

import com.webgis.ancientdata.application.service.NasBackupService;
import com.webgis.ancientdata.config.NasBackupConfig;
import com.webgis.ancientdata.domain.model.BackupType;
import com.webgis.ancientdata.domain.repository.BackupHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NasBackupServiceTests {

    private NasBackupConfig config;
    private BackupHistoryRepository backupHistoryRepository;
    private NasBackupService service;

    @TempDir
    Path mediaRoot;

    @TempDir
    Path nasBackupRoot;

    @BeforeEach
    void setUp() {
        config = new NasBackupConfig();
        config.setEnabled(false);
        config.setMountPath(nasBackupRoot.toString());

        backupHistoryRepository = mock(BackupHistoryRepository.class);
        service = new NasBackupService(config, backupHistoryRepository, mediaRoot.toString());
    }

    @Test
    void testInit_WhenDisabled_LogsAndReturns() {
        service.init();
        assertFalse(config.isEnabled());
    }



    @Test
    void testSync_WhenDisabled_DoesNothing() throws Exception {
        // Create a local file to back up
        Path localFile = mediaRoot.resolve("test.txt");
        Files.writeString(localFile, "test content");

        config.setEnabled(false);
        service.init();
        service.sync();

        // Nothing should be backed up
        try (Stream<Path> nasFiles = Files.list(nasBackupRoot)) {
            assertFalse(nasFiles.findAny().isPresent());
        }
        verify(backupHistoryRepository).save(argThat(h -> h.getBackupType() == BackupType.MEDIA));
    }

    @Test
    void testSync_WhenEnabledAndFileExists_CopiesToNas() throws Exception {
        // Setup
        config.setEnabled(true);
        service.init();

        // Create a local file
        Path localFile = mediaRoot.resolve("test.txt");
        Files.writeString(localFile, "test content");

        // Sync
        service.sync();

        // Verify file was copied to NAS
        Path backedUpFile = nasBackupRoot.resolve("test.txt");
        assertTrue(Files.exists(backedUpFile));
        assertEquals("test content", Files.readString(backedUpFile));
    }

    @Test
    void testSync_WhenOrphanFileExistsOnNas_DeletesIt() throws Exception {
        // Setup
        config.setEnabled(true);
        service.init();

        // Create an orphan file on NAS (no matching local file)
        Path orphanFile = nasBackupRoot.resolve("orphan.txt");
        Files.writeString(orphanFile, "orphan content");

        // Sync
        service.sync();

        // Verify orphan file was deleted
        assertFalse(Files.exists(orphanFile));
    }

    @Test
    void testConfigDefaults() {
        NasBackupConfig newConfig = new NasBackupConfig();
        assertFalse(newConfig.isEnabled());
        assertNull(newConfig.getMountPath());
        assertNull(newConfig.getSyncCron());
    }
}



