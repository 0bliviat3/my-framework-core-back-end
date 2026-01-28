package com.wan.framework.proxy.service;

import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.exception.ProxyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("API Registry 매칭 서비스 테스트")
class ApiRegistryMatchingServiceTest {

    @Mock
    private ApiRegistryRepository apiRegistryRepository;

    @InjectMocks
    private ApiRegistryMatchingService apiRegistryMatchingService;

    private ApiRegistry exactMatchRegistry;
    private ApiRegistry patternMatchRegistry;

    @BeforeEach
    void setUp() {
        // 정확한 매칭용 ApiRegistry
        exactMatchRegistry = ApiRegistry.builder()
                .apiId(1L)
                .serviceId("user-service")
                .httpMethod("GET")
                .uriPattern("/api/users")
                .controllerName("com.wan.framework.user.web.UserController")
                .handlerMethod("getUsers")
                .status(ApiStatus.ACTIVE)
                .build();

        // 패턴 매칭용 ApiRegistry (PathVariable 포함)
        patternMatchRegistry = ApiRegistry.builder()
                .apiId(2L)
                .serviceId("user-service")
                .httpMethod("GET")
                .uriPattern("/api/users/{id}")
                .controllerName("com.wan.framework.user.web.UserController")
                .handlerMethod("getUserById")
                .status(ApiStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("정확한 URI 매칭 성공")
    void findMatchingApiRegistryId_ExactMatch_Success() {
        // Given
        String targetUrl = "/api/users";
        String httpMethod = "GET";

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users")))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users");
        verify(apiRegistryRepository, never())
                .findActiveApisByMethod(anyString()); // 정확한 매칭이므로 패턴 매칭 미실행
    }

    @Test
    @DisplayName("패턴 매칭 성공 - PathVariable 포함")
    void findMatchingApiRegistryId_PatternMatch_Success() {
        // Given
        String targetUrl = "/api/users/123";
        String httpMethod = "GET";

        // 정확한 매칭 실패
        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users/123")))
                .thenReturn(Optional.empty());

        // 패턴 매칭 후보 조회
        when(apiRegistryRepository.findActiveApisByMethod(eq("GET")))
                .thenReturn(Arrays.asList(patternMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users/123");
        verify(apiRegistryRepository, times(1))
                .findActiveApisByMethod("GET");
    }

    @Test
    @DisplayName("전체 URL에서 URI 경로 추출 후 매칭 성공")
    void findMatchingApiRegistryId_FullUrl_Success() {
        // Given
        String fullUrl = "http://localhost:8080/api/users";
        String httpMethod = "GET";

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users")))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(fullUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users");
    }

    @Test
    @DisplayName("쿼리 파라미터 제거 후 매칭 성공")
    void findMatchingApiRegistryId_WithQueryParams_Success() {
        // Given
        String urlWithQuery = "/api/users?page=1&size=10";
        String httpMethod = "GET";

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users")))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(urlWithQuery, httpMethod);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users");
    }

    @Test
    @DisplayName("대소문자 무관 HTTP Method 매칭")
    void findMatchingApiRegistryId_CaseInsensitiveMethod_Success() {
        // Given
        String targetUrl = "/api/users";
        String httpMethod = "get"; // 소문자

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users")))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users");
    }

    @Test
    @DisplayName("매칭 실패 - ApiRegistry를 찾을 수 없음")
    void findMatchingApiRegistryId_NotFound_ThrowsException() {
        // Given
        String targetUrl = "/api/nonexistent";
        String httpMethod = "GET";

        when(apiRegistryRepository.findActiveApiByMethodAndUri(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiRegistryRepository.findActiveApisByMethod(anyString()))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() ->
                apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND.getMessage());

        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/nonexistent");
        verify(apiRegistryRepository, times(1))
                .findActiveApisByMethod("GET");
    }

