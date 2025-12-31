# Framework Core Back-end

Spring Boot 기반의 엔터프라이즈 프레임워크 백엔드 시스템입니다.

## 목차
- [기술 스택](#기술-스택)
- [필수 요구사항](#필수-요구사항)
- [프로젝트 실행 방법](#프로젝트-실행-방법)
- [필수 설정 파일](#필수-설정-파일)
- [데이터베이스 설정](#데이터베이스-설정)
- [구현된 모듈](#구현된-모듈)
- [API 엔드포인트](#api-엔드포인트)

## 기술 스택

### Backend Framework
- **Spring Boot**: 3.5.4
- **Java**: 17
- **Build Tool**: Gradle

### Core Dependencies
- **Spring Data JPA**: ORM 및 데이터 액세스
- **Spring Security**: 인증 및 권한 관리
- **Spring Batch**: 배치 처리
- **Quartz**: 스케줄링
- **MapStruct**: DTO 매핑
- **Lombok**: 코드 간소화

### Database
- **MariaDB**: 메인 데이터베이스 (JDBC Driver 3.3.3)

## 필수 요구사항

프로젝트를 실행하기 전에 다음 항목들이 설치되어 있어야 합니다:

1. **Java 17** 이상
2. **MariaDB 10.x** 이상
3. **Gradle** (또는 내장된 Gradle Wrapper 사용)

## 프로젝트 실행 방법

### 1. 데이터베이스 생성

MariaDB에 접속하여 데이터베이스를 생성합니다:

```sql
CREATE DATABASE framework CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'framework_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON framework.* TO 'framework_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 애플리케이션 설정 파일 생성

**반드시** 다음 설정 파일을 생성해야 합니다:

`src/main/resources/application.yml` 파일을 생성하고 아래 내용을 입력하세요:

```yaml
spring:
  application:
    name: framework

  datasource:
    url: jdbc:mariadb://localhost:3306/framework
    username: framework_user
    password: your_password
    driver-class-name: org.mariadb.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: update  # 운영 환경에서는 validate 또는 none 사용
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MariaDBDialect

  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # 시작 시 자동 실행 방지

server:
  port: 8080
  servlet:
    context-path: /
    session:
      timeout: 30m
      cookie:
        http-only: true
        secure: false  # HTTPS 사용 시 true로 변경

logging:
  level:
    root: INFO
    com.wan.framework: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 3. 프로젝트 빌드 및 실행

**Windows:**
```bash
gradlew.bat clean build
gradlew.bat bootRun
```

**Linux/Mac:**
```bash
./gradlew clean build
./gradlew bootRun
```

또는 JAR 파일로 실행:
```bash
java -jar build/libs/framework-0.0.1-SNAPSHOT.jar
```

### 4. 애플리케이션 접속 확인

브라우저에서 `http://localhost:8080` 으로 접속하여 서버가 정상 동작하는지 확인합니다.

## 필수 설정 파일

### ⚠️ 중요: application.yml 파일 필수

현재 프로젝트에는 `application.yml` 파일이 **존재하지 않습니다**.
위의 설정 예시를 참고하여 `src/main/resources/application.yml` 파일을 반드시 생성해야 합니다.

**민감한 정보(DB 비밀번호 등)는 환경 변수로 관리**하는 것을 권장합니다:
```yaml
spring:
  datasource:
    username: ${DB_USERNAME:framework_user}
    password: ${DB_PASSWORD}
```

### 환경별 설정 파일 (선택사항)

개발/운영 환경을 분리하려면 다음과 같이 프로파일별 설정 파일을 생성할 수 있습니다:

- `application-local.yml`: 로컬 개발 환경
- `application-dev.yml`: 개발 서버 환경
- `application-prod.yml`: 운영 환경

실행 시 프로파일 지정:
```bash
java -jar framework-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## 데이터베이스 설정

### 데이터베이스 스키마

JPA의 `ddl-auto: update` 설정으로 인해 애플리케이션 시작 시 자동으로 테이블이 생성됩니다.

주요 테이블:
- **t_user**: 사용자 정보
- **t_program**: 프로그램 정보
- **t_menu**: 메뉴 정보 (계층 구조)
- **t_board_meta**: 게시판 메타데이터
- **t_error_history**: 예외 로그 이력

### 초기 데이터 설정 (선택사항)

애플리케이션 시작 후 `/sign-up` API를 통해 관리자 계정을 생성할 수 있습니다.

## 구현된 모듈

### ✅ 완료된 모듈
- **사용자 관리 모듈**: 회원가입, 로그인, 사용자 CRUD
- **프로그램 관리 모듈**: 프로그램 등록 및 관리
- **메뉴 관리 모듈**: 계층형 메뉴 구조 관리, 역할 기반 권한
- **예외 처리 모듈**: 전역 예외 처리 및 히스토리 저장

### 🔄 진행 중인 모듈
- **동적 게시판 생성 모듈**: 게시판 메타 테이블 구현 완료

### 📋 예정된 모듈
- API key 관리 모듈
- 프로그램 실행 모듈 (proxy api)
- 레디스 관리 모듈
- 배치 관리 모듈 (레디스를 사용한 고가용성 배치 처리)
- 공통코드 관리 모듈 (레디스 활용 공통코드 관리)
- 세션관리 모듈 (레디스 활용 세션관리)

## API 엔드포인트

### 인증 관련
- `POST /sign-up`: 회원가입
- `POST /sign-in`: 로그인
- `GET /sign-out`: 로그아웃

### 사용자 관리
- `GET /users`: 사용자 목록 조회 (페이징)
- `GET /user`: 사용자 조회
- `PUT /user`: 사용자 수정
- `DELETE /user`: 사용자 삭제

### 프로그램 관리
- `GET /programs`: 프로그램 목록 조회 (페이징)
- `GET /programs/{id}`: 프로그램 단건 조회
- `POST /programs`: 프로그램 생성
- `PUT /programs`: 프로그램 수정
- `DELETE /programs/{id}`: 프로그램 삭제

### 메뉴 관리
- `GET /menus`: 전체 메뉴 조회
- `GET /menus/{id}`: 메뉴 단건 조회
- `GET /menus/tree`: 메뉴 트리 조회 (역할 기반)
- `POST /menus`: 메뉴 생성
- `PUT /menus/{id}`: 메뉴 수정
- `DELETE /menus/{id}`: 메뉴 삭제

### 게시판 관리
- `GET /board-metas`: 게시판 목록 조회 (페이징)
- `GET /board-metas/{id}`: 게시판 조회
- `POST /board-metas`: 게시판 생성
- `PUT /board-metas/{id}`: 게시판 수정
- `DELETE /board-metas/{id}`: 게시판 삭제

## 프론트엔드 연동 가이드

### CORS 설정

현재 프론트엔드 주소가 `http://localhost:9527`로 설정되어 있습니다.

**다른 주소를 사용하는 경우 변경이 필요합니다:**

1. `FrameworkSecurityConfig.java` 파일 수정:
```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
```

2. `FrameworkWebMVCConfig.java` 파일 수정:
```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:3000")
```

### 인증 방식: 세션 + 쿠키

이 프로젝트는 **세션 기반 인증**을 사용합니다.

#### 프론트엔드 설정 (필수)

모든 API 요청에 `withCredentials: true` 설정이 필요합니다:

**Axios 사용 시:**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true  // 쿠키/세션 포함
});

// 로그인
const login = async (userId, password) => {
  const response = await api.post('/sign-in', { userId, password });
  return response.data;
};

// 메뉴 조회 (세션 필요)
const getMenuTree = async () => {
  const response = await api.get('/menus/tree');
  return response.data;
};
```

**Fetch API 사용 시:**
```javascript
fetch('http://localhost:8080/sign-in', {
  method: 'POST',
  credentials: 'include',  // 쿠키 포함
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ userId: 'admin', password: 'password' })
});
```

#### 로그인 플로우

```javascript
// 1. 로그인
const response = await api.post('/sign-in', {
  userId: 'admin',
  password: 'password123'
});

// 2. 응답 데이터
console.log(response.data);
// {
//   userId: 'admin',
//   name: '관리자',
//   roles: 'ROLE_ADMIN'
// }

// 3. 이후 모든 요청은 자동으로 세션 쿠키 포함
const menus = await api.get('/menus/tree');  // 세션 자동 인증
```

#### 로그아웃

```javascript
await api.get('/sign-out');  // 세션 무효화
```

### API 응답 형식

#### 성공 응답

**단일 객체:**
```json
{
  "userId": "admin",
  "name": "관리자",
  "roles": "ROLE_ADMIN"
}
```

**페이징 응답:**
```json
{
  "content": [
    { "userId": "user1", "name": "사용자1" },
    { "userId": "user2", "name": "사용자2" }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false
}
```

#### 에러 응답 (HTTP 400)

```json
{
  "errorCode": "USER_001",
  "responseMessage": "사용자를 찾을 수 없습니다",
  "eventTime": "2025-12-31T10:30:00"
}
```

### 세션 타임아웃

- 세션 유효 시간: **30분**
- 타임아웃 시 401 에러 또는 세션 정보 없음
- 프론트엔드에서 자동 로그아웃 처리 권장

### Vue/React 연동 예시

**Vue 3 Composition API:**
```javascript
import { ref } from 'vue';
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true
});

export const useAuth = () => {
  const user = ref(null);

  const login = async (userId, password) => {
    const { data } = await api.post('/sign-in', { userId, password });
    user.value = data;
    return data;
  };

  const logout = async () => {
    await api.get('/sign-out');
    user.value = null;
  };

  return { user, login, logout };
};
```

**React:**
```javascript
import axios from 'axios';
import { useState } from 'react';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true
});

