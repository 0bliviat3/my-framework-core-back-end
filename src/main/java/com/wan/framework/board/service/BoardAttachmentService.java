package com.wan.framework.board.service;

import com.wan.framework.board.domain.BoardAttachment;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.dto.BoardAttachmentDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.board.mapper.BoardAttachmentMapper;
import com.wan.framework.board.repository.BoardAttachmentRepository;
import com.wan.framework.board.repository.BoardDataRepository;
import com.wan.framework.board.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.board.constant.BoardExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardAttachmentService {

    private final BoardAttachmentRepository repository;
    private final BoardAttachmentMapper mapper;
    private final BoardDataRepository boardDataRepository;
    private final FileStorageUtil fileStorageUtil;

    @Transactional
    public BoardAttachmentDTO uploadFile(Long boardDataId, MultipartFile file, String userId) {
        // 게시글 존재 확인
        BoardData boardData = boardDataRepository.findByIdAndDataStateCodeNot(boardDataId, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));

        // 파일 저장
        String savedFileName = fileStorageUtil.storeFile(file);
        String relativePath = fileStorageUtil.getRelativePath(savedFileName);

        // 다음 표시 순서 계산
        int nextOrder = (int) repository.countByBoardDataIdAndDataStateCodeNot(boardDataId, D);

        // BoardAttachment 엔티티 생성
        BoardAttachment attachment = BoardAttachment.builder()
                .boardData(boardData)
                .originalFileName(file.getOriginalFilename())
                .savedFileName(savedFileName)
                .filePath(relativePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .displayOrder(nextOrder)
                .uploadedBy(userId)
                .build();

        BoardAttachment saved = repository.save(attachment);
        log.info("파일 업로드 완료: {}, boardDataId: {}", file.getOriginalFilename(), boardDataId);

        return mapper.toDto(saved);
    }

    @Transactional
    public List<BoardAttachmentDTO> uploadMultipleFiles(Long boardDataId, List<MultipartFile> files, String userId) {
        return files.stream()
                .map(file -> uploadFile(boardDataId, file, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BoardAttachmentDTO> findByBoardDataId(Long boardDataId) {
        return mapper.toDtoList(
                repository.findByBoardDataIdAndDataStateCodeNotOrderByDisplayOrder(boardDataId, D)
        );
    }

    @Transactional(readOnly = true)
    public BoardAttachmentDTO findById(Long id) {
        BoardAttachment attachment = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ATTACHMENT));
        return mapper.toDto(attachment);
    }

    @Transactional
    public Resource downloadFile(Long id) {
        BoardAttachment attachment = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ATTACHMENT));

        try {
            Path filePath = fileStorageUtil.getFilePath(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 다운로드 횟수 증가
                attachment.incrementDownloadCount();
                log.info("파일 다운로드: {}, 다운로드 횟수: {}",
                        attachment.getOriginalFileName(), attachment.getDownloadCount());
                return resource;
            } else {
                throw new BoardException(NOT_FOUND_ATTACHMENT);
            }
        } catch (MalformedURLException e) {
            log.error("파일 다운로드 실패: {}", attachment.getOriginalFileName(), e);
            throw new BoardException(NOT_FOUND_ATTACHMENT);
        }
    }

    @Transactional
    public void deleteAttachment(Long id, String userId) {
        BoardAttachment attachment = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ATTACHMENT));

        // 권한 체크 (게시글 작성자 또는 파일 업로더)
        if (!attachment.getBoardData().getAuthorId().equals(userId)
                && !attachment.getUploadedBy().equals(userId)) {
            throw new BoardException(UNAUTHORIZED_ACCESS);
        }

        // 논리적 삭제
        attachment.setDataStateCode(D);

        // 실제 파일 삭제
        fileStorageUtil.deleteFile(attachment.getFilePath());

        log.info("첨부파일 삭제: {}", attachment.getOriginalFileName());
    }

    @Transactional
    public void deleteAllByBoardData(Long boardDataId) {
        List<BoardAttachment> attachments = repository
                .findByBoardDataIdAndDataStateCodeNotOrderByDisplayOrder(boardDataId, D);

        for (BoardAttachment attachment : attachments) {
            attachment.setDataStateCode(D);
            fileStorageUtil.deleteFile(attachment.getFilePath());
        }

        log.info("게시글의 모든 첨부파일 삭제: boardDataId={}", boardDataId);
    }

    @Transactional(readOnly = true)
    public long getTotalFileSize(Long boardDataId) {
        Long totalSize = repository.sumFileSizeByBoardDataIdAndDataStateCodeNot(boardDataId, D);
        return totalSize != null ? totalSize : 0L;
    }
}
