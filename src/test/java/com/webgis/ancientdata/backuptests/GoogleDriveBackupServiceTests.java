package com.webgis.ancientdata.backuptests;

import com.webgis.ancientdata.application.service.GoogleDriveBackupService;
import com.webgis.ancientdata.config.GoogleDriveBackupConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleDriveBackupServiceTests {

    @Test
    void init_whenDisabled_doesNotBuildDriveService() {
        GoogleDriveBackupConfig config = new GoogleDriveBackupConfig();
        config.setEnabled(false);

        GoogleDriveBackupService service = new GoogleDriveBackupService(config, "./media");
        service.init();

        // driveService should remain null when disabled
        assertNull(ReflectionTestUtils.getField(service, "driveService"));
    }

    @Test
    void scheduledSync_whenDisabled_doesNothing() {
        GoogleDriveBackupConfig config = new GoogleDriveBackupConfig();
        config.setEnabled(false);

        GoogleDriveBackupService service = new GoogleDriveBackupService(config, "./media");

        // Should not throw — just returns early
        assertDoesNotThrow(service::scheduledSync);
    }

    @Test
    void sync_whenNotInitialised_logsWarningAndReturns() {
        GoogleDriveBackupConfig config = new GoogleDriveBackupConfig();
        config.setEnabled(true);
        // Don't call init() — driveService stays null

        GoogleDriveBackupService service = new GoogleDriveBackupService(config, "./media");

        // Should not throw — logs warning and returns
        assertDoesNotThrow(service::sync);
    }

    @Test
    void config_defaultValues() {
        GoogleDriveBackupConfig config = new GoogleDriveBackupConfig();

        assertFalse(config.isEnabled());
        assertEquals("", config.getServiceAccountKeyPath());
        assertEquals("", config.getFolderId());
        assertEquals("0 0 3 * * SUN", config.getSyncCron());
    }

    @Test
    void config_settersAndGetters() {
        GoogleDriveBackupConfig config = new GoogleDriveBackupConfig();
        config.setEnabled(true);
        config.setServiceAccountKeyPath("/path/to/key.json");
        config.setFolderId("abc123");
        config.setSyncCron("0 0 * * * *");

        assertTrue(config.isEnabled());
        assertEquals("/path/to/key.json", config.getServiceAccountKeyPath());
        assertEquals("abc123", config.getFolderId());
        assertEquals("0 0 * * * *", config.getSyncCron());
    }
}

