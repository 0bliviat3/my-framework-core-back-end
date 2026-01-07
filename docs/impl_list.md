# Framework Core Back-end 구현 현황

> 최종 업데이트: 2026-01-07

## 📋 목차
- [1. 프로젝트 개요](#1-프로젝트-개요)
- [2. 기술 스택](#2-기술-스택)
- [3. 구현 완료 모듈](#3-구현-완료-모듈)
- [4. 아키텍처](#4-아키텍처)
- [5. 데이터베이스 스키마](#5-데이터베이스-스키마)
- [6. API 엔드포인트](#6-api-엔드포인트)
- [7. 테스트 현황](#7-테스트-현황)
- [8. 다음 개발 예정](#8-다음-개발-예정)

---

## 1. 프로젝트 개요

Spring Boot 기반의 엔터프라이즈 프레임워크 백엔드 시스템으로, 동적 게시판을 포함한 다양한 관리 기능을 제공합니다.

### 프로젝트 정보
- **Group**: `com.wan`
- **Version**: `0.0.1-SNAPSHOT`
- **Java**: 17
- **Spring Boot**: 3.5.4
- **Build Tool**: Gradle

---

## 2. 기술 스택

### Backend Framework
- Spring Boot 3.5.4
- Spring Data JPA
- Spring Data Redis (Lettuce)
- Spring Session Data Redis
- Spring Security
- Spring Batch
- Quartz Scheduler

### Database & Cache
- MariaDB (JDBC Driver 3.3.3)
- Redis 6.x (Lettuce Client)
- Hibernate ORM

### Libraries
- Lombok (코드 간소화)
- MapStruct 1.5.5 (DTO 매핑)
- Apache Commons Pool2 (Redis 커넥션 풀)
- AssertJ (테스트)

---

## 3. 구현 완료 모듈

### 3.1 Base 모듈 (공통 기반)

#### ✅ 설정 및 공통 컴포넌트
| 구분 | 파일명 | 설명 | 위치 |
|------|--------|------|------|
| Security | `FrameworkSecurityConfig.java` | Spring Security 설정 | `base/` |
| MVC | `FrameworkWebMVCConfig.java` | CORS, Interceptor 설정 | `base/` |
| Interceptor | `FrameworkInterceptor.java` | HTTP 요청 인터셉터 | `base/` |
| Exception | `FrameworkExceptionHandler.java` | 전역 예외 처리 | `base/exception/` |
| Exception | `FrameworkException.java` | 커스텀 예외 클래스 | `base/exception/` |
| Exception | `ExceptionResponse.java` | 예외 응답 DTO | `base/exception/` |
| Exception | `ExceptionConst.java` | 예외 상수 인터페이스 | `base/exception/` |

#### ✅ 공통 상수
| 구분 | 파일명 | 설명 | 값 |
|------|--------|------|-----|
| 데이터 상태 | `DataStateCode.java` | 데이터 상태 관리 | I(Insert), U(Update), D(Delete) |
| 활성화 상태 | `AbleState.java` | 활성/비활성 | ABLE, DISABLE |

**주요 기능:**
- CORS 설정: `http://localhost:9527` 허용
- CSRF 비활성화
- 모든 요청 허용 (개발 모드)
- 전역 예외 처리 및 에러 히스토리 자동 저장

---

### 3.2 User 모듈 (사용자 관리)

#### ✅ 구현 파일 (총 13개)

**Domain & DTO**
- `User.java` - 사용자 엔티티
- `UserDTO.java` - 사용자 DTO (비밀번호 제거 메서드 포함)

**Repository**
- `UserRepository.java` - JPA Repository

**Service**
- `SignService.java` - 회원가입/로그인 로직
- `UserService.java` - 사용자 CRUD
- `PasswordService.java` - 비밀번호 암호화 (PBKDF2)

**Controller**
- `LoginController.java` - 인증 및 사용자 관리 API

**Mapper**
- `UserMapper.java` - Entity ↔ DTO 변환

**Exception**
- `UserException.java` - 사용자 관련 예외
- `UserExceptionMessage.java` - 예외 메시지 상수

**Test**
- `PasswordServiceTest.java` - 비밀번호 서비스 테스트 (7개 테스트)

**주요 기능:**
- ✅ 회원가입 (비밀번호 암호화 + 솔트)
- ✅ 로그인 (세션 + 쿠키)
- ✅ 로그아웃
- ✅ 사용자 CRUD
- ✅ 사용자 목록 조회 (페이징)
- ✅ 비밀번호 검증

**API 엔드포인트:**
- `POST /sign-up` - 회원가입
- `POST /sign-in` - 로그인
- `GET /sign-out` - 로그아웃
- `GET /users` - 사용자 목록 (페이징)
- `GET /user` - 사용자 조회
- `PUT /user` - 사용자 수정
- `DELETE /user` - 사용자 삭제

---

### 3.3 Program 모듈 (프로그램 관리)

#### ✅ 구현 파일 (총 8개)

**Domain & DTO**
- `Program.java` - 프로그램 엔티티
- `ProgramDTO.java` - 프로그램 DTO

**Repository**
- `ProgramRepository.java` - JPA Repository

**Service**
- `ProgramService.java` - 프로그램 CRUD

**Controller**
- `ProgramController.java` - 프로그램 관리 API

**Mapper**
- `ProgramMapper.java` - Entity ↔ DTO 변환

**Exception**
- `ProgramException.java` - 프로그램 관련 예외
- `ProgramExceptionMessage.java` - 예외 메시지 상수

**주요 기능:**
- ✅ 프로그램 생성/수정/삭제
- ✅ 프로그램 목록 조회 (페이징)
- ✅ 활성/비활성 상태 관리

**API 엔드포인트:**
- `POST /programs` - 프로그램 생성
- `GET /programs` - 프로그램 목록
- `GET /programs/{id}` - 프로그램 조회
- `PUT /programs` - 프로그램 수정
- `DELETE /programs/{id}` - 프로그램 삭제

---

### 3.4 Menu 모듈 (메뉴 관리)

#### ✅ 구현 파일 (총 9개)

**Domain & DTO**
- `Menu.java` - 메뉴 엔티티 (계층 구조)
- `MenuDTO.java` - 메뉴 DTO
- `MenuTreeNodeDTO.java` - 메뉴 트리 DTO

**Repository**
- `MenuRepository.java` - JPA Repository

**Service**
- `MenuService.java` - 메뉴 CRUD 및 트리 구조

**Controller**
- `MenuController.java` - 메뉴 관리 API

**Mapper**
- `MenuMapper.java` - Entity ↔ DTO 변환

**Exception**
- `MenuException.java` - 메뉴 관련 예외
- `MenuExceptionMessage.java` - 예외 메시지 상수

**주요 기능:**
- ✅ 메뉴 생성/수정/삭제
- ✅ 계층형 메뉴 구조 (부모-자식)
- ✅ 메뉴 트리 조회 (역할 기반 필터링)
- ✅ Program과 연관 관계

**API 엔드포인트:**
- `POST /menus` - 메뉴 생성
- `GET /menus` - 전체 메뉴 조회
- `GET /menus/{id}` - 메뉴 조회
- `GET /menus/tree` - 메뉴 트리 조회 (역할 기반)
- `PUT /menus/{id}` - 메뉴 수정
- `DELETE /menus/{id}` - 메뉴 삭제

---

### 3.5 History 모듈 (예외 처리 및 로깅)

#### ✅ 구현 파일 (총 5개)

**Domain & DTO**
- `ErrorHistory.java` - 에러 로그 엔티티
- `ErrorHistoryDTO.java` - 에러 로그 DTO

**Repository**
- `ErrorHistoryRepository.java` - JPA Repository

**Service**
- `ErrorHistoryService.java` - 예외 저장 서비스

**Mapper**
- `ErrorHistoryMapper.java` - Entity ↔ DTO 변환

**주요 기능:**
- ✅ 예외 발생 시 자동 DB 저장
- ✅ HTTP 요청 정보 포함 (URL, 파라미터)
- ✅ 에러 메시지 및 스택 트레이스 저장
- ✅ 이벤트 시간 자동 기록

---

### 3.6 API Key 모듈 (API 키 관리) ⭐ NEW

#### ✅ 구현 파일 (총 21개)

**Domain (3개)**
- `ApiKey.java` - API Key 엔티티 (SHA-256 해싱, 만료일, 활성 상태)
- `ApiKeyPermission.java` - API Key 권한 매핑
- `ApiKeyUsageHistory.java` - API Key 사용 이력

**DTO (3개)**
- `ApiKeyDTO.java` - API Key DTO (rawApiKey, permissions 포함)
- `ApiKeyPermissionDTO.java` - 권한 DTO
- `ApiKeyUsageHistoryDTO.java` - 사용 이력 DTO

**Repository (3개)**
- `ApiKeyRepository.java` - API Key Repository
- `ApiKeyPermissionRepository.java` - 권한 Repository
- `ApiKeyUsageHistoryRepository.java` - 사용 이력 Repository

**Service (2개)**
- `ApiKeyService.java` - API Key 생성/검증/권한 관리
- `ApiKeyUsageHistoryService.java` - 사용 이력 조회/통계

**Controller (2개)**
- `ApiKeyController.java` - API Key 관리 API
- `ApiKeyUsageHistoryController.java` - 사용 이력 API

**Mapper (3개)**
- `ApiKeyMapper.java` - Entity ↔ DTO 변환
- `ApiKeyPermissionMapper.java` - 권한 매핑
- `ApiKeyUsageHistoryMapper.java` - 사용 이력 매핑

**Config & Interceptor**
- `ApiKeyWebConfig.java` - Bearer 인증 인터셉터 설정
- `BearerAuthenticationInterceptor.java` - Authorization 헤더 검증

**Util**
- `ApiKeyGenerator.java` - SecureRandom 기반 API Key 생성

**Exception**
- `ApiKeyException.java` - API Key 관련 예외
- `ApiKeyExceptionMessage.java` - 예외 메시지 (9개 상수)

**Test (2개)**
- `ApiKeyServiceTest.java` - API Key 서비스 테스트
- `ApiKeyGeneratorTest.java` - Key 생성기 테스트

#### 📋 API Key 모듈 상세 기능

**1. API Key 생성 및 관리**
- ✅ SecureRandom 기반 64자리 API Key 생성
- ✅ SHA-256 해싱으로 안전한 저장
- ✅ API Key Prefix (앞 8자) 저장으로 식별 지원
- ✅ 만료일 설정 (expiredAt)
- ✅ 활성/비활성 상태 관리 (AbleState)
- ✅ 논리적 삭제 (DataStateCode)
- ✅ 사용 횟수 자동 카운트
- ✅ 마지막 사용 시각 기록

**2. 권한 관리 (ApiKeyPermission)**
- ✅ API Key별 다중 권한 매핑
- ✅ 권한 추가/삭제
- ✅ 권한 존재 여부 확인
- ✅ 중복 권한 방지

**3. 사용 이력 추적 (ApiKeyUsageHistory)**
- ✅ 요청 URI, HTTP Method 기록
- ✅ IP 주소, User Agent 저장
- ✅ 성공/실패 여부 (isSuccess)
- ✅ 에러 메시지 기록
- ✅ 사용 시각 자동 기록
- ✅ 기간별 이력 조회
- ✅ 성공/실패 통계

**4. Bearer Token 인증**
- ✅ Authorization 헤더 검증
- ✅ Bearer {token} 형식 파싱
- ✅ API Key 유효성 검증 (존재, 활성, 만료)
- ✅ HTTP 인터셉터로 자동 인증
- ✅ 사용 이력 자동 기록

**5. 보안 기능**
- ✅ API Key는 생성 시 한 번만 반환 (rawApiKey)
- ✅ DB에는 SHA-256 해시값만 저장
- ✅ API Key Prefix로 부분 식별 가능
- ✅ 만료된 키 자동 차단
- ✅ 비활성화된 키 차단

---

### 3.7 Board 모듈 (동적 게시판)

#### ✅ 구현 파일 (총 48개)

**Domain (6개)**
- `BoardMeta.java` - 게시판 메타 정보
- `BoardFieldMeta.java` - 게시판 필드 정의 (동적 컬럼)
- `BoardData.java` - 게시글 데이터
- `BoardPermission.java` - 게시판 권한 관리
- `BoardComment.java` - 계층형 댓글
- `BoardAttachment.java` - 첨부파일

**DTO (6개)**
- `BoardMetaDTO.java`
- `BoardFieldMetaDTO.java`
- `BoardDataDTO.java`
- `BoardPermissionDTO.java`
- `BoardCommentDTO.java`
- `BoardAttachmentDTO.java`

**Repository (6개)**
- `BoardMetaRepository.java`
- `BoardFieldMetaRepository.java`
- `BoardDataRepository.java` - 이전/다음글 쿼리 포함
- `BoardPermissionRepository.java`
- `BoardCommentRepository.java` - Fetch Join
- `BoardAttachmentRepository.java`

**Service (5개)**
- `BoardMetaService.java` - 게시판 CRUD + 복제
- `BoardFieldMetaService.java` - 필드 관리
- `BoardDataService.java` - 게시글 CRUD + 조회수 + 검색
- `BoardCommentService.java` - 계층형 댓글
- `BoardAttachmentService.java` - 파일 업로드/다운로드

**Controller (5개)**
- `BoardMetaController.java`
- `BoardFieldMetaController.java`
- `BoardDataController.java`
- `BoardCommentController.java`
- `BoardAttachmentController.java`

**Mapper (5개)**
- `BoardMetaMapper.java`
- `BoardFieldMetaMapper.java`
- `BoardDataMapper.java`
- `BoardPermissionMapper.java`
- `BoardCommentMapper.java`
- `BoardAttachmentMapper.java`

**Config & Util**
- `FileStorageProperties.java` - 파일 업로드 설정
- `FileStorageUtil.java` - 파일 저장 유틸리티

**Constants (4개)**
- `FieldType.java` - 필드 타입 (TEXT, NUMBER, DATE 등 11가지)
- `BoardDataStatus.java` - 게시글 상태 (DRAFT, PUBLISHED, PINNED)
- `PermissionType.java` - 권한 타입 (READ, WRITE, UPDATE, DELETE, COMMENT)
- `BoardExceptionMessage.java` - 예외 메시지 (16개)

**Exception**
- `BoardException.java` - 게시판 관련 예외

**Test (4개, 총 73개 테스트)**
- `BoardMetaServiceTest.java` - 10개 테스트
- `BoardDataServiceTest.java` - 14개 테스트
- `BoardCommentServiceTest.java` - 11개 테스트
- `BoardAttachmentServiceTest.java` - 14개 테스트

#### 📋 Board 모듈 상세 기능

**1. 게시판 관리 (BoardMeta)**
- ✅ 게시판 생성/수정/삭제
- ✅ 게시판 복제 기능
- ✅ 게시판 활성/비활성
- ✅ 댓글 사용 여부 설정
- ✅ 역할 기반 접근 권한

**2. 필드 관리 (BoardFieldMeta)**
- ✅ 동적 필드 추가/수정/삭제
- ✅ 11가지 필드 타입 지원
  - TEXT, TEXTAREA, NUMBER, DATE, DATETIME
  - SELECT, CHECKBOX, RADIO, FILE, EMAIL, URL
- ✅ 필드별 옵션 (JSON)
- ✅ 목록/상세/작성 화면 노출 제어
- ✅ 검색 가능 여부 설정
- ✅ 필수 여부, 기본값, placeholder

**3. 게시글 관리 (BoardData)**
- ✅ 게시글 생성/수정/삭제 (논리적 삭제)
- ✅ 임시저장 (DRAFT) 기능
- ✅ 고정글 (PINNED) 기능
- ✅ 조회수 자동 증가
- ✅ 댓글 수 자동 관리
- ✅ 제목 검색
- ✅ 페이징 및 정렬
- ✅ 이전글/다음글 조회
- ✅ 동적 필드 데이터 (JSON)
- ✅ 작성자 권한 검증

**4. 권한 관리 (BoardPermission)**
- ✅ 역할 기반 권한 설정
- ✅ 사용자 예외 권한
- ✅ 5가지 권한 타입 (READ, WRITE, UPDATE, DELETE, COMMENT)
- ✅ 허용/거부 설정

**5. 댓글 (BoardComment)**
- ✅ 댓글 작성/수정/삭제
- ✅ 계층형 댓글 (부모-자식 관계)
- ✅ 대댓글 무제한 깊이
- ✅ 수정 이력 관리 (isModified)
- ✅ 삭제 이력 관리 (deletedAt)
- ✅ 작성자 권한 검증
- ✅ 게시글의 댓글 수 자동 업데이트

**6. 첨부파일 (BoardAttachment)**
- ✅ 파일 업로드 (단일/다중)
- ✅ 파일 다운로드 + 다운로드 횟수 카운트
- ✅ 파일 삭제 (논리적 + 물리적)
- ✅ 날짜별 폴더 구조 (`2025/01/15`)
- ✅ UUID 기반 파일명
- ✅ 파일 크기 제한 (10MB)
- ✅ 확장자 제한 (jpg, png, pdf, doc 등)
- ✅ 한글 파일명 지원
- ✅ 파일 크기 포맷팅 (B, KB, MB, GB)
- ✅ 총 파일 크기 계산

---

### 3.8 Redis 모듈 (분산 락 및 캐시 관리) ⭐ NEW

#### ✅ 구현 파일 (총 16개)

**Service (2개)**
- `DistributedLockService.java` - 분산 락 구현 (SET NX EX + Lua Script)
- `RedisCacheService.java` - 캐시 관리 (String, Hash, Set)

**Controller (2개)**
- `RedisLockController.java` - 분산 락 API
- `RedisCacheController.java` - 캐시 관리 API

**DTO (3개)**
- `LockRequest.java` - 락 요청 DTO
- `LockResponse.java` - 락 응답 DTO
- `CacheRequest.java` - 캐시 요청 DTO

**Config**
- `RedisConfig.java` - Redis 설정 (JSON 직렬화 RedisTemplate)

**Exception (2개)**
- `RedisException.java` - Redis 관련 예외
- `RedisExceptionMessage.java` - 예외 메시지 (10개 상수)

**Constants (2개)**
- `RedisKeyPrefix.java` - Redis 키 접두사
- `RedisLockConstants.java` - 락 관련 상수

**Test (2개, 총 28개 테스트)**
- `DistributedLockServiceTest.java` - 11개 테스트
- `RedisCacheServiceTest.java` - 17개 테스트

**Dependencies**
- `build.gradle` - Redis 의존성 추가 (spring-boot-starter-data-redis, lettuce-core, commons-pool2)
- `application.yml` - Redis 설정 (Standalone/Sentinel/Cluster 예시)

#### 📋 Redis 모듈 상세 기능

**1. 분산 락 (DistributedLockService)**
- ✅ Redis SET NX EX 기반 락 구현
- ✅ UUID 기반 락 소유자 식별 (`{uuid}:{serverId}`)
- ✅ Lua Script 기반 안전한 락 해제
- ✅ 락 소유자 검증 (다른 서버의 락 해제 방지)
- ✅ TTL 기반 자동 만료
- ✅ 락 연장 (Extend Lock)
- ✅ 락 존재 여부 확인
- ✅ 락 소유자 조회
- ✅ 락 TTL 조회
- ✅ Timeout 기반 락 획득 재시도

**Lua Script 예시**
```lua
-- 락 해제 (소유자 검증)
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end

-- 락 연장 (소유자 검증)
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('expire', KEYS[1], ARGV[2])
else
    return 0
end
```

**2. 캐시 관리 (RedisCacheService)**

**String 연산**
- ✅ set(key, value) - 캐시 저장
- ✅ set(key, value, ttl) - TTL 포함 저장
- ✅ get(key) - 캐시 조회
- ✅ delete(key) - 캐시 삭제
- ✅ exists(key) - 존재 여부 확인
- ✅ setTTL(key, ttl) - TTL 설정
- ✅ getTTL(key) - TTL 조회
- ✅ keys(pattern) - 패턴 매칭 키 조회

**Hash 연산**
- ✅ hSet(key, field, value) - 해시 필드 저장
- ✅ hGet(key, field) - 해시 필드 조회
- ✅ hGetAll(key) - 해시 전체 조회
- ✅ hDelete(key, fields...) - 해시 필드 삭제
- ✅ hExists(key, field) - 해시 필드 존재 여부

**Set 연산**
- ✅ sAdd(key, values...) - Set에 값 추가
- ✅ sRemove(key, values...) - Set에서 값 제거
- ✅ sMembers(key) - Set 전체 조회
- ✅ sIsMember(key, value) - Set 멤버 존재 여부

**3. 설정 관리 (RedisConfig)**
- ✅ Spring Boot 표준 설정 활용
- ✅ Standalone/Sentinel/Cluster 설정 전환 (설정 변경만으로 가능)
- ✅ Lettuce Connection Factory 자동 설정
- ✅ Connection Pooling (Apache Commons Pool2)
- ✅ JSON 직렬화 RedisTemplate (GenericJackson2JsonRedisSerializer)
- ✅ String 직렬화 StringRedisTemplate (자동 설정)

**설정 예시 (application.yml)**
```yaml
# Standalone (현재 활성)
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8

# Sentinel (주석 처리)
# spring:
#   data:
#     redis:
#       sentinel:
#         master: mymaster
#         nodes:
#           - localhost:26379

# Cluster (주석 처리)
# spring:
#   data:
#     redis:
#       cluster:
#         nodes:
#           - localhost:7000
```

**4. 주요 사용 사례**

**배치/스케줄러 중복 실행 방지**
```java
String lockValue = lockService.acquireLock("BATCH:JOB:001", 300L);
try {
    // 배치 작업 실행
} finally {
    lockService.releaseLock("BATCH:JOB:001", lockValue);
}
```

**세션 캐시**
```java
cacheService.set("SESSION:user123", userSessionData, 1800L); // 30분
```

**조회수 캐시**
```java
cacheService.hSet("BOARD:VIEW_COUNT", "post001", 1234);
```

**중복 요청 방지**
```java
if (cacheService.sIsMember("PROCESSED_REQUESTS", requestId)) {
    throw new DuplicateRequestException();
}
cacheService.sAdd("PROCESSED_REQUESTS", requestId);
```

**5. 보안 및 안정성**
- ✅ 락 소유자 검증으로 다른 서버의 락 해제 방지
- ✅ TTL 기반 자동 만료로 데드락 방지
- ✅ Lua Script로 원자적 연산 보장
- ✅ Master 노드 전용 쓰기 (Sentinel/Cluster)
- ✅ Connection Pool로 성능 최적화
- ✅ 예외 처리 및 에러 로깅

**6. 테스트 커버리지**
- ✅ 분산 락 획득/해제/연장
- ✅ 락 소유자 검증
- ✅ Timeout 기반 재시도
- ✅ TTL 자동 만료
- ✅ String/Hash/Set 연산
- ✅ 중복 방지
- ✅ 예외 처리

---

### 3.9 Proxy API 모듈 (동적 API 호출) ⭐ NEW

#### ✅ 구현 파일 (총 15개)

**Domain (2개)**
- `ApiEndpoint.java` - API 엔드포인트 메타 정보 (URL, 메서드, 헤더, 바디 템플릿)
- `ApiExecutionHistory.java` - API 실행 이력 (요청/응답, 성공/실패, 실행 시간)

**DTO (4개)**
- `ApiEndpointDTO.java` - API 엔드포인트 DTO
- `ApiExecutionHistoryDTO.java` - API 실행 이력 DTO
- `ProxyExecutionRequest.java` - Proxy 실행 요청 DTO
- `ProxyExecutionResponse.java` - Proxy 실행 응답 DTO

**Repository (2개)**
- `ApiEndpointRepository.java` - API 엔드포인트 Repository
- `ApiExecutionHistoryRepository.java` - 실행 이력 Repository (통계 쿼리 포함)

**Service (3개)**
- `ApiEndpointService.java` - API 엔드포인트 CRUD
- `ApiExecutionService.java` - HTTP 클라이언트 기반 API 실행, 재시도 로직
- `ApiExecutionHistoryService.java` - 실행 이력 조회 및 통계

**Controller (3개)**
- `ProxyApiController.java` - 공통 실행 엔드포인트 (POST /proxy/execute)
- `ApiEndpointController.java` - API 엔드포인트 관리 API
- `ApiExecutionHistoryController.java` - 실행 이력 조회 API

**Mapper (2개)**
- `ApiEndpointMapper.java` - Entity ↔ DTO 변환
- `ApiExecutionHistoryMapper.java` - Entity ↔ DTO 변환

**Config**
- `RestTemplateConfig.java` - RestTemplate 설정 (타임아웃 30초)

**Exception (2개)**
- `ProxyException.java` - Proxy API 관련 예외
- `ProxyExceptionMessage.java` - 예외 메시지 (14개 상수)

**Constants**
- `ExecutionTrigger.java` - 실행 트리거 (MANUAL, SCHEDULER, BATCH)

**Test (1개, 총 12개 테스트)**
- `ApiEndpointServiceTest.java` - 12개 테스트

#### 📋 Proxy API 모듈 상세 기능

**1. 동적 API 호출 (ApiExecutionService)**
- ✅ 데이터 기반 API 호출 (코드에 의존하지 않음)
- ✅ HTTP 메서드 지원 (GET, POST, PUT, DELETE, PATCH)
- ✅ 요청 헤더/바디 템플릿 치환 (`${variable}` 형식)
- ✅ RestTemplate 기반 HTTP 클라이언트
- ✅ 타임아웃 설정 (기본 30초, 개별 설정 가능)
- ✅ 재시도 로직 (재시도 횟수, 재시도 간격 설정)
- ✅ 성공/실패 자동 판별
- ✅ 실행 이력 자동 저장
- ✅ 내부/외부 API 구분 지원

**템플릿 치환 예시:**
```java
// API 엔드포인트 설정
targetUrl: "https://api.example.com/users/${userId}/orders"
requestBody: "{\"action\": \"${action}\", \"amount\": ${amount}}"

// 실행 요청
parameters: {
    "userId": "12345",
    "action": "approve",
    "amount": 50000
}

// 실제 호출
URL: "https://api.example.com/users/12345/orders"
Body: {"action": "approve", "amount": 50000}
```

**2. API 엔드포인트 관리 (ApiEndpoint)**
- ✅ API 메타 정보 등록/수정/삭제
- ✅ API 코드 (고유 식별자)
- ✅ 대상 URL (템플릿 변수 지원)
- ✅ HTTP 메서드
- ✅ 요청 헤더 (JSON)
- ✅ 요청 바디 템플릿 (JSON)
- ✅ 타임아웃 설정
- ✅ 재시도 설정 (횟수, 간격)
- ✅ 내부/외부 API 구분
- ✅ 활성/비활성 상태 관리
- ✅ 논리적 삭제

**3. 실행 이력 관리 (ApiExecutionHistory)**
- ✅ 모든 API 호출 이력 기록
- ✅ 요청 정보 저장 (URL, 메서드, 헤더, 바디)
- ✅ 응답 정보 저장 (상태 코드, 헤더, 바디)
- ✅ 실행 시간 측정 (밀리초)
- ✅ 성공/실패 기록
- ✅ 에러 메시지 저장
- ✅ 재시도 횟수 기록
- ✅ 실행 트리거 기록 (MANUAL, SCHEDULER, BATCH)
- ✅ 실행자 기록
- ✅ 기간별 조회
- ✅ 성공률 통계
- ✅ 평균 실행 시간 통계

**4. 공통 실행 엔드포인트**
```java
POST /proxy/execute

Request:
{
    "apiCode": "USER_API_001",
    "parameters": {
        "userId": "12345",
        "action": "approve"
    },
    "executionTrigger": "MANUAL",
    "executedBy": "admin"
}

Response:
{
    "executionHistoryId": 123,
    "apiCode": "USER_API_001",
    "isSuccess": true,
    "statusCode": 200,
    "responseBody": "{\"result\": \"success\"}",
    "executionTimeMs": 1250,
    "retryAttempt": 0,
    "executedAt": "2026-01-06T10:30:00"
}
```

**5. 재시도 로직**
- ✅ 설정 가능한 재시도 횟수
- ✅ 재시도 간격 (밀리초)
- ✅ HTTP 에러 발생 시 자동 재시도
- ✅ 연결 실패 시 재시도
- ✅ 최종 실패 시 에러 정보 기록

**6. 배치/스케줄러 연동**
- ✅ 실행 트리거 구분 (MANUAL, SCHEDULER, BATCH)
- ✅ 다중 서버 환경 안전성 (실행 이력으로 중복 실행 추적)
- ✅ 호출 실패가 시스템 전체에 영향 주지 않음
- ✅ 비동기 실행 가능

**7. 보안 및 안정성**
- ✅ API 활성/비활성 제어
- ✅ 타임아웃으로 무한 대기 방지
- ✅ 재시도 제한으로 리소스 보호
- ✅ 에러 핸들링 및 로깅
- ✅ 실행 이력으로 추적성 확보

**8. 사용 사례**

**배치 작업에서 외부 API 호출**
```java
ProxyExecutionRequest request = ProxyExecutionRequest.builder()
    .apiCode("PAYMENT_APPROVAL_API")
    .parameters(Map.of(
        "orderId", orderId,
        "amount", amount
    ))
    .executionTrigger("BATCH")
    .executedBy("batch-job")
    .build();

ProxyExecutionResponse response = proxyApiController.executeApi(request);
```

**스케줄러에서 주기적 API 호출**
```java
@Scheduled(cron = "0 0 * * * *")  // 매 시간
public void syncUserData() {
    ProxyExecutionRequest request = ProxyExecutionRequest.builder()
        .apiCode("USER_SYNC_API")
        .executionTrigger("SCHEDULER")
        .executedBy("scheduler")
        .build();

    proxyApiController.executeApi(request);
}
```

**수동 API 테스트**
```bash
curl -X POST http://localhost:8080/proxy/execute \
  -H "Content-Type: application/json" \
  -d '{
    "apiCode": "TEST_API",
    "parameters": {"userId": "12345"},
    "executionTrigger": "MANUAL",
    "executedBy": "admin"
  }'
```

---

### 3.8 Batch 모듈 (배치 관리)

#### ✅ 구현 파일 (총 22개)

**Domain & DTO**
- `BatchJob.java` - 배치 작업 엔티티
- `BatchExecution.java` - 배치 실행 이력 엔티티
- `BatchJobDTO.java` - 배치 작업 DTO
- `BatchExecutionDTO.java` - 배치 실행 이력 DTO
- `BatchExecutionRequest.java` - 배치 실행 요청 DTO

**Repository**
- `BatchJobRepository.java` - 배치 작업 Repository
- `BatchExecutionRepository.java` - 배치 실행 이력 Repository

**Service**
- `BatchJobService.java` - 배치 작업 CRUD 및 스케줄러 통합
- `BatchExecutionService.java` - 배치 실행 로직 (Redis Lock + Proxy API)
- `BatchHistoryService.java` - 실행 이력 조회 및 통계
- `BatchSchedulerService.java` - Quartz 스케줄러 관리

**Controller**
- `BatchJobController.java` - 배치 작업 관리 API
- `BatchExecutionController.java` - 배치 실행 및 이력 조회 API

**Mapper**
- `BatchJobMapper.java` - Entity ↔ DTO 변환
- `BatchExecutionMapper.java` - Entity ↔ DTO 변환

**Quartz**
- `QuartzBatchJob.java` - Quartz Job 실행자
- `QuartzConfig.java` - Quartz 스케줄러 설정
- `BatchStartupInitializer.java` - 애플리케이션 시작 시 배치 등록

**Exception**
- `BatchException.java` - 배치 관련 예외
- `BatchExceptionMessage.java` - 예외 메시지 상수 (15개)

**Constant**
- `BatchStatus.java` - 배치 상태 (WAIT, RUNNING, SUCCESS, FAIL, RETRY, TIMEOUT)
- `BatchTriggerType.java` - 트리거 타입 (SCHEDULER, MANUAL, RETRY)
- `ScheduleType.java` - 스케줄 타입 (CRON, INTERVAL)

**Test**
- `BatchJobServiceTest.java` - 배치 작업 서비스 테스트 (16개 테스트)
- `BatchExecutionServiceTest.java` - 배치 실행 서비스 테스트 (11개 테스트)
- `BatchSchedulerServiceTest.java` - 스케줄러 서비스 테스트 (11개 테스트)

**주요 기능:**

**1. 스케줄 기반 배치 실행**
- ✅ CRON 표현식 지원 (예: `0 0 * * * ?` - 매 시간)
- ✅ INTERVAL 지원 (예: `60000` - 60초마다)
- ✅ Quartz 클러스터 모드 (멀티 서버 환경)
- ✅ Misfire 처리 (DoNothing, NextWithRemainingCount)
- ✅ 스케줄 표현식 검증 (CRON, INTERVAL)
- ✅ 동적 스케줄 변경 (재등록)

**2. Redis 분산 락 기반 중복 실행 방지**
- ✅ Lock Key: `batch:{batchId}:lock`
- ✅ Lock TTL: timeout + 10초 (안전 버퍼)
- ✅ allowConcurrent 설정에 따른 동시 실행 제어
- ✅ Lock 획득 실패 시 예외 처리
- ✅ Lock 자동 해제 (finally 블록)

**3. Proxy API 통합**
- ✅ 배치 로직은 Proxy API로 완전 분리
- ✅ 템플릿 변수 자동 주입 (executionId, batchId)
- ✅ API 실행 결과로 배치 성공/실패 판단
- ✅ Proxy API 실행 이력 연동

**4. 배치 상태 관리**
```
WAIT → RUNNING → SUCCESS/FAIL/TIMEOUT
                ↓
              RETRY → SUCCESS/FAIL
```
- ✅ 6가지 상태 관리
- ✅ 상태 전이 로직
- ✅ 실행 시간 측정
- ✅ 종료 시간 기록

**5. 자동 재시도**
- ✅ 설정 가능한 최대 재시도 횟수 (maxRetryCount)
- ✅ 재시도 간격 설정 (retryIntervalSeconds)
- ✅ 재시도 이력 연결 (originalExecutionId)
- ✅ 재시도 횟수 추적 (retryCount)
- ✅ 재시도 대상 자동 조회
- ✅ 수동 재시도 API 제공

**6. 실행 트리거**
- ✅ **SCHEDULER**: Quartz 스케줄러 자동 실행
- ✅ **MANUAL**: Run Now (수동 즉시 실행)
- ✅ **RETRY**: 재시도 실행

**7. 배치 작업 관리**
- ✅ 배치 생성/수정/삭제
- ✅ 활성/비활성 토글 (재시작 없이)
- ✅ 실행 중인 배치 삭제 방지
- ✅ 배치 수정 시 다음 실행부터 적용
- ✅ 논리적 삭제 (DataStateCode)
- ✅ Proxy API 코드 연결

**8. 실행 이력 관리**
- ✅ 모든 실행 이력 기록 (성공/실패 무관)
- ✅ 배치별 이력 조회 (페이징)
- ✅ 상태별 이력 조회
- ✅ 트리거 타입별 이력 조회
- ✅ 기간별 이력 조회
- ✅ 최근 실행 이력 조회 (최대 10개)
- ✅ 실행 통계 조회
  - 총 실행 횟수
  - 성공 횟수
  - 실패 횟수
  - 평균 실행 시간

**9. 시작 시 자동 초기화**
- ✅ ApplicationRunner로 자동 실행
- ✅ 활성화된 배치 작업 조회
- ✅ Quartz 스케줄러 등록
- ✅ 예외 발생 시에도 애플리케이션 시작 (로깅만)

**10. 보안 및 안정성**
- ✅ 중복 실행 방지 (Redis 분산 락)
- ✅ 타임아웃 처리
- ✅ Lock 자동 해제 (finally)
- ✅ 예외 핸들링 및 로깅
- ✅ 실행 이력으로 추적성 확보

**API 엔드포인트:**

**배치 작업 관리**
- `POST /batch-jobs` - 배치 작업 생성
- `PUT /batch-jobs/{id}` - 배치 작업 수정
- `DELETE /batch-jobs/{id}` - 배치 작업 삭제
- `GET /batch-jobs/{id}` - 배치 작업 조회
- `GET /batch-jobs` - 배치 작업 목록 (페이징)
- `GET /batch-jobs/enabled` - 활성화된 배치 목록 (페이징)
- `POST /batch-jobs/{id}/toggle` - 활성/비활성 토글

**배치 실행 관리**
- `POST /batch-executions/execute` - 수동 실행 (Run Now)
- `POST /batch-executions/retry/{executionId}` - 재시도
- `GET /batch-executions/{id}` - 실행 이력 조회 (ID)
- `GET /batch-executions/execution-id/{executionId}` - 실행 이력 조회 (실행 ID)
- `GET /batch-executions/batch-job/{batchJobId}` - 배치별 이력 (페이징)
- `GET /batch-executions/batch-id/{batchId}` - 배치 ID별 이력 (페이징)
- `GET /batch-executions/status/{status}` - 상태별 이력 (페이징)
- `GET /batch-executions/trigger-type/{triggerType}` - 트리거별 이력 (페이징)
- `GET /batch-executions/period` - 기간별 이력
- `GET /batch-executions/recent/{batchId}` - 최근 이력 (최대 10개)
- `GET /batch-executions/stats/{batchJobId}` - 실행 통계
- `GET /batch-executions/retry-targets` - 재시도 대상 조회

**사용 예시:**

**1. 배치 작업 생성**
```json
POST /batch-jobs

{
  "batchId": "DAILY_REPORT",
  "batchName": "일일 리포트 생성",
  "description": "매일 자정 리포트 생성 및 전송",
  "scheduleType": "CRON",
  "scheduleExpression": "0 0 0 * * ?",
  "proxyApiCode": "REPORT_API",
  "executionParameters": "{\"reportType\":\"daily\"}",
  "enabled": true,
  "allowConcurrent": false,
  "maxRetryCount": 3,
  "retryIntervalSeconds": 300,
  "timeoutSeconds": 600
}
```

**2. 수동 실행 (Run Now)**
```json
POST /batch-executions/execute

{
  "batchId": "DAILY_REPORT",
  "executedBy": "admin",
  "parameters": {
    "reportType": "adhoc",
    "date": "2026-01-06"
  }
}

Response:
{
  "executionId": "exec-uuid-001",
  "batchId": "DAILY_REPORT",
  "status": "RUNNING",
  "triggerType": "MANUAL",
  "startTime": "2026-01-06T10:30:00"
}
```

**3. 배치 재시도**
```json
POST /batch-executions/retry/exec-uuid-001?executedBy=admin

Response:
{
  "executionId": "exec-uuid-002",
  "originalExecutionId": "exec-uuid-001",
  "status": "RUNNING",
  "triggerType": "RETRY",
  "retryCount": 1
}
```

**4. 실행 통계 조회**
```json
GET /batch-executions/stats/1

Response:
{
  "totalCount": 100,
  "successCount": 95,
  "failureCount": 5,
  "avgExecutionTime": 1250.5
}
```

**데이터베이스 스키마:**

**t_batch_job**
```sql
CREATE TABLE t_batch_job (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_id VARCHAR(100) NOT NULL UNIQUE,
  batch_name VARCHAR(200) NOT NULL,
  description TEXT,
  schedule_type VARCHAR(20) NOT NULL,  -- CRON, INTERVAL
  schedule_expression VARCHAR(100) NOT NULL,
  proxy_api_code VARCHAR(100) NOT NULL,
  execution_parameters TEXT,
  enabled TINYINT(1) DEFAULT 1,
  allow_concurrent TINYINT(1) DEFAULT 0,
  max_retry_count INT DEFAULT 0,
  retry_interval_seconds INT DEFAULT 60,
  timeout_seconds INT DEFAULT 300,
  create_time DATETIME,
  modified_time DATETIME,
  data_state CHAR(1),
  INDEX idx_batch_id (batch_id),
  INDEX idx_enabled (enabled)
);
```

**t_batch_execution**
```sql
CREATE TABLE t_batch_execution (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  execution_id VARCHAR(50) NOT NULL UNIQUE,
  batch_job_id BIGINT NOT NULL,
  batch_id VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL,  -- WAIT, RUNNING, SUCCESS, FAIL, RETRY, TIMEOUT
  trigger_type VARCHAR(20) NOT NULL,  -- SCHEDULER, MANUAL, RETRY
  proxy_execution_history_id BIGINT,
  start_time DATETIME,
  end_time DATETIME,
  execution_time_ms BIGINT,
  executed_by VARCHAR(100),
  retry_count INT DEFAULT 0,
  original_execution_id VARCHAR(50),
  error_message TEXT,
  create_time DATETIME,
  modified_time DATETIME,
  INDEX idx_batch_job_id (batch_job_id),
  INDEX idx_batch_id (batch_id),
  INDEX idx_status (status),
  INDEX idx_start_time (start_time),
  INDEX idx_trigger_type (trigger_type),
  INDEX idx_execution_id (execution_id),
  FOREIGN KEY (batch_job_id) REFERENCES t_batch_job(id)
);
```

**Quartz 테이블** (자동 생성)
- `QRTZ_JOB_DETAILS` - Job 정보
- `QRTZ_TRIGGERS` - Trigger 정보
- `QRTZ_CRON_TRIGGERS` - CRON Trigger
- `QRTZ_SIMPLE_TRIGGERS` - Simple Trigger
- `QRTZ_FIRED_TRIGGERS` - 실행 중인 Trigger
- `QRTZ_LOCKS` - 분산 락

---

### 3.9 Code 모듈 (공통코드 관리) ⭐ NEW

#### ✅ 구현 파일 (총 14개)

**Domain & DTO**
- `CodeGroup.java` - 공통코드 그룹 엔티티
- `CodeItem.java` - 공통코드 항목 엔티티
- `CodeGroupDTO.java` - 코드 그룹 DTO
- `CodeItemDTO.java` - 코드 항목 DTO

**Repository**
- `CodeGroupRepository.java` - 코드 그룹 Repository
- `CodeItemRepository.java` - 코드 항목 Repository

**Service**
- `CodeGroupService.java` - 코드 그룹 CRUD 및 Redis 캐시 관리
- `CodeItemService.java` - 코드 항목 CRUD 및 Redis 캐시 관리

**Controller**
- `CodeGroupController.java` - 코드 그룹 관리 API
- `CodeItemController.java` - 코드 항목 관리 API

**Mapper**
- `CodeGroupMapper.java` - Entity ↔ DTO 변환
- `CodeItemMapper.java` - Entity ↔ DTO 변환

**Exception**
- `CodeException.java` - 공통코드 관련 예외
- `CodeExceptionMessage.java` - 예외 메시지 상수 (10개)

**Test**
- `CodeGroupServiceTest.java` - 코드 그룹 서비스 테스트 (16개 테스트)
- `CodeItemServiceTest.java` - 코드 항목 서비스 테스트 (17개 테스트)

**주요 기능:**

1. **코드 그룹 관리**
   - 그룹 코드를 PK로 사용
   - 그룹명, 설명, 정렬 순서 관리
   - 활성화/비활성화 토글
   - 하위 코드 항목 존재 시 삭제 방지

2. **코드 항목 관리**
   - 그룹별 코드 값 및 코드명 관리
   - 추가 속성 3개 제공 (attribute1, attribute2, attribute3)
   - 정렬 순서 관리
   - 활성화/비활성화 토글

3. **Redis 캐싱 전략**
   - 그룹 단위 캐싱 (TTL: 1시간)
   - 조회 시 캐시 우선 조회 (Cache-Aside Pattern)
   - 변경 시 자동 캐시 갱신
   - 전체 캐시 강제 갱신 API 제공

4. **다중 서버 환경 지원**
   - Redis 중앙 캐시로 서버 간 데이터 일관성 보장
   - 캐시 갱신 시 모든 서버에 즉시 반영

5. **검색 및 필터링**
   - 그룹명으로 검색
   - 코드명으로 검색
   - 활성화된 코드만 조회
   - 그룹 코드 + 코드 값으로 직접 조회

6. **다양한 조회 형식**
   - 목록 조회 (페이징/비페이징)
   - Map 형식 조회 (codeValue → CodeItemDTO)
   - 그룹별 코드 조회

**API 엔드포인트:**

**코드 그룹 API:**
```http
POST   /code-groups                          # 그룹 생성
PUT    /code-groups/{groupCode}              # 그룹 수정
DELETE /code-groups/{groupCode}              # 그룹 삭제
GET    /code-groups/{groupCode}              # 그룹 조회
GET    /code-groups                          # 전체 그룹 조회 (페이징)
GET    /code-groups/list                     # 전체 그룹 조회 (목록)
GET    /code-groups/enabled                  # 활성화된 그룹 조회
GET    /code-groups/search?groupName={name}  # 그룹명 검색
PATCH  /code-groups/{groupCode}/toggle       # 활성/비활성 토글
POST   /code-groups/cache/refresh            # 전체 캐시 갱신
```

**코드 항목 API:**
```http
POST   /code-items                                 # 항목 생성
PUT    /code-items/{id}                            # 항목 수정
DELETE /code-items/{id}                            # 항목 삭제
GET    /code-items/{id}                            # 항목 조회
GET    /code-items/group/{groupCode}               # 그룹별 코드 조회
GET    /code-items/group/{groupCode}/enabled       # 그룹별 활성화 코드
GET    /code-items                                 # 전체 코드 조회 (페이징)
GET    /code-items/search?codeName={name}          # 코드명 검색
GET    /code-items/value?groupCode={g}&codeValue={v}  # 그룹+값으로 조회
GET    /code-items/group/{groupCode}/map           # Map 형식 조회
PATCH  /code-items/{id}/toggle                     # 활성/비활성 토글
POST   /code-items/cache/refresh/{groupCode}       # 그룹별 캐시 갱신
POST   /code-items/cache/refresh                   # 전체 캐시 갱신
```

**Request 예시 (그룹 생성):**
```json
{
  "groupCode": "USER_STATUS",
  "groupName": "사용자 상태",
  "description": "사용자 계정 상태 코드",
  "enabled": true,
  "sortOrder": 1
}
```

**Request 예시 (항목 생성):**
```json
{
  "groupCode": "USER_STATUS",
  "codeValue": "ACTIVE",
  "codeName": "활성",
  "description": "정상 활성화 상태",
  "enabled": true,
  "sortOrder": 1,
  "attribute1": "color:green",
  "attribute2": null,
  "attribute3": null
}
```

**Response 예시 (그룹별 코드 조회):**
```json
[
  {
    "id": 1,
    "groupCode": "USER_STATUS",
    "codeValue": "ACTIVE",
    "codeName": "활성",
    "description": "정상 활성화 상태",
    "enabled": true,
    "sortOrder": 1,
    "attribute1": "color:green",
    "attribute2": null,
    "attribute3": null,
    "dataState": "I",
    "createTime": "2026-01-06T14:30:00",
    "modifiedTime": null
  },
  {
    "id": 2,
    "groupCode": "USER_STATUS",
    "codeValue": "INACTIVE",
    "codeName": "비활성",
    "description": "계정 비활성화 상태",
    "enabled": true,
    "sortOrder": 2,
    "attribute1": "color:gray",
    "attribute2": null,
    "attribute3": null,
    "dataState": "I",
    "createTime": "2026-01-06T14:31:00",
    "modifiedTime": null
  }
]
```

**데이터베이스 스키마:**

```sql
-- 공통코드 그룹
CREATE TABLE t_code_group (
  group_code VARCHAR(50) PRIMARY KEY,
  group_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  enabled BOOLEAN NOT NULL DEFAULT true,
  sort_order INT,
  create_time DATETIME NOT NULL,
  modified_time DATETIME,
  data_state VARCHAR(1) NOT NULL,
  INDEX idx_enabled (enabled),
  INDEX idx_sort_order (sort_order)
);

-- 공통코드 항목
CREATE TABLE t_code_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_code VARCHAR(50) NOT NULL,
  code_value VARCHAR(50) NOT NULL,
  code_name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  enabled BOOLEAN NOT NULL DEFAULT true,
  sort_order INT,
  attribute1 VARCHAR(100),
  attribute2 VARCHAR(100),
  attribute3 VARCHAR(100),
  create_time DATETIME NOT NULL,
  modified_time DATETIME,
  data_state VARCHAR(1) NOT NULL,
  INDEX idx_group_code (group_code),
  INDEX idx_enabled (enabled),
  INDEX idx_sort_order (sort_order),
  UNIQUE KEY uk_group_value (group_code, code_value, data_state),
  FOREIGN KEY (group_code) REFERENCES t_code_group(group_code)
);
```

**Redis 캐시 키 구조:**

```
# 그룹별 캐시
CODE:GROUP:{groupCode}          # 개별 그룹 정보
CODE:ALL_GROUPS                 # 전체 그룹 목록

# 항목별 캐시
CODE:ITEMS:{groupCode}          # 그룹별 코드 항목 목록
```

**사용 시나리오:**

1. **프론트엔드 Select 박스 구성**
   ```javascript
   // 활성화된 사용자 상태 코드 조회
   GET /code-items/group/USER_STATUS/enabled

   // 결과를 Select 박스 옵션으로 사용
   codes.forEach(code => {
     option.value = code.codeValue;
     option.text = code.codeName;
   });
   ```

2. **배치 작업 초기 데이터 로드**
   ```java
   // 애플리케이션 시작 시 전체 캐시 갱신
   POST /code-groups/cache/refresh
   POST /code-items/cache/refresh

   // 이후 모든 조회는 Redis에서 빠르게 응답
   ```

3. **코드 값을 이름으로 변환**
   ```java
   // 코드 값으로 직접 조회
   GET /code-items/value?groupCode=USER_STATUS&codeValue=ACTIVE
   // → { codeName: "활성" }
   ```

4. **Map 형식으로 빠른 조회**
   ```javascript
   // Map 형식 조회 (O(1) 접근)
   GET /code-items/group/USER_STATUS/map

   // 결과: { "ACTIVE": {...}, "INACTIVE": {...} }
   const statusName = codeMap["ACTIVE"].codeName;
   ```

**테스트 커버리지:**

- `CodeGroupServiceTest`: 16개 테스트
  - 생성/수정/삭제/조회
  - 중복 검증
  - 캐시 히트/미스
  - 토글 기능
  - 전체 캐시 갱신

- `CodeItemServiceTest`: 17개 테스트
  - 생성/수정/삭제/조회
  - 그룹별 조회
  - 캐시 관리
  - 검증 로직
  - Map 형식 조회

---

### 3.10 Session 모듈 (세션 관리) ⭐ NEW

#### ✅ 구현 파일 (총 21개)

**Domain & DTO**
- `UserSession.java` - 세션 데이터 (Serializable)
- `SessionAudit.java` - 세션 감사 로그 엔티티
- `SessionDTO.java` - 세션 응답 DTO
- `LoginRequest.java` - 로그인 요청 DTO
- `SessionStatsDTO.java` - 세션 통계 DTO
- `SessionAuditDTO.java` - 감사 로그 DTO

**Repository & Mapper**
- `SessionAuditRepository.java` - JPA Repository
- `SessionAuditMapper.java` - MapStruct Mapper

**Service**
- `SessionService.java` - 핵심 세션 관리 (CRUD, 보안)
- `SessionSecurityService.java` - IP/User-Agent 검증
- `SessionManagementService.java` - 관리자 기능 (통계, 강제 로그아웃)

**Controller**
- `SessionController.java` - 사용자 API (5개)
- `SessionAdminController.java` - 관리자 API (5개)

**Configuration**
- `SessionConfig.java` - Spring Session + Redis 설정
- `SessionProperties.java` - 세션 설정 프로퍼티 (Cookie, Security, Refresh)

**Filter & Interceptor**
- `SessionValidationFilter.java` - 세션 유효성 검증 필터
- `SessionRefreshInterceptor.java` - 세션 자동 갱신 인터셉터

**Exception & Constants**
- `SessionException.java` - 세션 예외
- `SessionExceptionMessage.java` - 예외 메시지
- `SessionConstants.java` - 세션 상수

**Tests**
- `SessionServiceTest.java` - 6개 테스트
- `SessionSecurityServiceTest.java` - 5개 테스트

#### 📌 주요 기능

**1. Spring Session + Redis 통합**
- ✅ Spring Session Data Redis 활용
- ✅ 분산 환경에서 세션 공유
- ✅ Redis TTL 기반 자동 만료 (30분)
- ✅ Lettuce 클라이언트 사용
- ✅ Session Fixation Attack 방지

**2. 쿠키 보안 설정**
- ✅ HttpOnly: true (XSS 방지)
- ✅ Secure: true (HTTPS only)
- ✅ SameSite: Strict (CSRF 방지)
- ✅ 쿠키 이름 커스터마이징
- ✅ 쿠키 경로/도메인 설정

**3. 세션 보안 검증**
- ✅ IP 주소 검증 (설정 가능)
- ✅ User-Agent 검증 (설정 가능)
- ✅ 세션 하이재킹 방지
- ✅ 검증 실패 시 자동 세션 무효화

**4. 세션 라이프사이클 관리**
- ✅ 로그인 (세션 생성)
- ✅ 로그아웃 (세션 삭제)
- ✅ 세션 조회 (현재 세션 정보)
- ✅ 세션 갱신 (TTL 연장)
- ✅ 세션 검증 (유효성 확인)
- ✅ 강제 로그아웃 (관리자)

**5. Sliding Window TTL**
- ✅ 요청마다 세션 활동 감지
- ✅ 임계값 기반 자동 갱신 (50% 경과 시)
- ✅ SessionRefreshInterceptor 활용
- ✅ 설정으로 활성/비활성 토글

**6. 세션 감사 로그**
- ✅ 모든 세션 이벤트 기록
- ✅ 이벤트 타입: LOGIN, LOGOUT, EXPIRED, FORCE_LOGOUT
- ✅ IP 주소, User-Agent 기록
- ✅ 로그인/로그아웃 시간 추적
- ✅ 관리자 정보 기록 (강제 로그아웃 시)

**7. 관리자 기능**
- ✅ 전체 세션 목록 조회 (페이징)
- ✅ 사용자별 세션 조회
- ✅ 세션 통계 조회
  - 총 세션 수
  - 활성 세션 수
  - 만료 임박 세션 수 (5분 이내)
- ✅ 특정 세션 강제 종료
- ✅ 사용자 전체 세션 종료

**8. 세션 필터**
- ✅ SessionValidationFilter
- ✅ 로그인/로그아웃 경로 제외
- ✅ 세션 보안 검증 자동 실행
- ✅ 검증 실패 시 401 Unauthorized

**9. 설정 기반 동작**
```yaml
session:
  cookie:
    name: SESSION_ID
    path: /
    http-only: true
    secure: true
    max-age: 1800
  security:
    validate-ip: true
    validate-user-agent: false
  refresh:
    enabled: true
    threshold: 0.5
```

#### 📊 데이터베이스 스키마

**세션 감사 로그 테이블**
```sql
CREATE TABLE t_session_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(255) NOT NULL,
  user_id VARCHAR(100),
  event_type VARCHAR(20) NOT NULL,
  event_time DATETIME NOT NULL,
  ip_address VARCHAR(50),
  user_agent VARCHAR(500),
  admin_id VARCHAR(100),
  INDEX idx_session_id (session_id),
  INDEX idx_user_id (user_id),
  INDEX idx_event_type (event_type),
  INDEX idx_event_time (event_time)
);
```

**Redis 세션 구조**
```
# Spring Session 자동 관리
spring:session:sessions:{sessionId}          # 세션 데이터
spring:session:sessions:expires:{sessionId}  # 만료 정보
spring:session:expirations:{timestamp}       # 만료 인덱스
```

#### 🔌 REST API 엔드포인트 (총 10개)

**사용자 API (5개)**
- `POST /sessions/login` - 로그인 (세션 생성)
- `POST /sessions/logout` - 로그아웃 (세션 삭제)
- `GET /sessions/current` - 현재 세션 조회
- `POST /sessions/refresh` - 세션 TTL 갱신
- `GET /sessions/validate` - 세션 유효성 검증

**관리자 API (5개)**
- `GET /admin/sessions` - 전체 세션 목록 (페이징)
- `GET /admin/sessions/stats` - 세션 통계
- `GET /admin/sessions/user/{userId}` - 사용자별 세션 조회
- `DELETE /admin/sessions/{sessionId}` - 특정 세션 강제 종료
- `DELETE /admin/sessions/user/{userId}` - 사용자 전체 세션 종료

#### 🔄 세션 플로우

**1. 로그인 플로우**
```
Client Request
   ↓
SessionController.login()
   ↓
SignService.signIn() (사용자 인증)
   ↓
SessionService.createSession()
   ↓
1. 기존 세션 무효화 (Session Fixation 방지)
2. 새 세션 생성
3. 사용자 정보 저장
4. IP/User-Agent 저장
5. Redis에 저장 (Spring Session)
6. SessionAudit 로그 기록 (LOGIN)
   ↓
Response with Set-Cookie
```

**2. 요청 검증 플로우**
```
Client Request with Cookie
   ↓
SessionValidationFilter
   ↓
1. 세션 존재 확인
2. SessionSecurityService.validateSessionSecurity()
   - IP 검증 (설정된 경우)
   - User-Agent 검증 (설정된 경우)
   ↓
SessionRefreshInterceptor
   ↓
임계값 확인 (50% 경과 시)
   ↓
SessionService.refreshSession()
   ↓
Controller 처리
```

**3. 로그아웃 플로우**
```
Client Request
   ↓
SessionController.logout()
   ↓
SessionService.deleteSession()
   ↓
1. 세션 무효화
2. Redis에서 삭제 (자동)
3. SessionAudit 로그 기록 (LOGOUT)
4. 쿠키 삭제 (MaxAge=0)
   ↓
Response
```

#### 🛡️ 보안 기능

**1. Session Fixation Attack 방지**
```java
// 로그인 시 기존 세션 무효화 후 새 세션 생성
HttpSession oldSession = request.getSession(false);
if (oldSession != null) {
    oldSession.invalidate();
}
HttpSession newSession = request.getSession(true);
```

**2. IP/User-Agent 검증**
```java
// 세션 생성 시 기록
session.setAttribute(ATTR_IP_ADDRESS, request.getRemoteAddr());
session.setAttribute(ATTR_USER_AGENT, request.getHeader("User-Agent"));

// 요청마다 검증
if (validateIp) {
    String sessionIp = session.getAttribute(ATTR_IP_ADDRESS);
    String requestIp = request.getRemoteAddr();
    if (!sessionIp.equals(requestIp)) {
        throw new SessionException(IP_MISMATCH);
    }
}
```

**3. 쿠키 보안**
```java
@Bean
public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setUseHttpOnlyCookie(true);   // JavaScript 접근 차단
    serializer.setUseSecureCookie(true);      // HTTPS only
    serializer.setSameSite("Strict");         // CSRF 방지
    return serializer;
}
```

#### 📈 테스트 커버리지

**SessionServiceTest (6개)**
- ✅ 세션 생성 성공
- ✅ 현재 세션 조회 - 세션 없음
- ✅ 세션 삭제 성공
- ✅ 세션 유효성 검증 - 유효함
- ✅ 세션 유효성 검증 - 세션 없음

**SessionSecurityServiceTest (5개)**
- ✅ IP 검증 - 일치
- ✅ IP 검증 - 불일치
- ✅ User-Agent 검증 - 일치
- ✅ User-Agent 검증 - 불일치
- ✅ 검증 비활성화 - 예외 없음

#### 🔗 통합 포인트

**1. User 모듈 연동**
- SignService.signIn()을 통한 사용자 인증
- UserDTO를 세션에 저장

**2. Redis 모듈 연동**
- Spring Session Data Redis 사용
- 기존 Redis 설정 재사용 (RedisConnectionFactory)

**3. Filter Chain 통합**
- SessionValidationFilter 등록
- 로그인/로그아웃 경로 제외 설정

**4. Interceptor 통합**
- SessionRefreshInterceptor 등록
- 자동 TTL 갱신

---

## 4. 아키텍처

### 4.1 패키지 구조

```
com.wan.framework
├── base/                     # 공통 기반
│   ├── config/              # 설정
│   ├── constant/            # 공통 상수
│   └── exception/           # 예외 처리
│
├── user/                    # 사용자 모듈
│   ├── domain/             # 엔티티
│   ├── dto/                # DTO
│   ├── repository/         # Repository
│   ├── service/            # Service
│   ├── web/                # Controller
│   ├── mapper/             # Mapper
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
├── program/                 # 프로그램 모듈
├── menu/                    # 메뉴 모듈
├── history/                 # 히스토리 모듈
│
├── apikey/                  # API Key 모듈
│   ├── domain/             # 3개 엔티티
│   ├── dto/                # 3개 DTO
│   ├── repository/         # 3개 Repository
│   ├── service/            # 2개 Service
│   ├── web/                # 2개 Controller
│   ├── mapper/             # 3개 Mapper
│   ├── config/             # Web 설정
│   ├── interceptor/        # Bearer 인증
│   ├── util/               # API Key 생성기
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
├── board/                   # 게시판 모듈
│   ├── domain/             # 6개 엔티티
│   ├── dto/                # 6개 DTO
│   ├── repository/         # 6개 Repository
│   ├── service/            # 5개 Service
│   ├── web/                # 5개 Controller
│   ├── mapper/             # 6개 Mapper
│   ├── config/             # 파일 설정
│   ├── util/               # 파일 유틸리티
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
├── redis/                   # Redis 모듈
│   ├── dto/                # 3개 DTO
│   ├── service/            # 2개 Service
│   ├── web/                # 2개 Controller
│   ├── config/             # Redis 설정
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
├── proxy/                   # Proxy API 모듈
│   ├── domain/             # 2개 엔티티
│   ├── dto/                # 4개 DTO
│   ├── repository/         # 2개 Repository
│   ├── service/            # 3개 Service
│   ├── web/                # 3개 Controller
│   ├── mapper/             # 2개 Mapper
│   ├── config/             # RestTemplate 설정
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
├── batch/                   # Batch 모듈
│   ├── domain/             # 2개 엔티티
│   ├── dto/                # 3개 DTO
│   ├── repository/         # 2개 Repository
│   ├── service/            # 4개 Service
│   ├── web/                # 2개 Controller
│   ├── mapper/             # 2개 Mapper
│   ├── config/             # 2개 Config (Quartz, Initializer)
│   ├── job/                # 1개 QuartzBatchJob
│   ├── exception/          # 예외
│   └── constant/           # 상수
│
└── code/                    # Code 모듈 ⭐ NEW
    ├── domain/             # 2개 엔티티
    ├── dto/                # 2개 DTO
    ├── repository/         # 2개 Repository
    ├── service/            # 2개 Service
    ├── web/                # 2개 Controller
    ├── mapper/             # 2개 Mapper
    ├── exception/          # 예외
    └── constant/           # 상수
```

### 4.2 계층 구조

```
┌─────────────────┐
│   Controller    │ ← REST API
├─────────────────┤
│    Service      │ ← 비즈니스 로직
├─────────────────┤
│   Repository    │ ← 데이터 액세스
├─────────────────┤
│    Entity       │ ← JPA 엔티티
└─────────────────┘
        ↕
    Database
```

### 4.3 공통 패턴

**1. 데이터 상태 관리**
- 모든 엔티티는 `DataStateCode` 포함
- 논리적 삭제 (Soft Delete) 사용
- `@PrePersist`, `@PreUpdate`로 자동 관리

**2. 예외 처리**
- 도메인별 커스텀 예외
- Enum 기반 예외 메시지
- 전역 예외 핸들러로 일관된 응답

**3. DTO 매핑**
- MapStruct 사용
- Entity ↔ DTO 자동 변환
- `updateEntityFromDto` 메서드 제공

**4. 페이징**
- Spring Data의 `Page` 인터페이스 사용
- 기본값: page=0, size=10

---

## 5. 데이터베이스 스키마

### 5.1 테이블 목록 (총 19개 + Quartz 11개)

| 테이블명 | 설명 | 주요 컬럼 |
|----------|------|-----------|
| `t_user` | 사용자 | user_id(PK), password, name, roles, password_salt |
| `t_program` | 프로그램 | id(PK), name, front_path, api_path, api_key |
| `t_menu` | 메뉴 | id(PK), name, type, icon, parent_id(FK), program_id(FK) |
| `t_error_history` | 에러 로그 | id(PK), url, params, error_message, stack_trace |
| `t_api_key` | API Key | id(PK), api_key(SHA-256), api_key_prefix, expired_at, able_state |
| `t_api_key_permission` | API Key 권한 | id(PK), api_key_id(FK), permission |
| `t_api_key_usage_history` | API Key 사용 이력 | id(PK), api_key_id(FK), request_uri, method, ip_address |
| `t_board_meta` | 게시판 메타 | id(PK), title, description, roles, use_comment |
| `t_board_field_meta` | 게시판 필드 | id(PK), board_meta_id(FK), field_name, field_type |
| `t_board_data` | 게시글 | id(PK), board_meta_id(FK), title, content, status |
| `t_board_permission` | 게시판 권한 | id(PK), board_meta_id(FK), role_or_user_id, permission_type |
| `t_board_comment` | 댓글 | id(PK), board_data_id(FK), parent_id(FK), content |
| `t_board_attachment` | 첨부파일 | id(PK), board_data_id(FK), original_file_name, file_path |
| `t_api_endpoint` | API 엔드포인트 | id(PK), api_code, target_url, http_method, timeout_seconds |
| `t_api_execution_history` | API 실행 이력 | id(PK), api_endpoint_id(FK), executed_url, response_status_code |
| `t_batch_job` | 배치 작업 | id(PK), batch_id, schedule_type, schedule_expression, proxy_api_code |
| `t_batch_execution` | 배치 실행 이력 | id(PK), execution_id, batch_job_id(FK), status, trigger_type |
| `QRTZ_*` | Quartz 스케줄러 | 11개 테이블 (자동 생성) |

### 5.2 주요 인덱스

**ApiKey**
- `idx_api_key` (api_key) - 해시값 조회 최적화
- `idx_api_key_prefix` (api_key_prefix) - Prefix 검색
- `idx_created_by` (created_by) - 생성자별 조회

**ApiKeyUsageHistory**
- `idx_api_key_id` (api_key_id) - API Key별 이력 조회
- `idx_used_at` (used_at) - 시간별 조회

**BoardData**
- `idx_board_meta_status` (board_meta_id, status)
- `idx_author_id` (author_id)
- `idx_created_at` (created_at)

**BoardComment**
- `idx_board_data_id` (board_data_id)
- `idx_parent_id` (parent_id)
- `idx_created_at` (created_at)

**BoardAttachment**
- `idx_board_data_id` (board_data_id)
- `idx_uploaded_by` (uploaded_by)

**BatchJob**
- `idx_batch_id` (batch_id) - Batch ID 조회
- `idx_enabled` (enabled) - 활성화된 배치 조회

**BatchExecution**
- `idx_batch_job_id` (batch_job_id) - 배치 작업별 이력 조회
- `idx_batch_id` (batch_id) - Batch ID별 이력 조회
- `idx_status` (status) - 상태별 조회
- `idx_start_time` (start_time) - 시간별 조회
- `idx_trigger_type` (trigger_type) - 트리거 타입별 조회
- `idx_execution_id` (execution_id) - 실행 ID 조회

**ApiExecutionHistory**
- `idx_api_endpoint_id` (api_endpoint_id) - API 엔드포인트별 조회
- `idx_executed_at` (executed_at) - 시간별 조회
- `idx_is_success` (is_success) - 성공/실패별 조회

### 5.3 관계도

```
Program 1:N Menu

ApiKey 1:N ApiKeyPermission
ApiKey 1:N ApiKeyUsageHistory

BoardMeta 1:N BoardFieldMeta
BoardMeta 1:N BoardData
BoardMeta 1:N BoardPermission

BoardData 1:N BoardComment
BoardData 1:N BoardAttachment

BoardComment 1:N BoardComment (self-join)

ApiEndpoint 1:N ApiExecutionHistory
```

---

## 6. API 엔드포인트

### 6.1 인증 (`/`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/sign-up` | 회원가입 |
| POST | `/sign-in` | 로그인 |
| GET | `/sign-out` | 로그아웃 |

### 6.2 사용자 (`/user`, `/users`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/users?page={page}&pageSize={size}` | 사용자 목록 |
| GET | `/user?userId={userId}` | 사용자 조회 |
| PUT | `/user` | 사용자 수정 |
| DELETE | `/user` | 사용자 삭제 |

### 6.3 프로그램 (`/programs`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/programs?page={page}&pageSize={size}` | 프로그램 목록 |
| GET | `/programs/{id}` | 프로그램 조회 |
| POST | `/programs` | 프로그램 생성 |
| PUT | `/programs` | 프로그램 수정 |
| DELETE | `/programs/{id}` | 프로그램 삭제 |

### 6.4 메뉴 (`/menus`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/menus` | 전체 메뉴 조회 |
| GET | `/menus/{id}` | 메뉴 조회 |
| GET | `/menus/tree` | 메뉴 트리 (역할 기반) |
| POST | `/menus` | 메뉴 생성 |
| PUT | `/menus/{id}` | 메뉴 수정 |
| DELETE | `/menus/{id}` | 메뉴 삭제 |

### 6.5 게시판 메타 (`/board-metas`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/board-metas?page={page}&pageSize={size}` | 게시판 목록 |
| GET | `/board-metas/{id}` | 게시판 조회 |
| POST | `/board-metas` | 게시판 생성 |
| PUT | `/board-metas/{id}` | 게시판 수정 |
| DELETE | `/board-metas/{id}` | 게시판 삭제 |
| POST | `/board-metas/{id}/clone?newTitle={title}` | 게시판 복제 |

### 6.6 게시판 필드 (`/board-fields`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/board-fields/board-meta/{boardMetaId}` | 게시판 필드 목록 |
| POST | `/board-fields` | 필드 생성 |
| PUT | `/board-fields/{id}` | 필드 수정 |
| DELETE | `/board-fields/{id}` | 필드 삭제 |

### 6.7 게시글 (`/board-data`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/board-data/board-meta/{boardMetaId}?page={page}&size={size}` | 게시글 목록 |
| GET | `/board-data/board-meta/{boardMetaId}/search?title={title}` | 게시글 검색 |
| GET | `/board-data/{id}` | 게시글 조회 (조회수 증가) |
| POST | `/board-data` | 게시글 생성 |
| PUT | `/board-data/{id}` | 게시글 수정 |
| DELETE | `/board-data/{id}` | 게시글 삭제 |

### 6.8 댓글 (`/board-comments`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/board-comments/board-data/{boardDataId}` | 댓글 목록 (계층형) |
| POST | `/board-comments` | 댓글 작성 |
| PUT | `/board-comments/{id}` | 댓글 수정 |
| DELETE | `/board-comments/{id}` | 댓글 삭제 |

### 6.9 첨부파일 (`/board-attachments`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/board-attachments/upload/{boardDataId}` | 파일 업로드 (단일) |
| POST | `/board-attachments/upload-multiple/{boardDataId}` | 파일 업로드 (다중) |
| GET | `/board-attachments/board-data/{boardDataId}` | 첨부파일 목록 |
| GET | `/board-attachments/{id}` | 첨부파일 조회 |
| GET | `/board-attachments/download/{id}` | 파일 다운로드 |
| DELETE | `/board-attachments/{id}` | 첨부파일 삭제 |
| GET | `/board-attachments/board-data/{boardDataId}/total-size` | 총 파일 크기 |

### 6.10 API Key (`/api-keys`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api-keys` | API Key 생성 |
| GET | `/api-keys?page={page}&size={size}` | API Key 목록 |
| GET | `/api-keys/{id}` | API Key 조회 |
| POST | `/api-keys/{id}/toggle` | API Key 활성/비활성 전환 |
| DELETE | `/api-keys/{id}` | API Key 삭제 |
| POST | `/api-keys/{id}/permissions` | 권한 추가 |
| DELETE | `/api-keys/{id}/permissions/{permission}` | 권한 삭제 |

### 6.11 API Key 사용 이력 (`/api-key-usage`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api-key-usage/{apiKeyId}?page={page}&size={size}` | 사용 이력 목록 |
| GET | `/api-key-usage/{apiKeyId}/stats` | 사용 통계 |
| GET | `/api-key-usage/{apiKeyId}/period?start={start}&end={end}` | 기간별 이력 |

### 6.12 Redis 분산 락 (`/redis/locks`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/redis/locks/acquire` | 분산 락 획득 |
| POST | `/redis/locks/release` | 분산 락 해제 |
| POST | `/redis/locks/extend` | 분산 락 연장 |
| GET | `/redis/locks/exists?key={key}` | 락 존재 여부 확인 |
| GET | `/redis/locks/owner?key={key}` | 락 소유자 조회 |
| GET | `/redis/locks/ttl?key={key}` | 락 TTL 조회 |

### 6.13 Redis 캐시 (`/redis/cache`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/redis/cache` | 캐시 저장 (TTL 옵션) |
| GET | `/redis/cache/{key}` | 캐시 조회 |
| DELETE | `/redis/cache/{key}` | 캐시 삭제 |
| GET | `/redis/cache/{key}/exists` | 캐시 존재 여부 |
| PUT | `/redis/cache/{key}/ttl?seconds={seconds}` | TTL 설정 |
| GET | `/redis/cache/{key}/ttl` | TTL 조회 |
| GET | `/redis/cache/keys?pattern={pattern}` | 패턴 매칭 키 조회 |
| POST | `/redis/cache/hash` | Hash 필드 저장 |
| GET | `/redis/cache/hash/{key}/{field}` | Hash 필드 조회 |
| GET | `/redis/cache/hash/{key}` | Hash 전체 조회 |
| DELETE | `/redis/cache/hash/{key}/{field}` | Hash 필드 삭제 |
| POST | `/redis/cache/set` | Set 추가 |
| DELETE | `/redis/cache/set` | Set 제거 |
| GET | `/redis/cache/set/{key}` | Set 전체 조회 |
| GET | `/redis/cache/set/{key}/member?value={value}` | Set 멤버 존재 여부 |

### 6.14 Proxy API 실행 (`/proxy`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/proxy/execute` | API 실행 (데이터 기반) |

### 6.15 API 엔드포인트 관리 (`/api-endpoints`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api-endpoints` | API 엔드포인트 생성 |
| GET | `/api-endpoints?page={page}&size={size}` | API 엔드포인트 목록 |
| GET | `/api-endpoints/enabled?page={page}&size={size}` | 활성화된 API 목록 |
| GET | `/api-endpoints/{id}` | API 엔드포인트 조회 |
| PUT | `/api-endpoints/{id}` | API 엔드포인트 수정 |
| DELETE | `/api-endpoints/{id}` | API 엔드포인트 삭제 |
| POST | `/api-endpoints/{id}/toggle` | 활성/비활성 토글 |

### 6.16 API 실행 이력 (`/api-execution-history`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api-execution-history/{id}` | 실행 이력 조회 |
| GET | `/api-execution-history/endpoint/{apiEndpointId}` | API 엔드포인트별 이력 |
| GET | `/api-execution-history/api-code/{apiCode}` | API 코드별 이력 |
| GET | `/api-execution-history/period?startDate={start}&endDate={end}` | 기간별 이력 |
| GET | `/api-execution-history/success/{isSuccess}` | 성공/실패별 이력 |
| GET | `/api-execution-history/recent/{apiCode}` | 최근 이력 (10건) |
| GET | `/api-execution-history/stats/{apiEndpointId}` | 실행 통계 |

---

## 7. 테스트 현황

### 7.1 테스트 통계

| 모듈 | 테스트 클래스 | 테스트 케이스 수 |
|------|---------------|------------------|
| User | `PasswordServiceTest` | 7개 |
| API Key | `ApiKeyServiceTest` | TBD |
| API Key | `ApiKeyGeneratorTest` | TBD |
| Board | `BoardMetaServiceTest` | 10개 |
| Board | `BoardDataServiceTest` | 14개 |
| Board | `BoardCommentServiceTest` | 11개 |
| Board | `BoardAttachmentServiceTest` | 14개 |
| Redis | `DistributedLockServiceTest` | 11개 |
| Redis | `RedisCacheServiceTest` | 17개 |
| Proxy API | `ApiEndpointServiceTest` | 12개 |
| **합계** | **10개** | **96개+** |

### 7.2 테스트 커버리지

**User 모듈**
- ✅ 비밀번호 암호화/검증
- ✅ 솔트 생성
- ✅ 비밀번호 불일치 처리
- ✅ null 값 예외 처리

**API Key 모듈**
- ✅ API Key 생성 및 해싱
- ✅ API Key 검증 (만료, 활성 상태)
- ✅ 권한 관리
- ✅ 사용 이력 기록
- ✅ Bearer Token 인증

**Board 모듈**
- ✅ 게시판 CRUD
- ✅ 게시판 복제
- ✅ 게시글 CRUD + 조회수 + 검색
- ✅ 임시저장/고정글
- ✅ 이전글/다음글
- ✅ 계층형 댓글
- ✅ 파일 업로드/다운로드
- ✅ 권한 검증
- ✅ 예외 처리

**Redis 모듈**
- ✅ 분산 락 획득/해제/연장
- ✅ 락 소유자 검증
- ✅ Timeout 기반 락 획득 재시도
- ✅ TTL 자동 만료 테스트
- ✅ 동시성 제어 (중복 락 획득 차단)
- ✅ String 연산 (set, get, delete, exists, TTL)
- ✅ Hash 연산 (hSet, hGet, hGetAll, hDelete, hExists)
- ✅ Set 연산 (sAdd, sRemove, sMembers, sIsMember)
- ✅ 패턴 매칭 키 조회
- ✅ 중복 추가 방지
- ✅ 예외 처리

**Proxy API 모듈**
- ✅ API 엔드포인트 생성/수정/삭제
- ✅ 중복 API 코드 검증
- ✅ 잘못된 HTTP 메서드 검증
- ✅ API 엔드포인트 조회
- ✅ API 코드로 조회
- ✅ API 엔드포인트 목록 조회
- ✅ 활성화된 API 목록 조회
- ✅ 활성/비활성 토글
- ✅ 존재하지 않는 엔드포인트 처리
- ✅ 논리적 삭제
- ✅ 예외 처리

**Batch 모듈**
- ✅ 배치 작업 생성/수정/삭제
- ✅ 중복 batchId 검증
- ✅ CRON/INTERVAL 표현식 검증
- ✅ 배치 작업 조회 (ID, batchId, 목록)
- ✅ 활성화된 배치 목록 조회
- ✅ 활성/비활성 토글
- ✅ 배치 실행 (SCHEDULER, MANUAL, RETRY)
- ✅ Redis 분산 락 기반 중복 실행 방지
- ✅ Proxy API 통합 실행
- ✅ 배치 재시도 (자동/수동)
- ✅ 최대 재시도 횟수 초과 검증
- ✅ 실행 이력 조회 (배치별, 상태별, 트리거별, 기간별)
- ✅ 실행 통계 조회
- ✅ 재시도 대상 조회
- ✅ Quartz 스케줄러 등록/해제
- ✅ CRON/INTERVAL Trigger 생성
- ✅ 스케줄러 상태 확인
- ✅ 시작 시 자동 배치 등록
- ✅ 논리적 삭제
- ✅ 예외 처리

### 7.3 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew test --tests "com.wan.framework.user.*"
./gradlew test --tests "com.wan.framework.board.*"

# 특정 테스트 클래스
./gradlew test --tests "BoardMetaServiceTest"

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
```

---

## 8. 다음 개발 예정

### 8.1 예정된 모듈

| 우선순위 | 모듈명 | 설명 | 상태 |
|----------|--------|------|------|
| 1 | API Key 관리 | API 키 생성/검증 | ✅ 완료 |
| 2 | Redis 관리 | 분산 락 및 캐시 관리 | ✅ 완료 |
| 3 | Proxy API | 동적 API 호출 및 실행 관리 | ✅ 완료 |
| 4 | 배치 관리 | Quartz + Redis Lock + Proxy API | ✅ 완료 |
| 5 | 공통코드 관리 | 코드 관리 (Redis 활용) | ✅ 완료 |
| 6 | 세션 관리 | Spring Session + Redis | ✅ 완료 |

### 8.2 개선 예정

**보안**
- [ ] Spring Security 인증/인가 강화
- [ ] JWT 토큰 기반 인증 추가
- [ ] HTTPS 적용

**게시판**
- [ ] ElasticSearch 연동 (고급 검색)
- [ ] 동적 필드 검색 고도화
- [ ] 게시글 좋아요/싫어요
- [ ] 게시글 신고 기능
- [ ] 태그 기능

**파일**
- [ ] 이미지 리사이징
- [ ] 썸네일 생성
- [ ] S3 연동 (클라우드 스토리지)
- [ ] 바이러스 검사

**API**
- [ ] Swagger/OpenAPI 문서화
- [ ] API Rate Limiting
- [ ] API 버전 관리

**테스트**
- [ ] Controller 통합 테스트
- [ ] E2E 테스트
- [ ] 성능 테스트

**모니터링**
- [ ] Actuator 설정
- [ ] 로그 수집 (ELK Stack)
- [ ] APM 연동

---

## 9. 파일 통계

### 9.1 전체 파일 수

| 구분 | 파일 수 |
|------|---------|
| Entity | 22개 (User 1, Program 1, Menu 1, ErrorHistory 1, ApiKey 3, Board 6, Proxy 2, Batch 2, Code 2, Session 2) |
| DTO | 34개 (User 2, Program 1, Menu 2, ErrorHistory 1, ApiKey 3, Board 6, Redis 3, Proxy 4, Batch 3, Code 2, Session 6) |
| Repository | 22개 (User 1, Program 1, Menu 1, ErrorHistory 1, ApiKey 3, Board 6, Proxy 2, Batch 2, Code 2, Session 1) |
| Service | 30개 (User 3, Program 1, Menu 1, ErrorHistory 1, ApiKey 2, Board 5, Redis 2, Proxy 3, Batch 4, Code 2, Session 3) |
| Controller | 22개 (User 1, Program 1, Menu 1, ApiKey 2, Board 5, Redis 2, Proxy 3, Batch 2, Code 2, Session 2) |
| Mapper | 21개 (User 1, Program 1, Menu 1, ErrorHistory 1, ApiKey 3, Board 6, Proxy 2, Batch 2, Code 2, Session 1) |
| Exception | 23개 (Base 1, User 2, Program 2, Menu 2, ApiKey 2, Board 2, Redis 2, Proxy 2, Batch 2, Code 2, Session 2) |
| Constant | 18개 (Base 2, Board 4, ApiKey 1, Redis 2, Proxy 1, Batch 4, Code 1, Session 1) |
| Config | 12개 (Base 2, ApiKey 1, Board 1, Redis 1, Proxy 1, Batch 2, Session 2) |
| Job | 1개 (Batch 1 - QuartzBatchJob) |
| Util | 3개 (ApiKey 1, Board 1, Batch 1 - Initializer) |
| Interceptor | 3개 (Base 1, ApiKey 1, Session 1) |
| Filter | 1개 (Session 1 - SessionValidationFilter) |
| Test | 17개 (User 1, ApiKey 2, Board 4, Redis 2, Proxy 1, Batch 3, Code 2, Session 2) |
| **총계** | **229개** |

### 9.2 코드 라인 수 (추정)

- Java 소스 코드: ~19,000 lines
- 테스트 코드: ~6,000 lines
- 설정 파일: ~450 lines
- **총계: ~25,450 lines**

---

## 10. 참고 문서

- [README.md](../readme.md) - 프로젝트 실행 가이드
- [skills.md](../.claude/skills.md) - 개발 컨벤션 및 가이드
- [build.gradle](../build.gradle) - Gradle 빌드 설정

---

## 11. 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|-----------|
| 2025-12-31 | 0.0.1 | 초기 문서 작성 |
| 2025-12-31 | 0.0.1 | Board 모듈 완성 (6개 도메인, 파일 업로드 포함) |
| 2026-01-02 | 0.0.1 | API Key 관리 모듈 완성 (3개 도메인, Bearer 인증 포함) |
| 2026-01-02 | 0.0.1 | 설정 파일 보완 (password 암호화 설정 추가) |
| 2026-01-02 | 0.0.1 | Repository 쿼리 오류 수정 (aggregate 함수, 복합 필드명) |
| 2026-01-06 | 0.0.1 | Redis 관리 모듈 완성 (분산 락, 캐시 관리, Spring Boot 표준 설정) |
| 2026-01-06 | 0.0.1 | Proxy API 모듈 완성 (동적 API 호출, 실행 이력, 재시도 로직, RestTemplate) |
| 2026-01-06 | 0.0.1 | 배치 관리 모듈 완성 (Quartz 스케줄러, Redis 분산 락, Proxy API 통합, 자동 재시도, CRON/INTERVAL) |
| 2026-01-06 | 0.0.1 | 공통코드 관리 모듈 완성 (코드 그룹/항목 관리, Redis 캐싱, Cache-Aside 패턴) |
| 2026-01-07 | 0.0.1 | 세션 관리 모듈 완성 (Spring Session + Redis, 보안 검증, 감사 로그, Sliding Window TTL) |

---

**문서 작성자**: Claude Code
**마지막 업데이트**: 2026-01-07
