package com.wan.framework.proxy.web;

import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ProxyExecutionRequest;
import com.wan.framework.proxy.dto.ProxyExecutionResponse;
import com.wan.framework.proxy.exception.ProxyException;
import com.wan.framework.proxy.service.ApiEndpointService;
import com.wan.framework.proxy.service.ApiExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Proxy API 컨트롤러
 * - 공통 실행 엔드포인트
 */
@Slf4j
@RestController
@RequestMapping("/proxy")
@RequiredArgsConstructor
public class ProxyApiController {

    private final ApiEndpointService apiEndpointService;
    private final ApiExecutionService apiExecutionService;

    /**
     * Proxy API 실행
     * POST /proxy/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<ProxyExecutionResponse> executeApi(@RequestBody ProxyExecutionRequest request) {
        log.info("Proxy API execution requested: {}", request.getApiCode());

        // API 코드 필수 검증
        if (request.getApiCode() == null || request.getApiCode().isEmpty()) {
            throw new ProxyException(ProxyExceptionMessage.MISSING_REQUIRED_PARAMETER);
        }

        // API 엔드포인트 조회
        ApiEndpoint endpoint = apiEndpointService.getApiEndpointByCode(request.getApiCode());

        // 활성화 여부 확인
        if (!endpoint.getIsEnabled()) {
            throw new ProxyException(ProxyExceptionMessage.API_ENDPOINT_DISABLED);
        }

        // API 실행
        ProxyExecutionResponse response = apiExecutionService.execute(endpoint, request);

        log.info("Proxy API execution completed: {} (success: {})",
                request.getApiCode(), response.getIsSuccess());

        return ResponseEntity.ok(response);
    }
}
