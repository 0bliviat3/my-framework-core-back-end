# Framework Core Back-end 코드 리뷰

> 리뷰 일자: 2026-01-07
> 리뷰 대상: my-framework-core-back-end (v0.0.1)
> 리뷰어: Claude Code

---

## 📋 목차

- [1. 리뷰 개요](#1-리뷰-개요)
- [2. 전체 평가](#2-전체-평가)
- [3. 모듈별 상세 리뷰](#3-모듈별-상세-리뷰)
- [4. 보안 취약점 분석](#4-보안-취약점-분석)
- [5. 성능 최적화 권장사항](#5-성능-최적화-권장사항)
- [6. 아키텍처 분석](#6-아키텍처-분석)
- [7. 개선 우선순위](#7-개선-우선순위)
- [8. 결론 및 권장사항](#8-결론-및-권장사항)

---

## 1. 리뷰 개요

### 1.1 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 프로젝트명 | Framework Core Back-end |
| 버전 | 0.0.1-SNAPSHOT |
| 기술 스택 | Spring Boot 3.5.4, Java 17, Gradle |
| 총 파일 수 | 229개 |
| 코드 라인 수 | ~25,450 lines |
| 구현 모듈 | 8개 (User, Session, Redis, Batch, Proxy API, Code, Board, API Key) |

### 1.2 리뷰 관점

본 리뷰는 다음 관점에서 프로젝트를 종합 평가했습니다:

1. **로직 검증**: 구현 로직의 정확성 및 엣지 케이스 처리
2. **모듈 간 통합**: 의존성 관리 및 연동 상태
3. **결합도/응집도**: 모듈화 품질 및 설계 원칙 준수
4. **보안**: 보안 취약점 및 위험 요소
5. **성능**: 성능 병목 지점 및 최적화 기회
6. **코어 플랫폼 적합성**: 재사용성, 확장성, 범용성

---

## 2. 전체 평가

### 2.1 종합 점수

| 평가 항목 | 점수 | 등급 |
|----------|------|------|
| 로직 구현 | 85/100 | B+ |
| 보안 | 40/100 | D |
| 성능 | 70/100 | C+ |
| 결합도/응집도 | 80/100 | B |
| 재사용성 | 90/100 | A |
| 테스트 커버리지 | 75/100 | C+ |
| **총합** | **73/100** | **C+** |

### 2.2 강점 (Strengths)

✅ **우수한 모듈화**
- 8개 주요 모듈이 명확히 분리되어 있음
- 도메인별 패키지 구조 일관성 (domain, dto, service, repository, mapper, web)
- 단일 책임 원칙(SRP) 준수

✅ **효과적인 캐싱 전략**
- Redis 기반 공통코드 캐싱으로 읽기 성능 최적화
- Spring Session Redis로 분산 세션 관리
- Cache-Aside 패턴 적용

✅ **분산 시스템 대비**
- Redis 분산 락 구현 (Lua Script 기반 원자적 연산)
- Spring Session으로 멀티 서버 세션 공유
- Quartz Scheduler로 안정적인 스케줄링

✅ **동적 구성 지원**
- Proxy API로 코드 수정 없이 외부 API 호출 가능
- 동적 게시판 생성 (메타데이터 기반)
- 설정 파일 기반 동작 (application.yml)

✅ **보안 기능 구현**
- PBKDF2 기반 강력한 비밀번호 해싱
- Salt 값 개별 생성 및 저장
- 세션 고정 공격 방지
- IP/User-Agent 검증

### 2.3 약점 (Weaknesses)

🔴 **치명적인 보안 설정**
- Spring Security가 완전히 비활성화 (모든 요청 permitAll)
- CSRF 보호 완전 해제
- **프로덕션 배포 불가 상태**

⚠️ **성능 병목**
- Board 모듈에서 N+1 쿼리 발생
- 인덱스 설정 미흡 (일부 테이블만 명시적 인덱스)
- 대용량 데이터 처리 시 성능 저하 우려

⚠️ **분산 환경 미대비**
- Batch 모듈에서 분산 락 미사용 (중복 실행 가능성)
- 캐시 무효화가 단일 서버에서만 동작 (Redis Pub/Sub 미사용)

⚠️ **불완전한 기능 구현**
- API Key 권한 관리 미사용 (엔티티는 존재하나 검증 로직 없음)
- Rate Limiting 미구현 (DoS 공격 취약)
- 파일 업로드 보안 취약 (경로 조작 공격 가능)

---

## 3. 모듈별 상세 리뷰

### 3.1 User 모듈 (사용자 관리)

#### ✅ 정상 동작

**비밀번호 보안**
```java
// PasswordService.java
public String hashPassword(String password, String saltBase64) {
    PBEKeySpec spec = new PBEKeySpec(
        password.toCharArray(),
        Base64.getDecoder().decode(saltBase64),
        10000,  // 충분한 반복 횟수
        256     // 256-bit 해시
    );
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    // ...
}
```

**Soft Delete 구현**
```java
// User.java
@Column(nullable = false)
private DataStateCode dataState = DataStateCode.I;

// UserService.java
@Transactional
public UserDTO deleteUser(String userId) {
    user.setDataState(DataStateCode.D);  // 논리적 삭제
}
```

#### ⚠️ 개선 필요

**1. 트랜잭션 범위 문제** (src/main/java/com/wan/framework/user/service/SignService.java:98)
```java
@Transactional  // ❌ 검증 실패 시에도 트랜잭션 시작
public UserDTO modifyUser(UserDTO userDTO) {
    validateSignIn(userDTO);  // 이 부분은 트랜잭션 밖에 있어야 함

    String newSalt = passwordService.generateSaltBase64();
    String newHash = passwordService.hashPassword(userDTO.getPassword(), newSalt);
    // ...
}
```

**개선 방안:**
```java
// 검증과 수정 로직 분리
public UserDTO modifyUser(UserDTO userDTO) {
    validateSignIn(userDTO);  // 트랜잭션 밖
    return modifyUserInternal(userDTO);  // 트랜잭션 안
}

@Transactional
private UserDTO modifyUserInternal(UserDTO userDTO) {
    String newSalt = passwordService.generateSaltBase64();
    String newHash = passwordService.hashPassword(userDTO.getPassword(), newSalt);
    userDTO.setPassword(newHash);
    userDTO.setPasswordSalt(newSalt);
    return userService.modifyUser(userDTO).removePass();
}
```

**2. 역할 관리 미구현**
- User 엔티티에 roles 필드 존재하나 실제 사용되지 않음
- 세션 생성 시 하드코딩된 "ROLE_USER"만 부여
- 역할 기반 접근 제어(RBAC) 미구현

**개선 방안:**
```java
// UserRole.java (신규 엔티티)
@Entity
public class UserRole {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;  // ROLE_USER, ROLE_ADMIN, ROLE_MANAGER
}

// SessionController.java
SessionDTO session = sessionService.createSession(
    httpRequest, httpResponse,
    user.getUserId(),
    user.getName(),
    user.getRoles()  // 하드코딩 대신 실제 역할 사용
);
```

#### 🔴 심각한 문제

**Spring Security 완전 비활성화** (src/main/java/com/wan/framework/base/FrameworkSecurityConfig.java:18-23)
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 해제
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll());  // 모든 요청 허용
    return http.build();
}
```

**위험성:**
- 인증되지 않은 사용자가 모든 API 접근 가능
- CSRF 공격에 완전히 노출
- 프로덕션 환경 배포 시 즉각적인 보안 사고 발생 가능

---

### 3.2 Session 모듈 (세션 관리)

#### ✅ 정상 동작

**세션 고정 공격 방지** (src/main/java/com/wan/framework/session/service/SessionService.java:54-59)
```java
public SessionDTO createSession(...) {
    // 기존 세션 무효화
    HttpSession oldSession = request.getSession(false);
    if (oldSession != null) {
        oldSession.invalidate();
    }

    // 새 세션 생성
    HttpSession session = request.getSession(true);
    // ...
}
```

**보안 쿠키 설정** (src/main/java/com/wan/framework/session/config/SessionConfig.java:27-34)
```java
@Bean
public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setUseHttpOnlyCookie(true);   // XSS 방지
    serializer.setUseSecureCookie(true);      // HTTPS only
    serializer.setSameSite("Strict");         // CSRF 방지
    return serializer;
}
```

**세션 보안 검증** (src/main/java/com/wan/framework/session/service/SessionSecurityService.java:28-45)
```java
public void validateSessionSecurity(HttpSession session, HttpServletRequest request) {
    if (sessionProperties.getSecurity().isValidateIp()) {
        String sessionIp = (String) session.getAttribute(ATTR_IP_ADDRESS);
        String requestIp = request.getRemoteAddr();
        if (!sessionIp.equals(requestIp)) {
            throw new SessionException(IP_MISMATCH);
        }
    }
    // User-Agent 검증도 동일
}
```

#### ⚠️ 개선 필요

**1. 동시 로그인 제한 없음**
- 동일 사용자가 무제한 세션 생성 가능
- 계정 공유 방지 메커니즘 없음

**개선 방안:**
```java
// SessionService.java
@Transactional
public SessionDTO createSession(HttpServletRequest request, HttpServletResponse response,
                                String userId, String username, List<String> roles) {
    // 기존 세션 수 확인
    long sessionCount = sessionManagementService.getUserSessions(userId).size();
    int maxSessions = sessionProperties.getMaxSessionsPerUser();

    if (sessionCount >= maxSessions) {
        // 가장 오래된 세션 강제 종료
        sessionManagementService.terminateOldestSession(userId);
    }

    // 세션 생성 로직
    // ...
}
```

**2. IP 검증 우회 가능**
- 프록시 환경에서 X-Forwarded-For 헤더 조작 가능
- 신뢰할 수 있는 프록시 IP 화이트리스트 미구현

**개선 방안:**
```java
// SessionSecurityService.java
private String getClientIP(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    String remoteAddr = request.getRemoteAddr();

    // 신뢰할 수 있는 프록시인지 확인
    if (forwardedFor != null && isTrustedProxy(remoteAddr)) {
        return forwardedFor.split(",")[0].trim();
    }

    return remoteAddr;
}

private boolean isTrustedProxy(String ip) {
    List<String> trustedProxies = sessionProperties.getSecurity().getTrustedProxies();
    return trustedProxies.contains(ip);
}
```

**3. 세션 갱신 로직 불완전**
```java
// SessionService.java:107
public void refreshSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    session.setAttribute(ATTR_LAST_ACCESS_TIME, LocalDateTime.now());
    // Redis TTL은 Spring Session이 자동 관리하나 명시적 연장 없음
}
```

**개선 방안:**
```java
public void refreshSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    session.setAttribute(ATTR_LAST_ACCESS_TIME, LocalDateTime.now());

    // 명시적으로 세션 접근하여 TTL 갱신
    session.setMaxInactiveInterval(
        sessionProperties.getCookie().getMaxAge()
    );
}
```

---

### 3.3 Redis 모듈 (분산 락/캐시)

#### ✅ 정상 동작

**Lua Script 기반 원자적 락 해제** (src/main/java/com/wan/framework/redis/service/DistributedLockService.java:56-64)
```java
public boolean releaseLock(String key, String expectedValue) {
    String script =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(script, Long.class),
        Collections.singletonList(key),
        expectedValue
    );
    return result != null && result == 1L;
}
```

**락 소유자 식별** (src/main/java/com/wan/framework/redis/service/DistributedLockService.java:37-40)
```java
public String acquireLock(String key, long ttlSeconds) {
    String lockValue = UUID.randomUUID().toString() + ":" + serverIdentifier;
    // UUID로 락 소유자 구분 + 서버 ID로 디버깅 편의성
}
```

**TTL 필수 설정으로 데드락 방지**
```java
Boolean success = redisTemplate.opsForValue()
    .setIfAbsent(key, lockValue, ttlSeconds, TimeUnit.SECONDS);
// TTL 누락 시 락이 영구 보존되는 문제 원천 차단
```

#### ⚠️ 개선 필요

**1. 락 재진입 불가** (Reentrant Lock 미지원)
- 동일 스레드에서 같은 락을 다시 획득할 수 없음
- 재귀 호출이나 중첩 트랜잭션에서 데드락 발생 가능

**개선 방안:**
```java
// DistributedLockService.java
private final ThreadLocal<Map<String, LockInfo>> threadLocks =
    ThreadLocal.withInitial(HashMap::new);

public String acquireReentrantLock(String key, long ttlSeconds) {
    String threadKey = Thread.currentThread().getId() + ":" + key;
    LockInfo lockInfo = threadLocks.get().get(threadKey);

    if (lockInfo != null) {
        // 재진입: 카운트만 증가
        lockInfo.incrementCount();
        return lockInfo.getLockValue();
    }

    // 최초 획득
    String lockValue = acquireLock(key, ttlSeconds);
    if (lockValue != null) {
        threadLocks.get().put(threadKey, new LockInfo(lockValue, 1));
    }
    return lockValue;
}

public boolean releaseReentrantLock(String key, String lockValue) {
    String threadKey = Thread.currentThread().getId() + ":" + key;
    LockInfo lockInfo = threadLocks.get().get(threadKey);

    if (lockInfo == null) {
        return false;
    }

    lockInfo.decrementCount();
    if (lockInfo.getCount() > 0) {
        // 아직 재진입 중
        return true;
    }

    // 모든 재진입이 해제됨
    threadLocks.get().remove(threadKey);
    return releaseLock(key, lockValue);
}
```

**2. 락 만료 시 알림 없음**
- 작업 중 TTL 만료 시 조용히 락 해제
- 작업이 계속 진행되어 Race Condition 발생 가능

**개선 방안 (Lock Watch Dog):**
```java
// 백그라운드에서 활성 락의 TTL을 자동 연장
@Scheduled(fixedDelay = 10000)  // 10초마다
public void renewActiveLocks() {
    for (Map.Entry<String, String> entry : activeLocks.entrySet()) {
        String key = entry.getKey();
        String lockValue = entry.getValue();

        // 아직 처리 중인지 확인
        if (isProcessing(lockValue)) {
            extendLock(key, lockValue, 30);  // 30초 연장
            log.debug("Lock extended: key={}", key);
        }
    }
}
```

**3. Redis 장애 시 Fallback 없음**
- Redis 다운 시 모든 락 관련 기능 마비
- 단일 장애점(Single Point of Failure)

**개선 방안:**
```java
// Circuit Breaker 패턴 적용
@Service
public class ResilientDistributedLockService {
    private final DistributedLockService redisLockService;
    private final LocalLockService localLockService;  // Fallback

    @CircuitBreaker(name = "redisLock", fallbackMethod = "acquireLocalLock")
    public String acquireLock(String key, long ttlSeconds) {
        return redisLockService.acquireLock(key, ttlSeconds);
    }

    private String acquireLocalLock(String key, long ttlSeconds, Throwable t) {
        log.warn("Redis unavailable, using local lock: {}", t.getMessage());
        return localLockService.acquireLock(key, ttlSeconds);
    }
}
```

#### 🔴 심각한 문제

**RedisCacheService 타입 안전성 문제** (src/main/java/com/wan/framework/redis/service/RedisCacheService.java:33-40)
```java
public <T> T get(String key, Class<T> clazz) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) {
        return null;
    }

    // ❌ ObjectMapper로 변환 시 타입 정보 손실 가능
    return objectMapper.convertValue(value, clazz);
}
```

**문제점:**
- GenericJackson2JsonRedisSerializer 사용 시 클래스 메타데이터가 포함되나
- 직접 클래스를 전달하면 타입 불일치 가능
- List, Map 같은 제네릭 타입에서 ClassCastException 발생 가능

**개선 방안:**
```java
// TypeReference 지원
public <T> T get(String key, TypeReference<T> typeRef) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) {
        return null;
    }
    return objectMapper.convertValue(value, typeRef);
}

