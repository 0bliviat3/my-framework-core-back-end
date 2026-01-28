package com.wan.framework.proxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.dto.InternalApiInvocationResult;
import com.wan.framework.proxy.exception.ProxyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("내부 API 직접 호출 서비스 테스트")
class InternalApiInvokerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InternalApiInvoker internalApiInvoker;

    private ApiRegistry testApiRegistry;
    private TestController testController;

    @BeforeEach
    void setUp() {
        testController = new TestController();
        testApiRegistry = ApiRegistry.builder()
                .apiId(1L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/{id}")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("getById")
                .status(ApiStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("내부 API 직접 호출 성공 - ResponseEntity 반환")
    void invoke_ResponseEntity_Success() throws Exception {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 123L);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getResponseBody()).contains("\"id\":123");
        assertThat(result.getExecutionTimeMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("내부 API 호출 성공 - 일반 객체 반환")
    void invoke_PlainObject_Success() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(2L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/plain")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("getPlainObject")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getResponseBody()).contains("\"message\":\"plain object\"");
    }

    @Test
    @DisplayName("@RequestParam 파라미터 변환 성공")
    void invoke_RequestParam_Success() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(3L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/param")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("getByParam")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "testName");
        parameters.put("age", 25);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getResponseBody()).contains("testName");
        assertThat(result.getResponseBody()).contains("25");
    }

    @Test
    @DisplayName("@PathVariable 파라미터 변환 성공")
    void invoke_PathVariable_Success() throws Exception {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", 456L);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getResponseBody()).contains("\"id\":456");
    }

    @Test
    @DisplayName("@RequestBody 파라미터 변환 성공")
    void invoke_RequestBody_Success() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(4L)
                .serviceId("test-service")
                .httpMethod("POST")
                .uriPattern("/api/test")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("createWithBody")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("name", "testBody");
        bodyData.put("value", 100);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("body", bodyData);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(201);
        assertThat(result.getResponseBody()).contains("testBody");
    }

    @Test
    @DisplayName("컨트롤러 빈을 찾을 수 없는 경우 예외 발생")
    void invoke_ControllerNotFound_ThrowsException() throws Exception {
        // Given
        Map<String, Object> parameters = new HashMap<>();

        when(applicationContext.getBean(any(Class.class)))
                .thenThrow(new RuntimeException("Bean not found"));

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(500);
        assertThat(result.getErrorMessage()).isNotNull();
    }

    @Test
    @DisplayName("핸들러 메서드를 찾을 수 없는 경우 예외 발생")
    void invoke_HandlerMethodNotFound_ThrowsException() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(5L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("nonExistentMethod")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(500);
        assertThat(result.getErrorMessage()).contains("핸들러 메서드");
    }

    @Test
    @DisplayName("컨트롤러 메서드 실행 중 예외 발생")
    void invoke_MethodExecutionException_ReturnsError() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(6L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/error")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("throwException")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(500);
        assertThat(result.getErrorMessage()).contains("Test exception");
    }

    @Test
    @DisplayName("파라미터 타입 변환 - String to Long")
    void invoke_ParameterConversion_StringToLong() throws Exception {
        // Given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", "789"); // String으로 전달

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getResponseBody()).contains("\"id\":789");
    }

    @Test
    @DisplayName("파라미터 타입 변환 - Map to DTO")
    void invoke_ParameterConversion_MapToDto() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(7L)
                .serviceId("test-service")
                .httpMethod("POST")
                .uriPattern("/api/test/dto")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("createWithDto")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> dtoData = new HashMap<>();
        dtoData.put("name", "dtoTest");
        dtoData.put("value", 200);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("body", dtoData);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getResponseBody()).contains("dtoTest");
    }

    @Test
    @DisplayName("ResponseEntity with 4xx status code")
    void invoke_ResponseEntityWithClientError_ReturnsFalse() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(8L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/notfound")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("notFound")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("ResponseEntity with 201 Created status")
    void invoke_ResponseEntityWithCreated_Success() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(9L)
                .serviceId("test-service")
                .httpMethod("POST")
                .uriPattern("/api/test")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("createWithBody")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("name", "created");
        bodyData.put("value", 1);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("body", bodyData);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(201);
    }

    @Test
    @DisplayName("null 파라미터 처리")
    void invoke_NullParameter_Success() throws Exception {
        // Given
        testApiRegistry = ApiRegistry.builder()
                .apiId(10L)
                .serviceId("test-service")
                .httpMethod("GET")
                .uriPattern("/api/test/nullable")
                .controllerName("com.wan.framework.proxy.service.InternalApiInvokerTest$TestController")
                .handlerMethod("getNullable")
                .status(ApiStatus.ACTIVE)
                .build();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", null);

        when(applicationContext.getBean(any(Class.class)))
                .thenReturn(testController);

        // When
        InternalApiInvocationResult result = internalApiInvoker.invoke(testApiRegistry, parameters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSuccess()).isTrue();
    }

    // 테스트용 컨트롤러
    public static class TestController {

        public ResponseEntity<TestResponse> getById(@PathVariable("id") Long id) {
            TestResponse response = new TestResponse();
            response.setId(id);
            response.setMessage("success");
            return ResponseEntity.ok(response);
        }

        public TestResponse getPlainObject() {
            TestResponse response = new TestResponse();
            response.setMessage("plain object");
            return response;
        }

        public ResponseEntity<TestResponse> getByParam(
                @RequestParam("name") String name,
                @RequestParam("age") Integer age) {
            TestResponse response = new TestResponse();
            response.setMessage(name + ":" + age);
            return ResponseEntity.ok(response);
        }

        public ResponseEntity<TestResponse> createWithBody(@RequestBody Map<String, Object> body) {
            TestResponse response = new TestResponse();
            response.setMessage(body.get("name").toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        public ResponseEntity<TestResponse> createWithDto(@RequestBody TestDto dto) {
            TestResponse response = new TestResponse();
            response.setMessage(dto.getName());
            return ResponseEntity.ok(response);
        }

        public ResponseEntity<Void> notFound() {
            return ResponseEntity.notFound().build();
        }

        public ResponseEntity<String> throwException() {
            throw new RuntimeException("Test exception");
        }

        public ResponseEntity<String> getNullable(@RequestParam(required = false) String value) {
            return ResponseEntity.ok("nullable:" + value);
        }
    }

    // 테스트용 DTO
    public static class TestResponse {
        private Long id;
        private String message;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class TestDto {
        private String name;
        private Integer value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}
