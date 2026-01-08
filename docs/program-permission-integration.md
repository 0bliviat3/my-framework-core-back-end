# Program-Permission 통합 모듈

## 📋 개요

Program 모듈과 Permission 모듈을 통합하여 **Menu → Program → API** 3단계 연계 권한 관리 시스템 구축
- 프론트엔드 메뉴(Menu)와 백엔드 API(Permission)를 프로그램(Program)으로 연결
- 메뉴별 필요한 API 권한 자동 계산
- Role 기반으로 접근 가능한 메뉴 필터링

---

## 🏗️ 아키텍처 다이어그램

```
┌─────────┐         ┌─────────┐         ┌────────────┐         ┌──────┐
│  Menu   │ N     1 │ Program │ N     M │ ApiRegistry│ N     M │ Role │
│  (메뉴)  ├────────►│ (화면)   ├────────►│   (API)    │◄────────┤(역할)│
└─────────┘         └─────────┘         └────────────┘         └──────┘
  네비게이션            화면/페이지           API 엔드포인트          권한

연계 흐름:
1. 사용자가 메뉴 클릭
2. 메뉴 → Program 매핑 조회
3. Program → API 매핑 조회 (필요한 API 목록)
4. Role → API 권한 조회 (접근 가능한 API)
5. 필요 API ⊆ 접근 가능 API → 메뉴 표시
```

---

## 📊 데이터베이스 설계

### 1. t_program_api_mapping (NEW)
```sql
CREATE TABLE t_program_api_mapping (
    mapping_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id BIGINT NOT NULL,
    api_id BIGINT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,  -- 필수 API 여부
    description VARCHAR(500),                -- 사용 이유
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_program_api (program_id, api_id),
    FOREIGN KEY (program_id) REFERENCES t_program(program_id),
    FOREIGN KEY (api_id) REFERENCES t_api_registry(api_id)
);
```

### 2. 기존 테이블 관계
```
t_menu.program_id → t_program.program_id (기존)
t_program_api_mapping.program_id → t_program.program_id (NEW)
t_program_api_mapping.api_id → t_api_registry.api_id (NEW)
t_role_api_permission.api_id → t_api_registry.api_id (기존)
```

---

## 🔄 통합 프로세스

### 1. Program에 API 매핑 추가

**관리자 작업**:
```bash
# 1. 활성 API 목록 조회
GET /programs/available-apis

# 2. Program에 API 매핑
POST /programs/{programId}/apis/{apiId}?required=true&description=사용자 목록 조회용
→ t_program_api_mapping INSERT
```

### 2. Menu → API 권한 자동 계산

**자동 계산 로직** (`MenuPermissionService.java`):
```java
Set<String> requiredApis = getRequiredApisForMenu(menuId);

// 과정:
// 1. Menu → Program 조회
// 2. Program → ProgramApiMapping 조회
// 3. 필수 API 목록 추출
// 4. 부모 메뉴도 재귀적으로 처리
```

### 3. Role 기반 메뉴 필터링

**사용자 로그인 시**:
```java
// 1. 세션에서 User Role 조회
List<String> roles = ["ROLE_USER"];

// 2. Role별 접근 가능 API 조회 (Redis)
Set<String> accessibleApis = permissionCacheService.getRoleApis("ROLE_USER");

// 3. 메뉴별 필요 API 조회
Set<String> requiredApis = getRequiredApisForMenu(menuId);

// 4. 접근 권한 확인
boolean canAccess = accessibleApis.containsAll(requiredApis);
```

---

## 🎯 구현된 핵심 기능

### 1. ProgramApiMapping 엔티티
```java
@Entity
@Table(name = "t_program_api_mapping")
public class ProgramApiMapping {
    private Long mappingId;
    private Program program;         // N:1
    private ApiRegistry apiRegistry; // N:1
    private Boolean required;        // 필수 API 여부
    private String description;
}
```

### 2. ProgramPermissionIntegrationService

**주요 메서드**:
- `addApiToProgram()`: Program에 API 매핑 추가
- `removeApiFromProgram()`: API 매핑 제거
- `getProgramApis()`: Program별 API 목록 조회
- `getRequiredApis()`: 필수 API만 조회
- `calculateRequiredApisForMenu()`: Menu→Program→API 연계 계산

**코드 위치**: `ProgramPermissionIntegrationService.java:39-162`

### 3. MenuPermissionService

**주요 메서드**:
- `getRequiredApisForMenu()`: 메뉴에 필요한 API 계산 (재귀)
- `canAccessMenu()`: Role이 메뉴에 접근 가능한지 확인
- `filterAccessibleMenus()`: 접근 가능한 메뉴만 필터링
- `filterAccessibleMenuIds()`: 메뉴 ID 목록 필터링

**코드 위치**: `MenuPermissionService.java:27-135`

---

## 🚀 API 엔드포인트

### Program-API 매핑 관리
```bash
# Program에 API 추가
POST /programs/{programId}/apis/{apiId}?required=true&description=설명

# Program에서 API 제거
DELETE /programs/{programId}/apis/{apiId}

# Program별 API 목록 조회
GET /programs/{programId}/apis

# Program별 필수 API 목록 조회
GET /programs/{programId}/apis/required

# Menu에 필요한 API 계산
GET /programs/{programId}/required-apis

# 활성 API 목록 조회 (매핑용)
GET /programs/available-apis
```

---

## 💡 사용 시나리오

### 시나리오 1: 신규 화면 추가