// 사용 예
List<CodeItemDTO> items = redisCacheService.get(
    cacheKey,
    new TypeReference<List<CodeItemDTO>>() {}
);
```

---

### 3.4 Batch 모듈 (스케줄링)

#### ✅ 정상 동작

**Quartz 기반 안정적인 스케줄링**
- CRON/INTERVAL 두 가지 스케줄 타입 지원
- JobDataMap으로 배치 정보 전달
- 애플리케이션 시작 시 자동 스케줄 등록

**재시도 로직 구현** (src/main/java/com/wan/framework/batch/service/BatchExecutionService.java:86-97)
```java
private void handleRetry(BatchExecution execution, BatchJob batchJob) {
    if (execution.getRetryCount() < batchJob.getMaxRetryCount()) {
        BatchExecution retryExecution = BatchExecution.builder()
            .batchJob(batchJob)
            .triggerType(TriggerType.RETRY)
            .originalExecutionId(execution.getId())
            .retryCount(execution.getRetryCount() + 1)
            .build();
        // 재시도 스케줄링
    }
}
```

**Proxy API 통합**
```java
ApiExecutionHistory history = apiExecutionService.execute(endpoint, request);
execution.setApiExecutionHistory(history);
```

#### ⚠️ 개선 필요

**1. 분산 락 미사용** (치명적)

현재 구현:
```java
// QuartzBatchJob.java:32
@Override
public void execute(JobExecutionContext context) throws JobExecutionException {
    // ❌ 분산 락 없이 바로 실행
    batchExecutionService.executeBatch(batchId);
}
```

**문제점:**
- 다중 서버 환경에서 동일 배치가 동시 실행 가능
- 데이터 중복 처리 또는 Race Condition 발생

**개선 방안:**
```java
@Override
public void execute(JobExecutionContext context) throws JobExecutionException {
    Long batchId = context.getJobDetail().getJobDataMap().getLong("batchId");
    String lockKey = "BATCH_LOCK:" + batchId;
    String lockValue = null;

    try {
        // 분산 락 획득 (최대 5분)
        lockValue = distributedLockService.acquireLock(lockKey, 300);
        if (lockValue == null) {
            log.warn("Another instance is running batch: {}", batchId);
            return;
        }

        // 배치 실행
        batchExecutionService.executeBatch(batchId);

    } catch (Exception e) {
        log.error("Batch execution failed: batchId={}", batchId, e);
        throw new JobExecutionException(e);
    } finally {
        if (lockValue != null) {
            distributedLockService.releaseLock(lockKey, lockValue);
        }
    }
}
```

**2. 타임아웃 처리 미흡**
```java
// BatchJob.java
@Column(nullable = false)
private Integer timeoutSeconds;  // 필드는 존재하나 실제 사용 안됨
```

**개선 방안:**
```java
// BatchExecutionService.java
@Transactional
public void executeBatch(Long batchId) {
    BatchJob batchJob = batchJobRepository.findById(batchId)
        .orElseThrow(() -> new BatchException(BATCH_NOT_FOUND));

    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        executeBatchInternal(batchJob);
    });

    try {
        future.get(batchJob.getTimeoutSeconds(), TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        future.cancel(true);
        updateExecutionStatus(batchJob, ExecutionStatus.TIMEOUT);
        throw new BatchException(EXECUTION_TIMEOUT);
    }
}
```

**3. 실행 중 여부 확인 불완전** (src/main/java/com/wan/framework/batch/service/BatchJobService.java:142-145)
```java
private boolean isJobRunning(String jobKey) {
    // TODO: 실제 Job 실행 여부 확인 로직 필요
    return false;  // ❌ 항상 false 반환
}
```

**개선 방안:**
```java
private boolean isJobRunning(String jobKey) {
    try {
        List<JobExecutionContext> runningJobs =
            scheduler.getCurrentlyExecutingJobs();

        return runningJobs.stream()
            .anyMatch(ctx -> ctx.getJobDetail().getKey().toString().equals(jobKey));
    } catch (SchedulerException e) {
        log.error("Failed to check job running status", e);
        return false;
    }
}
```

#### 🔴 심각한 문제

**스케줄링 실패 시 예외 처리 미흡** (src/main/java/com/wan/framework/batch/service/BatchSchedulerService.java:39-48)
```java
public void scheduleAllEnabledJobs(List<BatchJob> batchJobs) {
    for (BatchJob batchJob : batchJobs) {
        try {
            scheduleBatchJob(batchJob);
        } catch (Exception e) {
            log.error("Failed to schedule batch job: {}", batchJob.getId(), e);
            // ❌ 에러 로그만 남기고 계속 진행
        }
    }
}
```

**문제점:**
- 스케줄링 실패해도 DB에는 enabled=true로 남아있음
- 사용자는 정상 등록되었다고 생각하나 실제로는 실행 안됨
- 운영 중 배치 누락 발생 가능

**개선 방안:**
```java
@Transactional
public void scheduleAllEnabledJobs(List<BatchJob> batchJobs) {
    List<Long> failedJobIds = new ArrayList<>();

    for (BatchJob batchJob : batchJobs) {
        try {
            scheduleBatchJob(batchJob);
        } catch (Exception e) {
            log.error("Failed to schedule batch job: {}", batchJob.getId(), e);
            failedJobIds.add(batchJob.getId());

            // DB 상태를 실제와 동기화
            batchJob.setEnabled(false);
            batchJobRepository.save(batchJob);
        }
    }

    if (!failedJobIds.isEmpty()) {
        // 관리자에게 알림
        alertService.sendAlert(
            "Batch scheduling failed for jobs: " + failedJobIds
        );
    }
}
```

---

### 3.5 Proxy API 모듈 (동적 API)

#### ✅ 정상 동작

**템플릿 변수 치환** (src/main/java/com/wan/framework/proxy/service/ApiExecutionService.java:58-67)
```java
private String buildUrl(String urlTemplate, Map<String, Object> parameters) {
    String url = urlTemplate;
    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
        String placeholder = "${" + entry.getKey() + "}";
        url = url.replace(placeholder, String.valueOf(entry.getValue()));
    }
    return url;
}
```

**재시도 로직**
```java
private ResponseEntity<String> executeWithRetry(...) {
    int maxRetries = apiEndpoint.getMaxRetries();
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return restTemplate.exchange(url, httpMethod, entity, String.class);
        } catch (Exception e) {
            if (attempt == maxRetries) throw e;
            Thread.sleep(retryIntervalMs);
        }
    }
}
```

#### ⚠️ 개선 필요

**1. RestTemplate 타임아웃 미설정** (src/main/java/com/wan/framework/proxy/config/RestTemplateConfig.java:19-23)
```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(30))  // ⚠️ Deprecated
        .setReadTimeout(Duration.ofSeconds(30))     // ⚠️ Deprecated
        .build();
}
```

**문제점:**
- 사용된 메서드가 deprecated됨
- 외부 API 응답 없을 시 무한 대기 가능

**개선 방안:**
```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(5000);      // 5초
    factory.setReadTimeout(30000);        // 30초
    factory.setConnectionRequestTimeout(3000);  // 3초

    return builder
        .requestFactory(() -> factory)
        .build();
}
```

**2. 헤더 파싱 실패 시 무시** (src/main/java/com/wan/framework/proxy/service/ApiExecutionService.java:82-99)
```java
private HttpHeaders buildHeaders(String headerTemplate, Map<String, Object> parameters) {
    HttpHeaders headers = new HttpHeaders();

    if (headerTemplate != null && !headerTemplate.isEmpty()) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> headerMap = mapper.readValue(headerTemplate, Map.class);
            // ...
        } catch (Exception e) {
            log.error("Failed to parse header template: {}", e.getMessage());
            // ❌ 예외 발생하지 않고 빈 헤더 반환
        }
    }

    return headers;
}
```

**문제점:**
- 인증 헤더가 필수인 API에서 헤더 파싱 실패 시
- 인증 없이 요청이 전송되어 401 Unauthorized 발생
- 원인 파악이 어려움

**개선 방안:**
```java
private HttpHeaders buildHeaders(String headerTemplate, Map<String, Object> parameters) {
    HttpHeaders headers = new HttpHeaders();

    if (headerTemplate != null && !headerTemplate.isEmpty()) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> headerMap = mapper.readValue(headerTemplate, Map.class);

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                String value = replaceVariables(entry.getValue(), parameters);
                headers.add(entry.getKey(), value);
            }
        } catch (Exception e) {
            log.error("Failed to parse header template: {}", e.getMessage());
            throw new ProxyException(INVALID_HEADER_TEMPLATE, e);  // ✅ 예외 발생
        }
    }

    return headers;
}
```

**3. 응답 크기 제한 없음**
- 대용량 응답(수백 MB)도 전부 메모리에 로드
- OutOfMemoryError 발생 가능

**개선 방안:**
```java
// RestTemplate Interceptor로 응답 크기 제한
public class ResponseSizeLimitInterceptor implements ClientHttpRequestInterceptor {
    private static final long MAX_RESPONSE_SIZE = 10 * 1024 * 1024;  // 10MB

