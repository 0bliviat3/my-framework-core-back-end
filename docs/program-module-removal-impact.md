# 프로그램 관리 모듈 제거 영향도 분석

## 📋 분석 개요

프로그램 관리 모듈(Program Module)은 권한 관리 모듈(Permission Module)이 구현되면서 역할이 중복되어 미사용 코드가 되었습니다. 이 문서는 해당 모듈 제거 시 영향도를 분석합니다.

---

## 🔍 현재 상태 분석

### 1. Program 모듈 구성
```
com.wan.framework.program/
├── constant/
│   └── ProgramExceptionMessage.java
├── domain/
│   └── Program.java                    # t_program 테이블 엔티티
├── dto/
│   └── ProgramDTO.java
├── exception/
│   └── ProgramException.java
├── mapper/
│   └── ProgramMapper.java
├── repository/
│   └── ProgramRepository.java
├── service/
│   └── ProgramService.java
└── web/
    └── ProgramController.java
```

### 2. Program 엔티티 구조 (`Program.java`)
```java
@Entity
@Table(name = "t_program")
public class Program {
    @Id
    private Long id;                    // 프로그램 ID
    private String name;                // 프로그램명 (UNIQUE)
    private String frontPath;           // 프론트엔드 경로
    private String path;                // API 경로
    private String apiKey;              // API Key
    private String description;         // 설명
    private DataStateCode dataStateCode;
    private AbleState ableState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## ⚠️ 영향도 분석

### 1. **Menu 모듈 의존성 (HIGH IMPACT)**

#### 영향 받는 파일
- `Menu.java` (line 34-36)
- `MenuService.java` (line 10, 32, 48-53, 120-126)

#### Menu 엔티티의 FK 관계
```java
@Entity
@Table(name = "t_menu")
public class Menu {
    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "program_id")
    private Program program;  // ← Program 참조
}
```

#### MenuService에서의 사용
```java
// MenuService.java - line 48-53
if (request.getProgramId() != null) {
    Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
            .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
    menu.setProgram(program);
}

