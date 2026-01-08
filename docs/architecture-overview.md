# Framework Core Back-end 아키텍처 개요

> **Version**: 0.0.1-SNAPSHOT
> **Last Updated**: 2026-01-08

---

## 📋 목차

1. [시스템 개요](#1-시스템-개요)
2. [전체 아키텍처](#2-전체-아키텍처)
3. [모듈 구조](#3-모듈-구조)
4. [보안 아키텍처](#4-보안-아키텍처)
5. [데이터베이스 설계](#5-데이터베이스-설계)
6. [캐싱 전략](#6-캐싱-전략)
7. [API 권한 관리](#7-api-권한-관리)
8. [세션 관리](#8-세션-관리)
9. [배치 및 스케줄링](#9-배치-및-스케줄링)
10. [성능 최적화](#10-성능-최적화)

---

## 1. 시스템 개요

### 1.1 프로젝트 정보

**Framework Core Back-end**는 Spring Boot 3.x 기반의 엔터프라이즈급 백엔드 프레임워크입니다.

| 항목 | 내용 |
|------|------|
| **프로젝트명** | Framework Core Back-end |
| **Group ID** | com.wan |
| **Version** | 0.0.1-SNAPSHOT |
| **Java** | 17 |
| **Spring Boot** | 3.5.4 |
| **빌드 도구** | Gradle |

### 1.2 핵심 기능

- ✅ **사용자 관리**: 회원가입, 로그인, Role 기반 권한
- ✅ **세션 관리**: Redis 기반 세션 저장, 감사 로그
- ✅ **동적 권한 시스템**: API 자동 스캔 + Role 기반 권한 제어
- ✅ **메뉴-프로그램-API 연계**: 3단계 권한 관리
- ✅ **동적 게시판**: JSON 기반 필드 정의
- ✅ **배치 작업**: Quartz 기반 스케줄링
- ✅ **공통 코드 관리**: 2단계 계층 구조
- ✅ **API Key 관리**: 외부 시스템 연동
- ✅ **Proxy API**: 외부 API 통합 호출

### 1.3 기술 스택

#### Backend Framework
- Spring Boot 3.5.4
- Spring Data JPA
- Spring Data Redis (Lettuce)
- Spring Security
- Spring Batch
- Quartz Scheduler

#### Database & Cache
- **MariaDB**: 관계형 데이터 저장
- **Redis 6.x**: 세션, 캐시, 분산 락

#### Libraries
- **Lombok**: 코드 간소화
- **MapStruct 1.5.5**: DTO 매핑
- **Apache Commons Pool2**: 커넥션 풀

---

## 2. 전체 아키텍처

### 2.1 시스템 아키텍처 다이어그램

```
┌──────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                          │
│                    http://localhost:3000                         │
└────────────────────────────┬─────────────────────────────────────┘
                             │ HTTP/HTTPS (Cookie-based Session)
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│                      http://localhost:8080                       │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              Web Layer (Controllers)                       │ │
│  │  - REST API Endpoints                                      │ │
│  │  - Request/Response Handling                               │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                            │
│  ┌───────────────────┴────────────────────────────────────────┐ │
│  │           Interceptor Chain                                │ │
│  │  1. FrameworkInterceptor (기본 처리)                        │ │
│  │  2. PermissionCheckInterceptor (권한 검증)                  │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                            │
│  ┌───────────────────┴────────────────────────────────────────┐ │
│  │          Service Layer (Business Logic)                    │ │
│  │  - 비즈니스 로직 처리                                         │ │
│  │  - 트랜잭션 관리                                             │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                            │
│  ┌───────────────────┴────────────────────────────────────────┐ │
│  │      Repository Layer (Data Access)                        │ │
│  │  - JPA Repository                                          │ │
│  │  - Custom Queries                                          │ │
│  └───────────────────┬────────────────────────────────────────┘ │
│                      │                                            │
└──────────────────────┼────────────────────────────────────────────┘
                       │
         ┌─────────────┴──────────────┐
         │                            │
         ↓                            ↓
┌─────────────────┐         ┌──────────────────┐
│    MariaDB      │         │      Redis       │
│  (Persistent)   │         │    (Cache)       │
│                 │         │                  │
│ - Users         │         │ - Sessions       │
│ - Roles         │         │ - Permissions    │
│ - Menus         │         │ - Code Cache     │
│ - Programs      │         │ - Distributed    │
│ - Boards        │         │   Lock           │
│ - Batch Jobs    │         │                  │
└─────────────────┘         └──────────────────┘
```

### 2.2 계층별 역할

#### Web Layer
- **책임**: HTTP 요청/응답 처리
- **구성요소**: `@RestController`, `@RequestMapping`
- **주요 작업**:
  - Request Body Validation
  - DTO ↔ Entity 변환
  - HTTP 상태 코드 반환

#### Service Layer
- **책임**: 비즈니스 로직 처리
- **구성요소**: `@Service`, `@Transactional`
- **주요 작업**:
  - 복잡한 비즈니스 규칙 구현
  - 여러 Repository 조율
  - 트랜잭션 경계 관리

#### Repository Layer
- **책임**: 데이터 접근
- **구성요소**: `JpaRepository`, `@Query`
- **주요 작업**:
  - CRUD 작업
  - 커스텀 쿼리 실행
  - 페이징/정렬

#### Domain Layer
- **책임**: 비즈니스 도메인 모델
- **구성요소**: `@Entity`, `@Table`
- **주요 작업**:
  - 데이터베이스 테이블 매핑
  - 연관관계 정의
  - 비즈니스 메서드

---

## 3. 모듈 구조

### 3.1 패키지 구조

```
com.wan.framework/
├── base/                      # 공통 기반
│   ├── constant/              # 상수 정의
│   ├── exception/             # 예외 처리
│   ├── config/                # 설정
│   ├── FrameworkSecurityConfig.java
│   └── FrameworkWebMVCConfig.java
│
├── user/                      # 사용자 관리
│   ├── domain/User.java
│   ├── dto/UserDTO.java
│   ├── repository/UserRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── SignService.java
│   │   └── PasswordService.java
│   └── web/UserController.java
│
├── session/                   # 세션 관리
│   ├── domain/
│   │   ├── UserSession.java  (Redis)
│   │   └── SessionAudit.java (MariaDB)
│   ├── service/
│   │   ├── SessionService.java
│   │   ├── SessionManagementService.java
│   │   └── SessionSecurityService.java
│   └── web/
│       ├── SessionController.java
│       └── SessionAdminController.java
│
├── permission/                # 권한 관리
│   ├── domain/
│   │   ├── Role.java
│   │   ├── ApiRegistry.java
│   │   └── RoleApiPermission.java
│   ├── service/
│   │   ├── PermissionService.java
│   │   ├── ApiRegistryScanService.java
│   │   └── PermissionCacheService.java
│   ├── interceptor/PermissionCheckInterceptor.java
│   └── web/PermissionController.java
│
├── menu/                      # 메뉴 관리
│   ├── domain/
│   │   ├── Menu.java
│   │   └── MenuTree.java
│   ├── service/
│   │   ├── MenuService.java
│   │   └── MenuPermissionService.java
│   └── web/MenuController.java
│
├── program/                   # 프로그램 관리
│   ├── domain/
│   │   ├── Program.java
│   │   └── ProgramApiMapping.java
│   ├── service/
│   │   ├── ProgramService.java
│   │   └── ProgramPermissionIntegrationService.java
│   └── web/
│       ├── ProgramController.java
│       └── ProgramPermissionController.java
│
├── code/                      # 공통 코드
│   ├── domain/
│   │   ├── CodeGroup.java
│   │   └── CodeItem.java
│   ├── service/
│   │   ├── CodeGroupService.java
│   │   ├── CodeItemService.java
│   │   └── CodeCacheSyncService.java
│   └── web/
│       ├── CodeGroupController.java
│       └── CodeItemController.java
│
├── board/                     # 동적 게시판
│   ├── domain/
│   │   ├── BoardMeta.java
│   │   ├── BoardFieldMeta.java
│   │   ├── BoardData.java
│   │   ├── BoardComment.java
│   │   ├── BoardAttachment.java
│   │   └── BoardPermission.java
│   ├── service/
│   │   ├── BoardMetaService.java
│   │   ├── BoardDataService.java
│   │   ├── BoardCommentService.java
│   │   └── BoardAttachmentService.java
│   └── web/
│       ├── BoardMetaController.java
│       ├── BoardDataController.java
│       ├── BoardCommentController.java
│       └── BoardAttachmentController.java
│
├── batch/                     # 배치 작업
│   ├── domain/
│   │   ├── BatchJob.java
│   │   └── BatchExecution.java
│   ├── service/
│   │   ├── BatchJobService.java
│   │   ├── BatchExecutionService.java
│   │   ├── BatchHistoryService.java
│   │   └── BatchSchedulerService.java
│   └── web/
│       ├── BatchJobController.java
│       └── BatchExecutionController.java
│
├── apikey/                    # API Key 관리
│   ├── domain/
│   │   ├── ApiKey.java
│   │   ├── ApiKeyPermission.java
│   │   └── ApiKeyUsageHistory.java
│   ├── service/
│   │   ├── ApiKeyService.java
│   │   ├── ApiKeyUsageHistoryService.java
│   │   └── RateLimitService.java
│   └── web/
│       ├── ApiKeyController.java
│       └── ApiKeyUsageHistoryController.java
│
├── proxy/                     # Proxy API
│   ├── domain/
│   │   ├── ApiEndpoint.java
│   │   └── ApiExecutionHistory.java
│   ├── service/
│   │   ├── ApiEndpointService.java
│   │   ├── ApiExecutionService.java
│   │   └── ApiExecutionHistoryService.java
│   └── web/
│       ├── ApiEndpointController.java
│       ├── ProxyApiController.java
│       └── ApiExecutionHistoryController.java
│
└── redis/                     # Redis 서비스
    ├── service/
    │   ├── RedisCacheService.java
    │   ├── DistributedLockService.java
    │   ├── ResilientDistributedLockService.java
    │   ├── LocalLockService.java
    │   └── LockWatchDogService.java
    └── web/
        ├── RedisCacheController.java
        └── RedisLockController.java
```

### 3.2 모듈 의존성 그래프

```
┌──────────┐
│   User   │
└────┬─────┘
     │
     ↓
┌──────────┐      ┌──────────┐
│ Session  │─────►│  Redis   │
└────┬─────┘      └──────────┘
     │
     ↓
┌──────────────────────────────────────┐
│           Permission                 │
│  - Role                              │
│  - ApiRegistry                       │
│  - RoleApiPermission                 │
└────┬─────────────────────────────────┘
     │
     ├──────────┐
     ↓          ↓
┌──────────┐ ┌──────────┐
│   Menu   │ │ Program  │
└────┬─────┘ └────┬─────┘
     │            │
     └─────┬──────┘
           ↓
  ┌─────────────────┐
  │ MenuPermission  │
  │ Integration     │
  └─────────────────┘

┌──────────┐      ┌──────────┐
│  Batch   │─────►│  Proxy   │
└──────────┘      └──────────┘

┌──────────┐
│  Board   │
└──────────┘

┌──────────┐
│  Code    │
└──────────┘

┌──────────┐
│ API Key  │
└──────────┘
```

---

## 4. 보안 아키텍처

### 4.1 Spring Security 설정

**위치**: `com.wan.framework.base.FrameworkSecurityConfig`

```java
@Configuration
public class FrameworkSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**특징**:
- CSRF 비활성화 (Session 기반 인증 사용)
- CORS 허용: `http://localhost:3000`
- 모든 요청 허용 (권한은 Interceptor에서 처리)

### 4.2 인터셉터 체인

**위치**: `com.wan.framework.base.FrameworkWebMVCConfig`

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    // 1. 기본 인터셉터
    registry.addInterceptor(frameworkInterceptor)
            .addPathPatterns("/**");

    // 2. 권한 검증 인터셉터
    registry.addInterceptor(permissionCheckInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/users/admin/exists",
                "/users/admin/initial",
                "/users/sign-up",
                "/sessions/login",
                "/api-docs",
                "/swagger-ui/**",
                "/error"
            );
}
```

**처리 순서**:
```
Request → FrameworkInterceptor → PermissionCheckInterceptor → Controller
```

### 4.3 권한 검증 플로우

**위치**: `com.wan.framework.permission.interceptor.PermissionCheckInterceptor`

```
┌─────────────────────────────────────────────────────────────────┐
│                  PermissionCheckInterceptor                     │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ↓
          ┌──────────────────────────────────────┐
          │ 1. API Registry 조회                 │
          │    (Method + URI Pattern)            │
          └──────────┬───────────────────────────┘
                     │
                     ↓
          ┌──────────────────────────────────────┐
          │ 2. auth_required 확인                │
          └──────────┬───────────────────────────┘
                     │
                     ├─ false → 통과
                     │
                     ↓ true
          ┌──────────────────────────────────────┐
          │ 3. 세션에서 User Roles 조회          │
          └──────────┬───────────────────────────┘
                     │
                     ↓
          ┌──────────────────────────────────────┐
          │ 4. ROLE_ADMIN 확인                   │
          └──────────┬───────────────────────────┘
                     │
                     ├─ ADMIN → 통과
                     │
                     ↓ 일반 사용자
          ┌──────────────────────────────────────┐
          │ 5. Redis 캐시에서 권한 확인          │
          │    SISMEMBER (O(1))                  │
          └──────────┬───────────────────────────┘
                     │
                     ├─ 권한 있음 → 통과
                     │
                     ↓ 권한 없음
          ┌──────────────────────────────────────┐
          │ 6. 403 Forbidden 응답                │
          └──────────────────────────────────────┘
```

### 4.4 비밀번호 암호화

**알고리즘**: PBKDF2WithHmacSHA256

**위치**: `com.wan.framework.user.service.PasswordService`

```java
public String hashPassword(String password, String salt) {
    PBEKeySpec spec = new PBEKeySpec(
        password.toCharArray(),
        salt.getBytes(),
        10000,  // iteration
        256     // key length
    );
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();
    return Base64.getEncoder().encodeToString(hash);
}
```

**특징**:
- Salt 사용 (16 bytes)
- Iteration: 10,000회
- Key Length: 256 bits

---

## 5. 데이터베이스 설계

### 5.1 ERD 개요

```
┌──────────────┐         ┌─────────────┐
│   t_user     │ N     M │   t_role    │
│              │◄────────┤             │
└──────┬───────┘         └──────┬──────┘
       │                        │
       │ 1:N                    │ 1:N
       │                        │
       ↓                        ↓
┌──────────────┐         ┌──────────────────┐
│t_session_audit│        │t_role_api_permission│
└──────────────┘         └──────┬───────────┘
                                │ N:M
                                │
                                ↓
                         ┌──────────────┐
                         │t_api_registry│
                         └──────┬───────┘
                                │ N:M
                                │
                                ↓
                         ┌─────────────────────┐
                         │t_program_api_mapping│
                         └──────┬──────────────┘
                                │ N:1
                                │
                                ↓
┌──────────┐            ┌──────────────┐
│  t_menu  │ N       1  │  t_program   │
│          ├───────────►│              │
└──────────┘            └──────────────┘
     │ (Self FK)
     │
     └───────┐
             │
             ↓
       ┌──────────┐
       │  t_menu  │
       └──────────┘

┌──────────────┐         ┌─────────────────┐
│t_board_meta  │ 1     N │  t_board_data   │
│              │◄────────┤                 │
└──────────────┘         └────┬────────────┘
                              │ 1:N
                              │
                              ↓
                         ┌────────────────┐
                         │t_board_comment │
                         └────────────────┘
                              │ (Self FK)
                              │
                              ↓
                         ┌────────────────┐
                         │t_board_comment │
                         └────────────────┘

┌──────────────┐         ┌──────────────────┐
│t_batch_job   │ 1     N │t_batch_execution │
│              │◄────────┤                  │
└──────┬───────┘         └──────────────────┘
       │ N:1
       │
       ↓
┌──────────────┐
│t_api_endpoint│
└──────────────┘

┌──────────────┐         ┌─────────────────────┐
│  t_api_key   │ 1     N │t_api_key_permission │
│              │◄────────┤                     │
└──────┬───────┘         └─────────────────────┘
       │ 1:N
       │
       ↓
┌──────────────────────┐
│t_api_key_usage_history│
└──────────────────────┘

┌──────────────┐         ┌──────────────┐
│t_code_group  │ 1     N │t_code_item   │
│              │◄────────┤              │
└──────────────┘         └──────────────┘
```

### 5.2 핵심 테이블

#### t_user
```sql
CREATE TABLE t_user (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    data_code VARCHAR(10) NOT NULL,
    create_time DATETIME NOT NULL,
    modified_time DATETIME
);

CREATE TABLE t_user_role (
    user_id VARCHAR(50),
    role VARCHAR(50),
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
);
```

#### t_role
```sql
CREATE TABLE t_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);
```

#### t_api_registry
```sql
CREATE TABLE t_api_registry (
    api_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(100) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    uri_pattern VARCHAR(500) NOT NULL,
    controller_name VARCHAR(255),
    handler_method VARCHAR(255),
    auth_required BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    UNIQUE KEY uk_api (service_id, http_method, uri_pattern)
);
```

#### t_role_api_permission
```sql
CREATE TABLE t_role_api_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    allowed BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_role_api (role_id, api_id),
    FOREIGN KEY (role_id) REFERENCES t_role(role_id),
    FOREIGN KEY (api_id) REFERENCES t_api_registry(api_id)
);
```

#### t_program_api_mapping
```sql
CREATE TABLE t_program_api_mapping (
    mapping_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_program_api (program_id, api_id),
    FOREIGN KEY (program_id) REFERENCES t_program(program_id),
    FOREIGN KEY (api_id) REFERENCES t_api_registry(api_id)
);
```

#### t_menu
```sql
CREATE TABLE t_menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    program_id BIGINT,
    menu_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    roles VARCHAR(255) NOT NULL DEFAULT 'guest',
    icon VARCHAR(100),
    data_code VARCHAR(10) NOT NULL,
    able_state VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (parent_id) REFERENCES t_menu(menu_id),
    FOREIGN KEY (program_id) REFERENCES t_program(program_id)
);
```

### 5.3 인덱스 전략

**주요 인덱스**:

```sql
-- User 조회 성능
CREATE INDEX idx_user_create_time ON t_user(create_time DESC);

-- API Registry 조회 (인터셉터에서 자주 사용)
CREATE INDEX idx_api_method_uri ON t_api_registry(http_method, uri_pattern);
CREATE INDEX idx_api_status ON t_api_registry(status);

-- 권한 조회
CREATE INDEX idx_permission_role ON t_role_api_permission(role_id);
CREATE INDEX idx_permission_api ON t_role_api_permission(api_id);

-- Menu 트리 조회
CREATE INDEX idx_menu_parent ON t_menu(parent_id);
CREATE INDEX idx_menu_program ON t_menu(program_id);

-- Board 조회
CREATE INDEX idx_board_data_meta ON t_board_data(board_meta_id);
CREATE INDEX idx_board_data_created ON t_board_data(created_at DESC);
CREATE INDEX idx_board_comment_data ON t_board_comment(board_data_id);
CREATE INDEX idx_board_comment_parent ON t_board_comment(parent_comment_id);

-- Batch 조회
CREATE INDEX idx_batch_execution_job ON t_batch_execution(batch_job_id);
CREATE INDEX idx_batch_execution_start ON t_batch_execution(start_time DESC);
```

---

## 6. 캐싱 전략

### 6.1 Redis 캐시 구조

```
Redis DB 0
├── SESSION:{sessionId}                    # 세션 데이터 (TTL: 30분)
│   └── UserSession (JSON)
│
├── ROLE_API_PERMISSION::{roleCode}        # Role별 API 권한 (TTL: 24시간)
│   └── Set<apiIdentifier>
│
├── CODE_GROUP::{groupCode}                # 공통 코드 (TTL: 무제한)
│   └── CodeGroup + CodeItems (JSON)
│
├── DISTRIBUTED_LOCK::{lockKey}            # 분산 락 (TTL: 30초)
│   └── ownerId
│
└── API_KEY::{apiKeyHash}                  # API Key 캐시 (TTL: 1시간)
    └── ApiKey (JSON)
```

### 6.2 캐시 전략별 설명

#### 세션 캐시
- **Key**: `SESSION:{sessionId}`
- **Value**: `UserSession` (JSON)
- **TTL**: 30분
- **갱신**: `/sessions/refresh` 호출 시 TTL 재설정
- **삭제**: 로그아웃 시 즉시 삭제

#### 권한 캐시
- **Key**: `ROLE_API_PERMISSION::{roleCode}`
- **Value**: `Set<apiIdentifier>`
- **TTL**: 24시간
- **구조**:
  ```
  ROLE_API_PERMISSION::ROLE_ADMIN
  ├── "framework::GET::/users"
  ├── "framework::POST::/users"
  ├── "framework::PUT::/users"
  └── ...
  ```
- **조회**: `SISMEMBER` (O(1))
- **갱신**: 권한 변경 시 즉시 삭제 후 재생성

#### 공통 코드 캐시
- **Key**: `CODE_GROUP::{groupCode}`
- **Value**: `CodeGroup + CodeItems` (JSON)
- **TTL**: 무제한
- **갱신**: `/code-groups/cache/refresh` 호출 시 수동 갱신
- **활용**: Select Box, Enum 대체

#### 분산 락
- **Key**: `DISTRIBUTED_LOCK::{lockKey}`
- **Value**: `ownerId` (UUID)
- **TTL**: 30초 (WatchDog가 자동 갱신)
- **활용**: 배치 동시 실행 방지, 중복 결제 방지

### 6.3 캐시 무효화 전략

**Write-Through 패턴**:
```java
@Transactional
public void updateRole(Long roleId, RoleDTO dto) {
    // 1. DB 업데이트
    Role role = roleRepository.findById(roleId).orElseThrow();
    role.update(dto);

    // 2. Redis 캐시 즉시 삭제
    permissionCacheService.evictRoleCache(role.getRoleCode());
}
```

**Lazy Loading 패턴** (공통 코드):
```java
public CodeGroup getCodeGroup(String groupCode) {
    // 1. Redis 조회
    CodeGroup cached = redisTemplate.opsForValue().get("CODE_GROUP::" + groupCode);
    if (cached != null) {
        return cached;
    }

    // 2. DB 조회 후 캐싱
    CodeGroup group = codeGroupRepository.findById(groupCode).orElseThrow();
    redisTemplate.opsForValue().set("CODE_GROUP::" + groupCode, group);
    return group;
}
```

---

## 7. API 권한 관리

### 7.1 Menu → Program → API 연계 구조

```
┌─────────┐         ┌─────────┐         ┌────────────┐         ┌──────┐
│  Menu   │ N     1 │ Program │ N     M │ ApiRegistry│ N     M │ Role │
│  (메뉴)  ├────────►│ (화면)   ├────────►│   (API)    │◄────────┤(역할)│
└─────────┘         └─────────┘         └────────────┘         └──────┘
  네비게이션            화면/페이지           API 엔드포인트          권한
```

**연계 흐름**:
```
1. 사용자가 메뉴 클릭
   ↓
2. Menu → Program 매핑 조회
   ↓
3. Program → API 매핑 조회 (필요한 API 목록)
   ↓
4. Role → API 권한 조회 (접근 가능한 API)
   ↓
5. 필요 API ⊆ 접근 가능 API → 메뉴 표시
```

### 7.2 자동 API 스캔

**위치**: `com.wan.framework.permission.service.ApiRegistryScanService`

**실행 시점**: `ApplicationReadyEvent` (애플리케이션 시작 완료 시)

**처리 로직**:
```java
@EventListener(ApplicationReadyEvent.class)
@Transactional
public void scanAndRegisterApis() {
    // 1. RequestMappingHandlerMapping에서 모든 API 스캔
    Map<String, ApiInfo> scannedApis = scanAllApis();

    // 2. DB의 기존 API 조회
    List<ApiRegistry> existingApis = apiRegistryRepository.findAll();

    // 3. 비교 및 동기화
    for (ApiInfo info : scannedApis.values()) {
        ApiRegistry existing = findExisting(existingApis, info);

        if (existing == null) {
            // INSERT: 새로운 API
            apiRegistryRepository.save(ApiRegistry.from(info));
        } else if (hasChanged(existing, info)) {
            // UPDATE: API 변경됨
            existing.update(info);
        }
    }

    // 4. DEACTIVATE: 삭제된 API
    for (ApiRegistry api : existingApis) {
        if (!scannedApis.containsKey(api.getApiIdentifier())) {
            api.setStatus(ApiStatus.INACTIVE);
        }
    }
}
```

**스캔 대상**:
- `@RestController` + `@RequestMapping`
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`

**제외 대상**:
- Spring Boot Actuator 엔드포인트
- Swagger UI 엔드포인트

### 7.3 권한 검증 최적화

**O(1) 성능**:

```java
public boolean hasPermission(String roleCode, String apiIdentifier) {
    String cacheKey = ROLE_API_PERMISSION + roleCode;
    Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, apiIdentifier);
    return Boolean.TRUE.equals(isMember);
}
```

**Redis SET 구조**:
```
ROLE_API_PERMISSION::ROLE_USER
├── "framework::GET::/users/{userId}"
├── "framework::PUT::/users"
└── "framework::GET::/menus/tree"
```

**복잡도**:
- **DB 조회**: O(N) - JOIN + WHERE
- **Redis SISMEMBER**: O(1) - Hash Table

**성능 차이**:
- DB: ~10ms
- Redis: ~1ms

---

## 8. 세션 관리

### 8.1 세션 아키텍처

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request (Cookie: SESSION=xxx)
       ↓
┌──────────────────────────────────────┐
│   SessionService                     │
│   - extractSessionId(Cookie)         │
│   - validateSession(sessionId)       │
└──────┬───────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│   Redis (Session Store)              │
│   Key: SESSION:{sessionId}           │
│   Value: {                           │
│     sessionId, userId, username,     │
│     roles, loginTime, ipAddress      │
│   }                                  │
│   TTL: 30분                          │
└──────┬───────────────────────────────┘
       │
       ↓ (비동기 로깅)
┌──────────────────────────────────────┐
│   MariaDB (Session Audit)            │
│   t_session_audit:                   │
│   - session_id                       │
│   - user_id                          │
│   - action (LOGIN/LOGOUT)            │
│   - ip_address                       │
│   - created_at                       │
└──────────────────────────────────────┘
```

### 8.2 세션 생성 플로우

```
1. POST /sessions/login (userId, password)
   ↓
2. UserService.authenticate(userId, password)
   ↓ (PBKDF2 검증)
3. SessionService.createSession(user, request)
   ↓
4. Generate sessionId (UUID)
   ↓
5. Redis.set("SESSION:"+sessionId, userSession, 30분)
   ↓
6. Response with Set-Cookie: SESSION=sessionId
   ↓
7. SessionAudit DB INSERT (비동기)
```

### 8.3 세션 검증 플로우

```
1. Request with Cookie: SESSION=xxx
   ↓
2. Extract sessionId from Cookie
   ↓
3. Redis.get("SESSION:"+sessionId)
   ↓
4. Found? → Return UserSession
   ↓
5. Not Found? → 401 Unauthorized
```

### 8.4 중복 로그인 방지

**전략**: 동일 사용자의 기존 세션 강제 종료

```java
public void checkConcurrentSessions(String userId) {
    // Redis에서 해당 사용자의 모든 세션 검색
    Set<String> sessionKeys = redisTemplate.keys("SESSION:*");

    for (String key : sessionKeys) {
        UserSession session = redisTemplate.opsForValue().get(key);
        if (session.getUserId().equals(userId)) {
            // 기존 세션 삭제
            redisTemplate.delete(key);
        }
    }
}
```

---

## 9. 배치 및 스케줄링

### 9.1 Quartz 아키텍처

```
┌──────────────────────────────────────┐
│        Quartz Scheduler              │
│  - JDBC JobStore (MariaDB)           │
│  - Clustered Mode                    │
└──────┬───────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│      BatchSchedulerService           │
│  - scheduleJob()                     │
│  - unscheduleJob()                   │
│  - pauseJob()                        │
│  - resumeJob()                       │
└──────┬───────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│         QuartzJob                    │
│  - execute(JobExecutionContext)      │
└──────┬───────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│      BatchExecutionService           │
│  1. BatchExecution INSERT (RUNNING)  │
│  2. Proxy API 호출                   │
│  3. 성공/실패 처리                    │
│  4. BatchExecution UPDATE            │
└──────┬───────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│      Proxy API Service               │
│  - HTTP 요청 전송                     │
│  - 템플릿 파라미터 치환               │
│  - Retry 로직                        │
│  - Timeout 처리                      │
└──────────────────────────────────────┘
```

### 9.2 스케줄 타입

#### CRON
```java
{
  "scheduleType": "CRON",
  "scheduleExpression": "0 0 2 * * ?"  // 매일 새벽 2시
}
```

**예시**:
- `0 0 2 * * ?` - 매일 새벽 2시
- `0 */10 * * * ?` - 10분마다
- `0 0 9-18 * * MON-FRI` - 평일 9시~18시 매시간

#### INTERVAL
```java
{
  "scheduleType": "INTERVAL",
  "scheduleExpression": "3600000"  // 1시간 (밀리초)
}
```

### 9.3 재시도 및 타임아웃

```java
@Transactional
public void executeBatch(BatchJob job) {
    int retryCount = 0;
    int maxRetry = job.getMaxRetryCount();

    while (retryCount <= maxRetry) {
        try {
            // Proxy API 호출 (타임아웃 적용)
            String result = proxyApiService.execute(
                job.getProxyApiCode(),
                job.getExecutionParameters(),
                job.getTimeoutSeconds()
            );

            // 성공
            batchExecution.setStatus(BatchExecutionStatus.SUCCESS);
            batchExecution.setResultMessage(result);
            break;

        } catch (TimeoutException e) {
            // 타임아웃
            batchExecution.setStatus(BatchExecutionStatus.TIMEOUT);
            break;

        } catch (Exception e) {
            // 재시도
            retryCount++;
            if (retryCount > maxRetry) {
                batchExecution.setStatus(BatchExecutionStatus.FAILED);
                batchExecution.setErrorMessage(e.getMessage());
            }
        }
    }
}
```

---

## 10. 성능 최적화

### 10.1 데이터베이스 최적화

#### N+1 문제 해결
```java
// Bad: N+1 문제 발생
@OneToMany(mappedBy = "menu")
private List<Menu> children;

// Good: Fetch Join 사용
@Query("SELECT m FROM Menu m LEFT JOIN FETCH m.children WHERE m.parent IS NULL")
List<Menu> findAllWithChildren();
```

#### 페이징 최적화
```java
// CountQuery 분리
@Query(
    value = "SELECT u FROM User u WHERE u.dataStateCode != 'D'",
    countQuery = "SELECT COUNT(u) FROM User u WHERE u.dataStateCode != 'D'"
)
Page<User> findAllActive(Pageable pageable);
```

### 10.2 캐싱 최적화

#### 캐시 워밍업
```java
@EventListener(ApplicationReadyEvent.class)
public void warmUpCache() {
    // 모든 Role의 권한 미리 캐싱
    List<Role> roles = roleRepository.findAll();
    for (Role role : roles) {
        permissionCacheService.cacheRolePermissions(role.getRoleCode());
    }

    // 공통 코드 미리 캐싱
    List<CodeGroup> groups = codeGroupRepository.findAll();
    for (CodeGroup group : groups) {
        codeCacheService.cacheCodeGroup(group.getGroupCode());
    }
}
```

#### 캐시 히트율 모니터링
```java
@Aspect
@Component
public class CacheMonitoringAspect {

    @Around("@annotation(Cacheable)")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long elapsed = System.currentTimeMillis() - start;

        if (elapsed < 5) {
            // Cache Hit (Redis)
            cacheHitCounter.increment();
        } else {
            // Cache Miss (DB)
            cacheMissCounter.increment();
        }

        return result;
    }
}
```

### 10.3 인덱스 활용

**Composite Index**:
```sql
-- 복합 조건 검색용
CREATE INDEX idx_board_meta_created
ON t_board_data(board_meta_id, created_at DESC);

-- Covering Index (SELECT 컬럼 포함)
CREATE INDEX idx_user_list
ON t_user(data_code, user_id, name, create_time);
```

### 10.4 Connection Pool 튜닝

**HikariCP 설정** (application.yml):
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # CPU 코어 수 * 2~3
      minimum-idle: 5            # maximum-pool-size / 2
      connection-timeout: 30000  # 30초
      idle-timeout: 600000       # 10분
      max-lifetime: 1800000      # 30분
```

**Redis Connection Pool**:
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: -1ms
```

### 10.5 비동기 처리

**세션 감사 로그** (비동기):
```java
@Async
@Transactional
public void logSessionAudit(String sessionId, String userId, String action) {
    SessionAudit audit = SessionAudit.builder()
        .sessionId(sessionId)
        .userId(userId)
        .action(action)
        .ipAddress(request.getRemoteAddr())
        .build();

    sessionAuditRepository.save(audit);
}
```

**@Async 설정**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

---

## 부록: 성능 지표

### 응답 시간 목표

| API 카테고리 | 목표 응답 시간 | 캐싱 여부 |
|-------------|--------------|----------|
| 로그인/로그아웃 | < 200ms | N |
| 메뉴 조회 | < 50ms | Y (Redis) |
| 권한 검증 | < 5ms | Y (Redis) |
| 게시글 목록 | < 100ms | N |
| 게시글 상세 | < 50ms | N |
| 공통 코드 조회 | < 10ms | Y (Redis) |
| 사용자 목록 | < 150ms | N (페이징) |

### 동시 사용자 목표

- **동시 접속자**: 1,000명
- **세션 수**: 최대 10,000개 (Redis)
- **TPS**: 100 req/sec

### 캐시 히트율 목표

- **권한 캐시**: > 95%
- **공통 코드**: > 99%
- **세션 조회**: > 99%

---

## 결론

이 프레임워크는 **엔터프라이즈급 백엔드 시스템**으로서 다음과 같은 특징을 가집니다:

1. ✅ **모듈화된 아키텍처**: 각 기능이 독립적으로 구성
2. ✅ **자동화된 권한 관리**: API 스캔 + Redis 캐싱
3. ✅ **확장 가능한 설계**: 동적 게시판, Proxy API 패턴
4. ✅ **고성능**: Redis 캐싱 + 인덱스 최적화
5. ✅ **보안 강화**: PBKDF2 암호화, 세션 감사, 권한 검증

**최종 권장 사항**:
- 운영 환경 배포 전 부하 테스트 수행
- 모니터링 도구 (Prometheus, Grafana) 도입
- 로그 수집 및 분석 (ELK Stack) 구축
- API 문서화 (Swagger/OpenAPI) 지속 업데이트