    @Override
    public ClientHttpResponse intercept(...) {
        ClientHttpResponse response = execution.execute(request, body);
        long contentLength = response.getHeaders().getContentLength();

        if (contentLength > MAX_RESPONSE_SIZE) {
            throw new ProxyException(RESPONSE_TOO_LARGE);
        }

        return response;
    }
}
```

---

### 3.6 Code 모듈 (공통코드)

#### ✅ 정상 동작

**Redis 캐시 통합** (src/main/java/com/wan/framework/code/service/CodeGroupService.java:50-60)
```java
@Transactional(readOnly = true)
public CodeGroupDTO getCodeGroup(String groupCode) {
    // 캐시 확인
    String cacheKey = CACHE_PREFIX + groupCode;
    CodeGroupDTO cached = redisCacheService.get(cacheKey, CodeGroupDTO.class);

    if (cached != null) {
        return cached;
    }

    // DB 조회 및 캐시 저장
    CodeGroup codeGroup = codeGroupRepository
        .findByGroupCodeAndDataStateNot(groupCode, D)
        .orElseThrow(() -> new CodeException(CODE_GROUP_NOT_FOUND));

    CodeGroupDTO dto = mapper.toDto(codeGroup);
    redisCacheService.set(cacheKey, dto, CACHE_TTL_SECONDS);
    return dto;
}
```

**계층 구조 (그룹/항목)**
```java
// CodeGroup: 그룹 코드 (예: USER_STATUS)
// CodeItem: 항목 코드 (예: ACTIVE, INACTIVE, SUSPENDED)
// 외래키로 연결되어 데이터 정합성 보장
```

#### ⚠️ 개선 필요

**1. 캐시 일관성 문제** (다중 서버 환경)
```java
// Server A에서 코드 수정
@Transactional
public CodeGroupDTO updateCodeGroup(CodeGroupDTO dto) {
    // DB 업데이트
    codeGroup = mapper.toEntity(dto);
    codeGroupRepository.save(codeGroup);

    // Server A의 캐시만 무효화
    redisCacheService.delete(CACHE_PREFIX + dto.getGroupCode());

    // ❌ Server B, C의 캐시는 여전히 이전 데이터 유지
}
```

**개선 방안 (Redis Pub/Sub):**
```java
// CodeCacheSyncService.java (신규)
@Service
public class CodeCacheSyncService {

