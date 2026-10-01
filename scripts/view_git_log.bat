@echo off
title Spotify System - Git Log Trace Generator (LDCW6123)
color 0B
cd /d "%~dp0\.."

echo =======================================================
echo          SPOTIFY SYSTEM - GIT LOG DEVELOPMENT TRACE
echo =======================================================
echo.

echo --- 1. Compact Graph Log (Rubric Format: git log --oneline --graph) ---
echo.
git log --oneline --graph
echo.
echo =======================================================

echo --- 2. Detailed Timeline Log (With Dates and Timestamps) ---
echo.
git log --date=format:"%%Y-%%m-%%d %%H:%%M:%%S" --pretty=format:"* %%h - %%ad | %%s"
echo.
echo =======================================================

if not exist data mkdir data

:: Export git log to text file in data/ folder for PDF report inclusion
git log --oneline --graph > data\git_log_output.txt
echo.
echo [SUCCESS] Git log trace saved to: data\git_log_output.txt
echo.
echo Press any key to close this window...
pause >nul
