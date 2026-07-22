# Guía de demostración — Ligo

## Arranque rápido

1. Ejecutar `iniciar.bat` (o `iniciar-interno.bat`) y esperar backend + frontends.
2. *(Opcional)* Almacenamiento S3 local: `docker compose up -d minio minio-init` y arrancar backend con perfil `minio`. Ver [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md).
3. Abrir **http://localhost:4200** (portal web) y **http://localhost:4201** (app móvil, opcional).
4. Los datos demo se cargan automáticamente al primer arranque (`app.demo.cargar-datos: true`).

## Credenciales

| Rol | Email | Contraseña | Uso |
|-----|-------|------------|-----|
| SuperAdmin | `admin@rrhh.local` | `Admin123!` | Consola de plataforma (`/plataforma`) — crear empresas y usuarios |
| RRHH | `rrhh@demo.local` | `Demo123!` | Panel RRHH en `:4200` — empresa **Demo SA** |
| Empleado | `empleado@demo.local` | `Demo123!` | Portal empleado `/mi-panel` |

> El SuperAdmin **no** usa el panel de solicitudes/vacaciones en operación normal. Para soporte técnico, activar **Modo soporte** desde la ficha de una empresa.

## Datos precargados (Demo SA)

- Empresa **Demo SA** con roles, política de vacaciones (14 días) activa.
- Empleados: **Juan Pérez** (vinculado a `empleado@demo.local`) y **María García** (solo legajo).
- Workflow **Aprobación Vacaciones** + tipos **Vacaciones** y **Licencia**.
- Contrato asignado a Juan, comunicación de bienvenida, **recibo del mes anterior** (PDF local + hash SHA-256, pendiente de firma).
- Onboarding de Juan en estado **EN_PROGRESO** (checklist con 4 tareas demo).
- Plantillas de onboarding y offboarding por empresa.
- Módulos **Activos** (`/activos`) y **Novedades** (`/novedades`) operativos.
- Módulo **Fichajes** (`/fichajes`) con zonas en mapa y fichaje móvil para empleados.

## Eliminación lógica

En la demo, los botones **Eliminar** / **Desactivar** no borran datos de la base:

- **Eliminar** (flujos, tipos, activos, novedades en borrador, etc.) → soft delete (`is_deleted = true`).
- **Desactivar empleado** → estado `DESVINCULADO` + checklist de offboarding; el legajo sigue accesible.
- **Cancelar comunicación programada** → estado `CANCELADA`.

