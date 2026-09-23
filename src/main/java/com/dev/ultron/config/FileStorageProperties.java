package com.dev.ultron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageProperties {
    private String uploadDir = "uploads";
    private long maxFileSize = 5242880;
}