    @Test
    @DisplayName("패턴 매칭 - 여러 후보 중 첫 번째 매칭")
    void findMatchingApiRegistryId_MultiplePatterns_FirstMatch() {
        // Given
        String targetUrl = "/api/users/123";
        String httpMethod = "GET";

        ApiRegistry anotherPatternRegistry = ApiRegistry.builder()
                .apiId(3L)
                .serviceId("user-service")
                .httpMethod("GET")
                .uriPattern("/api/users/{userId}")
                .controllerName("com.wan.framework.user.web.UserController")
                .handlerMethod("getUserByUserId")
                .status(ApiStatus.ACTIVE)
                .build();

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users/123")))
                .thenReturn(Optional.empty());
        when(apiRegistryRepository.findActiveApisByMethod(eq("GET")))
                .thenReturn(Arrays.asList(patternMatchRegistry, anotherPatternRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(2L); // 첫 번째로 매칭된 패턴
    }

    @Test
    @DisplayName("복잡한 경로 패턴 매칭 성공")
    void findMatchingApiRegistryId_ComplexPattern_Success() {
        // Given
        String targetUrl = "/api/users/123/orders/456";
        String httpMethod = "GET";

        ApiRegistry complexPatternRegistry = ApiRegistry.builder()
                .apiId(4L)
                .serviceId("order-service")
                .httpMethod("GET")
                .uriPattern("/api/users/{userId}/orders/{orderId}")
                .controllerName("com.wan.framework.order.web.OrderController")
                .handlerMethod("getUserOrder")
                .status(ApiStatus.ACTIVE)
                .build();

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users/123/orders/456")))
                .thenReturn(Optional.empty());
        when(apiRegistryRepository.findActiveApisByMethod(eq("GET")))
                .thenReturn(Arrays.asList(complexPatternRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(4L);
    }

    @Test
    @DisplayName("getApiRegistry 성공 - ACTIVE 상태 확인")
    void getApiRegistry_ActiveStatus_Success() {
        // Given
        Long apiRegistryId = 1L;
        when(apiRegistryRepository.findById(eq(1L)))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        ApiRegistry result = apiRegistryMatchingService.getApiRegistry(apiRegistryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getApiId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ApiStatus.ACTIVE);
    }

    @Test
    @DisplayName("getApiRegistry 실패 - INACTIVE 상태")
    void getApiRegistry_InactiveStatus_ThrowsException() {
        // Given
        Long apiRegistryId = 1L;
        ApiRegistry inactiveRegistry = ApiRegistry.builder()
                .apiId(1L)
                .serviceId("user-service")
                .httpMethod("GET")
                .uriPattern("/api/users")
                .status(ApiStatus.INACTIVE) // 비활성 상태
                .build();

        when(apiRegistryRepository.findById(eq(1L)))
                .thenReturn(Optional.of(inactiveRegistry));

        // When & Then
        assertThatThrownBy(() ->
                apiRegistryMatchingService.getApiRegistry(apiRegistryId))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getApiRegistry 실패 - 존재하지 않는 ID")
    void getApiRegistry_NotFound_ThrowsException() {
        // Given
        Long apiRegistryId = 999L;
        when(apiRegistryRepository.findById(eq(999L)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                apiRegistryMatchingService.getApiRegistry(apiRegistryId))
                .isInstanceOf(ProxyException.class)
                .hasMessageContaining(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("POST 메서드 매칭 성공")
    void findMatchingApiRegistryId_PostMethod_Success() {
        // Given
        String targetUrl = "/api/users";
        String httpMethod = "POST";

        ApiRegistry postRegistry = ApiRegistry.builder()
                .apiId(5L)
                .serviceId("user-service")
                .httpMethod("POST")
                .uriPattern("/api/users")
                .controllerName("com.wan.framework.user.web.UserController")
                .handlerMethod("createUser")
                .status(ApiStatus.ACTIVE)
                .build();

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("POST"), eq("/api/users")))
                .thenReturn(Optional.of(postRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(targetUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("HTTPS URL 매칭 성공")
    void findMatchingApiRegistryId_HttpsUrl_Success() {
        // Given
        String httpsUrl = "https://api.example.com/api/users";
        String httpMethod = "GET";

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/api/users")))
                .thenReturn(Optional.of(exactMatchRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(httpsUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(apiRegistryRepository, times(1))
                .findActiveApiByMethodAndUri("GET", "/api/users");
    }

    @Test
    @DisplayName("루트 경로 매칭")
    void findMatchingApiRegistryId_RootPath_Success() {
        // Given
        String rootUrl = "/";
        String httpMethod = "GET";

        ApiRegistry rootRegistry = ApiRegistry.builder()
                .apiId(6L)
                .serviceId("root-service")
                .httpMethod("GET")
                .uriPattern("/")
                .controllerName("com.wan.framework.root.web.RootController")
                .handlerMethod("root")
                .status(ApiStatus.ACTIVE)
                .build();

        when(apiRegistryRepository.findActiveApiByMethodAndUri(eq("GET"), eq("/")))
                .thenReturn(Optional.of(rootRegistry));

        // When
        Long result = apiRegistryMatchingService.findMatchingApiRegistryId(rootUrl, httpMethod);

        // Then
        assertThat(result).isEqualTo(6L);
    }
}
