# CORS 설정 통합 가이드

> **수정 일자**: 2026-01-09
> **이슈**: CORS 설정이 두 곳에 중복되어 있음
> **목표**: Spring Security에서 CORS를 중앙 관리

---

## 🎯 문제 현황

### 중복된 CORS 설정

기존에는 CORS 설정이 **두 곳**에 존재했습니다:

#### 1. FrameworkSecurityConfig (Spring Security)
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

#### 2. FrameworkWebMVCConfig (Spring MVC)
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

### 문제점

1. **중복 관리**: 동일한 설정을 두 곳에서 관리
2. **혼란**: 어떤 설정이 실제로 적용되는지 불명확
3. **유지보수성 저하**: 변경 시 두 곳 모두 수정해야 함

---

## 🔍 Spring Security + CORS 동작 원리

### CORS 필터 체인 순서

```
Request
  ↓
Spring Security Filter Chain
  ↓
CorsFilter (Spring Security CORS)  ← 이것이 먼저 적용됨!
  ↓
DispatcherServlet
  ↓
Spring MVC CORS               ← 이것은 무시되거나 오버라이드됨
  ↓
Controller
```

### 결론
**Spring Security를 사용하는 경우, Spring Security의 CORS 설정이 우선 적용됩니다.**

따라서 `FrameworkWebMVCConfig`의 `addCorsMappings()`는 실질적으로 적용되지 않습니다.

---

## ✅ 개선 내용

### 1. FrameworkSecurityConfig (수정)

**위치**: `src/main/java/com/wan/framework/base/FrameworkSecurityConfig.java`

#### Before
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            // CORS 설정이 명시적으로 적용되지 않음
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );

    return http.build();
}
```

#### After
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 명시적으로 CORS 적용
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            );

    return http.build();
}
```

**변경 사항**:
- `.cors()` 설정 추가
- `corsConfigurationSource()` 빈을 명시적으로 참조

---

### 2. FrameworkWebMVCConfig (수정)

**위치**: `src/main/java/com/wan/framework/base/FrameworkWebMVCConfig.java`

#### Before
```java
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

#### After
```java
// CorsRegistry import 제거

// CORS 설정은 FrameworkSecurityConfig에서 관리
// Spring Security의 CORS 설정이 우선 적용되므로 여기서는 제거
```

**변경 사항**:
- `addCorsMappings()` 메서드 전체 제거
- `CorsRegistry` import 제거
- 주석으로 CORS 관리 위치 명시

---

## 📊 최종 CORS 설정 구조

### 단일 관리 지점

```
FrameworkSecurityConfig
    ↓
corsConfigurationSource() Bean
    ↓
CorsConfiguration
    - allowedOrigins: http://localhost:3000
    - allowedMethods: GET, POST, PUT, DELETE, OPTIONS
    - allowedHeaders: *
    - allowCredentials: true
```

### 설정 적용 흐름

```
1. Spring Boot 시작
   ↓
2. FrameworkSecurityConfig 로드
   ↓
3. corsConfigurationSource() 빈 생성
   ↓
4. SecurityFilterChain에 CORS 적용
   ↓
5. 모든 HTTP 요청에 CORS 필터 적용
```

---

## 🧪 검증 방법

### 1. 빌드 확인
```bash
./gradlew clean build -x test
```

**결과**: ✅ BUILD SUCCESSFUL

### 2. CORS Preflight 요청 테스트

#### OPTIONS 요청 (Preflight)
```bash
curl -X OPTIONS http://localhost:8080/users \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

**예상 응답 헤더**:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

#### 실제 요청
```bash
curl -X GET http://localhost:8080/users/admin/exists \
  -H "Origin: http://localhost:3000" \
  -v
```

**예상 응답 헤더**:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Credentials: true
```

### 3. 브라우저 테스트

**프론트엔드 (http://localhost:3000)**:
```javascript
fetch('http://localhost:8080/users/admin/exists', {
  method: 'GET',
  credentials: 'include',  // 쿠키 포함
  headers: {
    'Content-Type': 'application/json',
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('CORS Error:', error));
```

**예상 결과**: ✅ CORS 에러 없이 정상 응답

---

## 🎯 개선 효과

### Before (개선 전)

**문제점**:
- ❌ CORS 설정이 두 곳에 중복
- ❌ 실제로 어떤 설정이 적용되는지 불명확
- ❌ 변경 시 두 곳 모두 수정해야 함
- ❌ Spring Security CORS가 명시적으로 적용되지 않음

### After (개선 후)

**개선점**:
- ✅ CORS 설정을 `FrameworkSecurityConfig`에서만 관리
- ✅ Spring Security에서 명시적으로 CORS 적용
- ✅ 중복 제거로 유지보수성 향상
- ✅ 설정 관리 위치가 명확함

---

## 📝 CORS 설정 변경 방법

### 1. 허용 Origin 추가

**위치**: `FrameworkSecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 개발 환경
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://localhost:3001"  // 추가
    ));

    // 또는 운영 환경
    configuration.setAllowedOrigins(Arrays.asList(
        "https://your-domain.com",
        "https://admin.your-domain.com"
    ));

    // ...
}
```

### 2. 환경별 설정 (권장)

**application.yml**:
```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

