# 프로덕션 배포 가이드 (Docker Registry)

## 개요

이 디렉토리는 **Docker Registry를 사용한 프로덕션 배포**를 위한 설정 파일들을 포함합니다.

**배포 플로우:**
```
[로컬 개발 환경]                    [Docker Registry]                 [배포 서버]
     │                                    │                               │
     ├─ 1. JAR 빌드                       │                               │
     │  ./gradlew bootJar                 │                               │
     │                                    │                               │
     ├─ 2. Docker 이미지 생성             │                               │
     │  ./build-and-push.sh               │                               │
     │                                    │                               │
     ├─ 3. 이미지 푸시 ──────────────────>│                               │
     │  docker push                       │                               │
     │                                    │                               │
     │                                    ├─ 4. 이미지 저장               │
     │                                    │                               │
     │                                    │  5. 이미지 다운로드 <─────────┤
     │                                    │     docker pull               │
     │                                    │                               │
     │                                    │                               ├─ 6. .env 설정
     │                                    │                               │
     │                                    │                               ├─ 7. 배포 실행
     │                                    │                               │  docker-compose up -d
```

---

## 디렉토리 구조

```
deploy/
├── Dockerfile                    # 빌드된 JAR를 사용하는 Dockerfile
├── docker-compose.prod.yml       # 프로덕션용 Compose 파일 (Registry 이미지 사용)
├── .env.example                  # 환경 변수 예시 파일
├── build-and-push.sh             # 빌드 및 푸시 스크립트 (Linux/Mac)
├── build-and-push.bat            # 빌드 및 푸시 스크립트 (Windows)
└── README.md                     # 이 파일
```

---

## 사전 준비

### 1. Docker Registry 선택

다음 중 하나를 선택하세요:

#### A. Docker Hub (공개/비공개)
- URL: https://hub.docker.com
- 무료 계정: 1개의 Private Repository
- 이미지 이름: `myusername/framework-backend:1.0.0`

#### B. Private Registry (회사 내부)
- 예: `registry.company.com`
- 이미지 이름: `registry.company.com/framework-backend:1.0.0`

#### C. AWS ECR, GCP GCR, Azure ACR
- AWS ECR: `123456789.dkr.ecr.region.amazonaws.com/framework-backend:1.0.0`
- GCP GCR: `gcr.io/project-id/framework-backend:1.0.0`
- Azure ACR: `myregistry.azurecr.io/framework-backend:1.0.0`

### 2. Docker Registry 로그인

```bash
# Docker Hub
docker login

# Private Registry
docker login registry.company.com

# AWS ECR (AWS CLI 필요)
aws ecr get-login-password --region region | docker login --username AWS --password-stdin 123456789.dkr.ecr.region.amazonaws.com
```

---

## 로컬 환경 (개발자 PC)

### 1단계: JAR 빌드 및 이미지 생성

#### Linux/Mac

```bash
cd deploy

# 실행 권한 부여
chmod +x build-and-push.sh

# 빌드 및 푸시 (Docker Hub 예시)
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0

# 빌드 및 푸시 (Private Registry 예시)
./build-and-push.sh -r registry.company.com -i framework-backend -t 1.0.0

# 빌드만 수행 (푸시 건너뛰기)
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0 -n

# 테스트 건너뛰기
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0 -s
```

#### Windows

```cmd
cd deploy

REM 빌드 및 푸시 (Docker Hub 예시)
build-and-push.bat -r myusername -i framework-backend -t 1.0.0

REM 빌드 및 푸시 (Private Registry 예시)
build-and-push.bat -r registry.company.com -i framework-backend -t 1.0.0
```

#### 스크립트 옵션

| 옵션 | 설명 | 예시 |
|------|------|------|
| `-i, --image` | 이미지 이름 | `-i framework-backend` |
| `-t, --tag` | 이미지 태그 | `-t 1.0.0` |
| `-r, --registry` | 레지스트리 URL | `-r myusername` |
| `-s, --skip-tests` | 테스트 건너뛰기 | `-s` |
| `-n, --no-push` | 푸시 건너뛰기 | `-n` |

