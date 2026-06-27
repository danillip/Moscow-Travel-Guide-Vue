@echo off
setlocal EnableExtensions

set "PROJECT_DIR=%~dp0"
set "FRONT_URL=http://localhost:5173"
set "API_URL=http://localhost:8080"

cd /d "%PROJECT_DIR%" || goto fatal_path

:menu
cls
echo.
echo  ============================================================
echo    MOSCOW TRAVEL GUIDE - DOCKER CONTROL
echo  ============================================================
echo.
call :print_status
echo.
echo   [1] Start project
echo   [2] Open frontend in browser
echo   [3] Show containers status
echo   [4] Watch logs
echo   [5] Restart project
echo   [6] Rebuild and start
echo   [7] Stop project
echo   [8] Open backend API in browser
echo   [0] Exit
echo.
choice /c 123456780 /n /m "Choose action: "
set "ACTION=%errorlevel%"

if "%ACTION%"=="1" goto start_project
if "%ACTION%"=="2" goto open_project
if "%ACTION%"=="3" goto status_project
if "%ACTION%"=="4" goto logs_project
if "%ACTION%"=="5" goto restart_project
if "%ACTION%"=="6" goto rebuild_project
if "%ACTION%"=="7" goto stop_project
if "%ACTION%"=="8" goto open_api
if "%ACTION%"=="9" goto finish
goto menu

:start_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
echo  Starting containers...
docker compose up -d
if errorlevel 1 goto command_failed
echo.
echo  Done.
echo  Frontend: %FRONT_URL%
echo  API:      %API_URL%
goto pause_menu

:open_project
echo.
echo  Opening frontend...
start "" "%FRONT_URL%"
goto pause_menu

:open_api
echo.
echo  Opening backend API...
start "" "%API_URL%"
goto pause_menu

:status_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
docker compose ps
goto pause_menu

:logs_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
echo  Logs are running. Press Ctrl+C to stop watching logs.
echo.
docker compose logs -f
goto pause_menu

:restart_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
echo  Restarting containers...
docker compose restart
if errorlevel 1 goto command_failed
goto pause_menu

:rebuild_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
echo  Rebuilding containers...
docker compose up -d --build
if errorlevel 1 goto command_failed
goto pause_menu

:stop_project
call :require_docker
if errorlevel 1 goto pause_menu
echo.
echo  Stopping containers...
docker compose down
if errorlevel 1 goto command_failed
goto pause_menu

:print_status
where docker >nul 2>nul
if errorlevel 1 (
    echo   Docker: not found
    echo   Project: not running
    exit /b 0
)

docker info >nul 2>nul
if errorlevel 1 (
    echo   Docker: installed, but Docker Desktop is not running
    echo   Project: not running
    exit /b 0
)

echo   Docker: running
docker compose ps --services --filter "status=running" > "%TEMP%\mtg-docker-running.txt" 2>nul
for %%A in ("%TEMP%\mtg-docker-running.txt") do set "RUNNING_SIZE=%%~zA"
if "%RUNNING_SIZE%"=="0" (
    echo   Project: containers are not running
) else (
    echo   Project: one or more services are running
)
del "%TEMP%\mtg-docker-running.txt" >nul 2>nul
exit /b 0

:require_docker
where docker >nul 2>nul
if errorlevel 1 (
    echo.
    echo  Docker was not found. Install Docker Desktop and try again.
    exit /b 1
)

docker info >nul 2>nul
if errorlevel 1 (
    echo.
    echo  Docker Desktop is not running.
    if exist "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
        echo  Trying to open Docker Desktop...
        start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
        echo  Wait until Docker starts, then choose the action again.
    ) else (
        echo  Start Docker Desktop manually and try again.
    )
    exit /b 1
)

docker compose version >nul 2>nul
if errorlevel 1 (
    echo.
    echo  docker compose is not available. Update Docker Desktop.
    exit /b 1
)
exit /b 0

:command_failed
echo.
echo  The last Docker command failed. The output above should explain why.
goto pause_menu

:fatal_path
echo.
echo  Cannot open project folder:
echo  %PROJECT_DIR%
goto pause_menu

:pause_menu
echo.
pause
goto menu

:finish
echo.
echo  Bye.
endlocal
exit /b 0
