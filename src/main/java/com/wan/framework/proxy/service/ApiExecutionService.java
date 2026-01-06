package com.wan.framework.proxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.domain.ApiExecutionHistory;
import com.wan.framework.proxy.dto.ProxyExecutionRequest;
import com.wan.framework.proxy.dto.ProxyExecutionResponse;
import com.wan.framework.proxy.exception.ProxyException;
import com.wan.framework.proxy.repository.ApiExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * API 실행 서비스
 * - HTTP 클라이언트를 통한 실제 API 호출
 * - 재시도 로직
 * - 실행 이력 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiExecutionService {

    private final ApiExecutionHistoryRepository executionHistoryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * API 실행
     */
    @Transactional
    public ProxyExecutionResponse execute(ApiEndpoint endpoint, ProxyExecutionRequest request) {
        log.info("Executing API: {} ({})", endpoint.getApiCode(), endpoint.getApiName());

        LocalDateTime startTime = LocalDateTime.now();
        int retryAttempt = 0;
        ApiExecutionHistory history = null;

        while (retryAttempt <= endpoint.getRetryCount()) {
            try {
                // 실행
                history = executeRequest(endpoint, request, retryAttempt);

                // 성공 시 응답 반환
                if (history.getIsSuccess()) {
                    long executionTimeMs = Duration.between(startTime, history.getExecutedAt()).toMillis();
                    history.setExecutionTimeMs(executionTimeMs);

                    ApiExecutionHistory savedHistory = executionHistoryRepository.save(history);

                    return buildSuccessResponse(savedHistory);
                }

                // 실패 시 재시도
                if (retryAttempt < endpoint.getRetryCount()) {
                    log.warn("API execution failed, retrying... (attempt {}/{})",
                            retryAttempt + 1, endpoint.getRetryCount());
                    Thread.sleep(endpoint.getRetryIntervalMs());
                    retryAttempt++;
                } else {
                    break;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ProxyException(ProxyExceptionMessage.API_EXECUTION_FAILED, e);
            } catch (Exception e) {
                log.error("API execution error: {}", e.getMessage());

                if (retryAttempt < endpoint.getRetryCount()) {
                    retryAttempt++;
                    try {
                        Thread.sleep(endpoint.getRetryIntervalMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ProxyException(ProxyExceptionMessage.API_EXECUTION_FAILED, ie);
                    }
                } else {
                    history = buildFailedHistory(endpoint, request, e, retryAttempt);
                    break;
                }
            }
        }

        // 최종 실패 처리
        if (history != null) {
            long executionTimeMs = Duration.between(startTime, LocalDateTime.now()).toMillis();
            history.setExecutionTimeMs(executionTimeMs);

            ApiExecutionHistory savedHistory = executionHistoryRepository.save(history);
            return buildFailureResponse(savedHistory);
        }

        throw new ProxyException(ProxyExceptionMessage.API_EXECUTION_FAILED);
    }

    /**
     * 실제 HTTP 요청 실행
     */
    private ApiExecutionHistory executeRequest(
            ApiEndpoint endpoint,
            ProxyExecutionRequest request,
            int retryAttempt) {

        try {
            // URL 및 바디 템플릿 처리
            String executedUrl = replaceTemplateVariables(endpoint.getTargetUrl(), request.getParameters());
            String requestBody = null;

            if (endpoint.getRequestBodyTemplate() != null && !endpoint.getRequestBodyTemplate().isEmpty()) {
                requestBody = replaceTemplateVariables(endpoint.getRequestBodyTemplate(), request.getParameters());
            }

            // 헤더 설정
            HttpHeaders headers = buildHeaders(endpoint.getRequestHeaders(), request.getParameters());

            // HTTP 엔티티 생성
            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

            // HTTP 메서드에 따른 요청
            HttpMethod method = HttpMethod.valueOf(endpoint.getHttpMethod().toUpperCase());

            log.debug("Executing {} {} with body: {}", method, executedUrl, requestBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    executedUrl,
                    method,
                    httpEntity,
                    String.class
            );

            // 성공 응답 기록
            return ApiExecutionHistory.builder()
                    .apiEndpointId(endpoint.getId())
                    .apiCode(endpoint.getApiCode())
                    .executedUrl(executedUrl)
                    .httpMethod(endpoint.getHttpMethod())
                    .requestHeaders(objectMapper.writeValueAsString(headers.toSingleValueMap()))
                    .requestBody(requestBody)
                    .responseStatusCode(response.getStatusCode().value())
                    .responseHeaders(objectMapper.writeValueAsString(response.getHeaders().toSingleValueMap()))
                    .responseBody(response.getBody())
                    .isSuccess(true)
                    .retryAttempt(retryAttempt)
                    .executionTrigger(request.getExecutionTrigger())
                    .executedBy(request.getExecutedBy())
                    .executedAt(LocalDateTime.now())
                    .build();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // HTTP 에러 응답
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            return ApiExecutionHistory.builder()
                    .apiEndpointId(endpoint.getId())
                    .apiCode(endpoint.getApiCode())
                    .executedUrl(endpoint.getTargetUrl())
                    .httpMethod(endpoint.getHttpMethod())
                    .responseStatusCode(e.getStatusCode().value())
                    .responseBody(e.getResponseBodyAsString())
                    .isSuccess(false)
                    .errorMessage(e.getMessage())
                    .retryAttempt(retryAttempt)
                    .executionTrigger(request.getExecutionTrigger())
                    .executedBy(request.getExecutedBy())
                    .executedAt(LocalDateTime.now())
                    .build();

        } catch (ResourceAccessException e) {
            // 타임아웃 또는 연결 실패
            log.error("Connection error: {}", e.getMessage());
            throw new ProxyException(ProxyExceptionMessage.API_CONNECTION_FAILED, e);

        } catch (Exception e) {
            log.error("Unexpected error during API execution: {}", e.getMessage());
            throw new ProxyException(ProxyExceptionMessage.API_EXECUTION_FAILED, e);
        }
    }

    /**
     * 실패 이력 생성
     */
    private ApiExecutionHistory buildFailedHistory(
            ApiEndpoint endpoint,
            ProxyExecutionRequest request,
            Exception exception,
            int retryAttempt) {

        return ApiExecutionHistory.builder()
                .apiEndpointId(endpoint.getId())
                .apiCode(endpoint.getApiCode())
                .executedUrl(endpoint.getTargetUrl())
                .httpMethod(endpoint.getHttpMethod())
                .isSuccess(false)
                .errorMessage(exception.getMessage())
                .retryAttempt(retryAttempt)
                .executionTrigger(request.getExecutionTrigger())
                .executedBy(request.getExecutedBy())
                .executedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 템플릿 변수 치환
     * 예: "https://api.example.com/users/${userId}" -> "https://api.example.com/users/12345"
     */
    private String replaceTemplateVariables(String template, Map<String, Object> parameters) {
        if (template == null || parameters == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * HTTP 헤더 빌드
     */
    private HttpHeaders buildHeaders(String headerTemplate, Map<String, Object> parameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (headerTemplate != null && !headerTemplate.isEmpty()) {
            try {
                String replacedHeaders = replaceTemplateVariables(headerTemplate, parameters);
                Map<String, String> headerMap = objectMapper.readValue(replacedHeaders, Map.class);

                headerMap.forEach(headers::add);
            } catch (Exception e) {
                log.error("Failed to parse header template: {}", e.getMessage());
            }
        }

        return headers;
    }

    /**
     * 성공 응답 빌드
     */
    private ProxyExecutionResponse buildSuccessResponse(ApiExecutionHistory history) {
        return ProxyExecutionResponse.builder()
                .executionHistoryId(history.getId())
                .apiCode(history.getApiCode())
                .isSuccess(true)
                .statusCode(history.getResponseStatusCode())
                .responseBody(history.getResponseBody())
                .executionTimeMs(history.getExecutionTimeMs())
                .retryAttempt(history.getRetryAttempt())
                .executedAt(history.getExecutedAt())
                .build();
    }

    /**
     * 실패 응답 빌드
     */
    private ProxyExecutionResponse buildFailureResponse(ApiExecutionHistory history) {
        return ProxyExecutionResponse.builder()
                .executionHistoryId(history.getId())
                .apiCode(history.getApiCode())
                .isSuccess(false)
                .statusCode(history.getResponseStatusCode())
                .responseBody(history.getResponseBody())
                .errorMessage(history.getErrorMessage())
                .executionTimeMs(history.getExecutionTimeMs())
                .retryAttempt(history.getRetryAttempt())
                .executedAt(history.getExecutedAt())
                .build();
    }
}