    @Autowired
    private RedisMessageListenerContainer listenerContainer;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_INVALIDATE_CHANNEL = "code:cache:invalidate";

    @PostConstruct
    public void init() {
        listenerContainer.addMessageListener(
            (message, pattern) -> {
                String groupCode = new String(message.getBody());
                redisCacheService.delete(CACHE_PREFIX + groupCode);
                log.debug("Cache invalidated for group: {}", groupCode);
            },
            new PatternTopic(CACHE_INVALIDATE_CHANNEL)
        );
    }

    public void invalidateCacheOnAllServers(String groupCode) {
        // 모든 서버에 캐시 무효화 메시지 발송
        redisTemplate.convertAndSend(CACHE_INVALIDATE_CHANNEL, groupCode);
    }
}

// CodeGroupService.java
@Transactional
public CodeGroupDTO updateCodeGroup(CodeGroupDTO dto) {
    // DB 업데이트
    codeGroup = mapper.toEntity(dto);
    codeGroupRepository.save(codeGroup);

    // 모든 서버의 캐시 무효화
    codeCacheSyncService.invalidateCacheOnAllServers(dto.getGroupCode());

    return mapper.toDto(codeGroup);
}
```

**2. sortOrder 필드 미사용**
```java
// CodeGroup.java, CodeItem.java
@Column
private Integer sortOrder;  // 필드는 존재하나 정렬에 사용 안됨
```

**개선 방안:**
```java
// CodeItemService.java
@Transactional(readOnly = true)
public List<CodeItemDTO> getCodeItemsByGroup(String groupCode) {
    // sortOrder, createdAt 순으로 정렬
    return codeItemRepository
        .findByGroupCodeAndDataStateNot(
            groupCode,
            D,
            Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("createdAt"))
        )
        .stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
}
```

**3. 캐시 갱신 실패 시 처리 미흡**
```java
// CodeGroupService.java:113
public void refreshAllCache() {
    List<CodeGroup> allGroups = codeGroupRepository.findByDataStateNot(D);
    for (CodeGroup group : allGroups) {
        // ❌ 중간에 실패하면 어떤 그룹이 실패했는지 알 수 없음
        cacheCodeGroup(mapper.toDto(group));
    }
}
```

**개선 방안:**
```java
public Map<String, Boolean> refreshAllCache() {
    List<CodeGroup> allGroups = codeGroupRepository.findByDataStateNot(D);
    Map<String, Boolean> results = new HashMap<>();

    for (CodeGroup group : allGroups) {
        try {
            cacheCodeGroup(mapper.toDto(group));
            results.put(group.getGroupCode(), true);
        } catch (Exception e) {
            log.error("Failed to cache group: {}", group.getGroupCode(), e);
            results.put(group.getGroupCode(), false);
        }
    }

    return results;  // 성공/실패 현황 반환
}
```

---

### 3.7 Board 모듈 (동적 게시판)

#### ✅ 정상 동작

**동적 게시판 생성**
- BoardMeta로 게시판 메타데이터 관리
- 코드 수정 없이 게시판 추가 가능

**이전글/다음글 자동 조회** (src/main/java/com/wan/framework/board/service/BoardDataService.java:59-75)
```java
private void setPrevNextPost(BoardDataDTO dto) {
    // 이전글 조회
    boardDataRepository.findTopByIdLessThanAndBoardMetaId(
        dto.getId(), dto.getBoardMetaId(), Sort.by("id").descending()
    ).ifPresent(prev -> {
        dto.setPrevId(prev.getId());
        dto.setPrevTitle(prev.getTitle());
    });

    // 다음글 조회
    // ...
}
```

**댓글 계층 구조**
```java
// BoardComment.java
@ManyToOne
@JoinColumn(name = "parent_id")
private BoardComment parent;  // 대댓글 지원

@OneToMany(mappedBy = "parent")
private List<BoardComment> replies;
```

#### ⚠️ 개선 필요

**1. 권한 검증 불완전** (src/main/java/com/wan/framework/board/service/BoardDataService.java:93-96)
```java
@Transactional
public BoardDataDTO updatePost(Long id, BoardDataDTO dto, String userId) {
    BoardData boardData = boardDataRepository.findById(id)
        .orElseThrow(() -> new BoardException(POST_NOT_FOUND));

    if (!boardData.getCreatedBy().equals(userId)) {
        throw new BoardException(UNAUTHORIZED);  // ❌ 관리자 권한 무시
    }
    // ...
}
```

**개선 방안:**
```java
@Transactional
public BoardDataDTO updatePost(Long id, BoardDataDTO dto, String userId, List<String> roles) {
    BoardData boardData = boardDataRepository.findById(id)
        .orElseThrow(() -> new BoardException(POST_NOT_FOUND));

    // 작성자 또는 관리자만 수정 가능
    boolean isAuthor = boardData.getCreatedBy().equals(userId);
    boolean isAdmin = roles.contains("ROLE_ADMIN");

    if (!isAuthor && !isAdmin) {
        throw new BoardException(UNAUTHORIZED);
    }
    // ...
}
```

**2. N+1 쿼리 문제**
```java
// BoardDataController.java
@GetMapping("/{boardMetaId}/posts")
public Page<BoardDataDTO> getPosts(...) {
    return boardDataService.getPosts(boardMetaId, pageable);
}

