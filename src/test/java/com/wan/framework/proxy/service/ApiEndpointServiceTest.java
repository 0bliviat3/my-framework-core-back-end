package com.wan.framework.proxy.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ApiEndpointDTO;
import com.wan.framework.proxy.exception.ProxyException;
import com.wan.framework.proxy.mapper.ApiEndpointMapper;
import com.wan.framework.proxy.repository.ApiEndpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("API 엔드포인트 서비스 테스트")
class ApiEndpointServiceTest {

    @Mock
    private ApiEndpointRepository apiEndpointRepository;

    @Mock
    private ApiEndpointMapper apiEndpointMapper;

    @Mock
    private ApiRegistryMatchingService registryMatchingService;

    @InjectMocks
    private ApiEndpointService apiEndpointService;

    private ApiEndpoint testEndpoint;
    private ApiEndpointDTO testEndpointDTO;

    @BeforeEach
    void setUp() {
        testEndpoint = ApiEndpoint.builder()
                .id(1L)
                .apiCode("TEST_API_001")
                .apiName("Test API")
                .targetUrl("https://api.example.com/test")
                .httpMethod("POST")
                .timeoutSeconds(30)
                .retryCount(3)
                .retryIntervalMs(1000)
                .isInternal(false)
                .isEnabled(true)
                .dataState(DataStateCode.I)
                .build();
        testEndpoint.setCreatedBy("admin");

        testEndpointDTO = ApiEndpointDTO.builder()
                .id(1L)
                .apiCode("TEST_API_001")
                .apiName("Test API")
                .targetUrl("https://api.example.com/test")
                .httpMethod("POST")
                .timeoutSeconds(30)
                .retryCount(3)
                .retryIntervalMs(1000)
                .isInternal(false)
                .isEnabled(true)
                .createdBy("admin")
                .build();
    }

    @Test
    @DisplayName("API 엔드포인트 생성 성공")
    void createApiEndpoint_Success() {
        // Given
        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(false);
        when(apiEndpointMapper.toEntity(any(ApiEndpointDTO.class))).thenReturn(testEndpoint);
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.createApiEndpoint(testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApiCode()).isEqualTo("TEST_API_001");
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("중복된 API 코드로 생성 시 예외 발생")
    void createApiEndpoint_DuplicateCode() {
        // Given
        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> apiEndpointService.createApiEndpoint(testEndpointDTO))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_CODE_ALREADY_EXISTS.getMessage());

