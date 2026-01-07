# 세션 관리 모듈 요구사항 명세서

> 작성일: 2026-01-06
> 대상: Spring Boot 3.5.4 + Redis 기반 세션 관리 모듈

---

## 📋 목차

- [1. 개요](#1-개요)
- [2. 기능 요구사항](#2-기능-요구사항)
- [3. 기술 스택](#3-기술-스택)
- [4. 아키텍처 설계](#4-아키텍처-설계)
- [5. 데이터베이스 스키마](#5-데이터베이스-스키마)
- [6. Redis 구조](#6-redis-구조)
- [7. API 명세](#7-api-명세)
- [8. 보안 요구사항](#8-보안-요구사항)
- [9. 성능 요구사항](#9-성능-요구사항)
- [10. 테스트 계획](#10-테스트-계획)
- [11. 구현 체크리스트](#11-구현-체크리스트)

---

## 1. 개요

### 1.1 목적

Redis 기반의 분산 세션 관리 시스템을 구축하여 다중 서버 환경에서 안전하고 효율적인 사용자 세션 관리를 제공합니다.

### 1.2 주요 목표

- **다중 서버 지원**: 여러 서버에서 세션 공유
- **보안 강화**: Secure, HttpOnly 쿠키 및 세션 탈취 방지
- **자동 만료**: Redis TTL 기반 자동 세션 만료
- **성능 최적화**: Redis를 통한 빠른 세션 조회
- **관리 편의성**: 관리자 API를 통한 세션 모니터링

### 1.3 적용 범위

- 사용자 로그인/로그아웃
- 인증이 필요한 모든 API 요청
- 다중 서버 환경 (Scale-out)
- Redis Standalone/Sentinel/Cluster 지원

---

## 2. 기능 요구사항

### 2.1 기본 기능

#### 2.1.1 세션 생성

**요구사항:**
- 사용자 로그인 성공 시 세션 자동 생성
- 고유한 세션 ID 생성 (UUID 기반)
- Redis에 세션 데이터 저장
- 쿠키에 세션 ID 설정

**세션 데이터 포함 항목:**
```java
{
  "sessionId": "UUID",
  "userId": "사용자 ID",
  "username": "사용자명",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "loginTime": "로그인 시각",
  "lastAccessTime": "마지막 접근 시각",
  "ipAddress": "접속 IP (선택)",
  "userAgent": "User-Agent (선택)"
}
```

#### 2.1.2 세션 조회

**요구사항:**
- 세션 ID로 사용자 정보 조회
- 세션 만료 여부 자동 체크
- 만료된 세션 접근 시 예외 발생

**조회 시나리오:**
1. 쿠키에서 세션 ID 추출
2. Redis에서 세션 데이터 조회
3. 세션 존재 여부 및 만료 확인
4. 사용자 정보 반환

#### 2.1.3 세션 갱신

**요구사항:**
- 사용자 활동 시 자동 TTL 연장
- 설정 가능한 갱신 정책
- Sliding Window 방식 지원

**갱신 정책 옵션:**
- 매 요청마다 갱신
- 일정 시간 경과 후 갱신 (예: 만료 시간의 50% 경과 시)
- 수동 갱신

#### 2.1.4 세션 삭제

**요구사항:**
- 로그아웃 시 세션 제거
- 쿠키 삭제
- Redis에서 세션 데이터 삭제

**삭제 시나리오:**
1. 사용자 로그아웃 요청
2. Redis에서 세션 삭제
3. 쿠키 무효화 (Max-Age=0)

### 2.2 쿠키 관리

#### 2.2.1 쿠키 속성

**필수 속성:**
```java
Cookie sessionCookie = new Cookie("SESSION_ID", sessionId);
sessionCookie.setHttpOnly(true);      // XSS 방지
sessionCookie.setSecure(true);        // HTTPS only (운영)
sessionCookie.setPath("/");           // 전체 경로
sessionCookie.setMaxAge(1800);        // 30분
sessionCookie.setAttribute("SameSite", "Strict");  // CSRF 방지
```

**설정 가능 항목:**
- 쿠키 이름 (기본: `SESSION_ID`)
- 만료시간 (기본: 30분)
- 도메인 (다중 서브도메인 지원)
- Path (기본: `/`)
- SameSite 정책 (Strict, Lax, None)

#### 2.2.2 환경별 설정

**개발 환경:**
```yaml
session:
  cookie:
    secure: false
    domain: localhost
```

**운영 환경:**
```yaml
session:
  cookie:
    secure: true
    domain: .example.com
```

### 2.3 Redis 기반 다중 서버 지원

#### 2.3.1 공유 세션

**요구사항:**
- 모든 서버가 동일한 Redis 인스턴스 사용
- 세션 데이터 실시간 동기화
- 서버 추가/제거 시에도 세션 유지

**구성도:**
```
[서버 1] ---+
[서버 2] ---+--> [Redis Master] <--> [Redis Replica]
[서버 3] ---+
```

#### 2.3.2 세션 TTL 관리

**요구사항:**
- Redis EXPIRE 명령으로 자동 만료
- TTL 설정: 30분 (기본값)
- 갱신 시 TTL 재설정

**TTL 갱신 전략:**
```java
// Sliding Window: 매 요청마다 TTL 갱신
redisTemplate.expire(sessionKey, Duration.ofMinutes(30));

// Absolute Window: 최초 생성 시간 기준 고정 만료
// (별도 구현 필요)
```

#### 2.3.3 세션 동시성 처리

**요구사항:**
- 동일 세션 동시 접근 시 데이터 일관성 보장
- Redis atomic 명령 사용
- 필요 시 분산 락 활용 (기존 Redis 모듈 활용)

**Race Condition 방지:**
```java
// Redis Transaction 사용
redisTemplate.execute(new SessionCallback<Object>() {
    public Object execute(RedisOperations operations) {
        operations.multi();
        operations.opsForValue().set(key, value);
        operations.expire(key, ttl);
        return operations.exec();
    }
});
```

#### 2.3.4 Redis 클러스터 호환

**요구사항:**
- Redis Cluster 모드 지원
- Key 분배 (Hash Slot) 고려
- Failover 시나리오 대응

**Key 네이밍 규칙:**
```
SESSION:{sessionId}         # 세션 데이터
SESSION:USER:{userId}       # 사용자별 세션 목록 (선택)
SESSION:INDEX:*             # 검색용 인덱스 (선택)
```

### 2.4 보안 요구사항

#### 2.4.1 세션 탈취 방지

**요구사항:**
- Secure, HttpOnly 쿠키 사용
- 세션 ID 난수화 (UUID)
- 충분한 엔트로피 확보 (128bit 이상)

**세션 ID 생성:**
```java
String sessionId = UUID.randomUUID().toString();  // 36자
// 또는
String sessionId = SecureRandom 기반 생성 (64자 이상)
```

#### 2.4.2 세션 만료 정책

**요구사항:**
- 비활성 세션 자동 만료 (30분)
- 최대 세션 수명 설정 (선택, 예: 8시간)
- 강제 로그아웃 기능

**만료 시나리오:**
1. **Idle Timeout**: 마지막 활동 후 30분 경과 시 만료
2. **Absolute Timeout**: 로그인 후 8시간 경과 시 무조건 만료
3. **Manual Logout**: 사용자 명시적 로그아웃
4. **Force Logout**: 관리자 강제 종료

#### 2.4.3 IP/User-Agent 검증 (선택)

**요구사항:**
- 세션 생성 시 IP 및 UA 기록
- 요청마다 IP/UA 일치 여부 검증
- 불일치 시 세션 무효화 또는 경고

**검증 로직:**
```java
if (session.getIpAddress() != null &&
    !session.getIpAddress().equals(requestIp)) {
    // 세션 탈취 의심 - 로그 기록 후 세션 종료
    sessionService.invalidateSession(sessionId);
    throw new SessionSecurityException("IP mismatch");
}
```

#### 2.4.4 세션 고정 공격 방지

**요구사항:**
- 로그인 성공 시 새로운 세션 ID 발급
- 기존 세션 ID 무효화

**방어 코드:**
```java
// 로그인 전 익명 세션 ID
String oldSessionId = getSessionId();

// 로그인 성공 후 새 세션 생성
String newSessionId = createNewSession(user);

// 기존 세션 삭제
deleteSession(oldSessionId);
```

### 2.5 로그 및 감사

#### 2.5.1 로깅 이벤트

**기록 항목:**
- 세션 생성: userId, sessionId, IP, UA, 생성 시각
- 세션 삭제: sessionId, 종료 사유, 종료 시각
- 세션 갱신: sessionId, 갱신 시각
- 비정상 접근: sessionId, IP, UA, 접근 시각, 실패 사유

**로그 레벨:**
```java
log.info("Session created: userId={}, sessionId={}, ip={}", ...);
log.warn("Session validation failed: sessionId={}, reason={}", ...);
log.error("Redis connection failed during session operation", e);
```

#### 2.5.2 감사 로그 (선택)

**요구사항:**
- DB에 세션 이력 저장 (선택)
- 로그인/로그아웃 이력 관리
- 보안 감사 추적

**테이블 스키마 (선택):**
```sql
CREATE TABLE t_session_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(50) NOT NULL,
  event_type VARCHAR(20) NOT NULL,  -- LOGIN, LOGOUT, EXPIRED, FORCE_LOGOUT
  ip_address VARCHAR(50),
  user_agent VARCHAR(500),
  event_time DATETIME NOT NULL,
  INDEX idx_user_id (user_id),
  INDEX idx_event_time (event_time)
);
```

### 2.6 관리 API

#### 2.6.1 관리자 API

**요구사항:**
- 특정 사용자 세션 강제 종료
- 전체 세션 상태 조회
- 사용자별 활성 세션 목록 조회
- 세션 통계 조회

**API 목록:**
```http
GET    /admin/sessions                    # 전체 세션 목록 조회
GET    /admin/sessions/stats              # 세션 통계
GET    /admin/sessions/user/{userId}      # 사용자별 세션 목록
DELETE /admin/sessions/{sessionId}        # 특정 세션 강제 종료
DELETE /admin/sessions/user/{userId}      # 사용자 전체 세션 종료
```

#### 2.6.2 서비스 API

**요구사항:**
- 세션 생성, 조회, 삭제, 갱신
- 현재 사용자 정보 조회
- 세션 유효성 검증

**API 목록:**
```http
POST   /sessions/login          # 세션 생성 (로그인)
POST   /sessions/logout         # 세션 삭제 (로그아웃)
GET    /sessions/current        # 현재 세션 정보 조회
POST   /sessions/refresh        # 세션 갱신 (TTL 연장)
GET    /sessions/validate       # 세션 유효성 검증
```

---

## 3. 기술 스택

### 3.1 Core

- **Spring Boot**: 3.5.4
- **Spring Session Data Redis**: 세션 관리
- **Spring Data Redis**: Redis 연동
- **Lettuce**: Redis 클라이언트
- **Java**: 17

### 3.2 Redis

- **버전**: 6.x 이상
- **모드**: Standalone / Sentinel / Cluster
- **지속성**: RDB + AOF (권장)

### 3.3 Dependencies

```gradle
dependencies {
    // Spring Session Redis
    implementation 'org.springframework.session:spring-session-data-redis'

    // Redis (이미 포함됨)
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // 기존 의존성
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

---

## 4. 아키텍처 설계

### 4.1 패키지 구조

```
com.wan.framework.session
├── domain/
│   └── UserSession.java              # 세션 도메인 객체
├── dto/
│   ├── SessionDTO.java                # 세션 정보 DTO
│   ├── LoginRequest.java              # 로그인 요청 DTO
│   ├── SessionStatsDTO.java           # 세션 통계 DTO
│   └── SessionAuditDTO.java           # 세션 감사 로그 DTO
├── repository/
│   └── SessionAuditRepository.java    # 세션 감사 로그 Repository (선택)
├── service/
│   ├── SessionService.java            # 세션 CRUD
│   ├── SessionManagementService.java  # 관리자 세션 관리
│   └── SessionSecurityService.java    # 보안 검증
├── web/
│   ├── SessionController.java         # 세션 API
│   └── SessionAdminController.java    # 관리자 API
├── config/
│   ├── SessionConfig.java             # Spring Session 설정
│   ├── SessionCookieConfig.java       # 쿠키 설정
│   └── SessionProperties.java         # 설정 프로퍼티
├── filter/
│   └── SessionValidationFilter.java   # 세션 검증 필터
├── interceptor/
│   └── SessionRefreshInterceptor.java # 세션 갱신 인터셉터
├── exception/
│   └── SessionException.java          # 세션 예외
└── constant/
    ├── SessionExceptionMessage.java   # 예외 메시지
    └── SessionConstants.java          # 세션 상수
```

### 4.2 계층별 역할

#### 4.2.1 Domain Layer
- `UserSession`: 세션 데이터 객체 (Redis 저장용)

#### 4.2.2 Service Layer
- `SessionService`: 세션 생성, 조회, 삭제, 갱신
- `SessionManagementService`: 관리자 기능 (통계, 강제 종료)
- `SessionSecurityService`: IP/UA 검증, 세션 탈취 감지

#### 4.2.3 Web Layer
- `SessionController`: 사용자 세션 API
- `SessionAdminController`: 관리자 세션 관리 API

#### 4.2.4 Config Layer
- `SessionConfig`: Spring Session Redis 설정
- `SessionCookieConfig`: 쿠키 속성 설정

#### 4.2.5 Filter/Interceptor Layer
- `SessionValidationFilter`: 요청마다 세션 검증
- `SessionRefreshInterceptor`: 세션 자동 갱신

### 4.3 데이터 흐름

```
1. 로그인 요청
   ┌─────────┐
   │ Client  │
   └────┬────┘
        │ POST /sessions/login
        ▼
   ┌─────────────────┐
   │ SessionController│
   └────┬────────────┘
        │ createSession()
        ▼
   ┌──────────────┐
   │SessionService│
   └────┬─────────┘
        │ save to Redis
        ▼
   ┌──────────┐
   │  Redis   │
   └──────────┘
        │ set cookie
        ▼
   ┌─────────┐
   │ Client  │
   └─────────┘

2. API 요청 (세션 필요)
   ┌─────────┐
   │ Client  │ (with SESSION_ID cookie)
   └────┬────┘
        │ GET /api/users
        ▼
   ┌─────────────────────┐
   │SessionValidationFilter│
   └────┬────────────────┘
        │ validate session
        ▼
   ┌──────────────┐
   │SessionService│
   └────┬─────────┘
        │ get from Redis
        ▼
   ┌──────────┐
   │  Redis   │
   └────┬─────┘
        │ session valid
        ▼
   ┌──────────────┐
   │UserController│
   └──────────────┘

3. 로그아웃
   ┌─────────┐
   │ Client  │
   └────┬────┘
        │ POST /sessions/logout
        ▼
   ┌─────────────────┐
   │ SessionController│
   └────┬────────────┘
        │ deleteSession()
        ▼
   ┌──────────────┐
   │SessionService│
   └────┬─────────┘
        │ delete from Redis
        ▼
   ┌──────────┐
   │  Redis   │
   └────┬─────┘
        │ clear cookie
        ▼
   ┌─────────┐
   │ Client  │
   └─────────┘
```

---

## 5. 데이터베이스 스키마

### 5.1 세션 감사 로그 (선택)

```sql
CREATE TABLE t_session_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL COMMENT '세션 ID',
  user_id VARCHAR(50) NOT NULL COMMENT '사용자 ID',
  event_type VARCHAR(20) NOT NULL COMMENT '이벤트 타입 (LOGIN, LOGOUT, EXPIRED, FORCE_LOGOUT)',
  ip_address VARCHAR(50) COMMENT '접속 IP',
  user_agent VARCHAR(500) COMMENT 'User-Agent',
  event_time DATETIME NOT NULL COMMENT '이벤트 발생 시각',
  additional_info TEXT COMMENT '추가 정보 (JSON)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session_id (session_id),
  INDEX idx_user_id (user_id),
  INDEX idx_event_type (event_type),
  INDEX idx_event_time (event_time)
) COMMENT='세션 감사 로그';
```

---

## 6. Redis 구조

### 6.1 세션 데이터 구조

**Key 패턴:**
```
spring:session:sessions:{sessionId}                # 세션 데이터 (Hash)
spring:session:expirations:{timestamp}             # 만료 이벤트 (Set)
spring:session:sessions:expires:{sessionId}        # 만료 시각 (String)
```

**세션 데이터 (Hash):**
```redis
HGETALL spring:session:sessions:{sessionId}

{
  "sessionAttr:userId": "user123",
  "sessionAttr:username": "홍길동",
  "sessionAttr:roles": "[\"ROLE_USER\"]",
  "sessionAttr:loginTime": "2026-01-06T15:30:00",
  "sessionAttr:lastAccessTime": "2026-01-06T15:45:00",
  "sessionAttr:ipAddress": "192.168.1.100",
  "sessionAttr:userAgent": "Mozilla/5.0...",
  "creationTime": "1704531000000",
  "lastAccessedTime": "1704531900000",
  "maxInactiveInterval": "1800"
}
```

### 6.2 TTL 관리

```redis
# 세션 생성
HMSET spring:session:sessions:{sessionId} ...
EXPIRE spring:session:sessions:{sessionId} 1800

# TTL 갱신
EXPIRE spring:session:sessions:{sessionId} 1800

# TTL 조회
TTL spring:session:sessions:{sessionId}
```

### 6.3 사용자별 세션 인덱스 (선택)

**Key 패턴:**
```
SESSION:USER:{userId}          # Set (사용자의 모든 세션 ID)
```

**명령:**
```redis
# 세션 추가
SADD SESSION:USER:user123 {sessionId1}
EXPIRE SESSION:USER:user123 7200

# 세션 조회
SMEMBERS SESSION:USER:user123

# 세션 삭제
SREM SESSION:USER:user123 {sessionId1}
```

---

## 7. API 명세

### 7.1 사용자 세션 API

#### 7.1.1 로그인 (세션 생성)

```http
POST /sessions/login
Content-Type: application/json

{
  "userId": "user123",
  "password": "password"
}
```

**Response 200:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "username": "홍길동",
  "roles": ["ROLE_USER"],
  "loginTime": "2026-01-06T15:30:00",
  "expiresIn": 1800
}
```

**Set-Cookie:**
```
SESSION_ID=550e8400-e29b-41d4-a716-446655440000; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=1800
```

#### 7.1.2 로그아웃 (세션 삭제)

```http
POST /sessions/logout
Cookie: SESSION_ID=550e8400-e29b-41d4-a716-446655440000
```

**Response 204 No Content**

**Set-Cookie:**
```
SESSION_ID=; Path=/; HttpOnly; Max-Age=0
```

#### 7.1.3 현재 세션 정보 조회

```http
GET /sessions/current
Cookie: SESSION_ID=550e8400-e29b-41d4-a716-446655440000
```

**Response 200:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "username": "홍길동",
  "roles": ["ROLE_USER"],
  "loginTime": "2026-01-06T15:30:00",
  "lastAccessTime": "2026-01-06T15:45:00",
  "remainingTime": 1500
}
```

#### 7.1.4 세션 갱신

```http
POST /sessions/refresh
Cookie: SESSION_ID=550e8400-e29b-41d4-a716-446655440000
```

**Response 200:**
```json
{
  "message": "Session refreshed successfully",
  "expiresIn": 1800
}
```

#### 7.1.5 세션 유효성 검증

```http
GET /sessions/validate
Cookie: SESSION_ID=550e8400-e29b-41d4-a716-446655440000
```

**Response 200:**
```json
{
  "valid": true,
  "remainingTime": 1500
}
```

**Response 401 (세션 없음):**
```json
{
  "valid": false,
  "reason": "Session not found or expired"
}
```

### 7.2 관리자 세션 API

#### 7.2.1 전체 세션 목록 조회

```http
GET /admin/sessions?page=0&pageSize=20
Authorization: Bearer {admin-token}
```

**Response 200:**
```json
{
  "content": [
    {
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "user123",
      "username": "홍길동",
      "loginTime": "2026-01-06T15:30:00",
      "lastAccessTime": "2026-01-06T15:45:00",
      "ipAddress": "192.168.1.100",
      "remainingTime": 1500
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

#### 7.2.2 세션 통계 조회

```http
GET /admin/sessions/stats
Authorization: Bearer {admin-token}
```

**Response 200:**
```json
{
  "totalSessions": 50,
  "activeSessions": 45,
  "expiredToday": 120,
  "averageSessionDuration": 1200,
  "topUsers": [
    {"userId": "user123", "sessionCount": 3},
    {"userId": "user456", "sessionCount": 2}
  ]
}
```

#### 7.2.3 사용자별 세션 목록 조회

```http
GET /admin/sessions/user/user123
Authorization: Bearer {admin-token}
```

**Response 200:**
```json
[
  {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "loginTime": "2026-01-06T15:30:00",
    "lastAccessTime": "2026-01-06T15:45:00",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0..."
  },
  {
    "sessionId": "660e8400-e29b-41d4-a716-446655440001",
    "loginTime": "2026-01-06T14:20:00",
    "lastAccessTime": "2026-01-06T15:40:00",
    "ipAddress": "192.168.1.101",
    "userAgent": "Chrome/..."
  }
]
```

#### 7.2.4 특정 세션 강제 종료

```http
DELETE /admin/sessions/{sessionId}
Authorization: Bearer {admin-token}
```

**Response 200:**
```json
{
  "message": "Session terminated successfully",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 7.2.5 사용자 전체 세션 종료

```http
DELETE /admin/sessions/user/{userId}
Authorization: Bearer {admin-token}
```

**Response 200:**
```json
{
  "message": "All sessions terminated for user",
  "userId": "user123",
  "terminatedCount": 2
}
```

---

## 8. 보안 요구사항

### 8.1 쿠키 보안

**필수 설정:**
```java
@Configuration
public class SessionCookieConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION_ID");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);      // XSS 방지
        serializer.setUseSecureCookie(true);        // HTTPS only (운영)
        serializer.setSameSite("Strict");           // CSRF 방지
        serializer.setCookieMaxAge(1800);           // 30분
        return serializer;
    }
}
```

### 8.2 세션 ID 생성

**요구사항:**
- UUID 기반 (36자)
- 또는 SecureRandom 기반 (64자 이상)
- 충분한 엔트로피 확보

```java
// UUID 방식
String sessionId = UUID.randomUUID().toString();

// SecureRandom 방식
SecureRandom random = new SecureRandom();
byte[] bytes = new byte[32];
random.nextBytes(bytes);
String sessionId = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
```

### 8.3 세션 고정 공격 방지

**요구사항:**
- 로그인 성공 시 새 세션 발급
- 기존 세션 무효화

```java
@PostMapping("/login")
public ResponseEntity<SessionDTO> login(@RequestBody LoginRequest request,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
    // 기존 세션 무효화
    HttpSession oldSession = httpRequest.getSession(false);
    if (oldSession != null) {
        oldSession.invalidate();
    }

    // 새 세션 생성
    HttpSession newSession = httpRequest.getSession(true);
    // ... 사용자 인증 및 세션 설정
}
```

### 8.4 IP/User-Agent 검증 (선택)

**구현 예시:**
```java
@Component
public class SessionSecurityService {

    public void validateSessionSecurity(UserSession session,
                                        HttpServletRequest request) {
        // IP 검증
        String requestIp = getClientIP(request);
        if (session.getIpAddress() != null &&
            !session.getIpAddress().equals(requestIp)) {
            log.warn("IP mismatch for session: {} (expected: {}, actual: {})",
                     session.getSessionId(), session.getIpAddress(), requestIp);
            throw new SessionSecurityException("IP address mismatch");
        }

        // User-Agent 검증 (선택)
        String requestUA = request.getHeader("User-Agent");
        if (session.getUserAgent() != null &&
            !session.getUserAgent().equals(requestUA)) {
            log.warn("User-Agent mismatch for session: {}",
                     session.getSessionId());
            // 경고만 하거나 선택적으로 예외 발생
        }
    }
}
```

---

## 9. 성능 요구사항

### 9.1 응답 시간

- 세션 조회: 평균 10ms 이하
- 세션 생성/삭제: 평균 20ms 이하
- Redis 연결 타임아웃: 3초

### 9.2 동시 사용자

- 최소 1,000명 동시 접속 지원
- 최대 10,000명 동시 접속 지원 (Scale-out)

### 9.3 Redis 성능

- Connection Pool: 최소 10, 최대 50
- Command Timeout: 3초
- Lettuce 비동기 모드 활용 (선택)

```yaml
spring:
  redis:
    lettuce:
      pool:
        min-idle: 10
        max-idle: 20
        max-active: 50
      shutdown-timeout: 100ms
    timeout: 3000ms
```

---

## 10. 테스트 계획

### 10.1 단위 테스트

**대상:**
- `SessionService`: 세션 CRUD 로직
- `SessionSecurityService`: IP/UA 검증 로직
- `SessionManagementService`: 관리자 기능

**테스트 케이스:**
- 세션 생성 성공
- 세션 조회 성공/실패 (만료 세션)
- 세션 갱신 성공
- 세션 삭제 성공
- IP/UA 검증 성공/실패
- 동시성 테스트 (멀티스레드)

### 10.2 통합 테스트

**대상:**
- Redis 연동 테스트
- 쿠키 생성/삭제 테스트
- API 엔드포인트 테스트

**테스트 케이스:**
- 로그인 → 세션 생성 → Redis 저장 확인
- 로그아웃 → 세션 삭제 → Redis 삭제 확인
- 세션 만료 → TTL 확인
- 다중 서버 환경 시뮬레이션

### 10.3 부하 테스트

**도구:** JMeter, Gatling

**시나리오:**
- 1,000명 동시 로그인
- 10,000개 세션 조회 (동시)
- 세션 갱신 부하 테스트
- Redis failover 시나리오

---

## 11. 구현 체크리스트

### 11.1 Phase 1: 기본 기능 (필수)

- [ ] Domain 및 DTO 작성
  - [ ] `UserSession.java`
  - [ ] `SessionDTO.java`
  - [ ] `LoginRequest.java`
  - [ ] `SessionStatsDTO.java`

- [ ] Service 구현
  - [ ] `SessionService.java` (생성, 조회, 삭제, 갱신)
  - [ ] `SessionManagementService.java` (관리자 기능)

- [ ] Controller 구현
  - [ ] `SessionController.java` (5개 API)
  - [ ] `SessionAdminController.java` (5개 API)

- [ ] Config 설정
  - [ ] `SessionConfig.java` (Spring Session Redis)
  - [ ] `SessionCookieConfig.java` (쿠키 설정)
  - [ ] `SessionProperties.java` (프로퍼티)

- [ ] Exception 처리
  - [ ] `SessionException.java`
  - [ ] `SessionExceptionMessage.java`

### 11.2 Phase 2: 보안 기능 (필수)

- [ ] `SessionSecurityService.java` 구현
  - [ ] IP/UA 검증
  - [ ] 세션 고정 방지
  - [ ] 세션 탈취 감지

- [ ] `SessionValidationFilter.java` 구현
  - [ ] 요청마다 세션 검증
  - [ ] 만료 세션 처리

- [ ] `SessionRefreshInterceptor.java` 구현
  - [ ] 자동 TTL 갱신

### 11.3 Phase 3: 고급 기능 (선택)

- [ ] 세션 감사 로그
  - [ ] `t_session_audit` 테이블 생성
  - [ ] `SessionAuditRepository.java`
  - [ ] 로그인/로그아웃 이력 기록

- [ ] 사용자별 세션 인덱스
  - [ ] Redis Set 기반 인덱스
  - [ ] 사용자별 세션 목록 조회 최적화

- [ ] 세션 통계
  - [ ] 실시간 통계 조회
  - [ ] 세션 이벤트 모니터링

### 11.4 Phase 4: 테스트 (필수)

- [ ] 단위 테스트 작성
  - [ ] `SessionServiceTest.java`
  - [ ] `SessionSecurityServiceTest.java`

- [ ] 통합 테스트 작성
  - [ ] `SessionControllerTest.java`
  - [ ] Redis 연동 테스트

- [ ] 부하 테스트 (선택)
  - [ ] JMeter 시나리오
  - [ ] 동시 사용자 테스트

### 11.5 Phase 5: 문서화 (필수)

- [ ] README 업데이트
  - [ ] 세션 관리 모듈 추가
  - [ ] API 엔드포인트 추가

- [ ] impl_list.md 업데이트
  - [ ] 모듈 상세 설명
  - [ ] 아키텍처 다이어그램
  - [ ] 파일 통계 업데이트

---

## 12. 설정 예시

### 12.1 application.yml

```yaml
spring:
  application:
    name: framework

  # Redis 설정
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
    lettuce:
      pool:
        min-idle: 10
        max-idle: 20
        max-active: 50
      shutdown-timeout: 100ms
    timeout: 3000ms

  # Session 설정
  session:
    store-type: redis
    redis:
      namespace: spring:session
      flush-mode: on_save
      cleanup-cron: "0 * * * * *"  # 매 분마다 만료 세션 정리
    timeout: 1800s  # 30분

# 커스텀 세션 설정
session:
  cookie:
    name: SESSION_ID
    path: /
    http-only: true
    secure: true  # 운영 환경
    same-site: Strict
    max-age: 1800
    domain: ${COOKIE_DOMAIN:}

  security:
    validate-ip: true
    validate-user-agent: false

  refresh:
    enabled: true
    threshold: 0.5  # 50% 경과 시 자동 갱신
```

### 12.2 SessionConfig.java

```java
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(SessionProperties properties) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(properties.getCookie().getName());
        serializer.setCookiePath(properties.getCookie().getPath());
        serializer.setUseHttpOnlyCookie(properties.getCookie().isHttpOnly());
        serializer.setUseSecureCookie(properties.getCookie().isSecure());
        serializer.setSameSite(properties.getCookie().getSameSite());
        serializer.setCookieMaxAge(properties.getCookie().getMaxAge());

        if (properties.getCookie().getDomain() != null) {
            serializer.setDomainName(properties.getCookie().getDomain());
        }

        return serializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
```

---

## 13. 참고 자료

### 13.1 공식 문서

- [Spring Session Data Redis](https://docs.spring.io/spring-session/reference/guides/boot-redis.html)
- [Redis Session Pattern](https://redis.io/docs/manual/patterns/sessions/)
- [OWASP Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)

### 13.2 관련 프로젝트 파일

- `RedisConfig.java`: Redis 기본 설정
- `RedisCacheService.java`: Redis 캐시 서비스
- `DistributedLockService.java`: 분산 락 서비스

---

**문서 버전**: 1.0
**작성일**: 2026-01-06
**작성자**: Framework Development Team
