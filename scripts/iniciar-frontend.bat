@echo off
cd /d "%~dp0..\frontend"
set NG_CLI_ANALYTICS=false
set CI=true
call npm start
pause
