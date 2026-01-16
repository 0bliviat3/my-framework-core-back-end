package com.wan.framework.proxy.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.base.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 엔드포인트 정보
 * - 호출할 API의 메타 정보 관리
 * - 데이터 기반으로 API 호출 정보 저장
 */
@Entity
@Table(name = "t_api_endpoint")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpoint extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API 식별 코드 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String apiCode;

    /**
     * API 이름
     */
    @Column(nullable = false, length = 200)
    private String apiName;

    /**
     * API 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 대상 URL
     */
    @Column(nullable = false, length = 1000)
    private String targetUrl;

    /**
     * HTTP 메서드 (GET, POST, PUT, DELETE, PATCH)
     */
    @Column(nullable = false, length = 10)
    private String httpMethod;

    /**
     * 요청 헤더 (JSON)
     * 예: {"Content-Type": "application/json", "Authorization": "Bearer ${apiKey}"}
     */
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * 요청 바디 템플릿 (JSON)
     * 예: {"userId": "${userId}", "action": "process"}
     */
    @Column(columnDefinition = "TEXT")
    private String requestBodyTemplate;

    /**
     * 타임아웃 (초)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 30;

    /**
     * 재시도 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 재시도 간격 (밀리초)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryIntervalMs = 1000;

    /**
     * 내부 API 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isInternal = true;

    /**
     * 사용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    /**
     * 데이터 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private DataStateCode dataState = DataStateCode.I;

    @PrePersist
    protected void onCreate() {
        if (this.dataState == null) {
            this.dataState = DataStateCode.I;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.dataState == DataStateCode.I) {
            this.dataState = DataStateCode.U;
        }
    }
}
