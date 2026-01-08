package com.wan.framework.board.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.dto.BoardDataDTO;
import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.user.constant.RoleType;
import com.wan.framework.user.domain.User;
import com.wan.framework.user.repositroy.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.wan.framework.board.constant.BoardExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BoardDataServiceTest {

    @Autowired
    private BoardDataService service;

    @Autowired
    private BoardMetaService boardMetaService;

    @Autowired
    private UserRepository userRepository;

    private Long testBoardMetaId;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        createTestUser("user1", RoleType.ROLE_USER);
        createTestUser("user2", RoleType.ROLE_USER);
        createTestUser("admin", RoleType.ROLE_ADMIN);

        BoardMetaDTO boardMeta = BoardMetaDTO.builder()
                .title("테스트 게시판")
                .description("테스트용")
                .roles("ROLE_USER")
                .useComment(true)
                .createdBy("admin")
                .build();
        testBoardMetaId = boardMetaService.saveBoardMeta(boardMeta).getId();
    }

    private void createTestUser(String userId, RoleType role) {
        if (!userRepository.existsById(userId)) {
            User user = User.builder()
                    .userId(userId)
                    .password("password")
                    .name(userId)
                    .passwordSalt("salt")
                    .roles(Set.of(role))
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    void 게시글_생성_성공() {
        // given
        BoardDataDTO dto = BoardDataDTO.builder()
                .boardMetaId(testBoardMetaId)
                .title("테스트 게시글")
                .content("내용입니다")
                .status(BoardDataStatus.PUBLISHED)
                .build();

        // when
        BoardDataDTO created = service.createPost(dto, "user1");

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("테스트 게시글");
        assertThat(created.getAuthorId()).isEqualTo("user1");
        assertThat(created.getViewCount()).isZero();
        assertThat(created.getCommentCount()).isZero();
    }

    @Test
    void 임시저장_게시글_생성() {
        // given
        BoardDataDTO dto = BoardDataDTO.builder()
                .boardMetaId(testBoardMetaId)
                .title("임시저장 글")
                .content("작성 중...")
                .status(BoardDataStatus.DRAFT)
                .build();

        // when
        BoardDataDTO created = service.createPost(dto, "user1");

        // then
        assertThat(created.getStatus()).isEqualTo(BoardDataStatus.DRAFT);
        assertThat(created.getPublishedAt()).isNull();
    }

    @Test
    void 게시글_조회_성공() {
        // given
        BoardDataDTO created = createTestPost("조회 테스트", "user1");

        // when
        BoardDataDTO found = service.findById(created.getId(), "user1");

        // then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("조회 테스트");
    }

    @Test
    void 조회수_증가() {
        // given
        BoardDataDTO created = createTestPost("조회수 테스트", "user1");
        Long initialViewCount = created.getViewCount();

        // when
        service.incrementViewCount(created.getId());
        service.incrementViewCount(created.getId());

        // then
        BoardDataDTO updated = service.findById(created.getId(), "user1");
        assertThat(updated.getViewCount()).isEqualTo(initialViewCount + 2);
    }

    @Test
    void 게시글_목록_조회() {
        // given
        createTestPost("게시글1", "user1");
        createTestPost("게시글2", "user2");
        createTestPost("게시글3", "user1");

        // when
        Page<BoardDataDTO> page = service.findByBoardMeta(testBoardMetaId, PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 제목으로_검색() {
        // given
        createTestPost("스프링 부트 강의", "user1");
        createTestPost("자바 기초", "user1");
        createTestPost("스프링 JPA", "user2");

        // when
        Page<BoardDataDTO> results = service.searchByTitle(
                testBoardMetaId, "스프링", PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .allMatch(dto -> dto.getTitle().contains("스프링"));
    }

    @Test
    void 게시글_수정_성공() {
        // given
        BoardDataDTO created = createTestPost("원본 제목", "user1");

        BoardDataDTO updateDto = BoardDataDTO.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        // when
        BoardDataDTO updated = service.updatePost(created.getId(), updateDto, "user1");

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        assertThat(updated.getDataStateCode()).isEqualTo(DataStateCode.U);
    }

    @Test
    void 다른_사용자가_수정시_예외발생() {
        // given
        BoardDataDTO created = createTestPost("원본", "user1");

        BoardDataDTO updateDto = BoardDataDTO.builder()
                .title("수정 시도")
                .build();

        // when & then
        assertThatThrownBy(() -> service.updatePost(created.getId(), updateDto, "user2"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(CANNOT_MODIFY_OTHER_POST.getMessage());
    }

    @Test
    void 게시글_삭제_성공() {
        // given
        BoardDataDTO created = createTestPost("삭제할 게시글", "user1");

        // when
        service.deletePost(created.getId(), "user1");

        // then
        assertThatThrownBy(() -> service.findById(created.getId(), "user1"))
                .isInstanceOf(BoardException.class);
    }

    @Test
    void 다른_사용자가_삭제시_예외발생() {
        // given
        BoardDataDTO created = createTestPost("삭제 테스트", "user1");

        // when & then
        assertThatThrownBy(() -> service.deletePost(created.getId(), "user2"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(CANNOT_DELETE_OTHER_POST.getMessage());
    }

    // 헬퍼 메서드
    private BoardDataDTO createTestPost(String title, String authorId) {
        BoardDataDTO dto = BoardDataDTO.builder()
                .boardMetaId(testBoardMetaId)
                .title(title)
                .content("내용")
                .status(BoardDataStatus.PUBLISHED)
                .build();
        return service.createPost(dto, authorId);
    }
}
