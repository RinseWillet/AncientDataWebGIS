package com.webgis.ancientdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for database backup (pg_dump).
 * Properties: backup.db.enabled, backup.db.host, backup.db.port, backup.db.name,
 * backup.db.user, backup.db.password, backup.db.output-path
 */
@Component
@ConfigurationProperties(prefix = "backup.db")
@Getter
@Setter
public class DbBackupConfig {

    /**
     * Enable/disable database backup (pg_dump).
     */
    private boolean enabled = false;

    private String host;
    private int port = 5432;
    private String name;
    private String user;
    private String password;

    /**
     * Directory where pg_dump output files are written.
     * e.g., /backup/db
     */
    private String outputPath;
}