// 목록 조회 시 각 게시글마다:
// - BoardMeta 조회 (지연 로딩)
// - BoardAttachment 개수 확인 (지연 로딩)
// → N+1 쿼리 발생
```

**개선 방안:**
```java
// BoardDataRepository.java
@EntityGraph(attributePaths = {"boardMeta", "attachments"})
@Query("SELECT bd FROM BoardData bd WHERE bd.boardMeta.id = :boardMetaId")
Page<BoardData> findByBoardMetaIdWithFetch(
    @Param("boardMetaId") Long boardMetaId,
    Pageable pageable
);

// 또는 Fetch Join
@Query("SELECT DISTINCT bd FROM BoardData bd " +
       "LEFT JOIN FETCH bd.boardMeta " +
       "LEFT JOIN FETCH bd.attachments " +
       "WHERE bd.boardMeta.id = :boardMetaId")
List<BoardData> findByBoardMetaIdWithFetchJoin(@Param("boardMetaId") Long boardMetaId);
```

#### 🔴 심각한 문제

**파일 다운로드 경로 조작 취약점** (src/main/java/com/wan/framework/board/service/BoardAttachmentService.java:82-88)
```java
public byte[] downloadFile(String storedFileName) throws IOException {
    Path filePath = Paths.get(uploadDir, storedFileName);
    // ❌ 경로 검증 없음

    if (!Files.exists(filePath)) {
        throw new BoardException(FILE_NOT_FOUND);
    }

    return Files.readAllBytes(filePath);
}
```

**공격 시나리오:**
```http
GET /board-attachments/download?fileName=../../../../etc/passwd
```

**개선 방안:**
```java
public byte[] downloadFile(String storedFileName) throws IOException {
    // 1. 파일명 검증
    if (storedFileName == null || storedFileName.isEmpty()) {
        throw new BoardException(INVALID_FILE_NAME);
    }

    // 2. 경로 조작 문자 검사
    if (storedFileName.contains("..") ||
        storedFileName.contains("/") ||
        storedFileName.contains("\\")) {
        throw new SecurityException("Path traversal attempt detected");
    }

    // 3. 경로 정규화 및 검증
    Path filePath = Paths.get(uploadDir, storedFileName).normalize();
    Path uploadPath = Paths.get(uploadDir).normalize();

    if (!filePath.startsWith(uploadPath)) {
        throw new SecurityException("File access denied");
    }

    // 4. 파일 존재 확인
    if (!Files.exists(filePath)) {
        throw new BoardException(FILE_NOT_FOUND);
    }

    // 5. 심볼릭 링크 검사
    if (Files.isSymbolicLink(filePath)) {
        throw new SecurityException("Symbolic link access denied");
    }

    return Files.readAllBytes(filePath);
}
```

**MIME Type 검증 미흡**
```java
// BoardAttachmentService.java:42
public BoardAttachmentDTO uploadFile(MultipartFile file, Long boardDataId, String uploadedBy) {
    // 확장자만 검증
    String extension = getFileExtension(originalFileName);
    if (!fileProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
        throw new BoardException(INVALID_FILE_EXTENSION);
    }

    // ❌ 실제 파일 내용 검증 없음
    // 악성 파일을 .jpg로 위장하여 업로드 가능
}
```

**개선 방안:**
```java
public BoardAttachmentDTO uploadFile(MultipartFile file, Long boardDataId, String uploadedBy) {
    String originalFileName = file.getOriginalFilename();
    String extension = getFileExtension(originalFileName);

    // 1. 확장자 검증
    if (!fileProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
        throw new BoardException(INVALID_FILE_EXTENSION);
    }

    // 2. MIME Type 검증
    String contentType = file.getContentType();
    if (!isAllowedMimeType(contentType)) {
        throw new BoardException(INVALID_FILE_TYPE);
    }

    // 3. 실제 파일 내용 검증
    try {
        Path tempFile = Files.createTempFile("upload-", extension);
        file.transferTo(tempFile);

        String detectedType = Files.probeContentType(tempFile);
        if (detectedType != null && !detectedType.equals(contentType)) {
            Files.delete(tempFile);
            throw new BoardException(FILE_TYPE_MISMATCH);
        }

        // 4. 파일 크기 검증
        if (file.getSize() > fileProperties.getMaxFileSize()) {
            Files.delete(tempFile);
            throw new BoardException(FILE_TOO_LARGE);
        }

        // 5. 안전한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + "." + extension;
        Path targetPath = Paths.get(uploadDir, storedFileName);
        Files.move(tempFile, targetPath);

        // ...
    } catch (IOException e) {
        throw new BoardException(FILE_UPLOAD_FAILED, e);
    }
}
```

---

### 3.8 API Key 모듈 (API 인증)

#### ✅ 정상 동작

**Bearer Token 인증**
```java
// BearerAuthenticationInterceptor.java
String authHeader = request.getHeader("Authorization");
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String rawApiKey = authHeader.substring(7);
    ApiKeyDTO apiKey = apiKeyService.validateApiKey(rawApiKey);
    // ...
}
```

**API Key 생성 (Prefix + Hash)**
```java
// ApiKeyService.java
private String generateApiKey() {
    String randomPart = UUID.randomUUID().toString().replace("-", "");
    return API_KEY_PREFIX + randomPart;
}
```

**사용 이력 기록**
```java
// ApiKeyUsageService.java
@Transactional
public void recordUsage(Long apiKeyId, HttpServletRequest request) {
    ApiKeyUsage usage = ApiKeyUsage.builder()
        .apiKey(apiKey)
        .requestUri(request.getRequestURI())
        .requestMethod(request.getMethod())
        .ipAddress(request.getRemoteAddr())
        .userAgent(request.getHeader("User-Agent"))
        .build();
    apiKeyUsageRepository.save(usage);
}
```

#### ⚠️ 개선 필요

**1. Rate Limiting 없음**
- API Key별 요청 제한 없음
- 무제한 요청으로 서버 과부하 가능

**개선 방안 (Redis 기반 Sliding Window):**
```java
// RateLimitService.java (신규)
@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final int WINDOW_SIZE_SECONDS = 60;
    private static final int MAX_REQUESTS = 100;

    public void checkRateLimit(Long apiKeyId) {
        String key = "RATE_LIMIT:API_KEY:" + apiKeyId;
        long now = System.currentTimeMillis();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);

        // 윈도우 밖의 요청 삭제
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 현재 윈도우 내 요청 수 확인
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= MAX_REQUESTS) {
            throw new ApiKeyException(RATE_LIMIT_EXCEEDED);
        }

        // 현재 요청 추가
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, WINDOW_SIZE_SECONDS, TimeUnit.SECONDS);
    }
}

// BearerAuthenticationInterceptor.java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    ApiKeyDTO apiKey = apiKeyService.validateApiKey(rawApiKey);

    // Rate Limit 확인
    rateLimitService.checkRateLimit(apiKey.getId());

    // ...
}
```

**2. 권한 검증 미사용**
```java
// ApiKeyPermission 엔티티는 존재하나 실제 검증 로직 없음
@Entity
@Table(name = "t_api_key_permission")
public class ApiKeyPermission {
    private String permission;  // 예: /api/users:READ
    // ...
}
```

**개선 방안:**
```java
// BearerAuthenticationInterceptor.java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    ApiKeyDTO apiKey = apiKeyService.validateApiKey(rawApiKey);

    // 권한 검증
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();

    if (!hasPermission(apiKey, requestUri, requestMethod)) {
        throw new ApiKeyException(PERMISSION_DENIED);
    }

    // ...
}

private boolean hasPermission(ApiKeyDTO apiKey, String uri, String method) {
    return apiKey.getPermissions().stream()
        .anyMatch(p -> {
            String[] parts = p.getPermission().split(":");
            String resourcePattern = parts[0];
            String allowedMethod = parts.length > 1 ? parts[1] : "ALL";

            boolean uriMatches = uri.matches(resourcePattern);
            boolean methodMatches = allowedMethod.equals("ALL") ||
                                   allowedMethod.equals(method);

            return uriMatches && methodMatches;
        });
}
```

**3. 만료일 관리 없음**
```java
// ApiKey.java
// expiresAt 필드가 없음
// 유출 시 영구적 피해
```

**개선 방안:**
```java
// ApiKey.java
@Column
private LocalDateTime expiresAt;

