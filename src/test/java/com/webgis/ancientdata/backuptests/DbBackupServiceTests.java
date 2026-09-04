package com.webgis.ancientdata.backuptests;

import com.webgis.ancientdata.application.service.DbBackupService;
import com.webgis.ancientdata.application.service.support.ProcessExecutor;
import com.webgis.ancientdata.config.DbBackupConfig;
import com.webgis.ancientdata.domain.model.BackupHistory;
import com.webgis.ancientdata.domain.model.BackupOutcome;
import com.webgis.ancientdata.domain.model.BackupRunResult;
import com.webgis.ancientdata.domain.model.BackupType;
import com.webgis.ancientdata.domain.repository.BackupHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DbBackupServiceTests {

    private DbBackupConfig config;
    private BackupHistoryRepository backupHistoryRepository;
    private ProcessExecutor processExecutor;
    private DbBackupService service;

    @TempDir
    Path outputDir;

    @BeforeEach
    void setUp() {
        config = new DbBackupConfig();
        config.setEnabled(true);
        config.setHost("localhost");
        config.setPort(5432);
        config.setName("testdb");
        config.setUser("testuser");
        config.setPassword("testpass");
        config.setOutputPath(outputDir.toString());

        backupHistoryRepository = mock(BackupHistoryRepository.class);
        processExecutor = mock(ProcessExecutor.class);

        service = new DbBackupService(config, backupHistoryRepository, processExecutor);
    }

    @Test
    void testRun_WhenDisabled_DoesNothing() {
        config.setEnabled(false);
        service.run();

        verifyNoInteractions(processExecutor);
        verifyNoInteractions(backupHistoryRepository);
    }

    @Test
    void testRun_WhenPgDumpSucceeds_RecordsSuccess() throws Exception {
        when(processExecutor.runToFile(any(), any(), any())).thenReturn(0);

        service.run();

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);
        verify(backupHistoryRepository).save(captor.capture());

        BackupHistory saved = captor.getValue();
        assertEquals(BackupType.DATABASE, saved.getBackupType());
        assertEquals(BackupOutcome.SUCCESS, saved.getOutcome());
    }

    @Test
    void testRun_WhenPgDumpFails_RecordsFailure() throws Exception {
        when(processExecutor.runToFile(any(), any(), any())).thenReturn(1);

        service.run();

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);
        verify(backupHistoryRepository).save(captor.capture());

        BackupHistory saved = captor.getValue();
        assertEquals(BackupType.DATABASE, saved.getBackupType());
        assertEquals(BackupOutcome.FAILURE, saved.getOutcome());
    }

    @Test
    void testRun_WhenProcessExecutorThrows_RecordsFailure() throws Exception {
        when(processExecutor.runToFile(any(), any(), any())).thenThrow(new java.io.IOException("pg_dump not found"));

        service.run();

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);
        verify(backupHistoryRepository).save(captor.capture());

        BackupHistory saved = captor.getValue();
        assertEquals(BackupOutcome.FAILURE, saved.getOutcome());
        assertTrue(saved.getMessage().contains("pg_dump not found"));
    }

    @Test
    void testRun_WhenPgDumpSucceeds_ReturnsSuccessResult() throws Exception {
        when(processExecutor.runToFile(any(), any(), any())).thenReturn(0);

        BackupRunResult result = service.run();

        assertEquals(BackupOutcome.SUCCESS, result.outcome());
    }

    @Test
    void testRun_WhenPgDumpFails_ReturnsFailureResult() throws Exception {
        when(processExecutor.runToFile(any(), any(), any())).thenReturn(1);

        BackupRunResult result = service.run();

        assertEquals(BackupOutcome.FAILURE, result.outcome());
    }

    @Test
    void testRun_WhenBackupHistoryPersistenceFails_StillReturnsRealOutcome() throws Exception {
        // Simulates backup_history being missing on the shared, manually-migrated
        // production database (docs/architecture/DB-MIGRATION-STRATEGY.md) — the
        // real pg_dump outcome must still reach the caller instead of a 500.
        when(processExecutor.runToFile(any(), any(), any())).thenReturn(0);
        doThrow(new org.springframework.dao.InvalidDataAccessResourceUsageException("relation \"backup_history\" does not exist"))
                .when(backupHistoryRepository).save(any());

        BackupRunResult result = service.run();

        assertEquals(BackupOutcome.SUCCESS, result.outcome());
        assertTrue(result.message().contains("Database backup written to"));
    }
}

