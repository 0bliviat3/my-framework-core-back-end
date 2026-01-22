# Dockerfile for Pre-built JAR
# 로컬에서 미리 빌드한 JAR 파일을 사용하여 Docker 이미지 생성

FROM eclipse-temurin:17-jre-alpine

# 메타데이터
LABEL maintainer="your-email@example.com"
LABEL description="Framework Core Backend Application (Pre-built)"
LABEL version="1.0.0"

# 보안: 비root 사용자 생성
RUN addgroup -g 1000 appmanager && \
    adduser -D -u 1000 -G appmanager appmanager

# 작업 디렉토리 생성
WORKDIR /app

# 필요한 디렉토리 생성
RUN mkdir -p /app/uploads/board /var/log/framework && \
    chown -R appmanager:appmanager /app /var/log/framework

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 빌드된 JAR 파일 복사 (build/libs에서)
# 주의: 이 Dockerfile을 실행하기 전에 ./gradlew bootJar로 빌드 필요
COPY --chown=appmanager:appmanager ../build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# 비root 사용자로 전환
USER appmanager

# JVM 옵션 설정 (환경변수로 오버라이드 가능)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Spring Profile 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
