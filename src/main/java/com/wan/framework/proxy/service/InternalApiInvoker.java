package com.wan.framework.proxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.dto.InternalApiInvocationResult;
import com.wan.framework.proxy.exception.ProxyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * 내부 API 직접 호출 서비스
 * - Reflection을 통한 컨트롤러 메서드 직접 호출
 * - HTTP 오버헤드 없이 빠른 응답
 * - 파라미터 자동 변환 및 매핑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalApiInvoker {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    /**
     * 내부 API 직접 호출
     *
     * @param apiRegistry API Registry 정보
     * @param parameters 동적 파라미터
     * @return 호출 결과 (statusCode, responseBody, executionTime)
     */
    public InternalApiInvocationResult invoke(
            ApiRegistry apiRegistry,
            Map<String, Object> parameters) {

        long startTime = System.nanoTime();

        try {
            log.info("Invoking internal API: {}.{}",
                apiRegistry.getControllerName(), apiRegistry.getHandlerMethod());

            // 1. 컨트롤러 빈 조회
            Object controllerBean = getControllerBean(apiRegistry.getControllerName());

            // 2. 핸들러 메서드 조회
            Method handlerMethod = findHandlerMethod(
                controllerBean,
                apiRegistry.getHandlerMethod()
            );

            // 3. 메서드 파라미터 변환
            Object[] methodArgs = convertParameters(handlerMethod, parameters);

            // 4. 메서드 호출
            Object result = handlerMethod.invoke(controllerBean, methodArgs);

            // 5. 응답 변환
            InternalApiInvocationResult invocationResult = convertResponse(result);

            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            invocationResult.setExecutionTimeMs(executionTimeMs);

            log.info("Internal API invoked successfully in {}ms", executionTimeMs);
            return invocationResult;

        } catch (InvocationTargetException e) {
            // 원본 예외 unwrapping
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Internal API invocation failed: {}", cause.getMessage(), cause);

            return InternalApiInvocationResult.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .responseBody(null)
                .errorMessage(cause.getMessage())
                .isSuccess(false)
                .executionTimeMs(executionTimeMs)
                .build();

        } catch (Exception e) {
            long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("Internal API invocation failed: {}", e.getMessage(), e);

            return InternalApiInvocationResult.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .responseBody(null)
                .errorMessage(e.getMessage())
                .isSuccess(false)
                .executionTimeMs(executionTimeMs)
                .build();
        }
    }

    /**
     * 컨트롤러 빈 조회
     */
    private Object getControllerBean(String controllerClassName) {
        try {
            // 1. 클래스 로드
            Class<?> controllerClass = Class.forName(controllerClassName);

            // 2. Spring 컨텍스트에서 빈 조회
            Object bean = applicationContext.getBean(controllerClass);

            log.debug("Found controller bean: {}", controllerClass.getSimpleName());
            return bean;

        } catch (ClassNotFoundException e) {
            log.error("Controller class not found: {}", controllerClassName);
            throw new ProxyException(ProxyExceptionMessage.CONTROLLER_BEAN_NOT_FOUND, e);
        } catch (Exception e) {
            log.error("Failed to get controller bean: {}", controllerClassName, e);
            throw new ProxyException(ProxyExceptionMessage.CONTROLLER_BEAN_NOT_FOUND, e);
        }
    }

    /**
     * 핸들러 메서드 조회
     */
    private Method findHandlerMethod(Object controllerBean, String methodName) {
        // 같은 이름의 메서드가 여러 개일 수 있으므로 모두 탐색
        Method[] methods = controllerBean.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                log.debug("Found handler method: {}", method.getName());
                return method;
            }
        }

        log.error("Handler method not found: {} in {}",
            methodName, controllerBean.getClass().getName());
        throw new ProxyException(ProxyExceptionMessage.HANDLER_METHOD_NOT_FOUND);
    }

    /**
     * 메서드 파라미터 변환
     */
    private Object[] convertParameters(Method method, Map<String, Object> parameters) {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            Class<?> paramType = param.getType();

            // 파라미터 어노테이션 확인
            String paramName = extractParameterName(param);

            // parameters 맵에서 값 추출 및 타입 변환
            Object value = parameters.get(paramName);
            args[i] = convertParameterValue(value, paramType);

            log.debug("Converted parameter[{}]: {} = {} (type: {})",
                i, paramName, value, paramType.getSimpleName());
        }

        return args;
    }

    /**
     * 파라미터 이름 추출 (@RequestParam, @PathVariable, @RequestBody 등)
     */
    private String extractParameterName(Parameter param) {
        // @RequestParam
        if (param.isAnnotationPresent(RequestParam.class)) {
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            String name = requestParam.value().isEmpty()
                ? requestParam.name()
                : requestParam.value();
            return name.isEmpty() ? param.getName() : name;
        }

        // @PathVariable
        if (param.isAnnotationPresent(PathVariable.class)) {
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            String name = pathVariable.value().isEmpty()
                ? pathVariable.name()
                : pathVariable.value();
            return name.isEmpty() ? param.getName() : name;
        }

        // @RequestBody (전체 파라미터 맵 전달)
        if (param.isAnnotationPresent(RequestBody.class)) {
            return "body";
        }

        // 기본: 파라미터 이름
        return param.getName();
    }

    /**
     * 파라미터 값 타입 변환
     */
    private Object convertParameterValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        try {
            // 이미 같은 타입인 경우
            if (targetType.isInstance(value)) {
                return value;
            }

            // Jackson ObjectMapper를 통한 변환 (DTO, 복잡한 객체)
            return objectMapper.convertValue(value, targetType);

        } catch (Exception e) {
            log.warn("Failed to convert parameter value: {} to {}",
                value, targetType.getName(), e);
            throw new ProxyException(ProxyExceptionMessage.PARAMETER_CONVERSION_FAILED, e);
        }
    }

    /**
     * 응답 변환 (ResponseEntity 또는 일반 객체)
     */
    private InternalApiInvocationResult convertResponse(Object result) {
        try {
            // ResponseEntity인 경우
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;

                int statusCode = responseEntity.getStatusCode().value();
                String responseBody = objectMapper.writeValueAsString(responseEntity.getBody());

                return InternalApiInvocationResult.builder()
                    .statusCode(statusCode)
                    .responseBody(responseBody)
                    .isSuccess(statusCode >= 200 && statusCode < 300)
                    .build();
            }

            // 일반 객체인 경우 (200 OK로 간주)
            String responseBody = objectMapper.writeValueAsString(result);

            return InternalApiInvocationResult.builder()
                .statusCode(HttpStatus.OK.value())
                .responseBody(responseBody)
                .isSuccess(true)
                .build();

        } catch (Exception e) {
            log.error("Failed to convert response: {}", e.getMessage(), e);
            throw new ProxyException(ProxyExceptionMessage.INVALID_RESPONSE, e);
        }
    }
}