        verify(apiEndpointRepository, never()).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("API 엔드포인트 수정 성공")
    void updateApiEndpoint_Success() {
        // Given
        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.updateApiEndpoint(1L, testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(apiEndpointMapper, times(1)).updateEntityFromDto(any(), any());
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("존재하지 않는 API 엔드포인트 수정 시 예외 발생")
    void updateApiEndpoint_NotFound() {
        // Given
        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> apiEndpointService.updateApiEndpoint(1L, testEndpointDTO))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("API 엔드포인트 삭제 성공")
    void deleteApiEndpoint_Success() {
        // Given
        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);

        // When
        apiEndpointService.deleteApiEndpoint(1L);

        // Then
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
        assertThat(testEndpoint.getDataState()).isEqualTo(DataStateCode.D);
    }

    @Test
    @DisplayName("API 엔드포인트 조회 성공")
    void getApiEndpoint_Success() {
        // Given
        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.getApiEndpoint(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApiCode()).isEqualTo("TEST_API_001");
    }

    @Test
    @DisplayName("API 코드로 조회 성공")
    void getApiEndpointByCode_Success() {
        // Given
        when(apiEndpointRepository.findByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(Optional.of(testEndpoint));

        // When
        ApiEndpoint result = apiEndpointService.getApiEndpointByCode("TEST_API_001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApiCode()).isEqualTo("TEST_API_001");
    }

    @Test
    @DisplayName("API 엔드포인트 목록 조회 성공")
    void getAllApiEndpoints_Success() {
        // Given
        Page<ApiEndpoint> page = new PageImpl<>(Arrays.asList(testEndpoint));
        when(apiEndpointRepository.findByDataStateNot(eq(DataStateCode.D), any(Pageable.class)))
                .thenReturn(page);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        Page<ApiEndpointDTO> result = apiEndpointService.getAllApiEndpoints(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("활성화된 API 엔드포인트 목록 조회 성공")
    void getEnabledApiEndpoints_Success() {
        // Given
        Page<ApiEndpoint> page = new PageImpl<>(Arrays.asList(testEndpoint));
        when(apiEndpointRepository.findByDataStateNotAndIsEnabledTrue(eq(DataStateCode.D), any(Pageable.class)))
                .thenReturn(page);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        Page<ApiEndpointDTO> result = apiEndpointService.getEnabledApiEndpoints(0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("API 엔드포인트 활성/비활성 토글 성공")
    void toggleApiEndpoint_Success() {
        // Given
        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        boolean originalState = testEndpoint.getIsEnabled();
        apiEndpointService.toggleApiEndpoint(1L);

        // Then
        assertThat(testEndpoint.getIsEnabled()).isNotEqualTo(originalState);
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("내부 API 생성 시 ApiRegistry 자동 매핑 성공")
    void createApiEndpoint_InternalApi_AutoMapping_Success() {
        // Given
        testEndpointDTO.setIsInternal(true);
        testEndpointDTO.setTargetUrl("/api/users/{id}");
        testEndpointDTO.setHttpMethod("GET");

        testEndpoint.setIsInternal(true);
        testEndpoint.setTargetUrl("/api/users/{id}");
        testEndpoint.setHttpMethod("GET");
        testEndpoint.setApiRegistryId(100L);

        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(false);
        when(apiEndpointMapper.toEntity(any(ApiEndpointDTO.class))).thenReturn(testEndpoint);
        when(registryMatchingService.findMatchingApiRegistryId("/api/users/{id}", "GET"))
                .thenReturn(100L);
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.createApiEndpoint(testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(registryMatchingService, times(1))
                .findMatchingApiRegistryId("/api/users/{id}", "GET");
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("내부 API 생성 시 ApiRegistry 매핑 실패")
    void createApiEndpoint_InternalApi_MappingFailed_ThrowsException() {
        // Given
        testEndpointDTO.setIsInternal(true);
        testEndpointDTO.setTargetUrl("/api/nonexistent");
        testEndpointDTO.setHttpMethod("GET");

        testEndpoint.setIsInternal(true);
        testEndpoint.setTargetUrl("/api/nonexistent");
        testEndpoint.setHttpMethod("GET");

        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(false);
        when(apiEndpointMapper.toEntity(any(ApiEndpointDTO.class))).thenReturn(testEndpoint);
        when(registryMatchingService.findMatchingApiRegistryId("/api/nonexistent", "GET"))
                .thenThrow(new ProxyException(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> apiEndpointService.createApiEndpoint(testEndpointDTO))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND.getMessage());

        verify(registryMatchingService, times(1))
                .findMatchingApiRegistryId("/api/nonexistent", "GET");
        verify(apiEndpointRepository, never()).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("외부 API 생성 시 ApiRegistry 매핑 안 함")
    void createApiEndpoint_ExternalApi_NoMapping() {
        // Given
        testEndpointDTO.setIsInternal(false);
        testEndpointDTO.setTargetUrl("https://external.api.com/test");

        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(false);
        when(apiEndpointMapper.toEntity(any(ApiEndpointDTO.class))).thenReturn(testEndpoint);
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.createApiEndpoint(testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(registryMatchingService, never()).findMatchingApiRegistryId(anyString(), anyString());
        verify(apiEndpointRepository, times(1)).save(any(ApiEndpoint.class));
    }

    @Test
    @DisplayName("내부 API로 전환 시 ApiRegistry 재매핑")
    void updateApiEndpoint_BecomeInternal_ReMapping() {
        // Given
        testEndpoint.setIsInternal(false); // Initially external
        testEndpoint.setApiRegistryId(null);
        testEndpoint.setTargetUrl("https://external.com/api");
        testEndpoint.setHttpMethod("GET");

        testEndpointDTO.setIsInternal(true); // Changing to internal
        testEndpointDTO.setTargetUrl("/api/users");
        testEndpointDTO.setHttpMethod("GET");

        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));

        // Simulate updateEntityFromDto behavior
        doAnswer(invocation -> {
            testEndpoint.setIsInternal(true);
            testEndpoint.setTargetUrl("/api/users");
            testEndpoint.setHttpMethod("GET");
            return null;
        }).when(apiEndpointMapper).updateEntityFromDto(any(), any());

        when(registryMatchingService.findMatchingApiRegistryId("/api/users", "GET"))
                .thenReturn(200L);
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.updateApiEndpoint(1L, testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(registryMatchingService, times(1))
                .findMatchingApiRegistryId("/api/users", "GET");
    }

    @Test
    @DisplayName("내부 API URL 변경 시 ApiRegistry 재매핑")
    void updateApiEndpoint_InternalApi_UrlChanged_ReMapping() {
        // Given
        testEndpoint.setIsInternal(true);
        testEndpoint.setTargetUrl("/api/old/path");
        testEndpoint.setHttpMethod("GET");
        testEndpoint.setApiRegistryId(100L);

        testEndpointDTO.setIsInternal(true);
        testEndpointDTO.setTargetUrl("/api/new/path");
        testEndpointDTO.setHttpMethod("GET");

        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));

        // Simulate updateEntityFromDto behavior
        doAnswer(invocation -> {
            testEndpoint.setTargetUrl("/api/new/path");
            return null;
        }).when(apiEndpointMapper).updateEntityFromDto(any(), any());

        when(registryMatchingService.findMatchingApiRegistryId("/api/new/path", "GET"))
                .thenReturn(300L);
        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenReturn(testEndpoint);
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.updateApiEndpoint(1L, testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(registryMatchingService, times(1))
                .findMatchingApiRegistryId("/api/new/path", "GET");
    }

    @Test
    @DisplayName("외부 API로 전환 시 ApiRegistry 매핑 제거")
    void updateApiEndpoint_BecomeExternal_ClearMapping() {
        // Given
        testEndpoint.setIsInternal(true);
        testEndpoint.setTargetUrl("/api/internal");
        testEndpoint.setHttpMethod("GET");
        testEndpoint.setApiRegistryId(100L);

        testEndpointDTO.setIsInternal(false); // Changing to external
        testEndpointDTO.setTargetUrl("https://external.com/api");
        testEndpointDTO.setHttpMethod("GET");

        when(apiEndpointRepository.findById(anyLong())).thenReturn(Optional.of(testEndpoint));

        // Simulate updateEntityFromDto behavior
        doAnswer(invocation -> {
            testEndpoint.setIsInternal(false);
            testEndpoint.setTargetUrl("https://external.com/api");
            return null;
        }).when(apiEndpointMapper).updateEntityFromDto(any(), any());

        when(apiEndpointRepository.save(any(ApiEndpoint.class))).thenAnswer(invocation -> {
            // Verify that apiRegistryId has been set to null
            ApiEndpoint saved = invocation.getArgument(0);
            assertThat(saved.getApiRegistryId()).isNull();
            return saved;
        });
        when(apiEndpointMapper.toDto(any(ApiEndpoint.class))).thenReturn(testEndpointDTO);

        // When
        ApiEndpointDTO result = apiEndpointService.updateApiEndpoint(1L, testEndpointDTO);

        // Then
        assertThat(result).isNotNull();
        verify(registryMatchingService, never()).findMatchingApiRegistryId(anyString(), anyString());
    }
}