### 2단계: 이미지 확인

```bash
# 로컬 이미지 확인
docker images | grep framework-backend

# 예상 출력:
# myusername/framework-backend   1.0.0   abc123def456   2 minutes ago   300MB
```

### 3단계: Registry에 푸시 완료 확인

- **Docker Hub**: https://hub.docker.com에서 확인
- **Private Registry**: 웹 UI 또는 API로 확인

---

## 배포 서버

### 1단계: 배포 파일 준비

배포 서버에 다음 파일들을 복사합니다:

```bash
# 배포 서버에 디렉토리 생성
mkdir -p /opt/framework
cd /opt/framework

# 필요한 파일 복사 (로컬에서 배포 서버로)
# - docker-compose.prod.yml
# - .env.example
```

또는 Git 저장소에서 직접 가져오기:

```bash
cd /opt/framework
git clone <repository-url> .
cd deploy
```

### 2단계: 환경 변수 설정

```bash
cd /opt/framework/deploy

# .env 파일 생성
cp .env.example .env

# .env 파일 편집
nano .env
```

**.env 파일 필수 수정 항목:**

```env
# Docker 이미지 (Registry에서 가져올 이미지)
DOCKER_IMAGE=myusername/framework-backend:1.0.0

# 데이터베이스 비밀번호
DB_ROOT_PASSWORD=your_secure_root_password
DB_PASSWORD=your_secure_user_password

# Redis 비밀번호
REDIS_PASSWORD=your_secure_redis_password

# CORS 허용 도메인
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# 쿠키 보안 (HTTPS 사용 시)
COOKIE_SECURE=true
COOKIE_SAME_SITE=strict
```

### 3단계: 이미지 다운로드 (선택 사항)

```bash
# 이미지를 미리 다운로드 (선택 사항, docker-compose가 자동으로 다운로드함)
docker pull myusername/framework-backend:1.0.0
```

### 4단계: 서비스 실행

```bash
cd /opt/framework/deploy

# 서비스 시작
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app

# 서비스 상태 확인
docker-compose -f docker-compose.prod.yml ps
```

### 5단계: 헬스체크

```bash
# 헬스체크 API 확인
curl http://localhost:8080/actuator/health

# 예상 응답:
# {"status":"UP"}
```

---

## 서비스 관리

### 서비스 중지

```bash
cd /opt/framework/deploy
docker-compose -f docker-compose.prod.yml stop
```

### 서비스 재시작

```bash
cd /opt/framework/deploy
docker-compose -f docker-compose.prod.yml restart
```

### 애플리케이션 업데이트

```bash
# 1. 새 이미지 빌드 및 푸시 (로컬 환경)
./build-and-push.sh -r myusername -i framework-backend -t 1.0.1

# 2. .env 파일 업데이트 (배포 서버)
nano .env
# DOCKER_IMAGE=myusername/framework-backend:1.0.1

# 3. 서비스 재시작 (배포 서버)
cd /opt/framework/deploy
docker-compose -f docker-compose.prod.yml pull app
docker-compose -f docker-compose.prod.yml up -d app

# 4. 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app
```

### 로그 확인

```bash
# 전체 로그
docker-compose -f docker-compose.prod.yml logs

# 특정 서비스 로그
docker-compose -f docker-compose.prod.yml logs app

# 실시간 로그
docker-compose -f docker-compose.prod.yml logs -f app

# 최근 100줄
docker-compose -f docker-compose.prod.yml logs --tail=100 app
```

---

## 버전 관리 전략

### Semantic Versioning

```bash
# 메이저 버전 (호환되지 않는 변경)
./build-and-push.sh -r myusername -i framework-backend -t 2.0.0

# 마이너 버전 (기능 추가, 호환 가능)
./build-and-push.sh -r myusername -i framework-backend -t 1.1.0

# 패치 버전 (버그 수정)
./build-and-push.sh -r myusername -i framework-backend -t 1.0.1
```

