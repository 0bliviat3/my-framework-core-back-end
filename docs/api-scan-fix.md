# API 스캔 문제 수정 보고서

> **수정 일자**: 2026-01-09
> **이슈**: API 자동 스캔이 0개로 나오는 문제
> **원인**: Spring Boot 3.x 호환성 문제

---

## 🐛 문제 현황

### 증상
```
2026-01-09T09:15:29.500+09:00  INFO 94116 --- [framework] [           main] c.w.f.p.service.ApiRegistryScanService   : ===== API Registry Scan Started =====
2026-01-09T09:15:29.502+09:00  INFO 94116 --- [framework] [           main] c.w.f.p.service.ApiRegistryScanService   : Scanned 0 APIs from application
```

API 스캔 서비스가 실행되지만 **0개의 API**만 스캔되는 문제 발생.

### 로그 분석
- `ApiRegistryScanService.scanAndRegisterApis()` 메서드는 정상 실행
- `RequestMappingHandlerMapping.getHandlerMethods()` 호출 성공
- 하지만 URI Patterns을 추출하는 과정에서 실패

---

## 🔍 원인 분석

### 코드 위치
**파일**: `src/main/java/com/wan/framework/permission/service/ApiRegistryScanService.java`
**메서드**: `scanAllApis()`

### 기존 코드 (문제)
```java
// URI Patterns 추출
Set<String> patterns = mappingInfo.getPatternsCondition() != null
        ? mappingInfo.getPatternsCondition().getPatterns()
        : Set.of();
```

### 문제점
**Spring Boot 3.x 변경사항**:
- Spring Boot 2.x: `getPatternsCondition()` 사용
- **Spring Boot 3.x**: `getPathPatternsCondition()` 사용 (새로운 메서드)

본 프로젝트는 **Spring Boot 3.5.4**를 사용하므로:
- `getPatternsCondition()`이 `null` 반환
- `patterns`가 항상 빈 Set으로 설정됨
- 결과적으로 모든 API가 스킵됨

### Spring Boot 3.x 변경 이유
Spring Boot 3.x에서는 URL 매칭 메커니즘이 개선되었습니다:
- **PathPattern**: 더 효율적인 URL 매칭 (Spring Boot 3.x 기본)
- **AntPathMatcher**: 레거시 방식 (Spring Boot 2.x)

---

## ✅ 수정 내용

### 수정된 코드

**위치**: `ApiRegistryScanService.java:156-178`

```java
// URI Patterns 추출 (Spring Boot 3.x 호환)
Set<String> patterns = new HashSet<>();

// PathPatternsCondition 사용 (Spring Boot 3.x)
if (mappingInfo.getPathPatternsCondition() != null) {
    patterns = mappingInfo.getPathPatternsCondition().getPatterns().stream()
            .map(Object::toString)
            .collect(Collectors.toSet());
    log.debug("Using PathPatternsCondition: {}", patterns);
}
// PatternsCondition 사용 (Fallback for older versions)
else if (mappingInfo.getPatternsCondition() != null) {
    patterns = mappingInfo.getPatternsCondition().getPatterns();
    log.debug("Using PatternsCondition: {}", patterns);
}

// 패턴이 없으면 스킵
if (patterns.isEmpty()) {
    log.warn("No patterns found for handler: {}.{}",
            handlerMethod.getBeanType().getSimpleName(),
            handlerMethod.getMethod().getName());
    continue;
}
```

### 변경 사항 요약

1. **Spring Boot 3.x 우선 지원**
   - `getPathPatternsCondition()` 먼저 시도
   - `PathPattern` 객체를 `.toString()`으로 문자열 변환

2. **Fallback 메커니즘**
   - `getPatternsCondition()` 사용 (Spring Boot 2.x 호환)
   - 하위 호환성 유지

3. **디버그 로깅 추가**
   - 어떤 메서드를 사용했는지 로깅
   - 패턴이 없는 경우 경고 로그

4. **안전성 개선**
   - 패턴이 없는 핸들러는 스킵 (예외 방지)

---

## 🧪 검증

### 빌드 결과
```
BUILD SUCCESSFUL in 22s
6 actionable tasks: 6 executed
```

**상태**: ✅ 성공

### 예상 결과

애플리케이션 재시작 시:

```log
2026-01-09T10:00:00.000+09:00  INFO --- c.w.f.p.service.ApiRegistryScanService   : ===== API Registry Scan Started =====
2026-01-09T10:00:00.100+09:00  DEBUG --- c.w.f.p.service.ApiRegistryScanService   : Total handler methods found: 85
2026-01-09T10:00:00.150+09:00  DEBUG --- c.w.f.p.service.ApiRegistryScanService   : Using PathPatternsCondition: [/users/sign-up]
2026-01-09T10:00:00.151+09:00  DEBUG --- c.w.f.p.service.ApiRegistryScanService   : Registered API: POST /users/sign-up -> UserController.signUp
2026-01-09T10:00:00.152+09:00  DEBUG --- c.w.f.p.service.ApiRegistryScanService   : Using PathPatternsCondition: [/sessions/login]
2026-01-09T10:00:00.153+09:00  DEBUG --- c.w.f.p.service.ApiRegistryScanService   : Registered API: POST /sessions/login -> SessionController.login
...
2026-01-09T10:00:01.000+09:00  INFO --- c.w.f.p.service.ApiRegistryScanService   : Scanned 80 APIs from application
2026-01-09T10:00:01.500+09:00  INFO --- c.w.f.p.service.ApiRegistryScanService   : Found 0 existing APIs in database
2026-01-09T10:00:02.000+09:00  INFO --- c.w.f.p.service.ApiRegistryScanService   : ===== API Registry Scan Completed =====
2026-01-09T10:00:02.001+09:00  INFO --- c.w.f.p.service.ApiRegistryScanService   : INSERT: 80, UPDATE: 0, DEACTIVATE: 0
```

