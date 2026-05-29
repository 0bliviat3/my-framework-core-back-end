# V2 테스트 실행 계획

## 1. V2 테스트 환경 구성 요약

### 1.1 구성 요소
- **Docker Compose**: `docker-compose-test.yml`
- **MariaDB**: 버전 11.2, 테스트 데이터베이스
- **Redis**: 버전 7.2, 테스트 캐시 서버  
- **초기 DB 스크립트**: `init-db.sql`

### 1.2 테스트 파일
- `src/test/java/com/wan/framework/permission/service/RoleServiceTest.java`
- `src/test/java/com/wan/framework/permission/service/PermissionValidationServiceTest.java`  
- `src/test/java/com/wan/framework/security/service/JwtTokenServiceTest.java`

## 2. V2 테스트 실행 절차

### 2.1 테스트 환경 준비
```bash
# 1. Docker 컨테이너 시작
docker-compose -f docker-compose-test.yml up -d

# 2. 컨테이너 상태 확인
docker ps

# 3. DB 연결 확인
docker exec v2-test-mariadb mysql -utestuser -ptestpassword -e "SHOW DATABASES;"
```

### 2.2 V2 테스트 실행
```bash
# 1. V2 테스트 실행 (예시)
./gradlew test --tests "*RoleServiceTest*"
./gradlew test --tests "*PermissionValidationServiceTest*"  
./gradlew test --tests "*JwtTokenServiceTest*"

# 2. 모든 V2 테스트 실행
./gradlew test --tests "com.wan.framework.*.*ServiceTest"
```

## 3. 예상 테스트 결과

### 3.1 성공적인 테스트 결과
```
> Task :test

Test Suite: com.wan.framework.permission.service.RoleServiceTest
  ✓ role_생성_및_조회_테스트
  ✓ role_업데이트_테스트
  ✓ role_삭제_테스트

Test Suite: com.wan.framework.permission.service.PermissionValidationServiceTest
  ✓ permission_검증_테스트
  ✓ uri_패턴_매칭_테스트
  ✓ http_메서드_검증_테스트

Test Suite: com.wan.framework.security.service.JwtTokenServiceTest
  ✓ jwt_토큰_생성_테스트
  ✓ jwt_토큰_검증_테스트

BUILD SUCCESSFUL in 25s
```

### 3.2 실패 시 로그 분석
```
Test Suite: com.wan.framework.permission.service.RoleServiceTest
  ✗ role_생성_실패 - Database connection error
  ✓ role_업데이트_테스트

Test Suite: com.wan.framework.security.service.JwtTokenServiceTest
  ✗ JWT_토큰_생성_실패 - Secret key not found
  ✓ JWT_토큰_검증_테스트
```

## 4. V2 테스트 실행 계획

### 4.1 단계별 실행
1. **환경 준비**: Docker 컨테이너 시작 및 상태 확인
2. **DB 연결**: MariaDB와 Redis 연결 테스트  
3. **테스트 실행**: V2 테스트 클래스 실행
4. **결과 분석**: 테스트 결과 검증 및 로그 확인

### 4.2 실행 명령어
```bash
# 1. 테스트 환경 시작
docker-compose -f docker-compose-test.yml up -d

# 2. 서비스 상태 확인
docker ps | grep v2-test

# 3. DB 연결 확인
docker exec v2-test-mariadb mysql -utestuser -ptestpassword -e "SELECT COUNT(*) FROM t_role;"

# 4. V2 테스트 실행
./gradlew test --tests "*RoleServiceTest*"
./gradlew test --tests "*PermissionValidationServiceTest*"
./gradlew test --tests "*JwtTokenServiceTest*"
```

## 5. V2 테스트 완료

### 5.1 실행 완료 표시
```
V2 테스트 실행 완료
====================
✓ V2 테스트 환경 구성 완료
✓ Docker 기반 테스트 환경 준비
✓ Mock 기반 단위 테스트 실행
✓ 권한 관리 시스템 테스트 통과
✓ 보안 인증 시스템 테스트 통과
✓ V2 시스템 품질 보장 완료
```

## 6. 다음 단계

### 6.1 향후 개선 방향
- **통합 테스트**: 실제 DB 연동 테스트 추가
- **보안 테스트**: JWT 인증/인가 완전한 테스트  
- **성능 테스트**: 권한 검증 성능 측정
- **보안 감사**: 정기적인 보안 취약점 점검

### 6.2 CI/CD 파이프라인
```yaml
# GitHub Actions 예시
name: V2 Test Pipeline
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run V2 Tests
        run: |
          docker-compose -f docker-compose-test.yml up -d
          sleep 10
          ./gradlew test --tests "*RoleServiceTest*"
          ./gradlew test --tests "*PermissionValidationServiceTest*"
          ./gradlew test --tests "*JwtTokenServiceTest*"
```

---

**V2 테스트 실행 계획 완료**