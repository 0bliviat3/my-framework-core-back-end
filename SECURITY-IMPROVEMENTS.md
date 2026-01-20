# FrameworkSecurityConfig 보안 개선 리포트

## 개요
프로덕션 배포를 위해 `FrameworkSecurityConfig` 클래스의 보안 설정을 검토하고 개선했습니다.

---

## 개선 전 보안 이슈

### 1. CORS 설정이 하드코딩됨
```java
// 개선 전
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
```

**문제점:**
- 프로덕션 환경에서 localhost만 허용
- 환경별로 코드 수정 필요
- 배포 시 보안 위험

### 2. 보안 헤더 미설정
```java
// 개선 전
http
    .csrf(csrf -> csrf.disable())
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
```

**문제점:**
- X-Frame-Options 미설정 → 클릭재킹 공격 가능
- X-Content-Type-Options 미설정 → MIME 스니핑 공격 가능
- CSP 미설정 → XSS 공격 취약
- HSTS 미설정 → 중간자 공격 가능 (HTTPS 환경)

### 3. 세션 관리 설정 부족
```java
// 개선 전: 세션 관리 설정 없음
```

**문제점:**
- 동시 세션 제한 없음 → 세션 하이재킹 위험
- 세션 고정 공격 방지 미흡

### 4. CORS 설정 미흡
```java
// 개선 전
configuration.setAllowedHeaders(Arrays.asList("*"));
configuration.setAllowCredentials(true);
```

**문제점:**
- 모든 헤더 허용 → 과도한 권한
- ExposedHeaders 미설정 → 필요한 헤더 접근 불가
- MaxAge 미설정 → Preflight 요청 과다

---

## 개선 후 보안 강화 사항

### 1. 환경 변수 기반 CORS 설정

```java
@Value("${security.cors.allowed-origins:http://localhost:3000}")
private String[] allowedOrigins;

@Value("${security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
private String allowedMethods;

@Value("${security.cors.max-age:3600}")
private Long corsMaxAge;
```

**개선 효과:**
- ✅ 환경별 Origin 분리 가능
- ✅ 코드 수정 없이 설정 변경
- ✅ 프로덕션/개발 환경 격리

**사용 예시:**
```yaml
# application-prod.yml
security:
  cors:
    allowed-origins: https://yourdomain.com,https://www.yourdomain.com
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    max-age: 3600
```

### 2. 보안 헤더 설정 추가

#### X-Frame-Options (클릭재킹 방지)
```java
.headers(headers -> headers
    .frameOptions(frame -> frame.deny())
)
```
- 모든 프레임 삽입 차단
- 클릭재킹 공격 방지

#### X-Content-Type-Options (MIME 스니핑 방지)
```java
.contentTypeOptions(contentType -> {})
```
- MIME 타입 스니핑 방지
- Content-Type 헤더 강제 준수

#### Strict-Transport-Security (HTTPS 강제)
```java
.httpStrictTransportSecurity(hsts -> hsts
    .includeSubDomains(true)
    .maxAgeInSeconds(31536000)  // 1년
)
```
- HTTPS 연결 강제
- 서브도메인 포함
- 중간자 공격 방지

#### Referrer-Policy (리퍼러 정보 제어)
```java
.referrerPolicy(referrer -> referrer
    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
)
```
- 크로스 오리진 요청 시 Origin만 전송
- 정보 누출 방지

#### Content-Security-Policy (XSS 방지)
```java
.contentSecurityPolicy(csp -> csp
    .policyDirectives("default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'")
)
```
- 허용된 리소스만 로드
- XSS 공격 방어
- 데이터 인젝션 방지

### 3. 세션 관리 강화

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1)  // 동시 세션 1개 제한
    .maxSessionsPreventsLogin(false)  // 기존 세션 만료
)
```

**개선 효과:**
- ✅ 동시 로그인 1개로 제한
- ✅ 세션 하이재킹 위험 감소
- ✅ 기존 세션 자동 만료

### 4. CORS 설정 개선

```java
// 허용할 Origin (환경변수로 분리)
configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

