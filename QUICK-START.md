# Framework Backend 빠른 시작 가이드

## 5분 안에 시작하기

### 1. 환경 변수 설정
```bash
cp .env.example .env
nano .env  # 비밀번호 변경
```

### 2. Docker로 실행
```bash
docker-compose up -d
```

### 3. 서비스 확인
```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# 로그 확인
docker-compose logs -f app
```

### 4. 초기 관리자 생성
```bash
curl -X POST http://localhost:8080/users/admin/initial \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "admin",
    "password": "admin1234!",
    "name": "시스템 관리자"
  }'
```

완료! 🎉

---

## 주요 명령어

```bash
# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose down

# 서비스 재시작
docker-compose restart app

# 로그 확인
docker-compose logs -f app

# 상태 확인
docker-compose ps
```

---

## 주요 엔드포인트

- **헬스체크**: `GET http://localhost:8080/actuator/health`
- **관리자 존재 확인**: `GET http://localhost:8080/users/admin/exists`
- **초기 관리자 생성**: `POST http://localhost:8080/users/admin/initial`
- **로그인**: `POST http://localhost:8080/sessions/login`
- **회원가입**: `POST http://localhost:8080/users/sign-up`

---

## 트러블슈팅

### 컨테이너가 계속 재시작될 때
```bash
docker-compose logs app
# DB 연결 실패 → MariaDB 준비 대기 (1-2분)
# 환경 변수 오류 → .env 파일 확인
```

### 데이터베이스 연결 안 될 때
```bash
docker-compose exec mariadb mariadb -u${DB_USERNAME} -p${DB_PASSWORD} ${DB_NAME}
```

### Redis 연결 안 될 때
```bash
docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} ping
```

---

상세한 내용은 [DEPLOYMENT.md](./DEPLOYMENT.md) 참고
