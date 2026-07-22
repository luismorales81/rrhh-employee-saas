# Ligo — App móvil

Aplicación Ionic/Capacitor dual: **espacio del empleado** y **panel de equipo**. Consume la misma API REST del portal web (`/api`).

## Requisitos

- Node.js 20+
- npm 10+
- Backend API en ejecución (por defecto `http://localhost:8080/api`)

## Instalación y arranque

```bash
cd app-movil
npm install
npm start    # http://localhost:4201
```

Desde la raíz del proyecto: `iniciar.bat` levanta backend, frontend y app móvil.

## Credenciales demo

| Rol | Email | Contraseña | Portal móvil |
|-----|-------|------------|--------------|
| Empleado | `empleado@demo.local` | `Demo123!` | `/tabs/inicio` |
| RRHH | `rrhh@demo.local` | `Demo123!` | `/tabs-rrhh/panel` |

SuperAdmin: no soportado en móvil (usar portal web).

## Portal empleado (`/tabs/*`)

| Ruta | Descripción |
|------|-------------|
| `/iniciar-sesion` | Login |
| `/tabs/inicio` | Panel con métricas, onboarding en curso y accesos rápidos |
| `/tabs/mis-activos` | Activos asignados (solo lectura) |
| `/tabs/solicitudes` | Solicitudes propias |
| `/tabs/ausentismo` | Ausencias y justificación |
| `/tabs/documentos` | Lectura y firma con canvas |
| `/tabs/notificaciones` | Notificaciones |
| `/tabs/perfil` | Datos del empleado |
| `/tabs/vacaciones` | Saldo, calendario y solicitud |
| `/tabs/recibos` | Recibos de sueldo |
| `/tabs/comunicaciones` | Comunicaciones internas |
| `/tabs/mas` | Hub con vacaciones, ausencias, recibos, notificaciones y onboarding |
| `/tabs/onboarding` | Tareas de onboarding y enlace a documentos |

**Tabs inferiores:** Inicio · Solicitudes · Documentos · Más · Perfil

Vacaciones, ausencias, comunicaciones, recibos y notificaciones están en **Más** (o en las tarjetas de Inicio).

## Portal RRHH (`/tabs-rrhh/*`)

| Ruta | Descripción |
|------|-------------|
| `/tabs-rrhh/panel` | Resumen: solicitudes pendientes y novedades en borrador |
| `/tabs-rrhh/activos` | Inventario, crear, asignar y devolver |
| `/tabs-rrhh/novedades` | Alta, confirmar lote y exportar CSV |
| `/tabs-rrhh/solicitudes` | Aprobar o rechazar pendientes |
| `/tabs-rrhh/perfil` | Sesión RRHH y cerrar sesión |

**Tabs inferiores:** Panel · Activos · Novedades · Solicitudes · Perfil

## Componentes compartidos

- `tarjeta-metrica` — tarjetas del panel con icono y color de acento
- `bloque-onboarding` — checklist de onboarding en Inicio
- `estado-carga` — spinner unificado

## Firma digital

Los documentos que requieren firma usan `canvas-firma` (HTML5). La imagen se envía en base64 al backend.

## Sesión y permisos

- Sesión en `localStorage` (`rrhh_sesion_movil`)
- `ServicioPermisos` detecta portal empleado vs RRHH tras el login
- Guards `guardiaPortalEmpleado` / `guardiaPortalRrhh` protegen rutas

## Capacitor (opcional)

```bash
npm run build
npx cap add android
npx cap sync android
```

## Estructura

```
src/app/
├── layout/              # layout-tabs (empleado), layout-tabs-rrhh
├── compartido/          # tarjeta-metrica, bloque-onboarding, estado-carga
├── nucleo/
│   ├── guardias/        # autenticación y portal
│   └── servicios/       # HTTP (onboarding, activos, novedades, etc.)
└── paginas/             # Pantallas standalone Ionic
```
