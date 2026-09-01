@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0playground\scripts\dev.ps1"
exit /b %ERRORLEVEL%
