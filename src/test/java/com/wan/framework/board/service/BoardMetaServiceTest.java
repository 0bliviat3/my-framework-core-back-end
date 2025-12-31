package com.wan.framework.board.service;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.exception.BoardException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.board.constant.BoardExceptionMessage.DUPLICATED_TITLE;
import static com.wan.framework.board.constant.BoardExceptionMessage.NOT_FOUND_META;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BoardMetaServiceTest {

    @Autowired
    private BoardMetaService service;

    @Test
    void 게시판_생성_성공() {
        // given
        BoardMetaDTO dto = BoardMetaDTO.builder()
                .title("공지사항")
                .description("공지사항 게시판")
                .roles("ROLE_USER")
                .useComment(true)
                .createdBy("admin")
                .build();

        // when
        BoardMetaDTO created = service.saveBoardMeta(dto);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("공지사항");
        assertThat(created.getDataStateCode()).isEqualTo(DataStateCode.I);
        assertThat(created.getAbleState()).isEqualTo(AbleState.ABLE);
    }

    @Test
    void 중복된_제목으로_생성시_예외발생() {
        // given
        createTestBoardMeta("자유게시판");

        BoardMetaDTO duplicate = BoardMetaDTO.builder()
                .title("자유게시판")
                .description("중복 테스트")
                .build();

        // when & then
        assertThatThrownBy(() -> service.saveBoardMeta(duplicate))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(DUPLICATED_TITLE.getMessage());
    }

    @Test
    void 게시판_조회_성공() {
        // given
        BoardMetaDTO created = createTestBoardMeta("Q&A 게시판");

        // when
        BoardMetaDTO found = service.findById(created.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Q&A 게시판");
    }

    @Test
    void 존재하지_않는_게시판_조회시_예외발생() {
        // when & then
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(NOT_FOUND_META.getMessage());
    }

    @Test
    void 게시판_목록_조회_페이징() {
        // given
        createTestBoardMeta("게시판1");
        createTestBoardMeta("게시판2");
        createTestBoardMeta("게시판3");

        // when
        Page<BoardMetaDTO> page = service.findAll(PageRequest.of(0, 10));

        // then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 게시판_수정_성공() {
        // given
        BoardMetaDTO created = createTestBoardMeta("수정 전 제목");

        BoardMetaDTO updateDto = BoardMetaDTO.builder()
                .id(created.getId())
                .title("수정 전 제목")
                .description("수정된 설명")
                .build();

        // when
        BoardMetaDTO updated = service.modifyBoardMeta(updateDto);

        // then
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getDataStateCode()).isEqualTo(DataStateCode.U);
    }

    @Test
    void 게시판_삭제_성공() {
        // given
        BoardMetaDTO created = createTestBoardMeta("삭제할 게시판");

        // when
        service.deleteBoardMeta(created.getId());

        // then
        assertThatThrownBy(() -> service.findById(created.getId()))
                .isInstanceOf(BoardException.class);
    }

    @Test
    void 게시판_복제_성공() {
        // given
        BoardMetaDTO original = createTestBoardMeta("원본 게시판");

        // when
        BoardMetaDTO cloned = service.cloneBoardMeta(original.getId(), "복제된 게시판");

        // then
        assertThat(cloned).isNotNull();
        assertThat(cloned.getId()).isNotEqualTo(original.getId());
        assertThat(cloned.getTitle()).isEqualTo("복제된 게시판");
        assertThat(cloned.getDescription()).isEqualTo(original.getDescription());
    }

    @Test
    void 게시판_복제시_중복제목_예외발생() {
        // given
        BoardMetaDTO original = createTestBoardMeta("원본");
        createTestBoardMeta("이미 존재");

        // when & then
        assertThatThrownBy(() -> service.cloneBoardMeta(original.getId(), "이미 존재"))
                .isInstanceOf(BoardException.class)
                .hasMessageContaining(DUPLICATED_TITLE.getMessage());
    }

    // 헬퍼 메서드
    private BoardMetaDTO createTestBoardMeta(String title) {
        BoardMetaDTO dto = BoardMetaDTO.builder()
                .title(title)
                .description("설명")
                .roles("ROLE_USER")
                .useComment(true)
                .createdBy("admin")
                .build();
        return service.saveBoardMeta(dto);
    }
}
