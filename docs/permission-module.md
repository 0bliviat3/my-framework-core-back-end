# 권한 관리 모듈 (Permission Module)

## 📋 개요

API 기반 동적 권한 관리 시스템
- **자동 API 스캔**: 애플리케이션 시작 시 모든 API 엔드포인트 자동 감지 및 DB 등록
- **Role 기반 권한**: Role과 API N:M 관계로 세밀한 권한 관리
- **Redis 캐싱**: O(1) 성능으로 권한 검증
- **ADMIN 특권**: ADMIN Role은 모든 API 접근 허용

---

## 🏗️ 패키지 구조

```
com.wan.framework.permission/
├── constant/           # 상수 및 Enum
│   ├── ApiStatus.java
│   ├── PermissionConstants.java
│   └── PermissionExceptionMessage.java
├── domain/             # 엔티티
│   ├── ApiRegistry.java
│   ├── Role.java
│   └── RoleApiPermission.java
├── dto/                # DTO
│   ├── ApiRegistryDTO.java
│   ├── RoleDTO.java
│   ├── RoleApiPermissionDTO.java
│   └── PermissionCheckRequest.java
├── exception/          # 예외
│   └── PermissionException.java
├── interceptor/        # 인터셉터
│   └── PermissionCheckInterceptor.java
├── repository/         # Repository
│   ├── ApiRegistryRepository.java
│   ├── RoleRepository.java
│   └── RoleApiPermissionRepository.java
├── service/            # 서비스
│   ├── ApiRegistryScanService.java
│   ├── PermissionCacheService.java
│   └── PermissionService.java
└── web/                # Controller
    └── PermissionController.java
```

---

## 📊 데이터베이스 설계

### 1. t_api_registry
```sql
CREATE TABLE t_api_registry (
    api_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(50) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    uri_pattern VARCHAR(500) NOT NULL,
    controller_name VARCHAR(200),
    handler_method VARCHAR(200),
    description VARCHAR(1000),
    auth_required BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    UNIQUE KEY uk_api (service_id, http_method, uri_pattern)
);
```

### 2. t_role
```sql
CREATE TABLE t_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

### 3. t_role_api_permission
```sql
CREATE TABLE t_role_api_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    allowed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_role_api (role_id, api_id),
    FOREIGN KEY (role_id) REFERENCES t_role(role_id),
    FOREIGN KEY (api_id) REFERENCES t_api_registry(api_id)
);
```

---

## 🔄 API 자동 스캔 프로세스

### ApiRegistryScanService.java

**실행 시점**: 애플리케이션 시작 완료 후 (`ApplicationReadyEvent`)

**처리 흐름**:
1. `RequestMappingHandlerMapping`을 통해 모든 API 엔드포인트 스캔
2. 각 API의 HTTP Method, URI Pattern, Controller, Handler 정보 추출
3. DB와 비교하여 변경사항 감지:
   - **신규 API** → INSERT (status=ACTIVE)
   - **변경된 API** → UPDATE (컨트롤러명, 핸들러명, 설명 갱신)
   - **삭제된 API** → status=INACTIVE로 변경

**API 식별자**:
```
{service_id}::{http_method}::{uri_pattern}
예: framework::GET::/users/{userId}
```

**코드 위치**: `ApiRegistryScanService.java:27-174`

---

## 🚀 권한 검증 흐름

### PermissionCheckInterceptor.java

**실행 순서**:
1. **API Registry 조회**: 요청 Method + URI로 API 검색
   - API 없음 or INACTIVE → 404 (API_NOT_FOUND)

2. **인증 체크 제외**: `auth_required=false`인 API는 바로 통과

3. **세션 확인**: 세션에서 사용자 Role 목록 조회
   - 세션 없음 → 403 (PERMISSION_DENIED)

4. **ADMIN 특권**: Role 목록에 `ROLE_ADMIN` 포함 시 → 무조건 통과

5. **권한 검증**: Redis 캐시에서 Role별 API 권한 조회 (O(1))
   - 권한 있음 → 통과
   - 권한 없음 → 403 (PERMISSION_DENIED)

**코드 위치**: `PermissionCheckInterceptor.java:37-108`

---

## 💾 Redis 캐싱 전략

### PermissionCacheService.java

**캐시 Key 구조**:
```
ROLE_API_PERMISSION::{roleCode}
예: ROLE_API_PERMISSION::ROLE_USER
```

**캐시 Value**: Set<apiIdentifier>
```
{
  "framework::GET::/users/{userId}",
  "framework::POST::/users",
  "framework::PUT::/users/{userId}"
}
```

**주요 메서드**:
- `cacheRolePermissions()`: Role별 접근 가능 API 목록 캐싱
- `hasPermission()`: O(1) 성능으로 권한 확인
- `invalidateRoleCache()`: 권한 변경 시 캐시 무효화
- `warmUpCache()`: 캐시 워밍업

**TTL**: 24시간

**코드 위치**: `PermissionCacheService.java:24-117`

---

## 🎯 API 엔드포인트

### Role 관리
```bash
# Role 생성
POST /permissions/roles
Body: {"roleCode": "ROLE_MANAGER", "roleName": "매니저", "description": "중간 관리자"}

