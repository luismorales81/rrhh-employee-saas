@echo off
call "%~dp0liberar-puerto-unico.bat" 8080
cd /d "%~dp0..\backend"
mvn spring-boot:run
pause
