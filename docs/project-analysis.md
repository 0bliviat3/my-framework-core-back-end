# 프로젝트 분석 보고서

## 1. 프로젝트 개요

이 프로젝트는 Spring Boot 기반의 백엔드 프레임워크로, 다음과 같은 주요 기능을 제공합니다:

- **Spring Boot 애플리케이션** (Java 17)
- **MariaDB 11.2** (데이터베이스)
- **Redis 7.2** (캐시 서버)

## 2. 주요 구성 요소

### 2.1 기술 스택
- **백엔드**: Spring Boot (Java 17)
- **데이터베이스**: MariaDB 11.2
- **캐시 서버**: Redis 7.2
- **배포 환경**: Docker + Docker Compose

### 2.2 구성 파일
- `docker-compose.yml`: 서비스 정의 및 네트워크 설정
- `.env.example`: 환경 변수 예제
- `DEPLOYMENT.md`: 배포 가이드
- `QUICK-START.md`: 빠른 시작 가이드

## 3. 프로젝트 구조

```
my-framework-core-back-end/
├── docker-compose.yml    # Docker 서비스 정의
├── Dockerfile            # 컨테이너 이미지 빌드 정의
├── .env.example         # 환경 변수 예제
├── DEPLOYMENT.md        # 배포 문서
├── QUICK-START.md       # 빠른 시작 문서
├── src/                 # 소스 코드 디렉토리
├── config/              # 설정 파일 디렉토리
├── deploy/              # 배포 관련 파일
└── docs/                # 문서 디렉토리
```

## 4. 주요 기능

### 4.1 인증 및 권한 관리
- 사용자 인증 (JWT 기반)
- 관리자 계정 생성
- 로그인/회원가입 API

### 4.2 데이터 관리
- MariaDB 연동 (JPA 사용)
- Redis 캐싱
- 데이터베이스 마이그레이션

### 4.3 운영 기능
- 헬스체크 엔드포인트
- 로그 관리
- 모니터링 엔드포인트

## 5. 배포 방식

### 5.1 Docker 기반 배포
- Docker Compose를 통한 다중 컨테이너 관리
- MariaDB, Redis, 애플리케이션 컨테이너 모두 포함
- 포트 매핑을 통한 외부 접근 가능

### 5.2 환경 설정
- `.env` 파일을 통한 환경 변수 관리
- 보안 키 및 비밀번호 관리
- 포트 및 메모리 설정

## 6. API 엔드포인트

- `/actuator/health` - 상태 확인
- `/users/admin/exists` - 관리자 계정 존재 여부
- `/users/admin/initial` - 초기 관리자 계정 생성
- `/sessions/login` - 로그인
- `/users/sign-up` - 회원가입

## 7. 보안 고려사항

- CORS 설정
- HTTPS 지원
- 환경 변수 보안
- DB/Redis 비밀번호 관리

## 8. 트러블슈팅

- 컨테이너 재시작 문제
- DB 연결 실패
- Redis 연결 문제
- 메모리 부족 오류
- 파일 업로드 권한 문제

## 9. 성능 및 확장성

- Docker 컨테이너 기반
- 다중 서비스 관리
- 로드 밸런싱 준비
- 모니터링 및 로깅 시스템