# Role 목록 조회
GET /permissions/roles

# Role 조회
GET /permissions/roles/{roleId}

# Role 수정
PUT /permissions/roles/{roleId}
Body: {"roleName": "수정된 이름", "description": "수정된 설명"}

# Role 삭제 (ADMIN은 삭제 불가)
DELETE /permissions/roles/{roleId}
```

### API 관리
```bash
# 활성 API 목록 조회
GET /permissions/apis

# Role별 권한 목록 조회
GET /permissions/roles/{roleId}/permissions
```

### 권한 매핑
```bash
# Role에 API 권한 부여
POST /permissions/roles/{roleId}/apis/{apiId}

# Role의 API 권한 제거
DELETE /permissions/roles/{roleId}/apis/{apiId}
```

---

## 🔐 정책 및 규칙

### 1. ADMIN Role 특권
- `ROLE_ADMIN`은 모든 API에 대해 접근 허용
- DB에 권한 매핑이 없어도 무조건 통과
- 권한 검증 인터셉터에서 우선 처리

### 2. 권한 매핑 규칙
- 권한 매핑 없으면 접근 거부 (Fail-Closed)
- API 상태가 INACTIVE면 접근 거부
- `auth_required=false`인 API는 권한 검사 제외

### 3. 캐시 무효화 시점
- Role 수정 시
- Role 삭제 시
- 권한 부여/제거 시
- API 스캔 완료 시 (선택적)

### 4. 제외 경로 (권한 검증 안 함)
- `/users/admin/exists` - 관리자 존재 확인
- `/users/admin/initial` - 초기 관리자 생성
- `/users/sign-up` - 회원가입
- `/sessions/login` - 로그인
- `/error`, `/swagger-ui/**`, `/api-docs` 등

---

## 📈 성능 최적화

### O(1) 권한 검증
- Redis Set의 `SISMEMBER` 사용
- 시간 복잡도: O(1)
- API 수가 많아져도 성능 저하 없음

### 캐시 워밍업
```java
// 애플리케이션 시작 시 모든 Role 캐시 로드
@EventListener(ApplicationReadyEvent.class)
public void warmUpAllRoles() {
    List<Role> roles = roleRepository.findAll();
    for (Role role : roles) {
        permissionCacheService.warmUpCache(role.getRoleId(), role.getRoleCode());
    }
}
```

---

## 🧪 테스트 시나리오

### 시나리오 1: 권한 성공
```
1. ROLE_USER 생성
2. GET /users API에 권한 부여
3. ROLE_USER로 로그인
4. GET /users 요청
→ 200 OK
```

### 시나리오 2: 권한 실패
```
1. ROLE_USER 생성 (권한 부여 안 함)
2. ROLE_USER로 로그인
3. POST /users 요청
→ 403 PERMISSION_DENIED
```

### 시나리오 3: ADMIN 특권
```
1. ROLE_ADMIN으로 로그인
2. 권한 매핑 없는 API 요청
→ 200 OK (모든 API 접근 가능)
```

### 시나리오 4: API 자동 스캔
```
1. 새로운 Controller 추가
2. 애플리케이션 재시작
3. DB 확인
→ 신규 API 자동 등록됨
```

---

## 🔄 확장 가능성

### ABAC (Attribute-Based Access Control) 확장
```java
// 속성 기반 권한 조건 추가
@Column(name = "condition_expression")
private String conditionExpression;  // 예: "user.department == 'IT'"
```

### 멀티 테넌트 확장
```java
// 테넌트별 권한 분리
@Column(name = "tenant_id")
private String tenantId;
```

### 동적 권한 조건
```java
// 시간, IP, 리소스 소유권 등 동적 조건 추가
public interface PermissionCondition {
    boolean evaluate(HttpServletRequest request, Object resource);
}
```

---

## 📝 주요 클래스 다이어그램

```
┌─────────────────────┐
│  ApiRegistry        │
│  (API 정보)         │
└──────────┬──────────┘
           │ N
           │
           │ M
┌──────────┴──────────┐
│  RoleApiPermission  │
│  (권한 매핑)        │
└──────────┬──────────┘
           │ M
           │
           │ N
┌──────────┴──────────┐
│  Role               │
│  (역할)             │
└─────────────────────┘
```

---

## ✅ 완료된 구현 사항

- [x] API Registry 자동 스캔
- [x] Role 관리 (생성/수정/삭제)
- [x] Role-API 권한 매핑
- [x] Redis 캐싱 (O(1) 성능)
- [x] 권한 검증 인터셉터
- [x] ADMIN 특권 처리
- [x] 빌드 성공