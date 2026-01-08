package com.wan.framework.board.util;

import com.wan.framework.board.config.FileStorageProperties;
import com.wan.framework.board.exception.BoardException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import static com.wan.framework.board.constant.BoardExceptionMessage.FILE_UPLOAD_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageUtil {

    private final FileStorageProperties properties;

    /**
     * 파일을 저장하고 저장된 파일명을 반환
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = createUploadDirectory();

            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String savedFileName = generateFileName(extension);

            // 파일 저장
            Path targetLocation = uploadPath.resolve(savedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 성공: {}", savedFileName);
            return savedFileName;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
            throw new BoardException(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = getFilePath(fileName);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 성공: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
        }
    }

    /**
     * 파일 경로 반환 (경로 조작 공격 방어)
     */
    public Path getFilePath(String fileName) {
        // 1. 파일명 유효성 검증
        if (fileName == null || fileName.isEmpty()) {
            throw new SecurityException("Invalid file name");
        }

        // 2. 경로 조작 문자 검사
        if (fileName.contains("..")) {
            log.error("Path traversal attempt detected: {}", fileName);
            throw new SecurityException("Path traversal attempt detected");
        }

        // 3. 경로 정규화
        Path uploadDir = Paths.get(properties.getUploadDir()).normalize().toAbsolutePath();
        Path filePath = uploadDir.resolve(fileName).normalize().toAbsolutePath();

        // 4. 파일 경로가 업로드 디렉토리 내에 있는지 검증
        if (!filePath.startsWith(uploadDir)) {
            log.error("File access denied. Path: {}, Upload dir: {}", filePath, uploadDir);
            throw new SecurityException("File access denied");
        }

        // 5. 심볼릭 링크 검사
        if (Files.exists(filePath) && Files.isSymbolicLink(filePath)) {
            log.error("Symbolic link access denied: {}", filePath);
            throw new SecurityException("Symbolic link access denied");
        }

        return filePath;
    }

    /**
     * 파일 유효성 검증 (보안 강화)
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BoardException(FILE_UPLOAD_FAILED);
        }

        // 파일 크기 체크
        if (file.getSize() > properties.getMaxFileSize()) {
            log.warn("File size exceeds limit: {} bytes", file.getSize());
            throw new BoardException(FILE_UPLOAD_FAILED);
        }

        // 확장자 체크
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            log.warn("File extension not allowed: {}", extension);
            throw new BoardException(FILE_UPLOAD_FAILED);
        }

        // MIME Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            log.warn("Content type not allowed: {}", contentType);
            throw new BoardException(FILE_UPLOAD_FAILED);
        }

        // 파일명에 위험한 문자 포함 여부 확인
        if (originalFilename != null && (originalFilename.contains("..") ||
                originalFilename.contains("/") ||
                originalFilename.contains("\\"))) {
            log.warn("Invalid filename detected: {}", originalFilename);
            throw new SecurityException("Invalid filename");
        }
    }

    /**
     * 허용된 확장자인지 확인
     */
    private boolean isAllowedExtension(String extension) {
        return Arrays.asList(properties.getAllowedExtensions())
                .contains(extension.toLowerCase());
    }

    /**
     * 허용된 MIME Type인지 확인
     */
    private boolean isAllowedContentType(String contentType) {
        // 허용된 MIME Type 목록
        String[] allowedMimeTypes = {
                // 이미지
                "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
                // 문서
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                // 압축
                "application/zip",
                "application/x-zip-compressed"
        };

        return Arrays.asList(allowedMimeTypes).contains(contentType.toLowerCase());
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 업로드 디렉토리 생성
     */
    private Path createUploadDirectory() throws IOException {
        // 날짜별 폴더 생성 (예: uploads/board/2025/01/15)
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Path uploadPath = Paths.get(properties.getUploadDir(), datePath);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        return uploadPath;
    }

    /**
     * 고유한 파일명 생성 (UUID + 확장자)
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 파일 상대 경로 반환 (DB 저장용)
     */
    public String getRelativePath(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + fileName;
    }
}
