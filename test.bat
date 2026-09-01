@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0playground\scripts\test.ps1" %*
exit /b %ERRORLEVEL%
