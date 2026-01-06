package com.wan.framework.code.service;

import com.wan.framework.code.domain.CodeItem;
import com.wan.framework.code.dto.CodeItemDTO;
import com.wan.framework.code.exception.CodeException;
import com.wan.framework.code.mapper.CodeItemMapper;
import com.wan.framework.code.repository.CodeGroupRepository;
import com.wan.framework.code.repository.CodeItemRepository;
import com.wan.framework.redis.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.I;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CodeItemService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CodeItemServiceTest {

    @Mock
    private CodeItemRepository codeItemRepository;

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CodeItemMapper codeItemMapper;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private CodeItemService codeItemService;

    private CodeItem testEntity;
    private CodeItemDTO testDTO;

    @BeforeEach
    void setUp() {
        testEntity = CodeItem.builder()
                .id(1L)
                .groupCode("TEST_GROUP")
                .codeValue("TEST_CODE")
                .codeName("테스트 코드")
                .description("테스트용 코드")
                .enabled(true)
                .sortOrder(1)
                .dataState(I)
                .build();

        testDTO = CodeItemDTO.builder()
                .id(1L)
                .groupCode("TEST_GROUP")
                .codeValue("TEST_CODE")
                .codeName("테스트 코드")
                .description("테스트용 코드")
                .enabled(true)
                .sortOrder(1)
                .dataState(I)
                .build();
    }

    @Test
    @DisplayName("코드 항목 생성 - 성공")
    void createCodeItem_Success() {
        // given
        given(codeGroupRepository.existsByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(true);
        given(codeItemRepository.existsByGroupCodeAndCodeValueAndDataStateNot(anyString(), anyString(), any()))
                .willReturn(false);
        given(codeItemMapper.toEntity(any(CodeItemDTO.class)))
                .willReturn(testEntity);
        given(codeItemRepository.save(any(CodeItem.class)))
                .willReturn(testEntity);
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.createCodeItem(testDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCodeValue()).isEqualTo("TEST_CODE");
        verify(codeItemRepository).save(any(CodeItem.class));
        verify(redisCacheService).delete(anyString());
    }

    @Test
    @DisplayName("코드 항목 생성 - 그룹 없음")
    void createCodeItem_GroupNotFound() {
        // given
        given(codeGroupRepository.existsByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> codeItemService.createCodeItem(testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 항목 생성 - 코드 값 중복")
    void createCodeItem_DuplicateValue() {
        // given
        given(codeGroupRepository.existsByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(true);
        given(codeItemRepository.existsByGroupCodeAndCodeValueAndDataStateNot(anyString(), anyString(), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> codeItemService.createCodeItem(testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 항목 생성 - 코드 값 없음")
    void createCodeItem_EmptyValue() {
        // given
        testDTO.setCodeValue(null);

        // when & then
        assertThatThrownBy(() -> codeItemService.createCodeItem(testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 항목 수정 - 성공")
    void updateCodeItem_Success() {
        // given
        given(codeItemRepository.findByIdAndDataStateNot(anyLong(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemRepository.save(any(CodeItem.class)))
                .willReturn(testEntity);
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.updateCodeItem(1L, testDTO);

        // then
        assertThat(result).isNotNull();
        verify(codeItemRepository).save(any(CodeItem.class));
        verify(redisCacheService).delete(anyString());
    }

    @Test
    @DisplayName("코드 항목 수정 - 항목 없음")
    void updateCodeItem_NotFound() {
        // given
        given(codeItemRepository.findByIdAndDataStateNot(anyLong(), any()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> codeItemService.updateCodeItem(1L, testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 항목 삭제 - 성공")
    void deleteCodeItem_Success() {
        // given
        given(codeItemRepository.findByIdAndDataStateNot(anyLong(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemRepository.save(any(CodeItem.class)))
                .willReturn(testEntity);
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.deleteCodeItem(1L);

        // then
        assertThat(result).isNotNull();
        verify(codeItemRepository).save(any(CodeItem.class));
        verify(redisCacheService).delete(anyString());
    }

    @Test
    @DisplayName("코드 항목 조회 - 성공")
    void getCodeItem_Success() {
        // given
        given(codeItemRepository.findByIdAndDataStateNot(anyLong(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.getCodeItem(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("그룹별 코드 조회 - 캐시 없음")
    void getCodeItemsByGroup_CacheMiss() {
        // given
        given(redisCacheService.get(anyString(), eq(List.class)))
                .willReturn(null);
        given(codeItemRepository.findAllByGroupCodeAndDataStateNotOrderBySortOrder(anyString(), any()))
                .willReturn(Arrays.asList(testEntity));
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        List<CodeItemDTO> result = codeItemService.getCodeItemsByGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("활성화된 코드 조회")
    void getEnabledCodeItemsByGroup() {
        // given
        given(codeItemRepository.findAllByGroupCodeAndEnabledTrueAndDataStateNotOrderBySortOrder(anyString(), any()))
                .willReturn(Arrays.asList(testEntity));
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        List<CodeItemDTO> result = codeItemService.getEnabledCodeItemsByGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("전체 코드 조회 - 페이징")
    void getAllCodeItems_Paging() {
        // given
        Page<CodeItem> page = new PageImpl<>(Arrays.asList(testEntity));
        given(codeItemRepository.findAllByDataStateNot(any(), any()))
                .willReturn(page);
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        Page<CodeItemDTO> result = codeItemService.getAllCodeItems(PageRequest.of(0, 10));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("코드 값으로 조회 - 성공")
    void getCodeItemByValue_Success() {
        // given
        given(codeItemRepository.findByGroupCodeAndCodeValueAndDataStateNot(anyString(), anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.getCodeItemByValue("TEST_GROUP", "TEST_CODE");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCodeValue()).isEqualTo("TEST_CODE");
    }

    @Test
    @DisplayName("코드 토글 - 활성화에서 비활성화")
    void toggleCodeItem_EnableToDisable() {
        // given
        testEntity.setEnabled(true);
        given(codeItemRepository.findByIdAndDataStateNot(anyLong(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemRepository.save(any(CodeItem.class)))
                .willReturn(testEntity);
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        CodeItemDTO result = codeItemService.toggleCodeItem(1L);

        // then
        assertThat(result).isNotNull();
        verify(codeItemRepository).save(any(CodeItem.class));
    }

    @Test
    @DisplayName("그룹별 캐시 갱신")
    void refreshGroupCache() {
        // given
        given(codeItemRepository.findAllByGroupCodeAndDataStateNotOrderBySortOrder(anyString(), any()))
                .willReturn(Arrays.asList(testEntity));
        given(codeItemMapper.toDto(any(CodeItem.class)))
                .willReturn(testDTO);

        // when
        codeItemService.refreshGroupCache("TEST_GROUP");

        // then
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }
}
