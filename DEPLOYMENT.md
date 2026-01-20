# Framework Backend 배포 가이드

## 목차
- [개요](#개요)
- [시스템 요구사항](#시스템-요구사항)
- [사전 준비](#사전-준비)
- [환경 변수 설정](#환경-변수-설정)
- [Docker로 배포](#docker로-배포)
- [운영 가이드](#운영-가이드)
- [보안 점검 사항](#보안-점검-사항)
- [트러블슈팅](#트러블슈팅)

---

## 개요

이 문서는 Framework Backend 애플리케이션을 Docker를 사용하여 프로덕션 환경에 배포하는 방법을 설명합니다.

**주요 구성 요소:**
- Spring Boot 애플리케이션 (Java 17)
- MariaDB 11.2 (데이터베이스)
- Redis 7.2 (캐시 서버)

---

## 시스템 요구사항

### 최소 사양
- **CPU**: 2 Core
- **메모리**: 4 GB RAM
- **디스크**: 20 GB 이상
- **OS**: Linux (Ubuntu 20.04+ 권장) 또는 Windows Server

### 권장 사양
- **CPU**: 4 Core 이상
- **메모리**: 8 GB RAM 이상
- **디스크**: 50 GB 이상 (SSD 권장)

### 필수 소프트웨어
- Docker Engine 24.0 이상
- Docker Compose 2.0 이상

---

## 사전 준비

### 1. Docker 설치

#### Linux (Ubuntu/Debian)
```bash
# Docker 공식 저장소 추가
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 일반 사용자에게 Docker 권한 부여
sudo usermod -aG docker $USER
newgrp docker

# 설치 확인
docker --version
docker-compose --version
```

#### Windows
- Docker Desktop for Windows 설치: https://www.docker.com/products/docker-desktop

### 2. 방화벽 설정

필요한 포트를 열어줍니다:
```bash
# UFW 사용 시 (Ubuntu)
sudo ufw allow 8080/tcp    # 애플리케이션
sudo ufw allow 13306/tcp   # MariaDB (외부 접근 필요 시)
sudo ufw allow 16379/tcp   # Redis (외부 접근 필요 시)
```

### 3. 소스 코드 다운로드

```bash
# Git에서 클론
git clone <repository-url>
cd my-framework-core-back-end

# 또는 압축 파일 해제
unzip my-framework-core-back-end.zip
cd my-framework-core-back-end
```

---

## 환경 변수 설정

### 1. 환경 변수 파일 생성

`.env.example`을 복사하여 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

### 2. 환경 변수 수정

`.env` 파일을 편집하여 실제 환경에 맞게 설정합니다:

```bash
nano .env
# 또는
vim .env
```

#### 필수 수정 항목

```env
# 데이터베이스 비밀번호 (강력한 비밀번호로 변경!)
DB_ROOT_PASSWORD=your_very_secure_root_password_here
DB_PASSWORD=your_very_secure_user_password_here

# Redis 비밀번호 (강력한 비밀번호로 변경!)
REDIS_PASSWORD=your_very_secure_redis_password_here

# CORS 허용 도메인 (프론트엔드 URL)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# HTTPS 사용 시 쿠키 보안 활성화
COOKIE_SECURE=true
COOKIE_SAME_SITE=strict
```

#### 권장 수정 항목

```env
# 외부 포트 변경 (보안 강화)
APP_EXTERNAL_PORT=8080
DB_EXTERNAL_PORT=13306   # 외부 접근 불필요 시 주석 처리
REDIS_EXTERNAL_PORT=16379  # 외부 접근 불필요 시 주석 처리

# JVM 힙 메모리 (서버 사양에 맞게 조정)
JVM_MIN_HEAP=512m
JVM_MAX_HEAP=2048m

# 데이터베이스 연결 풀 크기
DB_POOL_MAX=30
DB_POOL_MIN=10

# JPA DDL 설정 (운영에서는 validate 사용)
JPA_DDL_AUTO=validate

# 로깅 레벨
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO
LOG_LEVEL_SQL=WARN
```

---

## Docker로 배포

### 1. Docker 이미지 빌드 및 컨테이너 실행

#### 전체 서비스 시작 (권장)
```bash
# 백그라운드에서 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

#### 개별 서비스 시작
```bash
# MariaDB만 시작
docker-compose up -d mariadb

# Redis만 시작
docker-compose up -d redis

# 애플리케이션만 시작 (DB, Redis 실행 후)
docker-compose up -d app
```

### 2. 서비스 상태 확인

```bash
# 컨테이너 상태 확인
docker-compose ps

# 헬스체크 확인
docker-compose exec app wget -qO- http://localhost:8080/actuator/health

# 로그 확인
docker-compose logs app
docker-compose logs mariadb
docker-compose logs redis

# 실시간 로그 모니터링
docker-compose logs -f app
```

### 3. 데이터베이스 초기화

첫 실행 시 MariaDB가 자동으로 데이터베이스와 사용자를 생성합니다.

#### 수동으로 확인/생성하려면:
```bash
# MariaDB 컨테이너 접속
docker-compose exec mariadb mariadb -uroot -p${DB_ROOT_PASSWORD}

# SQL 쿼리 실행
MariaDB> SHOW DATABASES;
MariaDB> USE framework;
MariaDB> SHOW TABLES;
MariaDB> SELECT * FROM t_user LIMIT 5;
MariaDB> exit
```

### 4. 애플리케이션 접근 확인

```bash
# 헬스체크 API
curl http://localhost:8080/actuator/health

# 관리자 계정 존재 여부 확인
curl http://localhost:8080/users/admin/exists
```

웹 브라우저에서 `http://your-server-ip:8080` 접속 확인

---

## 운영 가이드

### 서비스 중지 및 재시작

```bash
# 서비스 중지 (컨테이너 유지)
docker-compose stop

# 서비스 시작 (기존 컨테이너 사용)
docker-compose start

# 서비스 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart app

# 서비스 완전 종료 (컨테이너 삭제)
docker-compose down

# 서비스 종료 + 볼륨 삭제 (데이터 삭제 주의!)
docker-compose down -v
```

### 애플리케이션 업데이트

```bash
# 1. 최신 코드 가져오기
git pull origin main

# 2. 이미지 재빌드
docker-compose build app

# 3. 서비스 재시작 (무중단 배포는 아래 참고)
docker-compose up -d app

# 4. 로그 확인
docker-compose logs -f app
```

### 무중단 배포 (Blue-Green Deployment)

```bash
# 1. 새 이미지 빌드
docker-compose build app

# 2. 새 컨테이너를 다른 포트로 실행
docker-compose -f docker-compose-blue.yml up -d

# 3. 헬스체크 확인 후 로드밸런서 스위칭
# (Nginx, HAProxy 등 사용)

# 4. 기존 컨테이너 종료
docker-compose down
```

### 데이터 백업

#### 데이터베이스 백업
```bash
# 백업
docker-compose exec mariadb mariadb-dump -uroot -p${DB_ROOT_PASSWORD} framework > backup_$(date +%Y%m%d).sql

# 복원
docker-compose exec -T mariadb mariadb -uroot -p${DB_ROOT_PASSWORD} framework < backup_20250120.sql
```

#### Redis 데이터 백업
```bash
# 백업 (RDB 스냅샷)
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} BGSAVE

# 백업 파일 복사
docker cp framework-redis:/data/dump.rdb ./redis_backup_$(date +%Y%m%d).rdb
```

#### 업로드 파일 백업
```bash
# 볼륨 백업
docker run --rm -v framework-core-back-end_app_uploads:/source -v $(pwd):/backup alpine tar czf /backup/uploads_backup_$(date +%Y%m%d).tar.gz -C /source .

# 복원
docker run --rm -v framework-core-back-end_app_uploads:/target -v $(pwd):/backup alpine tar xzf /backup/uploads_backup_20250120.tar.gz -C /target
```

### 로그 관리

```bash
# 로그 파일 확인
docker-compose exec app ls -lh /var/log/framework/

# 로그 파일 다운로드
docker cp framework-app:/var/log/framework/application.log ./

# 로그 로테이션 (logrotate 설정 권장)
```

### 모니터링

```bash
# 리소스 사용량 확인
docker stats

# 컨테이너 상세 정보
docker inspect framework-app

# Prometheus 메트릭 확인 (설정된 경우)
curl http://localhost:8080/actuator/prometheus
```

---

## 보안 점검 사항

### 배포 전 필수 체크리스트

- [ ] **환경 변수**: 모든 비밀번호를 강력한 값으로 변경
- [ ] **CORS 설정**: 실제 프론트엔드 도메인으로 설정
- [ ] **HTTPS**: 프로덕션에서는 HTTPS 필수 (`COOKIE_SECURE=true`)
- [ ] **방화벽**: 불필요한 포트 차단
- [ ] **DB 외부 접근**: 내부 네트워크만 접근하도록 설정
- [ ] **Redis 비밀번호**: 강력한 비밀번호 설정
- [ ] **JPA DDL**: `validate`로 설정 (자동 스키마 변경 방지)
- [ ] **로깅**: 프로덕션에서는 DEBUG 레벨 비활성화
- [ ] **파일 업로드**: 디렉토리 권한 확인

### 보안 헤더 확인

애플리케이션이 다음 보안 헤더를 반환하는지 확인:

```bash
curl -I http://localhost:8080/actuator/health

# 확인 항목:
# - X-Frame-Options: DENY
# - X-Content-Type-Options: nosniff
# - Strict-Transport-Security (HTTPS 환경)
# - Referrer-Policy: strict-origin-when-cross-origin
# - Content-Security-Policy
```

### 정기 보안 점검

```bash
# Docker 이미지 취약점 스캔
docker scan framework-backend:latest

# 컨테이너 보안 모범 사례 확인
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image framework-backend:latest
```

---

## 트러블슈팅

### 1. 애플리케이션이 시작되지 않음

**증상**: 컨테이너가 반복해서 재시작됨

**해결 방법**:
```bash
# 로그 확인
docker-compose logs app

# 일반적인 원인:
# - DB 연결 실패: DB 서비스가 정상인지 확인
# - 환경 변수 오류: .env 파일 확인
# - 메모리 부족: JVM 힙 크기 조정
```

### 2. 데이터베이스 연결 오류

**증상**: `Could not open JDBC Connection`

**해결 방법**:
```bash
# MariaDB 상태 확인
docker-compose ps mariadb

# MariaDB 로그 확인
docker-compose logs mariadb

# 연결 테스트
docker-compose exec mariadb mariadb -u${DB_USERNAME} -p${DB_PASSWORD} -h mariadb ${DB_NAME}

# 일반적인 원인:
# - MariaDB 컨테이너가 준비되지 않음: 1-2분 대기 후 재시도
# - 비밀번호 불일치: .env 파일 확인
# - 네트워크 문제: docker network ls 확인
```

### 3. Redis 연결 오류

**증상**: `Unable to connect to Redis`

**해결 방법**:
```bash
# Redis 상태 확인
docker-compose ps redis

# Redis 연결 테스트
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} ping

# 일반적인 원인:
# - Redis 비밀번호 불일치: .env 파일 확인
# - Redis 컨테이너 미실행: docker-compose up -d redis
```

### 4. 메모리 부족 오류

**증상**: `OutOfMemoryError: Java heap space`

**해결 방법**:
```bash
# JVM 힙 크기 증가 (.env 파일 수정)
JVM_MIN_HEAP=1g
JVM_MAX_HEAP=2g

# 서비스 재시작
docker-compose restart app
```

### 5. 파일 업로드 실패

**증상**: 파일 업로드 시 권한 오류

**해결 방법**:
```bash
# 업로드 디렉토리 권한 확인
docker-compose exec app ls -la /app/uploads/board/

# 권한 수정 (필요 시)
docker-compose exec app chmod 755 /app/uploads/board/
```

### 6. CORS 오류

**증상**: 프론트엔드에서 API 호출 시 CORS 오류

**해결 방법**:
```bash
# .env 파일에서 CORS_ALLOWED_ORIGINS 확인
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# 애플리케이션 재시작
docker-compose restart app

# 브라우저 개발자 도구에서 응답 헤더 확인
```

### 7. 로그 확인

```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs app
docker-compose logs mariadb
docker-compose logs redis

# 실시간 로그 (Ctrl+C로 종료)
docker-compose logs -f app

# 최근 100줄
docker-compose logs --tail=100 app
```

---

## 추가 리소스

### 관련 문서
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Docker Documentation](https://docs.docker.com/)
- [MariaDB Documentation](https://mariadb.com/kb/en/documentation/)
- [Redis Documentation](https://redis.io/documentation)

### 프로젝트 문서
- `README.md`: 프로젝트 개요
- `docs/`: 상세 개발 문서
- `.claude/skills.md`: 모듈 개발 가이드

### 지원
- GitHub Issues: <repository-url>/issues
- Email: your-email@example.com

---

## 라이센스

이 프로젝트는 [Your License] 라이선스를 따릅니다.

---

**배포 날짜**: 2025-01-20
**문서 버전**: 1.0.0
**마지막 업데이트**: 2025-01-20
