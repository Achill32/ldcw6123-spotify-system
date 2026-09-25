@echo off
title Spotify Interactive System - LDCW6123 Group Project
color 0A

echo =======================================================
echo          SPOTIFY INTERACTIVE SYSTEM (LDCW6123)
echo =======================================================
echo Checking Java installation...

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install JDK/JRE to run this application.
    pause
    exit /b
)

if not exist bin mkdir bin

echo Compiling Java source files from src/ into bin/...
javac -d bin src\Draft.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed! Please check src\Draft.java for errors.
    pause
    exit /b
)

echo Launching Spotify Desktop GUI Application...
start javaw -cp bin Draft

echo App started successfully! You can close this window.
timeout /t 3 >nul
exit
