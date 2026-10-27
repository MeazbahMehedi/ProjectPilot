@echo off
cd /d "%~dp0"
java -jar dist/projectpilot.jar
if errorlevel 1 (
    echo Launch failed. Install Java 17 or newer, with desktop support.
    pause
)