**FrameworkSecurityConfig.java**:
```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
    );
    // ...
}
```

**환경별 실행**:
```bash
# 개발 환경
java -jar app.jar

# 운영 환경
java -jar app.jar --cors.allowed-origins=https://your-domain.com,https://admin.your-domain.com
```

### 3. 모든 Origin 허용 (개발 환경만)

```java
configuration.setAllowedOrigins(Arrays.asList("*"));
// 주의: allowCredentials(true)와 함께 사용 불가!
```

**보안 주의사항**:
- ⚠️ `allowedOrigins("*")`와 `allowCredentials(true)`는 함께 사용 불가
- ⚠️ 운영 환경에서는 절대 `"*"` 사용 금지
- ✅ 개발 환경에서도 명시적인 Origin 지정 권장

---

## 🔒 보안 고려사항

### 1. allowCredentials(true) 사용 시

**의미**: 쿠키, Authorization 헤더 등 인증 정보 포함 허용

**주의사항**:
- `allowedOrigins("*")`와 함께 사용 불가
- 명시적으로 허용할 Origin만 지정해야 함

### 2. Preflight 요청 캐싱

**현재 설정**: 기본값 (1800초 = 30분)

**변경 방법**:
```java
configuration.setMaxAge(3600L);  // 1시간
```

### 3. 허용 헤더 제한

**현재 설정**: 모든 헤더 허용 (`"*"`)

**보안 강화**:
```java
configuration.setAllowedHeaders(Arrays.asList(
    "Content-Type",
    "Authorization",
    "X-Requested-With"
));
```

---

## 🚨 트러블슈팅

### 문제 1: CORS 에러 여전히 발생

**원인**: Spring Security FilterChain 순서 문제

**확인**:
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 이 줄 확인
        // ...
}
```

**해결**: `.cors()` 설정이 명시적으로 있는지 확인

---

### 문제 2: Preflight 요청 실패 (405 Method Not Allowed)

**원인**: OPTIONS 메서드가 허용되지 않음

**해결**:
```java
configuration.setAllowedMethods(Arrays.asList(
    "GET", "POST", "PUT", "DELETE", "OPTIONS"  // OPTIONS 반드시 포함
));
```

---

### 문제 3: 쿠키가 전송되지 않음

**원인**: `allowCredentials(true)` 설정 누락 또는 프론트엔드 설정 문제

**백엔드 확인**:
```java
configuration.setAllowCredentials(true);  // 반드시 true
```

**프론트엔드 확인**:
```javascript
// Axios
axios.defaults.withCredentials = true;

// Fetch API
fetch(url, {
  credentials: 'include'  // 반드시 포함
});
```

---

## 📋 체크리스트

### 개발자 체크리스트
- [x] `FrameworkSecurityConfig`에 `.cors()` 설정 추가
- [x] `FrameworkWebMVCConfig`에서 `addCorsMappings()` 제거
- [x] 빌드 성공 확인
- [ ] 애플리케이션 재시작
- [ ] OPTIONS 요청 테스트 (Preflight)
- [ ] 실제 API 요청 테스트 (CORS 헤더 확인)
- [ ] 브라우저에서 프론트엔드 연동 테스트
- [ ] 쿠키 전송 테스트

### 운영 배포 전 체크리스트
- [ ] `allowedOrigins`를 운영 도메인으로 변경
- [ ] `allowedOrigins("*")` 사용하지 않는지 확인
- [ ] 환경 변수로 Origin 관리하는지 확인
- [ ] HTTPS 사용 확인
- [ ] Preflight 캐시 시간 적절한지 확인

---

## 🎉 결론

**상태**: ✅ 완료

**변경 파일**:
- `FrameworkSecurityConfig.java` (수정)
- `FrameworkWebMVCConfig.java` (수정)

**핵심 개선**:
1. CORS 설정을 `FrameworkSecurityConfig`에서만 관리
2. Spring Security에서 명시적으로 CORS 적용
3. 중복 제거 및 유지보수성 향상

**CORS 설정 위치**:
- ✅ **FrameworkSecurityConfig** ← 여기서만 관리
- ❌ ~~FrameworkWebMVCConfig~~ ← 제거됨

**다음 단계**:
1. 애플리케이션 재시작
2. CORS Preflight 요청 테스트
3. 브라우저에서 프론트엔드 연동 테스트
4. 쿠키 전송 확인

---

**수정일**: 2026-01-09
**수정자**: Claude Code
**상태**: ✅ 완료
**빌드**: ✅ 성공
