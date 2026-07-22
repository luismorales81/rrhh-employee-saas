@echo off
setlocal enabledelayedexpansion

REM Backup de referencia para producción / staging con Docker PostgreSQL local
REM Uso: scripts\backup-produccion.bat [nombre_contenedor_postgres]

set CONTENEDOR=%~1
if "%CONTENEDOR%"=="" set CONTENEDOR=rrhh-postgres

for /f "tokens=1-3 delims=/ " %%a in ("%date%") do set FECHA=%%c%%b%%a
set HORA=%time:~0,2%%time:~3,2%%time:~6,2%
set HORA=%HORA: =0%
set DESTINO=backup\prod_%FECHA%_%HORA%

echo === Backup produccion Ligo ===
echo Destino: %DESTINO%
mkdir "%DESTINO%" 2>nul

where docker >nul 2>&1
if errorlevel 1 (
  echo ERROR: Docker no encontrado. Ejecutar pg_dump manualmente.
  exit /b 1
)

docker inspect %CONTENEDOR% >nul 2>&1
if errorlevel 1 (
  echo ERROR: Contenedor %CONTENEDOR% no esta en ejecucion.
  exit /b 1
)

echo [1/2] Volcado PostgreSQL...
docker exec -t %CONTENEDOR% pg_dump -U rrhh -d rrhh_saas --no-owner --no-acl > "%DESTINO%\rrhh_saas.sql"
if errorlevel 1 (
  echo ERROR al exportar base de datos.
  exit /b 1
)

echo [2/2] Copia almacenamiento local (si existe)...
if exist "backend\data\archivos" (
  xcopy /E /I /Y "backend\data\archivos" "%DESTINO%\archivos" >nul
  echo Archivos copiados desde backend\data\archivos
) else (
  echo Sin carpeta backend\data\archivos — en prod usar snapshot del bucket S3/MinIO.
)

echo.
echo Backup completado en %DESTINO%
echo Ver docs\RETENCION_RECIBOS.md y docs\RUNBOOK_RESTAURACION_ALMACENAMIENTO.md
endlocal
