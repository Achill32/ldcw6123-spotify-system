@echo off
cd /d "%~dp0"
echo Compiling SpotifySystem.java...
javac -d bin src\SpotifySystem.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Launching Spotify System...
java -cp bin SpotifySystem
pause