// ApiKeyService.java
@Transactional(readOnly = true)
public ApiKeyDTO validateApiKey(String rawApiKey) {
    String hashedKey = hashApiKey(rawApiKey);
    ApiKey apiKey = apiKeyRepository.findByHashedKeyAndIsEnabled(hashedKey, true)
        .orElseThrow(() -> new ApiKeyException(INVALID_API_KEY));

    // 만료일 확인
    if (apiKey.getExpiresAt() != null &&
        apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new ApiKeyException(API_KEY_EXPIRED);
    }

    return mapper.toDto(apiKey);
}
```

---

## 4. 보안 취약점 분석

### 4.1 심각도 분류

#### 🔴 긴급 (Critical) - 즉시 수정 필요

**1. Spring Security 완전 비활성화**
- **위치**: `src/main/java/com/wan/framework/base/FrameworkSecurityConfig.java:18-23`
- **위험도**: 10/10
- **영향**: 전체 시스템
- **설명**: 모든 API가 인증 없이 접근 가능, CSRF 보호 완전 해제
- **조치**: Spring Security 활성화, 역할 기반 접근 제어 구현

**2. 파일 다운로드 경로 조작 (Path Traversal)**
- **위치**: `src/main/java/com/wan/framework/board/service/BoardAttachmentService.java:82-88`
- **위험도**: 9/10
- **영향**: Board 모듈
- **설명**: `../../../etc/passwd` 같은 경로로 시스템 파일 접근 가능
- **조치**: 경로 정규화 및 검증 추가

**3. MIME Type 검증 미흡**
- **위치**: `src/main/java/com/wan/framework/board/service/BoardAttachmentService.java:42-55`
- **위험도**: 8/10
- **영향**: Board 모듈
- **설명**: 악성 파일을 이미지 확장자로 위장하여 업로드 가능
- **조치**: 파일 내용 기반 검증 추가

#### ⚠️ 높음 (High) - 단기 개선 필요

**4. Batch 분산 락 미사용**
- **위치**: `src/main/java/com/wan/framework/batch/job/QuartzBatchJob.java:32`
- **위험도**: 7/10
- **영향**: Batch 모듈
- **설명**: 다중 서버 환경에서 배치 중복 실행 가능
- **조치**: Redis 분산 락 적용

**5. Rate Limiting 없음**
- **위치**: 전체 API
- **위험도**: 7/10
- **영향**: 전체 시스템
- **설명**: DoS 공격에 취약, 무제한 요청 가능
- **조치**: Redis 기반 Rate Limiter 구현

**6. SQL Injection 위험 (일부)**
- **위치**: Native Query 사용 지점
- **위험도**: 6/10
- **영향**: 해당 Repository
- **설명**: 파라미터 바인딩 미흡 시 SQL Injection 가능
- **조치**: PreparedStatement 또는 JPQL 사용

#### 💡 중간 (Medium) - 중기 개선 권장

**7. 세션 고정 공격 일부 방어**
- **위치**: `src/main/java/com/wan/framework/session/service/SessionService.java:54-59`
- **위험도**: 5/10
- **영향**: Session 모듈
- **설명**: 세션 무효화는 하나 세션 ID 재생성 명시적 확인 필요
- **조치**: Spring Security Session Management 활성화

**8. API Key 만료 없음**
- **위치**: `src/main/java/com/wan/framework/apikey/domain/ApiKey.java`
- **위험도**: 5/10
- **영향**: API Key 모듈
- **설명**: API Key 유출 시 영구적 피해
- **조치**: expiresAt 필드 추가 및 검증

**9. 민감 정보 로깅**
- **위치**: 여러 Controller
- **위험도**: 4/10
- **영향**: 전체 시스템
- **설명**: 비밀번호, 개인정보 로그 출력 가능성
- **조치**: 민감 정보 마스킹 처리

### 4.2 OWASP Top 10 체크리스트

| OWASP 항목 | 상태 | 비고 |
|-----------|------|------|
| A01: 접근 제어 실패 | 🔴 실패 | Spring Security 비활성화 |
| A02: 암호화 실패 | ✅ 통과 | PBKDF2 사용, Salt 개별 생성 |
| A03: 인젝션 | ⚠️ 주의 | JPA 사용으로 대부분 안전 |
| A04: 안전하지 않은 설계 | ⚠️ 주의 | Rate Limiting 미구현 |
| A05: 보안 설정 오류 | 🔴 실패 | CSRF 해제, permitAll |
| A06: 취약한 구성 요소 | ✅ 통과 | 최신 Spring Boot 사용 |
| A07: 식별 및 인증 실패 | ⚠️ 주의 | 세션 관리는 양호 |
| A08: 소프트웨어 무결성 실패 | ✅ 통과 | 의존성 관리 양호 |
| A09: 로깅 및 모니터링 실패 | ⚠️ 주의 | 로깅은 있으나 모니터링 미구현 |
| A10: 서버측 요청 위조 | ⚠️ 주의 | Proxy API에서 URL 검증 필요 |

---

## 5. 성능 최적화 권장사항

### 5.1 데이터베이스 최적화

#### 1. N+1 쿼리 해결

**문제 지점:**
```java
// BoardDataController.java
@GetMapping("/{boardMetaId}/posts")
public Page<BoardDataDTO> getPosts(...) {
    // 1. 게시글 목록 조회 (1 쿼리)
    // 2. 각 게시글마다 BoardMeta 조회 (N 쿼리)
    // 3. 각 게시글마다 첨부파일 개수 확인 (N 쿼리)
    // → 총 1 + 2N 쿼리 발생
}
```

**해결 방안:**
```java
// BoardDataRepository.java
@EntityGraph(attributePaths = {"boardMeta", "attachments"})
Page<BoardData> findByBoardMetaId(Long boardMetaId, Pageable pageable);

// 또는 Fetch Join
@Query("SELECT DISTINCT bd FROM BoardData bd " +
       "LEFT JOIN FETCH bd.boardMeta " +
       "LEFT JOIN FETCH bd.attachments " +
       "WHERE bd.boardMeta.id = :boardMetaId")
List<BoardData> findByBoardMetaIdWithFetch(@Param("boardMetaId") Long boardMetaId);
```

**예상 성능 개선:**
- 게시글 100개 조회 시: 201 쿼리 → 1 쿼리 (99.5% 감소)

#### 2. 인덱스 추가

**현재 상태:**
- 대부분의 테이블에 명시적 인덱스 없음
- 기본키 인덱스만 의존

**권장 인덱스:**
```sql
-- User 테이블
CREATE INDEX idx_user_data_state ON t_user(data_state);
CREATE INDEX idx_user_created_at ON t_user(created_at);

-- BoardData 테이블
CREATE INDEX idx_board_data_meta_id ON t_board_data(board_meta_id);
CREATE INDEX idx_board_data_created_by ON t_board_data(created_by);
CREATE INDEX idx_board_data_created_at ON t_board_data(created_at);
CREATE INDEX idx_board_data_status ON t_board_data(data_state);

-- CodeItem 테이블
CREATE INDEX idx_code_item_group ON t_code_item(group_code, data_state);
CREATE INDEX idx_code_item_enabled ON t_code_item(enabled, sort_order);

-- SessionAudit 테이블
CREATE INDEX idx_session_audit_user ON t_session_audit(user_id, event_time);
CREATE INDEX idx_session_audit_event ON t_session_audit(event_type, event_time);

-- ApiKeyUsage 테이블
CREATE INDEX idx_api_key_usage_key ON t_api_key_usage(api_key_id, created_at);
CREATE INDEX idx_api_key_usage_ip ON t_api_key_usage(ip_address, created_at);

