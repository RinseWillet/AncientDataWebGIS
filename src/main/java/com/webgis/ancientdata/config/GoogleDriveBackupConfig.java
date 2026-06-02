package com.webgis.ancientdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "backup.gdrive")
public class GoogleDriveBackupConfig {

    private boolean enabled = false;
    private String serviceAccountKeyPath = "";
    private String folderId = "";
    private String syncCron = "0 0 3 * * SUN";
}

