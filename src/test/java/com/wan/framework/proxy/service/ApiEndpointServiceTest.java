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
                .createdBy("admin")
                .build();

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
    @DisplayName("잘못된 HTTP 메서드로 생성 시 예외 발생")
    void createApiEndpoint_InvalidHttpMethod() {
        // Given
        testEndpointDTO.setHttpMethod("INVALID");
        when(apiEndpointRepository.existsByApiCodeAndDataStateNot(anyString(), eq(DataStateCode.D)))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> apiEndpointService.createApiEndpoint(testEndpointDTO))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.INVALID_HTTP_METHOD.getMessage());
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
}