-- BatchExecution 테이블
CREATE INDEX idx_batch_exec_job ON t_batch_execution(batch_job_id, created_at);
CREATE INDEX idx_batch_exec_status ON t_batch_execution(status, created_at);
```

**예상 성능 개선:**
- 목록 조회 속도: 50-80% 개선
- 통계 쿼리 속도: 90% 이상 개선

#### 3. 쿼리 최적화

**문제 코드:**
```java
// CodeGroupService.java:113
public void refreshAllCache() {
    List<CodeGroup> allGroups = codeGroupRepository.findByDataStateNot(D);
    for (CodeGroup group : allGroups) {
        List<CodeItem> items = codeItemRepository
            .findByGroupCodeAndDataStateNot(group.getGroupCode(), D);
        // 각 그룹마다 추가 쿼리 발생
    }
}
```

**개선 방안:**
```java
// CodeGroupRepository.java
@Query("SELECT cg FROM CodeGroup cg " +
       "LEFT JOIN FETCH cg.codeItems ci " +
       "WHERE cg.dataState <> :dataState " +
       "AND (ci.dataState <> :dataState OR ci.dataState IS NULL)")
List<CodeGroup> findAllWithItemsByDataStateNot(@Param("dataState") DataStateCode dataState);

// CodeGroupService.java
public void refreshAllCache() {
    List<CodeGroup> allGroups = codeGroupRepository
        .findAllWithItemsByDataStateNot(D);  // 단일 쿼리로 모든 데이터 조회

    for (CodeGroup group : allGroups) {
        cacheCodeGroup(mapper.toDto(group));
        cacheCodeItems(group.getGroupCode(), group.getCodeItems());
    }
}
```

### 5.2 캐싱 전략 개선

#### 1. 다층 캐시 구조

**현재**: Redis만 사용 (네트워크 오버헤드)

**개선 방안** (Local Cache + Redis):
```java
// CaffeineCacheConfig.java (신규)
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats());
        return cacheManager;
    }
}

// CodeGroupService.java
@Cacheable(value = "codeGroups", key = "#groupCode")
public CodeGroupDTO getCodeGroup(String groupCode) {
    // 1차: Local Cache (Caffeine) - 수십 나노초
    // 2차: Redis Cache - 수 밀리초
    // 3차: DB 조회 - 수십 밀리초
}
```

**예상 성능 개선:**
- 캐시 히트 시 응답 속도: 1-2ms → 0.1ms 이하
- Redis 네트워크 트래픽: 70% 감소

#### 2. 캐시 Warm-up

```java
// CacheWarmupService.java (신규)
@Service
public class CacheWarmupService {

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Cache warmup started");

        // 공통코드 전체 캐싱
        codeGroupService.refreshAllCache();

        // 자주 사용되는 데이터 미리 로드
        userService.cacheActiveUsers();
        boardMetaService.cacheAllBoardMetas();

        log.info("Cache warmup completed");
    }
}
```

### 5.3 커넥션 풀 최적화

**현재 설정:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

**권장 설정:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # CPU 코어 수 * 2 ~ 4
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000  # 커넥션 누수 감지

      # 성능 튜닝
      auto-commit: false  # 트랜잭션 명시적 관리
      connection-test-query: SELECT 1
      validation-timeout: 5000
```

### 5.4 비동기 처리

**개선 대상:**
```java
// SessionAuditService.java
@Transactional
public void saveAuditLog(SessionAudit audit) {
    sessionAuditRepository.save(audit);  // 동기 처리로 응답 지연
}
```

**비동기 개선:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

// SessionAuditService.java
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
public CompletableFuture<Void> saveAuditLogAsync(SessionAudit audit) {
    sessionAuditRepository.save(audit);
    return CompletableFuture.completedFuture(null);
}
```

**예상 성능 개선:**
- 로그인 API 응답 시간: 100ms → 50ms (50% 개선)

---

## 6. 아키텍처 분석

### 6.1 결합도와 응집도

#### ✅ 잘 설계된 부분

**1. 계층 분리**
```
Controller (Web Layer)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
```
- 각 계층의 책임이 명확
- 의존성 방향이 단방향 (Controller → Service → Repository)

**2. 모듈 분리**
```
com.wan.framework
├── user        (독립적)
├── session     (user 의존)
├── redis       (독립적)
├── batch       (redis, proxy 의존)
├── proxy       (독립적)
├── code        (redis 의존)
├── board       (독립적)
└── apikey      (독립적)
```

**3. DTO ↔ Entity 변환**
- MapStruct 사용으로 변환 로직 분리
- Entity가 외부로 노출되지 않음

#### ⚠️ 개선 필요

**1. Controller의 다중 Service 의존**
```java
// SessionController.java
public class SessionController {
    private final SessionService sessionService;
    private final SignService signService;  // 2개 Service 의존
}
```

**개선 방안 (Facade 패턴):**
```java
// AuthenticationFacade.java (신규)
@Service
public class AuthenticationFacade {
    private final SessionService sessionService;
    private final SignService signService;

    @Transactional
    public SessionDTO login(LoginRequest request,
                           HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) {
        // 사용자 인증
        UserDTO loginUser = UserDTO.builder()
            .userId(request.getUserId())
            .password(request.getPassword())
            .build();
        UserDTO user = signService.signIn(loginUser);

        // 세션 생성
        return sessionService.createSession(
            httpRequest, httpResponse,
            user.getUserId(), user.getName(), user.getRoles()
        );
    }
}

// SessionController.java
public class SessionController {
    private final AuthenticationFacade authFacade;  // 단일 의존

    @PostMapping("/login")
    public ResponseEntity<SessionDTO> login(...) {
        SessionDTO session = authFacade.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(session);
    }
}
```

**2. 빈 껍데기 클래스**
```java
// FrameworkInterceptor.java
@Component
public class FrameworkInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(...) {
        // 아무 작업도 안함
        return true;
    }
}
```

**조치**: 삭제 권장

### 6.2 모듈 간 의존성 그래프

```
       ┌─────────┐
       │  User   │
       └────┬────┘
            │
       ┌────▼────┐
       │ Session │
       └────┬────┘
            │
       ┌────▼────┐     ┌─────────┐
       │  Redis  │◄────┤  Code   │
       └────┬────┘     └─────────┘
            │
       ┌────▼────┐     ┌─────────┐
       │  Batch  │◄────┤  Proxy  │
       └─────────┘     └─────────┘

    독립 모듈: Board, API Key
