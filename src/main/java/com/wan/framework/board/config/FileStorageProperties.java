package com.wan.framework.board.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileStorageProperties {

    private String uploadDir = "uploads/board"; // 기본 업로드 디렉토리
    private long maxFileSize = 10485760; // 10MB (bytes)
    private long maxRequestSize = 52428800; // 50MB (bytes)
    private String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "pdf",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "zip", "rar"
    };
}
