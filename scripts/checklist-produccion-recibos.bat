@echo off

REM Checklist operativo CP-01 / CP-02 para despliegue en PostgreSQL



setlocal

set CONTENEDOR=rrhh-postgres

set DB=rrhh_saas

set DB_USER=rrhh



echo === Checklist produccion - recibos CP-01 CP-02 ===

echo.



where docker >nul 2>&1

if errorlevel 1 goto sin_docker



docker inspect %CONTENEDOR% >nul 2>&1

if errorlevel 1 goto sin_contenedor



echo [OK] PostgreSQL activo (%CONTENEDOR%)



docker exec %CONTENEDOR% psql -U %DB_USER% -d %DB% -tAc "SELECT 1 FROM pg_trigger WHERE tgname = 'trg_evento_firma_recibo_inmutable';" 2>nul | findstr "1" >nul

if errorlevel 1 goto sin_trigger



echo [OK] Trigger trg_evento_firma_recibo_inmutable presente

goto resumen



:sin_docker

echo [AVISO] Docker no disponible.

echo   Arrancar backend con perfil prod contra PostgreSQL (Flyway aplica V23).

echo   Ver docs\RETENCION_RECIBOS.md para backups.

goto fin



:sin_contenedor

echo [AVISO] Contenedor %CONTENEDOR% no activo.

echo   Ejecutar: docker compose up -d postgres

goto fin



:sin_trigger

echo [PENDIENTE] Trigger de inmutabilidad NO detectado.

echo   Arrancar el backend una vez (Flyway V23).

goto resumen



:resumen

echo.

echo [INFO] Ejecutar backup: scripts\backup-produccion.bat

echo [INFO] Perfil prod: --spring.profiles.active=prod

echo [INFO] Verificacion integridad habilitada en application-prod.yml



:fin

echo.

endlocal

