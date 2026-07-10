package com.webgis.ancientdata.backuptests;

import com.webgis.ancientdata.application.service.BackupStatusService;
import com.webgis.ancientdata.domain.dto.BackupStatusDTO;
import com.webgis.ancientdata.domain.model.BackupHistory;
import com.webgis.ancientdata.domain.model.BackupOutcome;
import com.webgis.ancientdata.domain.model.BackupType;
import com.webgis.ancientdata.domain.repository.BackupHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupStatusServiceTests {

    private BackupHistoryRepository backupHistoryRepository;
    private BackupStatusService service;

    private static final long THRESHOLD_HOURS = 192;

    @BeforeEach
    void setUp() {
        backupHistoryRepository = mock(BackupHistoryRepository.class);
        service = new BackupStatusService(backupHistoryRepository, THRESHOLD_HOURS);
    }

    @Test
    void testGetStatus_WhenNeverBackedUp_IsStale() {
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.DATABASE))
                .thenReturn(Optional.empty());
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.MEDIA))
                .thenReturn(Optional.empty());

        BackupStatusDTO status = service.getStatus();

        assertNull(status.database().lastRunAt());
        assertTrue(status.database().stale());
        assertNull(status.media().lastRunAt());
        assertTrue(status.media().stale());
    }

    @Test
    void testGetStatus_WhenRecentSuccess_IsNotStale() {
        BackupHistory recent = history(Instant.now().minus(1, ChronoUnit.HOURS));
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.DATABASE))
                .thenReturn(Optional.of(recent));
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.MEDIA))
                .thenReturn(Optional.empty());

        BackupStatusDTO status = service.getStatus();

        assertFalse(status.database().stale());
        assertEquals("SUCCESS", status.database().outcome());
    }

    @Test
    void testGetStatus_WhenOlderThanThreshold_IsStale() {
        BackupHistory old = history(Instant.now().minus(THRESHOLD_HOURS + 1, ChronoUnit.HOURS));
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.DATABASE))
                .thenReturn(Optional.of(old));
        when(backupHistoryRepository.findTopByBackupTypeOrderByStartedAtDesc(BackupType.MEDIA))
                .thenReturn(Optional.empty());

        BackupStatusDTO status = service.getStatus();

        assertTrue(status.database().stale());
    }

    private BackupHistory history(Instant startedAt) {
        BackupHistory h = new BackupHistory();
        h.setBackupType(BackupType.DATABASE);
        h.setOutcome(BackupOutcome.SUCCESS);
        h.setStartedAt(startedAt);
        h.setFinishedAt(startedAt.plusSeconds(5));
        h.setMessage("ok");
        return h;
    }
}

