package com.wan.framework.board.service;

import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.dto.BoardCommentDTO;
import com.wan.framework.board.dto.BoardDataDTO;
import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.exception.BoardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wan.framework.board.constant.BoardExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BoardCommentServiceTest {

    @Autowired
    private BoardCommentService service;

    @Autowired
    private BoardDataService boardDataService;

    @Autowired
    private BoardMetaService boardMetaService;

    private Long testBoardDataId;

    @BeforeEach
    void setUp() {
        BoardMetaDTO boardMeta = BoardMetaDTO.builder()
                .title("테스트 게시판")
                .description("테스트용")
                .roles("ROLE_USER")
                .useComment(true)
                .createdBy("admin")
                .build();
        Long boardMetaId = boardMetaService.saveBoardMeta(boardMeta).getId();

        BoardDataDTO boardData = BoardDataDTO.builder()
                .boardMetaId(boardMetaId)
                .title("테스트 게시글")
                .content("내용")
                .status(BoardDataStatus.PUBLISHED)
                .build();
        testBoardDataId = boardDataService.createPost(boardData, "user1").getId();
    }

    @Test
    void 댓글_생성_성공() {
        // given
        BoardCommentDTO dto = BoardCommentDTO.builder()
                .boardDataId(testBoardDataId)
                .content("좋은 글 감사합니다!")
                .build();

        // when
        BoardCommentDTO created = service.createComment(dto, "user2");

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getContent()).isEqualTo("좋은 글 감사합니다!");
        assertThat(created.getAuthorId()).isEqualTo("user2");
        assertThat(created.getIsModified()).isFalse();
    }

    @Test
    void 대댓글_생성_성공() {
        // given
        BoardCommentDTO parent = createTestComment("부모 댓글", "user1");

        BoardCommentDTO child = BoardCommentDTO.builder()
                .boardDataId(testBoardDataId)
                .parentId(parent.getId())
                .content("대댓글입니다")
                .build();

        // when
        BoardCommentDTO created = service.createComment(child, "user2");

        // then
        assertThat(created.getParentId()).isEqualTo(parent.getId());
    }

    @Test
    void 게시글별_댓글_목록_조회() {
        // given
        createTestComment("첫 번째 댓글", "user1");
        createTestComment("두 번째 댓글", "user2");
        createTestComment("세 번째 댓글", "user3");

        // when
        List<BoardCommentDTO> comments = service.findByBoardDataId(testBoardDataId);

        // then
        assertThat(comments).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 계층형_댓글_조회() {
        // given
        BoardCommentDTO parent = createTestComment("부모 댓글", "user1");

        BoardCommentDTO child1 = BoardCommentDTO.builder()
                .boardDataId(testBoardDataId)
                .parentId(parent.getId())
                .content("대댓글1")
                .build();
        service.createComment(child1, "user2");

        BoardCommentDTO child2 = BoardCommentDTO.builder()
                .boardDataId(testBoardDataId)
                .parentId(parent.getId())
                .content("대댓글2")
                .build();
        service.createComment(child2, "user3");

        // when
        List<BoardCommentDTO> comments = service.findByBoardDataId(testBoardDataId);

        // then
        BoardCommentDTO parentComment = comments.stream()
                .filter(c -> c.getId().equals(parent.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(parentComment.getChildren()).hasSize(2);
    }

    @Test
    void 댓글_수정_성공() {
        // given
        BoardCommentDTO created = createTestComment("원본 댓글", "user1");

        // when
        BoardCommentDTO updated = service.updateComment(created.getId(), "수정된 댓글", "user1");

        // then
        assertThat(updated.getContent()).isEqualTo("수정된 댓글");
        assertThat(updated.getIsModified()).isTrue();
    }

    @Test
    void 다른_사용자가_댓글_수정시_예외발생() {
        // given
        BoardCommentDTO created = createTestComment("댓글", "user1");

        // when & then
        assertThatThrownBy(() -> service.updateComment(created.getId(), "수정 시도", "user2"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(CANNOT_MODIFY_OTHER_COMMENT.getMessage());
    }

    @Test
    void 댓글_삭제_성공() {
        // given
        BoardCommentDTO created = createTestComment("삭제할 댓글", "user1");

        // when
        service.deleteComment(created.getId(), "user1");

        // then
        List<BoardCommentDTO> comments = service.findByBoardDataId(testBoardDataId);
        assertThat(comments).noneMatch(c -> c.getId().equals(created.getId()));
    }

    @Test
    void 다른_사용자가_댓글_삭제시_예외발생() {
        // given
        BoardCommentDTO created = createTestComment("댓글", "user1");

        // when & then
        assertThatThrownBy(() -> service.deleteComment(created.getId(), "user2"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(CANNOT_DELETE_OTHER_COMMENT.getMessage());
    }

    // 헬퍼 메서드
    private BoardCommentDTO createTestComment(String content, String authorId) {
        BoardCommentDTO dto = BoardCommentDTO.builder()
                .boardDataId(testBoardDataId)
                .content(content)
                .build();
        return service.createComment(dto, authorId);
    }
}
