@echo off
setlocal
set "PUERTO=%~1"
if not defined PUERTO exit /b 0
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PUERTO% " ^| findstr LISTENING') do (
    echo   Puerto %PUERTO% ocupado por PID %%a - finalizando...
    taskkill /F /PID %%a >nul 2>&1
)
endlocal
exit /b 0