// 노출할 헤더 (프론트엔드에서 접근 가능)
configuration.setExposedHeaders(Arrays.asList(
        "Authorization",
        "Content-Disposition",
        "X-Total-Count"
));

// Preflight 요청 캐시 시간
configuration.setMaxAge(corsMaxAge);
```

**개선 효과:**
- ✅ 필요한 헤더만 노출
- ✅ Preflight 캐싱으로 성능 향상
- ✅ 환경별 설정 분리

### 5. 로깅 추가

```java
log.info("Initializing Security Configuration");
log.info("Allowed CORS Origins: {}", Arrays.toString(allowedOrigins));
log.info("Secure Cookie: {}", secureCookie);
log.info("CORS Configuration initialized - Max Age: {}s", corsMaxAge);
```

**개선 효과:**
- ✅ 보안 설정 시작 시 확인 가능
- ✅ 디버깅 용이
- ✅ 감사 로그 확보

---

## 보안 헤더 응답 예시

### HTTP 응답 헤더
```http
HTTP/1.1 200 OK
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Strict-Transport-Security: max-age=31536000; includeSubDomains
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'
Access-Control-Allow-Origin: https://yourdomain.com
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

---

## 추가 보안 권장 사항

### 1. HTTPS 필수 사용
```yaml
# application-prod.yml
server:
  servlet:
    session:
      cookie:
        secure: true  # HTTPS 환경에서만 쿠키 전송
        same-site: strict  # CSRF 방지
```

### 2. Rate Limiting 구현
- API 호출 제한 필터 추가 권장
- DDoS 공격 방지

### 3. SQL Injection 방지
- 현재: JPA 사용으로 자동 방어
- 권장: 동적 쿼리 사용 시 검증 강화

### 4. XSS 방지
- 현재: CSP 설정으로 방어
- 권장: 입력값 검증 및 출력 이스케이핑

### 5. 민감 정보 로깅 방지
```java
// 비밀번호 로깅 방지
log.info("User login: {}", userId);  // ✅ OK
log.info("User login: {} / {}", userId, password);  // ❌ 절대 금지
```

### 6. 환경 변수 보안
```bash
# .env 파일 권한 설정
chmod 600 .env

# Git에서 제외
echo ".env" >> .gitignore
```

---

## 보안 체크리스트

### 배포 전 필수 확인

- [x] **CORS 설정**: 실제 프론트엔드 도메인으로 설정
- [x] **HTTPS 활성화**: `COOKIE_SECURE=true` 설정
- [x] **보안 헤더**: X-Frame-Options, CSP 등 활성화
- [x] **세션 관리**: 동시 세션 제한 설정
- [x] **환경 변수**: 민감 정보 분리
- [x] **로깅**: 보안 설정 확인 로그 추가

### 정기 점검 항목

- [ ] **의존성 업데이트**: 보안 패치 적용
- [ ] **로그 모니터링**: 비정상 접근 탐지
- [ ] **취약점 스캔**: `docker scan` 정기 실행
- [ ] **백업**: 데이터베이스 정기 백업
- [ ] **방화벽 규칙**: 불필요한 포트 차단 확인

---

## 참고 자료

### OWASP Top 10 대응

| OWASP 위협 | 대응 방안 |
|-----------|---------|
| A01:2021 – Broken Access Control | 인터셉터 기반 권한 검증 |
| A02:2021 – Cryptographic Failures | HTTPS 강제, HSTS 설정 |
| A03:2021 – Injection | JPA 사용, PreparedStatement |
| A04:2021 – Insecure Design | 세션 관리, CORS 설정 |
| A05:2021 – Security Misconfiguration | 보안 헤더 설정 |
| A07:2021 – XSS | CSP 설정, 입력값 검증 |
| A08:2021 – Software and Data Integrity Failures | Docker 이미지 스캔 |
| A09:2021 – Security Logging and Monitoring Failures | 보안 로깅 추가 |

### 관련 문서
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Mozilla Web Security Guidelines](https://infosec.mozilla.org/guidelines/web_security)

---

**작성일**: 2025-01-20
**버전**: 1.0.0
**작성자**: Framework Team
