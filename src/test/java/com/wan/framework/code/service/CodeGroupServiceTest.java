package com.wan.framework.code.service;

import com.wan.framework.code.domain.CodeGroup;
import com.wan.framework.code.dto.CodeGroupDTO;
import com.wan.framework.code.exception.CodeException;
import com.wan.framework.code.mapper.CodeGroupMapper;
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
 * CodeGroupService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CodeGroupServiceTest {

    @Mock
    private CodeGroupRepository codeGroupRepository;

    @Mock
    private CodeItemRepository codeItemRepository;

    @Mock
    private CodeGroupMapper codeGroupMapper;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private CodeGroupService codeGroupService;

    private CodeGroup testEntity;
    private CodeGroupDTO testDTO;

    @BeforeEach
    void setUp() {
        testEntity = CodeGroup.builder()
                .groupCode("TEST_GROUP")
                .groupName("테스트 그룹")
                .description("테스트용 그룹")
                .enabled(true)
                .sortOrder(1)
                .dataState(I)
                .build();

        testDTO = CodeGroupDTO.builder()
                .groupCode("TEST_GROUP")
                .groupName("테스트 그룹")
                .description("테스트용 그룹")
                .enabled(true)
                .sortOrder(1)
                .dataState(I)
                .build();
    }

    @Test
    @DisplayName("코드 그룹 생성 - 성공")
    void createCodeGroup_Success() {
        // given
        given(codeGroupRepository.existsByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(false);
        given(codeGroupMapper.toEntity(any(CodeGroupDTO.class)))
                .willReturn(testEntity);
        given(codeGroupRepository.save(any(CodeGroup.class)))
                .willReturn(testEntity);
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.createCodeGroup(testDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupCode()).isEqualTo("TEST_GROUP");
        verify(codeGroupRepository).save(any(CodeGroup.class));
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("코드 그룹 생성 - 그룹 코드 중복")
    void createCodeGroup_DuplicateGroupCode() {
        // given
        given(codeGroupRepository.existsByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> codeGroupService.createCodeGroup(testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 그룹 생성 - 그룹 코드 없음")
    void createCodeGroup_EmptyGroupCode() {
        // given
        testDTO.setGroupCode(null);

        // when & then
        assertThatThrownBy(() -> codeGroupService.createCodeGroup(testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 그룹 수정 - 성공")
    void updateCodeGroup_Success() {
        // given
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeGroupRepository.save(any(CodeGroup.class)))
                .willReturn(testEntity);
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.updateCodeGroup("TEST_GROUP", testDTO);

        // then
        assertThat(result).isNotNull();
        verify(codeGroupRepository).save(any(CodeGroup.class));
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("코드 그룹 수정 - 그룹 없음")
    void updateCodeGroup_NotFound() {
        // given
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> codeGroupService.updateCodeGroup("INVALID", testDTO))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 그룹 삭제 - 성공")
    void deleteCodeGroup_Success() {
        // given
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemRepository.countByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(0L);
        given(codeGroupRepository.save(any(CodeGroup.class)))
                .willReturn(testEntity);
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.deleteCodeGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        verify(codeGroupRepository).save(any(CodeGroup.class));
        verify(redisCacheService).delete(anyString());
    }

    @Test
    @DisplayName("코드 그룹 삭제 - 하위 항목 존재")
    void deleteCodeGroup_HasItems() {
        // given
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeItemRepository.countByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(5L);

        // when & then
        assertThatThrownBy(() -> codeGroupService.deleteCodeGroup("TEST_GROUP"))
                .isInstanceOf(CodeException.class);
    }

    @Test
    @DisplayName("코드 그룹 조회 - 캐시 없음")
    void getCodeGroup_CacheMiss() {
        // given
        given(redisCacheService.get(anyString(), eq(CodeGroupDTO.class)))
                .willReturn(null);
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.getCodeGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupCode()).isEqualTo("TEST_GROUP");
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }

    @Test
    @DisplayName("코드 그룹 조회 - 캐시 히트")
    void getCodeGroup_CacheHit() {
        // given
        given(redisCacheService.get(anyString(), eq(CodeGroupDTO.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.getCodeGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGroupCode()).isEqualTo("TEST_GROUP");
    }

    @Test
    @DisplayName("전체 그룹 조회 - 페이징")
    void getAllCodeGroups_Paging() {
        // given
        Page<CodeGroup> page = new PageImpl<>(Arrays.asList(testEntity));
        given(codeGroupRepository.findAllByDataStateNot(any(), any()))
                .willReturn(page);
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        Page<CodeGroupDTO> result = codeGroupService.getAllCodeGroups(PageRequest.of(0, 10));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("활성화된 그룹 조회")
    void getEnabledCodeGroups() {
        // given
        given(codeGroupRepository.findAllByEnabledTrueAndDataStateNot(any()))
                .willReturn(Arrays.asList(testEntity));
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        List<CodeGroupDTO> result = codeGroupService.getEnabledCodeGroups();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("그룹 토글 - 활성화에서 비활성화")
    void toggleCodeGroup_EnableToDisable() {
        // given
        testEntity.setEnabled(true);
        given(codeGroupRepository.findByGroupCodeAndDataStateNot(anyString(), any()))
                .willReturn(Optional.of(testEntity));
        given(codeGroupRepository.save(any(CodeGroup.class)))
                .willReturn(testEntity);
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        CodeGroupDTO result = codeGroupService.toggleCodeGroup("TEST_GROUP");

        // then
        assertThat(result).isNotNull();
        verify(codeGroupRepository).save(any(CodeGroup.class));
    }

    @Test
    @DisplayName("캐시 전체 갱신")
    void refreshAllCache() {
        // given
        given(codeGroupRepository.findAllByDataStateNot(any()))
                .willReturn(Arrays.asList(testEntity));
        given(codeGroupMapper.toDto(any(CodeGroup.class)))
                .willReturn(testDTO);

        // when
        codeGroupService.refreshAllCache();

        // then
        verify(redisCacheService).set(anyString(), any(), anyLong());
    }
}
