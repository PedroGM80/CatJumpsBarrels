@echo off
REM Cat Jump Barrels - Web Development Server (Windows)
REM This script starts a simple HTTP server to test the web version

setlocal enabledelayedexpansion

cls
echo ============================================================
echo  CAT JUMP BARRELS - Web Development Server
echo ============================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.x from https://www.python.org/
    echo.
    pause
    exit /b 1
)

echo Starting server...
echo.
echo Open your browser and navigate to:
echo   http://localhost:8000/dev-server.html
echo.
echo Press Ctrl+C to stop the server
echo.

python dev-server.py

endlocal
