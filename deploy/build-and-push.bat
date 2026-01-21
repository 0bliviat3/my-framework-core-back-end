@echo off
REM Docker Image Build and Push Script (Windows)
REM Build JAR locally, create Docker image, and push to registry

setlocal enabledelayedexpansion

REM Configuration
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
cd /d "%PROJECT_ROOT%"
set "PROJECT_ROOT=%CD%"

REM Default values
set "IMAGE_NAME=framework-backend"
set "IMAGE_TAG=latest"
set "REGISTRY="
set "SKIP_TESTS=false"
set "NO_PUSH=false"

REM Parse arguments
:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="-i" set "IMAGE_NAME=%~2" & shift & shift & goto parse_args
if /i "%~1"=="--image" set "IMAGE_NAME=%~2" & shift & shift & goto parse_args
if /i "%~1"=="-t" set "IMAGE_TAG=%~2" & shift & shift & goto parse_args
if /i "%~1"=="--tag" set "IMAGE_TAG=%~2" & shift & shift & goto parse_args
if /i "%~1"=="-r" set "REGISTRY=%~2" & shift & shift & goto parse_args
if /i "%~1"=="--registry" set "REGISTRY=%~2" & shift & shift & goto parse_args
if /i "%~1"=="-s" set "SKIP_TESTS=true" & shift & goto parse_args
if /i "%~1"=="--skip-tests" set "SKIP_TESTS=true" & shift & goto parse_args
if /i "%~1"=="-n" set "NO_PUSH=true" & shift & goto parse_args
if /i "%~1"=="--no-push" set "NO_PUSH=true" & shift & goto parse_args
if /i "%~1"=="-h" goto usage
if /i "%~1"=="--help" goto usage
shift
goto parse_args

:end_parse

REM Build full image name
if not "%REGISTRY%"=="" (
    set "FULL_IMAGE_NAME=%REGISTRY%/%IMAGE_NAME%:%IMAGE_TAG%"
) else (
    set "FULL_IMAGE_NAME=%IMAGE_NAME%:%IMAGE_TAG%"
)

echo ========================================
echo Framework Backend Image Build Started
echo ========================================
echo Project Directory: %PROJECT_ROOT%
echo Image Name: %FULL_IMAGE_NAME%
echo.

REM Step 1: Build JAR
cd /d "%PROJECT_ROOT%"

if "%SKIP_TESTS%"=="true" goto build_skip_tests
echo [1/4] Building JAR with tests...
call gradlew.bat clean build
goto build_done

:build_skip_tests
echo [1/4] Building JAR without tests...
call gradlew.bat clean bootJar -x test

:build_done

if errorlevel 1 (
    echo Error: JAR build failed
    exit /b 1
)

REM Find JAR file
set "JAR_FILE="
for %%f in (build\libs\*.jar) do (
    set "filename=%%~nf"
    if not "!filename:plain=!"=="!filename!" (
        REM Skip plain JAR
    ) else (
        set "JAR_FILE=%%f"
        goto found_jar
    )
)

:found_jar
if "%JAR_FILE%"=="" (
    echo Error: JAR file not found
    exit /b 1
)

echo [OK] JAR build completed: %JAR_FILE%
echo.

REM Step 2: Build Docker image
echo [2/4] Building Docker image...
cd /d "%SCRIPT_DIR%"

docker build -f Dockerfile -t "%FULL_IMAGE_NAME%" "%PROJECT_ROOT%"

if errorlevel 1 (
    echo Error: Docker image build failed
    exit /b 1
)

echo [OK] Docker image build completed
echo.

REM Step 3: Show image info
echo [3/4] Image Information
docker images "%FULL_IMAGE_NAME%"
echo.

REM Step 4: Push to registry
if "%NO_PUSH%"=="true" goto skip_push

echo [4/4] Pushing to Docker registry...
docker push "%FULL_IMAGE_NAME%"

if errorlevel 1 (
    echo Error: Docker image push failed
    echo You may need to login: docker login %REGISTRY%
    exit /b 1
)

echo [OK] Docker image push completed
goto push_done

:skip_push
echo [4/4] Push skipped (--no-push option)

:push_done

echo.
echo ========================================
echo All tasks completed successfully!
echo ========================================
echo.
echo Image: %FULL_IMAGE_NAME%
echo.
echo Next steps:
echo 1. Pull image on deployment server:
echo    docker pull %FULL_IMAGE_NAME%
echo.
echo 2. Set image in .env file on deployment server:
echo    DOCKER_IMAGE=%FULL_IMAGE_NAME%
echo.
echo 3. Run on deployment server:
echo    docker-compose -f docker-compose.prod.yml up -d
echo.

exit /b 0

:usage
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   -i, --image NAME      Image name (default: framework-backend)
echo   -t, --tag TAG         Image tag (default: latest)
echo   -r, --registry URL    Registry URL (e.g., myusername, registry.company.com)
echo   -s, --skip-tests      Skip tests
echo   -n, --no-push         Skip push (build only)
echo   -h, --help            Show this help
echo.
echo Examples:
echo   %~nx0 -i framework-backend -t 1.0.0
echo   %~nx0 -r myusername -i framework-backend -t 1.0.0
echo   %~nx0 -r registry.company.com -i framework-backend -t 1.0.0 -n
exit /b 0
