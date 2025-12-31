package com.wan.framework.board.web;

import com.wan.framework.board.dto.BoardAttachmentDTO;
import com.wan.framework.board.service.BoardAttachmentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board-attachments")
public class BoardAttachmentController {

    private final BoardAttachmentService service;

    @PostMapping("/upload/{boardDataId}")
    public ResponseEntity<BoardAttachmentDTO> uploadFile(
            @PathVariable Long boardDataId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.uploadFile(boardDataId, file, userId));
    }

    @PostMapping("/upload-multiple/{boardDataId}")
    public ResponseEntity<List<BoardAttachmentDTO>> uploadMultipleFiles(
            @PathVariable Long boardDataId,
            @RequestParam("files") List<MultipartFile> files,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.uploadMultipleFiles(boardDataId, files, userId));
    }

    @GetMapping("/board-data/{boardDataId}")
    public ResponseEntity<List<BoardAttachmentDTO>> getAttachments(@PathVariable Long boardDataId) {
        return ResponseEntity.ok(service.findByBoardDataId(boardDataId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardAttachmentDTO> getAttachment(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        BoardAttachmentDTO attachment = service.findById(id);
        Resource resource = service.downloadFile(id);

        String encodedFileName = encodeFileName(attachment.getOriginalFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.deleteAttachment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/board-data/{boardDataId}/total-size")
    public ResponseEntity<Long> getTotalFileSize(@PathVariable Long boardDataId) {
        return ResponseEntity.ok(service.getTotalFileSize(boardDataId));
    }

    /**
     * 파일명 인코딩 (한글 파일명 지원)
     */
    private String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return fileName;
        }
    }
}
