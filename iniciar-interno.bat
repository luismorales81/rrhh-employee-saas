@echo off
setlocal EnableDelayedExpansion

cd /d "%~dp0"

set "PERFIL_SPRING=local"
set "MODO_DB=H2 local (sin Docker)"

echo ============================================
echo   Ligo - Inicio del sistema
echo ============================================
echo.

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven no esta instalado o no esta en el PATH.
    goto fin_error
)

where npm >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js/npm no esta instalado o no esta en el PATH.
    goto fin_error
)

if not exist "backend\pom.xml" (
    echo [ERROR] No se encuentra backend\pom.xml. Ejecute desde la raiz del proyecto.
    goto fin_error
)

if not exist "frontend\package.json" (
    echo [ERROR] No se encuentra frontend\package.json.
    goto fin_error
)

if not exist "app-movil\package.json" (
    echo [ERROR] No se encuentra app-movil\package.json.
    goto fin_error
)

set "DOCKER_CMD="
where docker >nul 2>&1
if not errorlevel 1 set "DOCKER_CMD=docker"

if not defined DOCKER_CMD (
    if exist "%ProgramFiles%\Docker\Docker\resources\bin\docker.exe" (
        set "DOCKER_CMD=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
    )
)

if defined DOCKER_CMD goto intentar_docker
goto usar_h2

:intentar_docker
echo [1/5] Docker detectado. Levantando PostgreSQL...
"%DOCKER_CMD%" compose up -d
if errorlevel 1 (
    echo [AVISO] No se pudo iniciar Docker Compose. Se usara H2 local.
    goto usar_h2
)

set "PERFIL_SPRING="
set "MODO_DB=PostgreSQL (Docker)"
echo [2/5] Esperando que la base de datos este lista...
set /a intentos=0

:esperar_db
"%DOCKER_CMD%" compose exec -T postgres pg_isready -U rrhh -d rrhh_saas >nul 2>&1
if not errorlevel 1 goto docker_listo
set /a intentos+=1
if !intentos! GEQ 30 (
    echo [AVISO] PostgreSQL no respondio. Se usara H2 local.
    set "PERFIL_SPRING=local"
    set "MODO_DB=H2 local (fallback)"
    goto usar_h2
)
timeout /t 2 /nobreak >nul
goto esperar_db

:docker_listo
echo       PostgreSQL listo.
goto deps_npm

:usar_h2
if not defined PERFIL_SPRING set "PERFIL_SPRING=local"
echo [1/5] Docker no disponible o no se uso.
echo [2/5] Base H2 embebida (perfil: local).
echo       Datos en: backend\data

:deps_npm
if not exist "frontend\node_modules" (
    echo [3/5] Instalando dependencias del frontend - primera vez...
    pushd frontend
    call npm install
    if errorlevel 1 (
        echo [ERROR] Fallo npm install en frontend.
        popd
        goto fin_error
    )
    popd
) else (
    echo [3/5] Dependencias del frontend OK.
)

if not exist "app-movil\node_modules" (
    echo [4/5] Instalando dependencias de app-movil - primera vez...
    pushd app-movil
    call npm install
    if errorlevel 1 (
        echo [ERROR] Fallo npm install en app-movil.
        popd
        goto fin_error
    )
    popd
) else (
    echo [4/5] Dependencias de app-movil OK.
)

echo [5/5] Liberando puertos y abriendo servicios...
echo       Base de datos: !MODO_DB!
call "%~dp0scripts\liberar-puertos.bat"

if "!PERFIL_SPRING!"=="local" (
    start "Ligo-Backend" cmd /k "%~dp0scripts\iniciar-backend-local.bat"
) else (
    start "Ligo-Backend" cmd /k "%~dp0scripts\iniciar-backend.bat"
)
timeout /t 2 /nobreak >nul
start "Ligo-Frontend" cmd /k "%~dp0scripts\iniciar-frontend.bat"
start "Ligo-App-Movil" cmd /k "%~dp0scripts\iniciar-app-movil.bat"

echo.
echo ============================================
echo   Sistema en arranque
echo ============================================
echo.
echo   Ligo (web):            http://localhost:4200
echo   App movil:             http://localhost:4201
echo   API:                   http://localhost:8080
echo   Swagger:               http://localhost:8080/swagger-ui.html
echo.
echo   LOGIN SUPERADMIN (portal web):
echo   Email:     admin@rrhh.local
echo   Password:  Admin123^!
echo.
echo   Luego: crear o elegir empresa, usuarios y equipo.
echo   App movil: usar usuario con rol Empleado vinculado al legajo.
echo.
echo   NO cierre las ventanas Ligo-Backend, Ligo-Frontend
echo   ni Ligo-App-Movil.
echo   Espere "Started RrhhSaasApplication" en el backend
echo   y "Application bundle generation complete" en los frontends.
echo ============================================
echo.
goto fin_ok

:fin_error
echo.
echo ERROR - revise los mensajes de arriba.
goto fin_ok

:fin_ok
echo.
echo Listo. Esta ventana puede quedar abierta.
endlocal
