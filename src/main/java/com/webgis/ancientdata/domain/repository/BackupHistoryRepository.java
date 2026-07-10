package com.webgis.ancientdata.domain.repository;

import com.webgis.ancientdata.domain.model.BackupHistory;
import com.webgis.ancientdata.domain.model.BackupType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackupHistoryRepository extends JpaRepository<BackupHistory, Long> {

    Optional<BackupHistory> findTopByBackupTypeOrderByStartedAtDesc(BackupType backupType);
}

