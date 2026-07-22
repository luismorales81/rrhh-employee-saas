@echo off
setlocal EnableDelayedExpansion

echo Liberando puertos 8080, 4200 y 4201 si estan ocupados...

call :liberar_puerto 8080
call :liberar_puerto 4200
call :liberar_puerto 4201

echo Listo.
exit /b 0

:liberar_puerto
call "%~dp0liberar-puerto-unico.bat" %~1
exit /b 0
