#!/bin/bash
# V2 테스트 환경 실행 스크립트

echo "=== V2 테스트 환경 실행 ==="

# 1. Docker 테스트 환경 시작
echo "1. Docker 테스트 환경 시작..."
docker-compose -f docker-compose-test.yml up -d

# 2. 컨테이너 상태 확인
echo "2. 컨테이너 상태 확인..."
sleep 5
docker ps | grep v2-test

# 3. DB 연결 테스트
echo "3. DB 연결 테스트..."
if docker exec -it v2-test-mariadb mysql -utestuser -ptestpassword -e "SHOW DATABASES;" > /dev/null 2>&1; then
    echo "✓ DB 연결 성공"
else
    echo "✗ DB 연결 실패"
    exit 1
fi

# 4. 테스트 실행
echo "4. V2 테스트 실행..."
./gradlew test --tests "*RoleServiceTest*" --tests "*PermissionValidationServiceTest*" --tests "*JwtTokenServiceTest*"

# 5. 결과 요약
echo "5. 테스트 종료"
docker-compose -f docker-compose-test.yml down
echo "=== 테스트 완료 ==="