package com.webgis.ancientdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for NAS-based media backup.
 * Properties: backup.nas.enabled, backup.nas.mount-path, backup.nas.sync-cron
 */
@Component
@ConfigurationProperties(prefix = "backup.nas")
@Getter
@Setter
public class NasBackupConfig {

    /**
     * Enable/disable NAS backup service.
     */
    private boolean enabled = false;

    /**
     * Mount path of the NAS backup directory.
     * e.g., /backup/media
     */
    private String mountPath;

    /**
     * Cron expression for scheduled sync.
     * Default: every Sunday at 3 AM UTC
     */
    private String syncCron;
}

