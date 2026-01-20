# 멀티 스테이지 빌드 - Stage 1: 빌드
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle clean bootJar -x test --no-daemon

# 멀티 스테이지 빌드 - Stage 2: 실행
FROM eclipse-temurin:17-jre-alpine

# 메타데이터
LABEL maintainer="your-email@example.com"
LABEL description="Framework Core Backend Application"
LABEL version="1.0.0"

# 보안: 비root 사용자 생성
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# 작업 디렉토리 생성
WORKDIR /app

# 필요한 디렉토리 생성
RUN mkdir -p /app/uploads/board /var/log/framework && \
    chown -R appuser:appuser /app /var/log/framework

# 타임존 설정 (선택 사항)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown appuser:appuser app.jar

# 비root 사용자로 전환
USER appuser

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정 (환경변수로 오버라이드 가능)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Spring Profile 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
