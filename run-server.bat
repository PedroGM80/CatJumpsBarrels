@echo off
echo.
echo ============================================================
echo  Cat Jump Barrels - Web Server
echo ============================================================
echo.

cd /d "%~dp0"

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo.
    echo Please install Python from https://www.python.org/
    pause
    exit /b 1
)

REM Ask which server to run
echo Choose which server to run:
echo.
echo 1. Diagnostic Server (shows build status and missing files)
echo 2. Production Server (serves compiled files)
echo.

set /p choice="Enter choice [1 or 2, default 1]: "
if "%choice%"=="" set choice=1

if "%choice%"=="1" (
    echo.
    echo Starting diagnostic server...
    echo.
    python diagnostic-server.py
) else if "%choice%"=="2" (
    echo.
    echo Starting production server...
    echo.
    python server.py
) else (
    echo Invalid choice
    pause
    exit /b 1
)
