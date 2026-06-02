@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%spark-submit-wrapper.ps1" %*
exit /b %errorlevel%
