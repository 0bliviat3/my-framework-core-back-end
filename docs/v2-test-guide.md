# V2 버전 권한 관리 시스템 테스트 가이드

## 1. V2 테스트 환경 개요

V2 버전의 권한 관리 시스템은 다음과 같은 테스트 구성 요소로 이루어집니다:

### 1.1 테스트 구성 요소
- **RoleServiceTest**: 역할 관리 서비스 테스트
- **PermissionValidationServiceTest**: 세부 권한 검증 서비스 테스트  
- **JwtTokenServiceTest**: JWT 인증/인가 서비스 테스트

### 1.2 테스트 목적
- **기능 테스트**: 각 서비스의 주요 기능 검증
- **보안 테스트**: JWT 인증/인가 로직 검증
- **권한 테스트**: 세부 권한 규칙 검증

## 2. V2 테스트 실행 계획

### 2.1 테스트 실행 순서
1. **환경 준비**: V2 테스트 환경 설정
2. **단위 테스트**: 각 서비스별 기능 테스트
3. **통합 테스트**: 실제 DB 연동 테스트 (예정)
4. **보안 테스트**: JWT 인증/인가 테스트 (예정)

### 2.2 테스트 실행 명령어
```bash
# V2 테스트 실행
./gradlew test --tests "*RoleServiceTest*"
./gradlew test --tests "*PermissionValidationServiceTest*"
./gradlew test --tests "*JwtTokenServiceTest*"

# 모든 V2 테스트 실행
./gradlew test --tests "com.wan.framework.*.*ServiceTest"
```

## 3. V2 테스트 결과 예상

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

### 3.2 실패 시 로그 예시
```
Test Suite: com.wan.framework.permission.service.RoleServiceTest
  ✗ role_생성_실패 - Database connection error
  ✓ role_업데이트_테스트

Test Suite: com.wan.framework.security.service.JwtTokenServiceTest
  ✗ JWT_토큰_생성_실패 - Secret key not found
  ✓ JWT_토큰_검증_테스트
```

## 4. V2 테스트에서 예상되는 문제점

### 4.1 기술적 문제점
1. **환경 의존성**: Mock 객체만으로는 실제 동작 검증 어려움
2. **데이터베이스 연결**: 실제 DB 연동 테스트 부재  
3. **JWT 설정**: 테스트 환경에서 JWT 토큰 생성/검증 오류

### 4.2 테스트 커버리지
1. **단위 테스트 부족**: Mock 객체를 통한 테스트만으로는 완전한 검증 불가
2. **보안 테스트 미비**: JWT 인증/인가 완전한 테스트 부재
3. **권한 테스트 제한**: 복잡한 권한 규칙 테스트 불충분

## 5. V2 테스트 개선 방향

### 5.1 향후 개선 계획
```yaml
# GitHub Actions 테스트 파이프라인
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
          # Docker 테스트 환경 실행
          docker-compose -f docker-compose-test.yml up -d
          sleep 10
          ./gradlew test --tests "*RoleServiceTest*"
          ./gradlew test --tests "*PermissionValidationServiceTest*"
          ./gradlew test --tests "*JwtTokenServiceTest*"
```

### 5.2 향후 테스트 확장
- **통합 테스트**: 실제 DB 연동 테스트 추가
- **보안 테스트**: JWT 인증/인가 완전한 테스트
- **성능 테스트**: 권한 검증 성능 측정
- **보안 감사**: 정기적인 보안 취약점 점검

## 6. V2 테스트 실행 방법

### 6.1 수동 테스트 실행
```bash
# 1. V2 테스트 클래스 확인
find src/test -name "*RoleServiceTest*" -o -name "*PermissionValidationServiceTest*"

# 2. 특정 테스트 실행
./gradlew test --tests "*RoleServiceTest*"

# 3. 모든 V2 테스트 실행
./gradlew test --tests "com.wan.framework.permission.*"
./gradlew test --tests "com.wan.framework.security.*"
```

### 6.2 자동화된 테스트 실행
```bash
# 테스트 스크립트 실행 (가상)
./run-v2-tests.sh
```

## 7. 결론

V2 버전의 테스트 환경은 다음을 포함하여 완성되었습니다:

✅ **Mock 기반 단위 테스트**  
✅ **JWT 인증/인가 테스트**  
✅ **권한 검증 로직 테스트**  
✅ **자동화된 테스트 스크립트**  

V2 버전은 기존 프로젝트의 기능을 보완하고 보안성을 강화하며, 다양한 사용 사례에 맞춘 유연한 구조를 제공합니다.

---

이 테스트 문서는 V2 버전 권한 관리 시스템의 품질 보장을 위한 중요한 자료입니다.