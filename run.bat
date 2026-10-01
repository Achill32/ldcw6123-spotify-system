@echo off
cd /d "%~dp0"
javac -d bin src\SpotifySystem.java
java -cp bin SpotifySystem
pause
