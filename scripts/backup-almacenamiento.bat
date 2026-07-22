@echo off
setlocal enabledelayedexpansion

REM Backup de referencia para entorno de desarrollo (CP-01)
REM Ajustar rutas segun instalacion local.

set FECHA=%date:~-4%%date:~3,2%%date:~0,2%
set DESTINO=backup\%FECHA%
set ORIGEN_ARCHIVOS=backend\data\archivos

echo Creando backup en %DESTINO%...

if not exist "%DESTINO%" mkdir "%DESTINO%"

if exist "%ORIGEN_ARCHIVOS%" (
  xcopy /E /I /Y "%ORIGEN_ARCHIVOS%" "%DESTINO%\archivos"
  echo Archivos copiados.
) else (
  echo Advertencia: no existe %ORIGEN_ARCHIVOS%
)

echo.
echo Para backup de BD ejecutar manualmente, por ejemplo:
echo   pg_dump -h localhost -U rrhh -d rrhh_saas -f "%DESTINO%\rrhh_saas.sql"
echo.
echo Ver docs\RETENCION_RECIBOS.md y docs\RUNBOOK_RESTAURACION_ALMACENAMIENTO.md

endlocal