```

**분석:**
- ✅ 순환 의존성 없음
- ✅ Redis가 공통 모듈로 적절히 활용됨
- ⚠️ User와 Session이 강하게 결합 (역할 정보 동기화 이슈)

### 6.3 설계 원칙 준수

| 원칙 | 평가 | 비고 |
|------|------|------|
| 단일 책임 (SRP) | ✅ 양호 | Service 분리 잘됨 |
| 개방-폐쇄 (OCP) | ✅ 우수 | 동적 게시판, Proxy API |
| 리스코프 치환 (LSP) | ✅ 통과 | 상속 거의 미사용 |
| 인터페이스 분리 (ISP) | ✅ 양호 | Repository 인터페이스 활용 |
| 의존성 역전 (DIP) | ✅ 우수 | 인터페이스 의존 |

---

## 7. 개선 우선순위

### 7.1 즉시 수정 (P0 - 1주 내)

**프로덕션 배포 차단 요소**

1. **Spring Security 활성화**
   - 작업량: 중 (3일)
   - 영향도: 전체 시스템
   - 담당: 백엔드 개발자
   ```java
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) {
       http
           .csrf(csrf -> csrf
               .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/sessions/login", "/users/sign-up").permitAll()
               .requestMatchers("/admin/**").hasRole("ADMIN")
               .anyRequest().authenticated())
           .sessionManagement(session -> session
               .sessionFixation().changeSessionId()
               .maximumSessions(3)
               .maxSessionsPreventsLogin(false));
       return http.build();
   }
   ```

2. **파일 업로드 보안 강화**
   - 작업량: 소 (1일)
   - 영향도: Board 모듈
   - 담당: 백엔드 개발자
   - 조치 사항:
     - 경로 조작 방지 로직 추가
     - MIME Type 검증 강화
     - 파일 크기 제한 적용

3. **Batch 분산 락 적용**
   - 작업량: 소 (1일)
   - 영향도: Batch 모듈
   - 담당: 백엔드 개발자
   - 조치 사항:
     - QuartzBatchJob에 Redis 분산 락 추가
     - 락 획득 실패 시 로깅

### 7.2 단기 개선 (P1 - 1개월 내)

**보안 및 성능 개선**

4. **Rate Limiting 구현**
   - 작업량: 중 (3일)
   - 영향도: 전체 API
   - 담당: 백엔드 개발자
   - 구현 방식: Redis Sliding Window

5. **N+1 쿼리 해결**
   - 작업량: 중 (3일)
   - 영향도: Board, Code 모듈
   - 담당: 백엔드 개발자
   - 조치 사항:
     - @EntityGraph 또는 Fetch Join 적용
     - 쿼리 로그 분석 및 최적화

6. **인덱스 추가**
   - 작업량: 소 (1일)
   - 영향도: 전체 DB
   - 담당: DBA 또는 백엔드 개발자
   - 조치 사항:
     - 자주 조회되는 컬럼에 인덱스 추가
     - 실행 계획 분석 및 검증

7. **API Key 권한 관리 구현**
   - 작업량: 중 (2일)
   - 영향도: API Key 모듈
   - 담당: 백엔드 개발자
   - 조치 사항:
     - ApiKeyPermission 검증 로직 구현
     - 권한 기반 접근 제어

8. **세션 동시 로그인 제한**
   - 작업량: 소 (1일)
   - 영향도: Session 모듈
   - 담당: 백엔드 개발자
   - 조치 사항:
     - maxSessionsPerUser 설정 추가
     - 최대 세션 수 초과 시 가장 오래된 세션 종료

### 7.3 중기 개선 (P2 - 3개월 내)

**운영 및 모니터링**

9. **모니터링 구축**
   - 작업량: 대 (5일)
   - 담당: DevOps, 백엔드 개발자
   - 구현 내용:
     - Spring Boot Actuator 활성화
     - Prometheus + Grafana 연동
     - 메트릭 수집 및 대시보드 구성

10. **CI/CD 파이프라인**
    - 작업량: 대 (7일)
    - 담당: DevOps
    - 구현 내용:
      - GitHub Actions 또는 Jenkins 설정
      - 자동 빌드, 테스트, 배포
      - Blue-Green 배포 전략

11. **API 버전 관리**
    - 작업량: 중 (3일)
    - 담당: 백엔드 개발자
    - 구현 내용:
      - /api/v1, /api/v2 경로 구조
      - 버전별 Controller 분리
      - Swagger UI에 버전 표시

12. **로그 수집 시스템**
    - 작업량: 대 (5일)
    - 담당: DevOps, 백엔드 개발자
    - 구현 내용:
      - ELK Stack (Elasticsearch, Logstash, Kibana)
      - 구조화된 로깅 (JSON)
      - 로그 레벨별 분류

### 7.4 장기 개선 (P3 - 6개월 내)

**아키텍처 고도화**

13. **마이크로서비스 전환 검토**
    - 작업량: 초대 (30일+)
    - 담당: 아키텍트, 전체 팀
    - 검토 사항:
      - 모듈별 독립 배포 필요성
      - 데이터베이스 분리 전략
      - API Gateway 도입

14. **이벤트 기반 아키텍처**
    - 작업량: 대 (10일)
    - 담당: 백엔드 개발자
    - 구현 내용:
      - Kafka 또는 RabbitMQ 도입
      - 비동기 이벤트 처리
      - CQRS 패턴 적용

15. **성능 테스트 자동화**
    - 작업량: 중 (5일)
    - 담당: QA, 백엔드 개발자
    - 구현 내용:
      - JMeter/Gatling 스크립트 작성
      - CI/CD에 통합
      - 성능 기준선 설정

---

## 8. 결론 및 권장사항

### 8.1 종합 평가

본 프로젝트는 **엔터프라이즈급 백엔드 플랫폼의 견고한 기반**을 갖추고 있습니다. 모듈화, 계층 분리, 캐싱 전략, 분산 시스템 지원 등 핵심 아키텍처 설계가 우수하며, 동적 구성 지원으로 **재사용성과 확장성이 매우 뛰어납니다**.

그러나 **Spring Security가 완전히 비활성화된 상태**로, 현재는 **프로덕션 배포가 불가능**합니다. 보안 설정을 최우선으로 개선하고, 파일 업로드 보안 취약점, 배치 분산 락, N+1 쿼리 등의 이슈를 해결하면 **즉시 상용 서비스에 투입 가능한 수준**이 될 것입니다.

### 8.2 강점 요약

1. **모듈화 우수**: 8개 모듈이 명확히 분리, 단일 책임 원칙 준수
2. **동적 구성**: Proxy API, 동적 게시판으로 코드 수정 없이 기능 확장
3. **캐싱 전략**: Redis 기반 효율적인 캐시 관리
4. **분산 시스템 대비**: Redis 분산 락, Spring Session
5. **보안 기능**: PBKDF2 비밀번호 해싱, 세션 보안 검증

### 8.3 주요 개선 과제

#### 즉시 수정 (P0)
- 🔴 Spring Security 활성화
- 🔴 파일 업로드 경로 조작 방지
- 🔴 Batch 분산 락 적용

#### 단기 개선 (P1)
- ⚠️ Rate Limiting 구현
- ⚠️ N+1 쿼리 해결
- ⚠️ 인덱스 추가
- ⚠️ API Key 권한 관리

#### 중기 개선 (P2)
- 💡 모니터링 구축
- 💡 CI/CD 파이프라인
- 💡 API 버전 관리

### 8.4 최종 권장사항

**1. 보안 최우선**
- Spring Security 활성화가 가장 시급
- 파일 업로드 보안 취약점 즉시 수정
- 프로덕션 배포 전 보안 점검 필수

**2. 성능 최적화**
- N+1 쿼리 해결로 응답 속도 대폭 개선
- 인덱스 추가로 DB 쿼리 성능 향상
- 비동기 처리로 사용자 체감 속도 개선

**3. 운영 준비**
- 모니터링 시스템 구축 (Actuator + Prometheus)
- CI/CD 파이프라인으로 배포 자동화
- 로그 수집 시스템으로 장애 추적

**4. 단계적 개선**
- P0 작업 완료 → 프로덕션 배포
- P1 작업 진행하며 초기 운영
- P2/P3는 서비스 성장에 따라 단계적 적용

### 8.5 적용 대상

본 플랫폼은 다음 환경에 적합합니다:

✅ **적합한 환경**
- 중소규모 웹 애플리케이션 (일 사용자 수 10만 이하)
- 동적 구성이 필요한 멀티 테넌트 시스템
- 마이크로서비스 전환 전 모놀리식 단계
- 내부 관리 시스템, 백오피스

⚠️ **추가 작업 필요**
- 대규모 트래픽 (일 사용자 수 100만 이상)
  → 마이크로서비스 전환, 샤딩, CQRS 검토
- 금융/의료 등 고도의 보안 요구
  → 추가 보안 감사, PCI-DSS/HIPAA 준수
- 글로벌 서비스
  → CDN, 다국어, 타임존 처리 추가

### 8.6 향후 로드맵

**Phase 1 (1개월)**: 보안 강화 및 성능 최적화
- Spring Security 활성화
- 파일 업로드 보안
- N+1 쿼리 해결
- Rate Limiting

**Phase 2 (3개월)**: 운영 안정화
- 모니터링 시스템
- CI/CD 파이프라인
- 로그 수집
- API 버전 관리

**Phase 3 (6개월)**: 고도화
- 성능 테스트 자동화
- 이벤트 기반 아키텍처
- 마이크로서비스 전환 검토

---

**리뷰 완료**

본 리뷰 문서는 프로젝트의 현재 상태를 객관적으로 평가하고, 실행 가능한 개선 방안을 제시했습니다. 제시된 개선 사항을 우선순위에 따라 단계적으로 적용하면, **엔터프라이즈급 프로덕션 환경에서도 안정적으로 운영 가능한 백엔드 플랫폼**으로 발전할 수 있습니다.

추가 문의 사항이나 특정 모듈에 대한 심화 리뷰가 필요한 경우, 언제든지 요청해 주시기 바랍니다.