### 태그 전략

```bash
# 특정 버전 태그
-t 1.0.0

# 환경별 태그
-t prod
-t staging
-t dev

# Git 커밋 해시 사용
-t $(git rev-parse --short HEAD)

# 날짜 기반
-t $(date +%Y%m%d)
```

---

## 트러블슈팅

### 1. 이미지를 찾을 수 없음

**증상:**
```
Error response from daemon: manifest for myusername/framework-backend:1.0.0 not found
```

**해결:**
```bash
# 레지스트리 로그인 확인
docker login

# 이미지 푸시 재시도
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0

# Registry에서 이미지 확인
docker search myusername/framework-backend
```

### 2. JAR 파일을 찾을 수 없음

**증상:**
```
Error: JAR 파일을 찾을 수 없습니다.
```

**해결:**
```bash
# 프로젝트 루트에서 빌드
cd ..
./gradlew clean bootJar

# JAR 파일 확인
ls -la build/libs/*.jar

# 다시 배포 스크립트 실행
cd deploy
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0
```

### 3. Docker 빌드 실패

**증상:**
```
ERROR: failed to solve: failed to compute cache key
```

**해결:**
```bash
# 빌드 캐시 제거
docker builder prune -a

# 다시 빌드
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0
```

### 4. 환경 변수가 반영되지 않음

**증상:**
설정을 변경했는데 적용되지 않음

**해결:**
```bash
# .env 파일 확인
cat .env

# 컨테이너 환경 변수 확인
docker-compose -f docker-compose.prod.yml exec app env | grep DB_

# 서비스 재시작
docker-compose -f docker-compose.prod.yml restart app
```

---

## 보안 고려사항

### 1. .env 파일 보호

```bash
# 파일 권한 설정 (소유자만 읽기/쓰기)
chmod 600 .env

# Git에서 제외 (.gitignore에 추가)
echo "deploy/.env" >> ../.gitignore
```

### 2. Registry 인증

```bash
# Docker Hub: 2FA 활성화 권장
# Private Registry: HTTPS + 인증 필수
```

### 3. 이미지 스캔

```bash
# Docker Hub에서 자동 스캔
# 또는 Trivy 사용
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image myusername/framework-backend:1.0.0
```

---

## CI/CD 연동 예시

### GitHub Actions

```yaml
name: Build and Push Docker Image

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build JAR
        run: ./gradlew clean bootJar

      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and Push
        run: |
          cd deploy
          ./build-and-push.sh -r ${{ secrets.DOCKER_USERNAME }} -i framework-backend -t ${{ github.ref_name }}
```

---

## 요약

### 로컬 환경 (1회)

```bash
# 1. JAR 빌드 및 이미지 푸시
cd deploy
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0
```

### 배포 서버 (1회 설정)

```bash
# 1. 배포 파일 준비
cd /opt/framework/deploy

# 2. .env 설정
cp .env.example .env
nano .env
# DOCKER_IMAGE=myusername/framework-backend:1.0.0 설정

# 3. 서비스 실행
docker-compose -f docker-compose.prod.yml up -d
```

### 업데이트 시 (로컬)

```bash
# 새 버전 빌드 및 푸시
cd deploy
./build-and-push.sh -r myusername -i framework-backend -t 1.0.1
```

### 업데이트 시 (배포 서버)

```bash
# .env 업데이트
nano .env  # DOCKER_IMAGE=myusername/framework-backend:1.0.1

# 서비스 업데이트
docker-compose -f docker-compose.prod.yml pull app
docker-compose -f docker-compose.prod.yml up -d app
```

---

**완료!** 🎉

이제 로컬에서 JAR를 빌드하고, Docker 이미지를 생성하여 Registry에 푸시한 뒤, 배포 서버에서 .env 파일만 설정하면 바로 배포할 수 있습니다!