// MenuService.java - line 120-126
if (request.getProgramId() != null) {
    Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
            .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
    menu.setProgram(program);
}
```

**분석**: Menu는 Program을 nullable FK로 참조하고 있습니다.

---

### 2. **데이터베이스 영향 (MEDIUM IMPACT)**

#### 테이블 구조
```sql
-- t_program 테이블
CREATE TABLE t_program (
    program_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_name VARCHAR(100) NOT NULL UNIQUE,
    front_path VARCHAR(255),
    program_path VARCHAR(255) NOT NULL,
    api_key VARCHAR(255),
    description VARCHAR(500),
    data_code VARCHAR(1) NOT NULL,
    able_state VARCHAR(10) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- t_menu 테이블의 FK
ALTER TABLE t_menu
    ADD CONSTRAINT fk_menu_program
    FOREIGN KEY (program_id) REFERENCES t_program(program_id);
```

**분석**:
- Menu 테이블이 Program 테이블을 FK로 참조
- FK 제약조건 존재 시 Program 삭제 불가

---

### 3. **Permission 모듈과의 비교**

| 항목 | Program 모듈 | Permission 모듈 (신규) |
|------|--------------|----------------------|
| **목적** | 프로그램(화면) 단위 권한 관리 | API 단위 동적 권한 관리 |
| **자동화** | 수동 등록 | 자동 스캔 (ApplicationReadyEvent) |
| **권한 단위** | 프로그램(화면) | API 엔드포인트 |
| **캐싱** | 없음 | Redis 캐싱 (O(1)) |
| **역할** | Menu와 연계된 화면 권한 | Role 기반 API 접근 제어 |

**분석**: 두 모듈의 목적이 다릅니다.
- **Program**: 프론트엔드 메뉴/화면 관리
- **Permission**: 백엔드 API 권한 관리

---

## 🎯 제거 시나리오 및 대응 방안

### 시나리오 1: Program 모듈 완전 제거

#### 필요 작업
1. **Menu 엔티티 수정**
   ```java
   // Menu.java - 제거할 코드
   @ManyToOne
   @JoinColumn(name = "program_id")
   private Program program;  // ← 삭제
   ```

2. **MenuService 수정**
   ```java
   // MenuService.java - 제거할 코드
   private final ProgramRepository programRepository;  // ← 삭제

   // createMenu() 메서드에서 제거
   if (request.getProgramId() != null) {
       Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
               .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
       menu.setProgram(program);
   }

   // updateMenu() 메서드에서 제거
   if (request.getProgramId() != null) {
       Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
               .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
       menu.setProgram(program);
   }
   ```

3. **MenuDTO 수정**
   ```java
   // MenuDTO.java
   private Long programId;  // ← 삭제
   ```

4. **MenuMapper 수정**
   - Program 관련 매핑 로직 제거

5. **MenuExceptionMessage 수정**
   ```java
   NOT_FOUND_PROGRAM("MENU_004", "프로그램을 찾을 수 없습니다.");  // ← 삭제
   ```

6. **데이터베이스 마이그레이션**
   ```sql
   -- 1. FK 제약조건 삭제
   ALTER TABLE t_menu DROP FOREIGN KEY fk_menu_program;

   -- 2. program_id 컬럼 삭제
   ALTER TABLE t_menu DROP COLUMN program_id;

   -- 3. t_program 테이블 삭제
   DROP TABLE t_program;
   ```

7. **Program 패키지 삭제**
   ```bash
   rm -rf src/main/java/com/wan/framework/program
   ```

#### 영향 받는 파일 목록
- ✅ `Menu.java` - program 필드 제거
- ✅ `MenuService.java` - ProgramRepository 및 로직 제거
- ✅ `MenuDTO.java` - programId 필드 제거
- ✅ `MenuMapper.java` - Program 매핑 제거
- ✅ `MenuExceptionMessage.java` - NOT_FOUND_PROGRAM 제거
- ✅ `t_menu` 테이블 - program_id 컬럼 제거
- ✅ `t_program` 테이블 - 전체 삭제

---

### 시나리오 2: Program 모듈 유지 (추천)

#### 근거
1. **Menu와의 연계**: Menu는 프론트엔드 화면 구조를 관리하며, Program은 각 메뉴에 연결된 화면(페이지) 정보를 제공
2. **역할 분리**:
   - **Program**: 프론트엔드 메뉴/화면 관리
   - **Permission**: 백엔드 API 접근 제어
3. **데이터 보존**: 기존 Menu-Program 연계 데이터 유지

#### 개선 방안
Program 모듈을 현대화하여 Permission 모듈과 통합:

```java
// Program.java - API Registry와 연계 추가
@Entity
@Table(name = "t_program")
public class Program {
    // 기존 필드 유지

    // Permission 모듈과 연계
    @ManyToMany
    @JoinTable(
        name = "t_program_api_mapping",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "api_id")
    )
    private Set<ApiRegistry> apiRegistries;  // 프로그램이 사용하는 API 목록
}
```

이렇게 하면:
- Menu → Program → ApiRegistry 연계 가능
- 프론트엔드 메뉴별로 필요한 API 권한을 자동 계산 가능

---

## 📊 제거 vs 유지 비교

| 항목 | 완전 제거 | 유지 및 개선 |
|------|----------|------------|
| **작업량** | 높음 (7개 파일 수정 + DB 변경) | 낮음 (현상 유지) |
| **리스크** | 높음 (Menu 기능 영향) | 낮음 |
| **데이터 손실** | 기존 Program 데이터 손실 | 데이터 보존 |
| **확장성** | 제한적 | 높음 (Program-API 연계 가능) |
| **메뉴 관리** | 단순화 (메뉴만) | 풍부함 (메뉴+화면+API) |

---

## 💡 권장 사항

### 🟢 **추천: Program 모듈 유지**

**이유**:
1. **명확한 역할 분리**
   - Program: 프론트엔드 화면(페이지) 관리
   - Permission: 백엔드 API 권한 관리
   - Menu: 네비게이션 구조 관리

2. **실제 사용 중**
   - Menu가 Program을 참조하여 화면 정보 관리
   - 제거 시 Menu 기능 축소

3. **낮은 리스크**
   - 현재 정상 동작 중
   - 제거로 인한 부작용 없음

4. **향후 확장 가능**
   - Program과 ApiRegistry를 연계하면 더 강력한 권한 관리 가능
   - 예: "이 화면을 보려면 이 API들에 접근 권한이 필요함"

### 🔴 **비추천: Program 모듈 제거**

**이유**:
1. Menu 모듈에 영향 (High Impact)
2. 데이터베이스 구조 변경 필요
3. 기존 Program 데이터 손실
4. 추후 유사 기능 재구현 가능성

---

## 🔧 결론

**Program 모듈은 "미사용 코드"가 아닙니다.**

- ✅ Menu 모듈이 실제로 사용 중
- ✅ Permission 모듈과 역할이 다름 (화면 vs API)
- ✅ 제거 시 Menu 기능 저하
- ✅ 유지하면서 Permission과 통합하는 것이 최선

**최종 권장**: **Program 모듈 유지 + Permission 모듈과의 통합 고려**

---

## 📝 추가 고려사항

만약 정말로 제거를 원한다면:
1. Menu에서 Program 참조 제거
2. Menu를 단순 네비게이션 구조로만 사용
3. 화면별 권한은 Permission (API 기반)으로만 관리

하지만 이 경우 "메뉴 항목 = 화면 = API 집합" 연계가 끊어져서
프론트엔드에서 권한 기반 메뉴 필터링이 복잡해질 수 있습니다.
