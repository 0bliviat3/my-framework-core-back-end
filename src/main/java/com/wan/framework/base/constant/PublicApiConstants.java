package com.wan.framework.base.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 공개 API 경로 상수
 * - 인증이 필요없는 공개 API 경로 정의
 * - SessionValidationFilter와 ApiRegistryScanService에서 공유
 */
public class PublicApiConstants {

    /**
     * 인증이 필요없는 공개 API 경로 목록
     * - 로그인/회원가입
     * - 관리자 초기 설정
     * - Swagger/Actuator
     * - 에러 페이지
     */
    public static final List<String> PUBLIC_API_PATHS = Arrays.asList(
            "/sessions/login",
            "/sessions/logout",
            "/users/sign-in",
            "/users/sign-up",
            "/users/admin/exists",
            "/users/admin/initial",
            "/error",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs"
    );

    /**
     * 경로가 공개 API인지 확인
     */
    public static boolean isPublicApi(String path) {
        if (path == null) {
            return false;
        }
        return PUBLIC_API_PATHS.stream().anyMatch(path::startsWith);
    }

    private PublicApiConstants() {
        // 인스턴스 생성 방지
    }
}
