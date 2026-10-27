@echo off
cd /d "%~dp0"
java tools/Build.java --test
pause