```bash
# 1. Program 생성
POST /programs
{
  "name": "사용자 관리 화면",
  "path": "/admin/users",
  "frontPath": "/users",
  "description": "사용자 CRUD 화면"
}
→ Program ID: 10

# 2. 필요한 API 매핑
POST /programs/10/apis/101  # GET /users (목록 조회)
POST /programs/10/apis/102  # POST /users (생성)
POST /programs/10/apis/103  # PUT /users/{id} (수정)
POST /programs/10/apis/104  # DELETE /users/{id} (삭제)

# 3. Menu 생성 및 Program 연결
POST /menus
{
  "name": "사용자 관리",
  "programId": 10,
  "parentId": 1
}

# 4. Role에 API 권한 부여
POST /permissions/roles/2/apis/101  # ROLE_MANAGER
POST /permissions/roles/2/apis/102
POST /permissions/roles/2/apis/103
POST /permissions/roles/2/apis/104

# 5. 결과: ROLE_MANAGER로 로그인 시
# - "사용자 관리" 메뉴 표시 (모든 필수 API 권한 있음)
# - 메뉴 클릭 시 화면 정상 로드
```

### 시나리오 2: Role별 메뉴 필터링

```bash
# 상황:
# - Program A는 API 1, 2 필요
# - Program B는 API 3, 4 필요
# - ROLE_USER는 API 1, 2만 권한 있음
# - ROLE_ADMIN은 모든 API 권한 있음

# ROLE_USER 로그인
GET /menus/tree
→ Menu A 표시 (API 1,2 권한 있음)
→ Menu B 숨김 (API 3,4 권한 없음)

# ROLE_ADMIN 로그인
GET /menus/tree
→ Menu A 표시
→ Menu B 표시 (ADMIN 특권)
```

### 시나리오 3: 부모-자식 메뉴 권한

```bash
# 메뉴 구조:
# - 관리 메뉴 (Program X: API 10)
#   ├── 사용자 관리 (Program A: API 1,2)
#   └── 권한 관리 (Program B: API 3,4)

# 필요 API 계산:
# - "사용자 관리" 메뉴를 보려면:
#   API 10 (부모) + API 1,2 (자신) = {10, 1, 2}

# - "권한 관리" 메뉴를 보려면:
#   API 10 (부모) + API 3,4 (자신) = {10, 3, 4}
```

---

## 📈 장점 및 효과

### 1. 통합 권한 관리
- **Before**: 메뉴 권한과 API 권한 따로 관리 → 불일치 가능성
- **After**: Menu → Program → API 자동 연계 → 일관성 보장

### 2. 자동 메뉴 필터링
- **Before**: 프론트엔드에서 하드코딩으로 메뉴 숨김
- **After**: Role 기반 자동 필터링 → 동적 메뉴 구성

### 3. 명확한 역할 분리
- **Menu**: 네비게이션 구조
- **Program**: 화면/페이지 단위
- **ApiRegistry**: API 엔드포인트
- **Role**: 권한 그룹

### 4. 확장성
- 신규 화면 추가: Program-API 매핑만 설정
- 신규 API 추가: 자동 스캔 + Program 매핑
- 신규 Role 추가: Role-API 권한만 부여

---

## 🔧 기술적 특징

### 1. 재귀적 권한 계산
```java
// 부모 메뉴의 API도 포함
Menu parent = menu.getParent();
while (parent != null) {
    requiredApis.addAll(getApisForProgram(parent.getProgram()));
    parent = parent.getParent();
}
```

### 2. Redis 캐싱 활용
```java
// O(1) 성능으로 권한 확인
boolean hasPermission = redisTemplate.opsForSet()
    .isMember("ROLE_API_PERMISSION::ROLE_USER", apiIdentifier);
```

### 3. 필수/선택 API 구분
```java
// 필수 API만 권한 확인 대상
@Column(name = "required")
private Boolean required = true;
```

---

## 📝 생성된 파일 목록

**엔티티 & Repository (3개)**:
1. `ProgramApiMapping.java` - Program-API 매핑 엔티티
2. `ProgramApiMappingRepository.java` - Repository
3. `ProgramApiMappingDTO.java` - DTO

**서비스 (2개)**:
4. `ProgramPermissionIntegrationService.java` - Program-Permission 통합
5. `MenuPermissionService.java` - Menu-Program-API 연계

**Controller (1개)**:
6. `ProgramPermissionController.java` - REST API

**문서 (2개)**:
7. `program-module-removal-impact.md` - 영향도 분석
8. `program-permission-integration.md` - 통합 가이드 (본 문서)

---

## ✅ 완료된 구현 사항

- [x] Program-API 매핑 엔티티
- [x] Program에 API 추가/제거 기능
- [x] Menu → Program → API 연계 계산
- [x] Role 기반 메뉴 접근 권한 확인
- [x] 재귀적 부모 메뉴 권한 처리
- [x] REST API 엔드포인트
- [x] 빌드 성공

---

## 🔄 향후 개선 방향

### 1. 캐시 워밍업 자동화
```java
@EventListener(ApplicationReadyEvent.class)
public void warmUpProgramApiCache() {
    // 모든 Program의 필요 API 미리 캐싱
}
```

### 2. 프론트엔드 통합
```javascript
// 사용자 로그인 시 접근 가능 메뉴만 조회
GET /menus/accessible?roles=ROLE_USER
```

### 3. 감사 로그
```java
// Program-API 매핑 변경 이력 추적
@Audited
public class ProgramApiMapping { ... }
```

---

## 🎉 결론

**Program 모듈은 단순 미사용 코드가 아니라 Permission 모듈과 통합하여 더 강력한 권한 관리 시스템을 구축하는 핵심 요소입니다.**

- ✅ Menu (프론트엔드) ↔ Program (화면) ↔ API (백엔드) 완벽 연계
- ✅ Role 기반 동적 메뉴 필터링
- ✅ 자동 권한 계산 및 검증
- ✅ 확장 가능한 아키텍처

**최종 권장**: Program 모듈 유지 + Permission 통합 ✨
