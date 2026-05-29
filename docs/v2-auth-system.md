# V2 버전 권한 관리 시스템 문서

## 1. 개요

V2 버전은 기존 프로젝트의 권한 관리 시스템을 다음과 같은 방향으로 고도화한 버전입니다:

- **다중 인증 방식 지원** (OAuth2, SAML 등)
- **세부 권한 관리** (URI 패턴, HTTP 메서드별 권한)
- **권한 상속 체계** (역할 계층 구조)
- **실시간 권한 업데이트**
- **권한 모니터링 및 감사 로그**

## 2. 주요 기능

### 2.1 역할 관리
- **기본 역할**: ROLE_USER, ROLE_ADMIN, ROLE_MANAGER 등
- **사용자 정의 역할**: 사용자가 직접 생성하고 관리하는 역할
- **역할 계층 구조**: 상위/하위 역할 관계 설정 및 자동 상속

### 2.2 세부 권한 관리
- **HTTP 메서드별 권한**: GET, POST, PUT, DELETE 별로 권한 설정
- **URI 패턴 기반 권한**: 특정 URI 패턴에 대한 접근 제어
- **시간 기반 권한**: 특정 시간대에만 접근 가능한 권한 설정

### 2.3 보안 기능
- **보안 로그**: 모든 권한 관련 작업에 대한 감사 로그
- **실시간 모니터링**: 권한 변경 실시간 알림
- **권한 통계**: 권한 사용 통계 분석

## 3. API 명세

### 3.1 역할 관리 API

#### 3.1.1 역할 목록 조회
```
GET /v2/roles
```

#### 3.1.2 특정 역할 조회
```
GET /v2/roles/{roleId}
```

#### 3.1.3 역할 생성
```
POST /v2/roles
Content-Type: application/json

{
    "roleCode": "ROLE_CUSTOM",
    "roleName": "커스텀 역할",
    "description": "사용자 정의 역할"
}
```

#### 3.1.4 역할 수정
```
PUT /v2/roles/{roleId}
Content-Type: application/json

{
    "roleName": "수정된 역할",
    "description": "수정된 설명"
}
```

#### 3.1.5 역할 삭제
```
DELETE /v2/roles/{roleId}
```

### 3.2 권한 관리 API

#### 3.2.1 세부 권한 목록 조회
```
GET /v2/permissions
```

#### 3.2.2 특정 역할의 권한 조회
```
GET /v2/permissions/role/{roleId}
```

#### 3.2.3 권한 생성
```
POST /v2/permissions
Content-Type: application/json

{
    "httpMethod": "GET",
    "uriPattern": "/api/users/*",
    "allowed": true
}
```

#### 3.2.4 권한 수정
```
PUT /v2/permissions/{permissionId}
Content-Type: application/json

{
    "httpMethod": "GET",
    "uriPattern": "/api/users/*",
    "allowed": false
}
```

#### 3.2.5 권한 삭제
```
DELETE /v2/permissions/{permissionId}
```

### 3.3 권한 계층 관리 API

#### 3.3.1 권한 계층 구조 조회
```
GET /v2/roles/{roleId}/hierarchy
```

#### 3.3.2 상위 역할 설정
```
POST /v2/roles/{roleId}/hierarchy/parent/{parentId}
```

### 3.4 보안 로그 API

#### 3.4.1 보안 로그 조회
```
GET /v2/security/logs
```

#### 3.4.2 사용자별 보안 로그 조회
```
GET /v2/security/logs/user/{userId}
```

## 4. 데이터베이스 스키마

### 4.1 테이블 구조

#### 4.1.1 t_role (역할 테이블)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| role_id | BIGINT | 역할 ID (PK) |
| role_code | VARCHAR(50) | 역할 코드 (UNIQUE) |
| role_name | VARCHAR(100) | 역할 이름 |
| description | TEXT | 역할 설명 |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |
| is_active | BOOLEAN | 활성 상태 |

#### 4.1.2 t_role_hierarchy (역할 계층 테이블)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | ID (PK) |
| parent_role_id | BIGINT | 상위 역할 ID (FK) |
| child_role_id | BIGINT | 하위 역할 ID (FK) |
| inherited | BOOLEAN | 상속 여부 |
| created_at | TIMESTAMP | 생성 시간 |

#### 4.1.3 t_detailed_permission (세부 권한 테이블)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| permission_id | BIGINT | 권한 ID (PK) |
| role_id | BIGINT | 역할 ID (FK) |
| http_method | VARCHAR(10) | HTTP 메서드 |
| uri_pattern | VARCHAR(500) | URI 패턴 |
| allowed | BOOLEAN | 접근 허용 여부 |
| valid_from | TIMESTAMP | 유효 시작 시간 |
| valid_to | TIMESTAMP | 유효 종료 시간 |
| created_at | TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | 수정 시간 |

#### 4.1.4 t_security_audit_log (보안 로그 테이블)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| log_id | BIGINT | 로그 ID (PK) |
| user_id | VARCHAR(50) | 사용자 ID |
| action | VARCHAR(50) | 작업 유형 |
| resource_type | VARCHAR(50) | 리소스 타입 |
| resource_id | VARCHAR(100) | 리소스 ID |
| ip_address | VARCHAR(45) | IP 주소 |
| user_agent | TEXT | User-Agent |
| timestamp | TIMESTAMP | 발생 시간 |
| details | JSON | 추가 정보 |

## 5. 보안 기능

### 5.1 권한 검증
- HTTP 요청 시 각 API에 대해 권한 검증 수행
- 세부 권한 규칙에 따라 접근 허용/거부 결정

### 5.2 보안 로그
- 모든 권한 관련 작업 기록
- 로그에는 사용자 ID, IP 주소, 요청 시간, 작업 내용 포함

### 5.3 실시간 모니터링
- 권한 변경 시 실시간 알림 전송
- 보안 감사 로그를 통한 실시간 모니터링 가능

## 6. 설치 및 실행

### 6.1 환경 요구사항
- Java 21 이상
- Spring Boot 3.x
- MariaDB 11.2 이상

### 6.2 실행 방법
```bash
# 1. 빌드
./gradlew build

# 2. Docker 이미지 빌드
docker build -t my-framework-core-back-end:v2 .

# 3. 실행
docker run -p 8080:8080 my-framework-core-back-end:v2
```

## 7. 향후 개선 방향

### 7.1 OAuth2 연동
- Google, GitHub 등 외부 인증 제공자 연동
- JWT 토큰 기반 인증 확장

### 7.2 SAML 연동
- 엔터프라이즈 환경에서의 SAML 인증 지원
- Single Sign-On(SAML) 구현

### 7.3 2FA 기능
- Google Authenticator 등 2단계 인증 기능
- 보안 강화

### 7.4 권한 모니터링 대시보드
- 실시간 권한 사용 통계 시각화
- 보안 위험 요소 자동 탐지

---

이 문서는 V2 버전 권한 관리 시스템의 전체적인 설계와 기능을 설명합니다. 실제 구현에서는 이 문서를 바탕으로 코드를 작성하고 테스트를 진행해야 합니다.