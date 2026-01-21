#!/bin/bash

# Docker 이미지 빌드 및 레지스트리 푸시 스크립트
# 로컬에서 JAR를 빌드하고 Docker 이미지를 생성한 뒤 레지스트리에 푸시합니다.

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# 기본값
IMAGE_NAME="${DOCKER_IMAGE:-framework-backend}"
IMAGE_TAG="${DOCKER_TAG:-latest}"
REGISTRY="${DOCKER_REGISTRY:-}"

# 사용법 출력
usage() {
    echo "사용법: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -i, --image NAME      이미지 이름 (기본값: framework-backend)"
    echo "  -t, --tag TAG         이미지 태그 (기본값: latest)"
    echo "  -r, --registry URL    레지스트리 URL (예: myusername, registry.company.com)"
    echo "  -s, --skip-tests      테스트 건너뛰기"
    echo "  -n, --no-push         푸시 건너뛰기 (빌드만 수행)"
    echo "  -h, --help            도움말 표시"
    echo ""
    echo "예시:"
    echo "  $0 -i framework-backend -t 1.0.0"
    echo "  $0 -r myusername -i framework-backend -t 1.0.0"
    echo "  $0 -r registry.company.com -i framework-backend -t 1.0.0 -n"
    exit 1
}

# 파라미터 파싱
SKIP_TESTS=false
NO_PUSH=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -i|--image)
            IMAGE_NAME="$2"
            shift 2
            ;;
        -t|--tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -n|--no-push)
            NO_PUSH=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo -e "${RED}알 수 없는 옵션: $1${NC}"
            usage
            ;;
    esac
done

# 전체 이미지 이름 구성
if [ -n "$REGISTRY" ]; then
    FULL_IMAGE_NAME="${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
else
    FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Framework Backend 이미지 빌드 시작${NC}"
echo -e "${GREEN}========================================${NC}"
echo "프로젝트 디렉토리: $PROJECT_ROOT"
echo "이미지 이름: $FULL_IMAGE_NAME"
echo ""

# 1. JAR 빌드
cd "$PROJECT_ROOT"

if [ "$SKIP_TESTS" = true ]; then
    echo -e "${YELLOW}[1/4] JAR 빌드 중 (테스트 건너뛰기)...${NC}"
    ./gradlew clean bootJar -x test
else
    echo -e "${YELLOW}[1/4] JAR 빌드 및 테스트 중...${NC}"
    ./gradlew clean build
fi

# JAR 파일 확인
JAR_FILE=$(find build/libs -name "*.jar" ! -name "*-plain.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ JAR 빌드 완료: $JAR_FILE${NC}"
echo ""

# 2. Docker 이미지 빌드
echo -e "${YELLOW}[2/4] Docker 이미지 빌드 중...${NC}"
cd "$SCRIPT_DIR"

docker build \
    -f Dockerfile \
    -t "$FULL_IMAGE_NAME" \
    "$PROJECT_ROOT"

echo -e "${GREEN}✓ Docker 이미지 빌드 완료${NC}"
echo ""

# 3. 이미지 크기 확인
echo -e "${YELLOW}[3/4] 이미지 정보${NC}"
docker images "$FULL_IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
echo ""

# 4. 레지스트리에 푸시
if [ "$NO_PUSH" = false ]; then
    echo -e "${YELLOW}[4/4] Docker 레지스트리에 푸시 중...${NC}"

    # 레지스트리 로그인 확인 (선택 사항)
    if [ -n "$REGISTRY" ] && [[ ! "$REGISTRY" =~ ^[a-zA-Z0-9_-]+$ ]]; then
        echo -e "${YELLOW}레지스트리 로그인이 필요할 수 있습니다.${NC}"
        echo -e "${YELLOW}명령어: docker login $REGISTRY${NC}"
        read -p "계속하시겠습니까? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${RED}푸시가 취소되었습니다.${NC}"
            exit 1
        fi
    fi

    docker push "$FULL_IMAGE_NAME"
    echo -e "${GREEN}✓ Docker 이미지 푸시 완료${NC}"
else
    echo -e "${YELLOW}[4/4] 푸시 건너뛰기 (--no-push 옵션)${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}모든 작업이 완료되었습니다!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "이미지: $FULL_IMAGE_NAME"
echo ""
echo "다음 단계:"
echo "1. 배포 서버에서 다음 명령어로 이미지 가져오기:"
echo "   docker pull $FULL_IMAGE_NAME"
echo ""
echo "2. 배포 서버의 .env 파일에 이미지 설정:"
echo "   DOCKER_IMAGE=$FULL_IMAGE_NAME"
echo ""
echo "3. 배포 서버에서 실행:"
echo "   docker-compose -f docker-compose.prod.yml up -d"
echo ""