Documentación completa: [16_DATABASE_DESIGN.md](16_DATABASE_DESIGN.md#soft-delete-eliminación-lógica).

## Checklist pre-reunión (5 min)

- [ ] `iniciar.bat` → backend `:8080`, frontend `:4200`, app móvil `:4201`
- [ ] Login `rrhh@demo.local` → empresa Demo SA → panel carga sin errores
- [ ] Login `empleado@demo.local` → `/mi-panel` muestra saldo vacaciones y notificaciones
- [ ] Probar circuito vacaciones (solicitar → aprobar en `/solicitudes` → ver saldo)
- [ ] Dos navegadores listos (Chrome perfil RRHH + Chrome perfil empleado)

## Guion de reunión (5–10 min)

```
[Chrome 1 - RRHH :4200]              [Chrome 2 - Empleado :4200/mi-panel]
        │                                        │
        ├─ Carga recibo / asigna doc ───────────►│ Aparece en panel / recibos / docs
        ├─ Publica comunicación ─────────────────►│ Mi comunicaciones
        │                                        ├─ Pide vacaciones
        ├─ Solicitudes → Aprobar ◄─────────────────┤ Estado cambia
        └─ Ve saldos / legajo                    └─ Ve saldo actualizado
```

### Pasos sugeridos

1. **Empleado** (`empleado@demo.local`): mostrar `/mi-panel` — vacaciones, notificaciones, documentos pendientes.
2. **Empleado**: `/mi-vacaciones` → solicitar **mínimo 7 días** desde un lunes (patrón descanso sáb-dom) → enviar.
3. **RRHH** (`rrhh@demo.local`): ir a **`/vacaciones`** → pestaña **Bandeja** → **Ver detalle** → **Aprobar** (o contraproponer).
4. **Empleado**: recargar `/mi-vacaciones` → saldo actualizado + notificación en `/mi-notificaciones`. Si hubo contrapropuesta, aceptarla desde el historial.
5. **RRHH**: `/documentos` → asignar documento a Juan; `/comunicaciones` → publicar aviso. En `/recibos`: **Carga masiva** (ZIP por CUIL) o **Expediente** probatorio.
6. **Empleado**: `/mi-documentos` (ver PDF), `/mi-comunicaciones`, `/mi-notificaciones`.
7. **Opcional**: app móvil en `:4201` con las mismas credenciales de empleado.

### Mensaje clave

> No son dos sistemas: es la misma plataforma con dos experiencias según el rol.

## Guion V1 — módulos nuevos (~2 min cada uno)

### Onboarding (empleado + RRHH)

1. **Empleado** (`empleado@demo.local`): `/mi-panel` → bloque **Onboarding en curso** con tareas pendientes.
2. Marcar una tarea como completada; indicar que el contrato y documentos se firman en `/mi-documentos`.
3. **RRHH**: `/empleados` → legajo de **Juan Pérez** → pestaña/sección onboarding con progreso y tareas.

### Activos

1. **RRHH**: `/activos` → crear activo (ej. notebook) o usar uno existente.
2. **Asignar** a Juan Pérez; mostrar estado `ASIGNADO`.
3. **Devolver** activo → vuelve a `DISPONIBLE` (si hay offboarding pendiente, la tarea *Devolver activos* se completa sola).

### Novedades de liquidación

1. **RRHH**: `/novedades` → filtrar período → **Nueva novedad** (queda en `BORRADOR`).
2. **Confirmar lote** del período → estado `CONFIRMADA` (ya no editable).
3. **Exportar CSV** → descarga archivo; las filas pasan a `EXPORTADA`.

### Fichaje móvil (~2 min)

1. **RRHH** (`rrhh@demo.local`): `/fichajes/zonas` → ver zona demo **Casa Central Demo** (requiere clave Google Maps en `claves-local.ts`).
2. **RRHH**: `/fichajes/configuracion` → confirmar que Juan tiene fichaje móvil habilitado.
3. **Empleado** (`empleado@demo.local`): app móvil `:4201` → **Más → Fichaje** → aceptar política → fichar (simular ubicación en microcentro CABA si probás en navegador).
4. **RRHH**: `/fichajes` → pestaña **Pendientes** si fichó fuera de zona, o **Todas** / **Resumen**.

### Offboarding

1. **RRHH**: `/empleados` → elegir empleado de prueba (no Juan si lo usás en demo principal) → **Desactivar**.
2. Abrir legajo → checklist offboarding `EN_PROGRESO`.
3. Completar tareas manualmente o devolviendo activos asignados → estado `COMPLETADO`; empleado queda `DESVINCULADO` pero legajo visible.

### Checklists (plantillas)

1. **RRHH**: `/plantillas-checklists` → pestaña **Onboarding** u **Offboarding**.
2. Crear, editar o eliminar plantillas (orden, tipo, obligatoria).
3. Las plantillas se aplican al **iniciar onboarding** o al **desactivar empleado**.

## Demo app móvil (`:4201`)

Tras login, la app detecta el portal según permisos:

| Usuario | Destino | Tabs principales |
|---------|---------|------------------|
| `empleado@demo.local` | `/tabs/inicio` | Inicio, Solicitudes, Documentos, Más, Perfil |
| `rrhh@demo.local` | `/tabs-rrhh/panel` | Panel, Activos, Novedades, Solicitudes, Perfil |

**Empleado móvil:** onboarding en Inicio (si `EN_PROGRESO`), tarjeta **Todos los módulos → Más** con vacaciones, ausencias, comunicaciones, recibos y notificaciones. Mismo diseño de tarjetas en todas las pantallas.

**RRHH móvil:** gestionar activos (crear/asignar/devolver), novedades (alta/confirmar/exportar CSV), aprobar solicitudes pendientes.

## URLs útiles

| Pantalla | URL |
|----------|-----|
| Panel RRHH | http://localhost:4200/panel |
| Solicitudes (aprobar) | http://localhost:4200/solicitudes |
| Vacaciones RRHH | http://localhost:4200/vacaciones |
| Activos | http://localhost:4200/activos |
| Checklists plantillas | http://localhost:4200/plantillas-checklists |
| Novedades liquidación | http://localhost:4200/novedades |
| Fichajes RRHH | http://localhost:4200/fichajes |
| Zonas de fichaje | http://localhost:4200/fichajes/zonas |
| Legajo empleado | http://localhost:4200/empleados/{id}/legajo |
| Panel empleado | http://localhost:4200/mi-panel |
| Mis vacaciones | http://localhost:4200/mi-vacaciones |
| Notificaciones | http://localhost:4200/mi-notificaciones |
| App móvil | http://localhost:4201 |

## Slide de credenciales (copiar a presentación)

```
Ligo — Demo

RRHH:     rrhh@demo.local      / Demo123!
Empleado: empleado@demo.local  / Demo123!
Empresa:  Demo SA

Web:   http://localhost:4200
Móvil: http://localhost:4201
```

## Solución de problemas

| Problema | Solución |
|----------|----------|
| No aparece Demo SA | Verificar que el backend arrancó; revisar logs "Cargando datos de demostración" |
| Vacaciones fallan al solicitar | Confirmar tipo "Vacaciones" y política activa (seed las crea) |
| No puedo aprobar | Usar `/solicitudes` logueado como `rrhh@demo.local` |
| App móvil no conecta | Ejecutar `iniciar.bat` y esperar el backend. La app usa proxy `/api` → `:8080`. Si ves banner rojo, el backend no está listo. |
| En móvil no veo vacaciones/recibos | Entrar con `empleado@demo.local` (no RRHH) y usar tab **Más** o las tarjetas de Inicio |
| Solo veo pantallas vacías o "estático" | Confirmar backend en `:8080`, reiniciar app móvil (`npm start` en `app-movil`). En login debe decir "Hola, Juan" y vacaciones "14 días" |
| PDF no abre | Frontend debe estar en `:4200`; archivos en `/demo/*.pdf` |
| Recibo sin firmar / sin hash | RRHH debe cargar recibo con PDF vía **Cargar recibo** (sube archivo → `archivoId`). Ver [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md) |

## Demo: firma de recibo (empleado)

1. Login `empleado@demo.local` → **Mis recibos** (`/mis-recibos`) o app móvil → **Recibos**.
2. Abrir el recibo del mes anterior → se registra visualización (`VISTO`).
3. Leer PDF en visor integrado → marcar casilla legal → **Confirmar recepción conforme**.
4. Alternativa: **Firmar en disconformidad** con motivo (mín. 10 caracteres).
5. RRHH puede ver eventos de auditoría en **Recibos** → botón **Eventos**.

> **Cumplimiento legal:** el flujo demo cubre Ley 27.802 y 25.506 con CP-01 a CP-14 implementados. Antes de producción: revisar [DECRETO_407_2026_CHECKLIST.md](DECRETO_407_2026_CHECKLIST.md) y [ACCESO_ALTERNATIVO_RECIBOS.md](ACCESO_ALTERNATIVO_RECIBOS.md) con asesoría legal.