export const useAuth = () => {
  const [user, setUser] = useState(null);

  const login = async (userId, password) => {
    const { data } = await api.post('/sign-in', { userId, password });
    setUser(data);
    return data;
  };

  const logout = async () => {
    await api.get('/sign-out');
    setUser(null);
  };

  return { user, login, logout };
};
```

## 보안 설정

### Spring Security
현재 모든 요청이 허용되도록 설정되어 있습니다 (`permitAll()`).
운영 환경에서는 적절한 인증/인가 설정이 필요합니다.

### HTTPS 사용 권장
운영 환경에서는 HTTPS를 사용하고 `application.yml`의 쿠키 설정을 변경하세요:
```yaml
server:
  servlet:
    session:
      cookie:
        secure: true  # HTTPS 전용
```

## 트러블슈팅

### 1. 데이터베이스 연결 실패
- MariaDB 서버가 실행 중인지 확인
- `application.yml`의 DB 연결 정보 확인
- 방화벽 설정 확인 (포트 3306)

### 2. 빌드 오류
- Java 17 버전 확인: `java -version`
- Gradle 캐시 삭제 후 재빌드: `./gradlew clean build --refresh-dependencies`

### 3. 포트 충돌
- 8080 포트가 이미 사용 중인 경우 `application.yml`에서 포트 변경

## 개발 가이드

### skills.md 활용

프로젝트의 일관된 개발을 위해 `.claude/skills.md` 파일을 작성했습니다.

#### skills.md란?

- 프로젝트의 **코딩 컨벤션**과 **구현 패턴**을 문서화한 가이드
- 새로운 모듈 개발 시 참고할 **표준 템플릿** 제공
- **단위 테스트** 작성 규칙 포함

#### 사용 방법

**1. Claude Code에서 모듈 개발 요청 시**

```
"skills.md를 참고하여 Comment(댓글) 모듈을 구현해줘. 테스트 코드도 함께 작성해줘."
```

Claude Code는 자동으로 `.claude/skills.md`를 참조하여:
- 패키지 구조에 맞게 파일 생성
- 네이밍 규칙 준수
- 예외 처리 구현
- 단위 테스트 작성

**2. 직접 참고하여 개발**

`.claude/skills.md` 파일을 열어서 다음 내용 확인:
- 아키텍처 구조
- 네이밍 규칙
- 어노테이션 규칙
- 공통 필드 규칙
- 테스트 작성 방법
- 완전한 예시 코드 (Post 모듈)

#### 포함된 내용

1. **프로젝트 아키텍처**: 패키지 구조 설명
2. **코딩 컨벤션**: Entity, DTO, Service, Controller 등 네이밍 규칙
3. **공통 필드 관리**: createTime, modifiedTime, dataCode 자동 설정
4. **데이터 상태 관리**: 논리적 삭제(Soft Delete) 구현 방법
5. **예외 처리**: 도메인별 예외 정의 및 사용법
6. **테스트 작성**: Given-When-Then 패턴, AssertJ 사용법
7. **완전한 예시**: Post(게시글) 모듈 전체 구현 예시

#### 모듈 개발 체크리스트

새 모듈 개발 시 다음 순서로 진행:

- [ ] 1. 도메인 엔티티 작성 (`domain/`)
- [ ] 2. DTO 작성 (`dto/`)
- [ ] 3. Repository 인터페이스 작성 (`repository/`)
- [ ] 4. Mapper 인터페이스 작성 (`mapper/`)
- [ ] 5. 예외 클래스 작성 (`exception/`)
- [ ] 6. 예외 메시지 상수 작성 (`constant/`)
- [ ] 7. Service 작성 (`service/`)
- [ ] 8. Controller 작성 (`web/`)
- [ ] 9. Service 단위 테스트 작성 (`test/`)
- [ ] 10. API 통합 테스트 작성 (선택)

#### 예시: 새 모듈 개발

**Claude Code에 요청:**
```
skills.md를 참고하여 Comment(댓글) 모듈을 구현해줘.

요구사항:
- 게시글 ID와 연결
- 작성자, 내용, 부모 댓글 ID 포함
- 대댓글 기능 지원
- CRUD API 구현
- Service 단위 테스트 작성
```

Claude Code는 자동으로:
1. `com.wan.framework.comment` 패키지 생성
2. Entity, DTO, Repository, Mapper, Service, Controller 생성
3. 예외 처리 구현
4. 단위 테스트 코드 작성

### 테스트 실행

**전체 테스트 실행:**
```bash
./gradlew test
```

**특정 테스트만 실행:**
```bash
./gradlew test --tests "com.wan.framework.user.service.PasswordServiceTest"
```

**테스트 커버리지 확인:**
```bash
./gradlew test jacocoTestReport
```

### 빌드 (MapStruct 코드 생성)

Mapper 인터페이스 작성 후 반드시 빌드:
```bash
./gradlew clean build
```

생성된 Mapper 구현체 확인:
```
build/generated/sources/annotationProcessor/java/main/com/wan/framework/{domain}/mapper/{Domain}MapperImpl.java
```

## 라이선스

이 프로젝트는 [라이선스 정보]에 따라 배포됩니다.