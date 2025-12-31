package com.wan.framework.board.service;

import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.dto.BoardAttachmentDTO;
import com.wan.framework.board.dto.BoardDataDTO;
import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.exception.BoardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.wan.framework.board.constant.BoardExceptionMessage.NOT_FOUND_ATTACHMENT;
import static com.wan.framework.board.constant.BoardExceptionMessage.UNAUTHORIZED_ACCESS;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BoardAttachmentServiceTest {

    @Autowired
    private BoardAttachmentService service;

    @Autowired
    private BoardDataService boardDataService;

    @Autowired
    private BoardMetaService boardMetaService;

    private Long testBoardDataId;

    @BeforeEach
    void setUp() {
        BoardMetaDTO boardMeta = BoardMetaDTO.builder()
                .title("첨부파일 테스트 게시판")
                .description("테스트용")
                .roles("ROLE_USER")
                .useComment(true)
                .createdBy("admin")
                .build();
        Long boardMetaId = boardMetaService.saveBoardMeta(boardMeta).getId();

        BoardDataDTO boardData = BoardDataDTO.builder()
                .boardMetaId(boardMetaId)
                .title("첨부파일 테스트 게시글")
                .content("내용")
                .status(BoardDataStatus.PUBLISHED)
                .build();
        testBoardDataId = boardDataService.createPost(boardData, "user1").getId();
    }

    @Test
    void 파일_업로드_성공() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test file content".getBytes()
        );

        // when
        BoardAttachmentDTO uploaded = service.uploadFile(testBoardDataId, file, "user1");

        // then
        assertThat(uploaded).isNotNull();
        assertThat(uploaded.getId()).isNotNull();
        assertThat(uploaded.getOriginalFileName()).isEqualTo("test.txt");
        assertThat(uploaded.getFileSize()).isEqualTo(file.getSize());
        assertThat(uploaded.getContentType()).isEqualTo("text/plain");
        assertThat(uploaded.getUploadedBy()).isEqualTo("user1");
        assertThat(uploaded.getDownloadCount()).isZero();
    }

    @Test
    void 여러_파일_업로드_성공() {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "file1.txt", "text/plain", "Content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "file2.txt", "text/plain", "Content 2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile(
                "file3", "file3.txt", "text/plain", "Content 3".getBytes());

        // when
        List<BoardAttachmentDTO> uploaded = service.uploadMultipleFiles(
                testBoardDataId,
                Arrays.asList(file1, file2, file3),
                "user1"
        );

        // then
        assertThat(uploaded).hasSize(3);
        assertThat(uploaded.get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(uploaded.get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(uploaded.get(2).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void 게시글별_첨부파일_조회() {
        // given
        uploadTestFile("file1.txt");
        uploadTestFile("file2.txt");
        uploadTestFile("file3.txt");

        // when
        List<BoardAttachmentDTO> attachments = service.findByBoardDataId(testBoardDataId);

        // then
        assertThat(attachments).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 첨부파일_단건_조회() {
        // given
        BoardAttachmentDTO uploaded = uploadTestFile("test.txt");

        // when
        BoardAttachmentDTO found = service.findById(uploaded.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    void 파일_다운로드_성공() {
        // given
        BoardAttachmentDTO uploaded = uploadTestFile("download-test.txt");

        // when
        Resource resource = service.downloadFile(uploaded.getId());

        // then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();

        // 다운로드 횟수 증가 확인
        BoardAttachmentDTO updated = service.findById(uploaded.getId());
        assertThat(updated.getDownloadCount()).isEqualTo(1L);
    }

    @Test
    void 여러번_다운로드시_카운트_증가() {
        // given
        BoardAttachmentDTO uploaded = uploadTestFile("test.txt");

        // when
        service.downloadFile(uploaded.getId());
        service.downloadFile(uploaded.getId());
        service.downloadFile(uploaded.getId());

        // then
        BoardAttachmentDTO updated = service.findById(uploaded.getId());
        assertThat(updated.getDownloadCount()).isEqualTo(3L);
    }

    @Test
    void 첨부파일_삭제_성공() {
        // given
        BoardAttachmentDTO uploaded = uploadTestFile("delete-test.txt");

        // when
        service.deleteAttachment(uploaded.getId(), "user1");

        // then
        assertThatThrownBy(() -> service.findById(uploaded.getId()))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(NOT_FOUND_ATTACHMENT.getMessage());
    }

    @Test
    void 권한없는_사용자가_삭제시_예외발생() {
        // given
        BoardAttachmentDTO uploaded = uploadTestFile("test.txt");

        // when & then
        assertThatThrownBy(() -> service.deleteAttachment(uploaded.getId(), "user2"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    void 게시글의_모든_첨부파일_삭제() {
        // given
        uploadTestFile("file1.txt");
        uploadTestFile("file2.txt");
        uploadTestFile("file3.txt");

        // when
        service.deleteAllByBoardData(testBoardDataId);

        // then
        List<BoardAttachmentDTO> attachments = service.findByBoardDataId(testBoardDataId);
        assertThat(attachments).isEmpty();
    }

    @Test
    void 총_파일_크기_계산() {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "file1", "file1.txt", "text/plain", new byte[1000]);
        MockMultipartFile file2 = new MockMultipartFile(
                "file2", "file2.txt", "text/plain", new byte[2000]);

        service.uploadFile(testBoardDataId, file1, "user1");
        service.uploadFile(testBoardDataId, file2, "user1");

        // when
        long totalSize = service.getTotalFileSize(testBoardDataId);

        // then
        assertThat(totalSize).isEqualTo(3000L);
    }

    @Test
    void 존재하지_않는_파일_다운로드시_예외발생() {
        // when & then
        assertThatThrownBy(() -> service.downloadFile(999L))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(NOT_FOUND_ATTACHMENT.getMessage());
    }

    // 헬퍼 메서드
    private BoardAttachmentDTO uploadTestFile(String fileName) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                "Test content".getBytes()
        );
        return service.uploadFile(testBoardDataId, file, "user1");
    }
}
