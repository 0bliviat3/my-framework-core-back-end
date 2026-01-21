# DEPLOYMENT.md에 추가할 JAR 배포 섹션

아래 내용을 DEPLOYMENT.md의 "Docker로 배포" 섹션 다음에 추가하세요.

---

## JAR로 직접 배포

Docker 없이 JAR 파일을 직접 실행하여 배포할 수 있습니다.

### 중요: .env 파일 사용 가능 여부

**질문:** JAR로 패키징하면 .env 변경이 불가능한가?

**답변:** **맞습니다!** 기본적으로 JAR 직접 실행 시 `.env` 파일을 읽지 않습니다.

| 배포 방법 | .env 파일 사용 | 설명 |
|----------|---------------|------|
| Docker Compose | ✅ 가능 | docker-compose.yml에서 자동 로드 |
| JAR 직접 실행 | ❌ 불가능 | 기본적으로 .env 읽지 않음 |
| **JAR + start.sh** | ✅ 가능 | **스크립트가 .env 읽어 환경 변수로 전달 (권장)** |

### 해결 방법 1: 실행 스크립트 사용 (권장)

`.env` 파일을 읽어 환경 변수로 설정한 후 JAR를 실행하는 스크립트를 제공합니다.

#### Linux/Mac

```bash
# 1. .env 파일 설정
cp .env.example .env
nano .env  # 환경 변수 수정

# 2. 빌드
./gradlew clean bootJar

# 3. 실행 스크립트에 권한 부여
chmod +x start.sh

# 4. 실행
./start.sh
```

#### Windows

```cmd
REM 1. .env 파일 설정
copy .env.example .env
notepad .env

REM 2. 빌드
gradlew clean bootJar

REM 3. 실행
start.bat
```

#### 백그라운드 실행

```bash
# nohup 사용 (Linux)
nohup ./start.sh > app.log 2>&1 &

# 프로세스 확인
ps aux | grep java

# 로그 확인
tail -f app.log

# 종료
kill <PID>
```

### 해결 방법 2: 외부 설정 파일 사용

JAR 파일과 같은 디렉토리에 `config/application-prod.yml`을 배치합니다.

```bash
# 디렉토리 구조
├── framework-backend.jar
├── config/
│   └── application-prod.yml  # 환경별 설정 (비밀번호 등)
└── logs/

# config/application-prod.yml 수정
nano config/application-prod.yml

# JAR 실행 (외부 설정 파일 자동 로드)
java -jar framework-backend.jar --spring.profiles.active=prod
```

**보안 강화:**
```bash
# 설정 파일 권한 제한 (읽기 전용, 소유자만)
chmod 600 config/application-prod.yml
```

### 시스템 서비스로 등록 (Systemd)

#### 서비스 파일 생성

```bash
sudo nano /etc/systemd/system/framework-backend.service
```

#### 서비스 파일 내용

```ini
[Unit]
Description=Framework Backend Service
After=network.target mariadb.service redis.service

[Service]
Type=simple
User=framework
Group=framework
WorkingDirectory=/opt/framework
EnvironmentFile=/opt/framework/.env
ExecStart=/usr/bin/java -jar /opt/framework/framework-backend.jar --spring.profiles.active=prod
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

StandardOutput=journal
StandardError=journal
SyslogIdentifier=framework-backend

NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

#### 서비스 관리

```bash
# 서비스 등록 및 시작
sudo systemctl daemon-reload
sudo systemctl enable framework-backend
sudo systemctl start framework-backend

# 서비스 상태 확인
sudo systemctl status framework-backend

# 로그 확인
sudo journalctl -u framework-backend -f

# 서비스 재시작
sudo systemctl restart framework-backend
```

### JAR 배포 요약

| 방법 | 편의성 | 보안성 | .env 사용 | 권장도 |
|------|--------|--------|-----------|--------|
| **start.sh 스크립트** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | **권장** |
| 외부 설정 파일 | ⭐⭐⭐⭐ | ⭐⭐⭐ | △ | 권장 |
| Systemd 서비스 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | 권장 |

**상세 가이드:** `docs/JAR-DEPLOYMENT.md` 참고

---