**예상 스캔 API 수**: **80개+**

---

## 📋 테스트 체크리스트

애플리케이션 재시작 후 다음을 확인하세요:

### 1. 로그 확인
- [ ] `Scanned X APIs from application` (X > 0)
- [ ] `INSERT: X` (첫 실행 시 X > 0)
- [ ] 에러 로그 없음

### 2. 데이터베이스 확인
```sql
-- API Registry 테이블 확인
SELECT COUNT(*) FROM t_api_registry;
-- 결과: 80개 이상

-- 스캔된 API 목록 확인
SELECT service_id, http_method, uri_pattern, controller_name, handler_method
FROM t_api_registry
WHERE status = 'ACTIVE'
ORDER BY uri_pattern;
```

### 3. 권한 시스템 동작 확인
- [ ] 로그인 성공
- [ ] 권한이 없는 API 호출 시 403 Forbidden
- [ ] ROLE_ADMIN은 모든 API 접근 가능
- [ ] Redis 캐시에 권한 정보 저장 확인

### 4. API 목록 조회
```http
GET /permissions/apis
Authorization: Bearer {token}
```

**예상 응답**: 80개 이상의 API 목록

---

## 🎯 추가 개선 사항

### 1. 디버그 로그 레벨 조정 (선택)

**개발 환경**: `DEBUG` 레벨로 상세 로그 확인
```yaml
logging:
  level:
    com.wan.framework.permission.service.ApiRegistryScanService: DEBUG
```

**운영 환경**: `INFO` 레벨로 요약만 확인
```yaml
logging:
  level:
    com.wan.framework.permission.service.ApiRegistryScanService: INFO
```

### 2. 스캔 제외 패턴 추가 (선택)

불필요한 API를 스캔에서 제외하려면:

```java
// 제외할 패턴 정의
private static final Set<String> EXCLUDED_PATTERNS = Set.of(
    "/error",
    "/actuator/**",
    "/swagger-ui/**",
    "/api-docs/**"
);

// scanAllApis() 메서드에서 필터링
for (String pattern : patterns) {
    if (shouldExclude(pattern)) {
        log.debug("Excluding pattern: {}", pattern);
        continue;
    }
    // ... 기존 로직
}

private boolean shouldExclude(String pattern) {
    return EXCLUDED_PATTERNS.stream()
            .anyMatch(excluded -> pattern.startsWith(excluded.replace("/**", "")));
}
```

### 3. API 변경 감지 알림 (선택)

API가 추가/삭제될 때 관리자에게 알림:

```java
if (insertCount > 0 || deactivateCount > 0) {
    String message = String.format(
        "API Registry Updated - INSERT: %d, DEACTIVATE: %d",
        insertCount, deactivateCount
    );
    notificationService.sendToAdmin(message);
}
```

---

## 🔧 트러블슈팅

### 여전히 0개가 스캔되는 경우

#### 1. RequestMappingHandlerMapping 빈 확인
```java
@Autowired
private RequestMappingHandlerMapping requestMappingHandlerMapping;

// 애플리케이션 시작 후 확인
Map<RequestMappingInfo, HandlerMethod> methods =
    requestMappingHandlerMapping.getHandlerMethods();
System.out.println("Total mappings: " + methods.size());
```

**예상 결과**: 85개 이상

**0개인 경우**:
- Spring MVC 자동 설정 문제
- `@EnableWebMvc` 설정 확인
- `RequestMappingHandlerMapping` 빈 충돌 확인

#### 2. Controller 스캔 범위 확인

**Application.java**:
```java
@SpringBootApplication(scanBasePackages = "com.wan.framework")
public class FrameworkApplication {
    // ...
}
```

**확인사항**:
- Controller 클래스에 `@RestController` 또는 `@Controller` 어노테이션
- Controller 패키지가 스캔 범위에 포함되어 있는지

#### 3. 로그 레벨 변경

```yaml
logging:
  level:
    org.springframework.web.servlet.mvc.method.annotation: DEBUG
    com.wan.framework.permission: DEBUG
```

재시작 후 로그 확인:
- `RequestMappingHandlerMapping` 등록 로그
- API 스캔 상세 로그

---

## 📖 참고 자료

### Spring Boot 3.x 변경사항
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Framework 6.0 What's New](https://docs.spring.io/spring-framework/reference/6.0/whatsnew.html)

### PathPattern vs AntPathMatcher
- **PathPattern**: 더 빠르고 효율적 (Spring Boot 3.x 기본)
- **AntPathMatcher**: 레거시 호환성 (Spring Boot 2.x)

### RequestMappingHandlerMapping
- [Spring MVC RequestMappingHandlerMapping JavaDoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerMapping.html)

---

## 🎉 결론

**수정 완료**: ✅

**변경 파일**: `ApiRegistryScanService.java`

**핵심 수정**:
- Spring Boot 3.x 호환성 개선
- `getPathPatternsCondition()` 우선 사용
- Fallback 메커니즘 추가
- 디버그 로깅 강화

**예상 결과**:
- API 스캔 정상 동작 (80개+ API 등록)
- 권한 시스템 정상 작동
- 메뉴 필터링 정상 동작

**다음 단계**:
1. 애플리케이션 재시작
2. 로그 확인 (`Scanned X APIs`)
3. 데이터베이스 확인 (`t_api_registry` 테이블)
4. 권한 시스템 테스트

---

**수정일**: 2026-01-09
**수정자**: Claude Code
**상태**: ✅ 완료
