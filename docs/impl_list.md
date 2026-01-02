# Framework Core Back-end 구현 현황

> 최종 업데이트: 2026-01-02

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
- Spring Security
- Spring Batch
- Quartz Scheduler

### Database
- MariaDB (JDBC Driver 3.3.3)
- Hibernate ORM

### Libraries
- Lombok (코드 간소화)
- MapStruct 1.5.5 (DTO 매핑)
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
└── board/                   # 게시판 모듈
    ├── domain/             # 6개 엔티티
    ├── dto/                # 6개 DTO
    ├── repository/         # 6개 Repository
    ├── service/            # 5개 Service
    ├── web/                # 5개 Controller
    ├── mapper/             # 6개 Mapper
    ├── config/             # 파일 설정
    ├── util/               # 파일 유틸리티
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

### 5.1 테이블 목록 (총 15개)

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
| **합계** | **7개** | **56개+** |

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
| 2 | 프로그램 실행 (Proxy API) | 동적 API 라우팅 | 📋 예정 |
| 3 | Redis 관리 | 캐싱 및 세션 관리 | 📋 예정 |
| 4 | 배치 관리 | Spring Batch + Quartz | 📋 예정 |
| 5 | 공통코드 관리 | 코드 관리 (Redis 활용) | 📋 예정 |
| 6 | 세션 관리 | Redis 기반 세션 | 📋 예정 |

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
| Entity | 15개 |
| DTO | 16개 |
| Repository | 15개 |
| Service | 16개 |
| Controller | 11개 |
| Mapper | 14개 |
| Exception | 13개 |
| Constant | 9개 |
| Config | 4개 |
| Util | 2개 |
| Interceptor | 1개 |
| Test | 7개 |
| **총계** | **123개** |

### 9.2 코드 라인 수 (추정)

- Java 소스 코드: ~10,000 lines
- 테스트 코드: ~2,500 lines
- 설정 파일: ~250 lines
- **총계: ~12,750 lines**

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

---

**문서 작성자**: Claude Code
**마지막 업데이트**: 2026-01-02
