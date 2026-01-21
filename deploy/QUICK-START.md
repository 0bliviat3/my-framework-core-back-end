# 5분 빠른 시작 가이드

## 🚀 원하시는 배포 플로우

```
[로컬] JAR 빌드
   ↓
[로컬] Docker 이미지 생성
   ↓
[로컬] Docker Hub/Registry에 푸시
   ↓
[배포 서버] docker-compose.yml + .env 설정
   ↓
[배포 서버] docker-compose up -d
   ↓
✅ 배포 완료!
```

---

## 로컬 환경 (개발자 PC)

### 1단계: Docker Hub 로그인

```bash
docker login
# Username: myusername
# Password: ********
```

### 2단계: 빌드 및 푸시

```bash
cd deploy

# Linux/Mac
chmod +x build-and-push.sh
./build-and-push.sh -r myusername -i framework-backend -t 1.0.0

# Windows
build-and-push.bat -r myusername -i framework-backend -t 1.0.0
```

**완료!** 이미지가 Docker Hub에 업로드됩니다.

---

## 배포 서버

### 1단계: 배포 파일 준비

```bash
# 배포 디렉토리 생성
mkdir -p /opt/framework/deploy
cd /opt/framework/deploy

# 필요한 파일 복사 (Git 또는 수동)
# - docker-compose.prod.yml
# - .env.example
```

### 2단계: .env 파일 설정

```bash
cp .env.example .env
nano .env
```

**필수 수정 항목:**
```env
# 이미지 설정
DOCKER_IMAGE=myusername/framework-backend:1.0.0

# DB 비밀번호
DB_ROOT_PASSWORD=your_secure_password
DB_PASSWORD=your_secure_password

# Redis 비밀번호
REDIS_PASSWORD=your_secure_password

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# 보안
COOKIE_SECURE=true
```

### 3단계: 배포 실행

```bash
docker-compose -f docker-compose.prod.yml up -d
```

**완료!** 🎉

---

## 확인

```bash
# 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app

# 헬스체크
curl http://localhost:8080/actuator/health
```

---

## 업데이트

### 로컬: 새 버전 푸시

```bash
cd deploy
./build-and-push.sh -r myusername -i framework-backend -t 1.0.1
```

### 배포 서버: 업데이트

```bash
# .env 수정
nano .env
# DOCKER_IMAGE=myusername/framework-backend:1.0.1

# 재배포
docker-compose -f docker-compose.prod.yml pull app
docker-compose -f docker-compose.prod.yml up -d app
```

---

## 요약

✅ **맞습니다!** 원하시는 플로우대로:

1. ✅ 로컬에서 JAR 빌드
2. ✅ Dockerfile로 이미지 생성
3. ✅ Docker Hub/Registry에 푸시
4. ✅ 배포 서버에서 .env만 설정
5. ✅ docker-compose up -d로 바로 배포

**기존 프로세스와 영향 없음!**
- `/deploy` 디렉토리만 사용
- 루트의 `Dockerfile`, `docker-compose.yml`은 그대로 유지
