@echo off
cd /d "%~dp0"
call scripts\liberar-puertos.bat
echo Puertos 8080 y 4200 liberados.
pause
