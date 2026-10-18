@echo off
setlocal enabledelayedexpansion
title PromptVault Admin Panel & Server
cd /d "%~dp0"

echo ===================================================
echo       PromptVault - Admin Panel Launcher
echo ===================================================
echo.
echo Checking runtime environment...

where node >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Node.js found.
    echo [INFO] Starting local Admin server on http://localhost:3000/ ...
    echo [INFO] Live project files: prompts.json, app_config.json, images/
    echo.
    echo Opening Admin Panel in your default browser...
    timeout /t 1 /nobreak >nul
    start "" "http://localhost:3000/"
    node admin\server.js
    goto :end
)

where python >nul 2>nul
if %errorlevel% equ 0 (
    echo [OK] Python found.
    echo [INFO] Starting Python local Admin server on http://localhost:3000/ ...
    echo.
    echo Opening Admin Panel in your default browser...
    timeout /t 1 /nobreak >nul
    start "" "http://localhost:3000/"
    python admin\server.py
    goto :end
)

echo.
echo [NOTE] Neither Node.js nor Python was detected in PATH.
echo Opening Admin Panel in offline browser mode...
start "" "%~dp0admin\index.html"
echo.
pause

:end
