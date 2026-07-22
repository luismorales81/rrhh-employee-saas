# Vacaciones — Cumplimiento LCT (Argentina)

Marco legal, reglas implementadas y limitaciones de la versión actual.

## Marco legal

| Norma | Artículo | Regla |
|-------|----------|-------|
| LCT | Art. 150 | Días de vacaciones según antigüedad (14/21/28/35) |
| LCT | Art. 151 | Inicio el día inmediato siguiente al bloque de descanso semanal |
| Ley 27.802 | — | Fraccionamiento mínimo de 7 días corridos por período |

## Arquitectura

```
Empleado → simular-fecha / solicitudes
         ↓
Validadores puros (vacacion.validacion)
         ↓
SolicitudVacaciones + Solicitud (workflow) + SaldoVacaciones (anual)
         ↓
evento_solicitud_vacaciones (append-only)
```

El estado de la solicitud vive en `solicitud.estado` (`EN_REVISION`, `APROBADO`, `CAMBIOS_SOLICITADOS`, `RECHAZADO`, etc.).

## Reglas implementadas

### Antigüedad y días anuales

- `CalculadorAntiguedadVacaciones`: años completos al **31/12** del año de goce.
- `CalculadorDiasProporcionales`: ingresos a mitad de año.
- `ServicioRecalculoSaldoVacaciones`: recalcula `(empleado, año)` según política activa.

### Fraccionamiento (Ley 27.802)

- Mínimo **7 días corridos** por solicitud (`ValidadorSolicitudVacaciones`).
- Configurable por empresa en `dias_minimos_fraccion` (piso 7 en `ServicioConfiguracionVacaciones`).

### Fecha de inicio (Art. 151 LCT)

- `ValidadorFechaInicioVacaciones` valida contra `patron_descanso` vigente.
- **Bloque consecutivo** (ej. sáb-dom): inicio válido = lunes.
- **Bloque consecutivo** (ej. mié-jue): inicio válido = viernes.
- **Días no consecutivos** (ej. mar+vie): cada día de descanso genera su propio día válido de inicio (día siguiente a ese descanso). Regla v1 documentada en código y seed demo (EMP-003).

### Anticipación

- `anticipacion_minima_dias` (default 45) genera **advertencia**, no bloqueo.

### Saldo

- Un saldo por `(empleado_id, anio)`.
- Reserva en solicitud; confirmación al aprobar; liberación al cancelar/rechazar.
- Ajuste manual con motivo obligatorio.

### Contrapropuesta

1. RRHH envía fechas alternativas → `CAMBIOS_SOLICITADOS` + campos en `solicitud_vacaciones`.
2. Empleado acepta → fechas actualizadas, vuelve a `EN_REVISION`.
3. Cada transición registra evento append-only.

## API principal (`/api/vacaciones`)

| Método | Ruta | Actor |
|--------|------|-------|
| GET | `/saldo/propio?anio=` | Empleado |
| POST | `/simular-fecha` | Empleado |
| POST | `/solicitudes` | Empleado |
| POST | `/solicitudes/{id}/aceptar-contrapropuesta` | Empleado |
| POST | `/solicitudes/{id}/cancelar` | Empleado |
| GET | `/solicitudes?estado=` | RRHH |
| GET | `/solicitudes/{id}` | RRHH / empleado (detalle + validación) |
| PATCH | `/solicitudes/{id}/aprobar` | RRHH |
| PATCH | `/solicitudes/{id}/contraproponer` | RRHH |
| PATCH | `/solicitudes/{id}/rechazar` | RRHH |
| GET/PUT | `/configuracion` | RRHH |
| GET/POST | `/empleados/{id}/patron-descanso` | RRHH |

Errores de validación: HTTP **422** con `{ errores, advertencias }`.

## Permisos

- `vacacion.configurar`
- `vacacion.patron.gestionar`
- `vacacion.aprobar`

## Demo (ServicioCargaDatosDemo)

- Política LCT completa (14/21/28/35).
- Tres patrones: estándar L-V (sáb-dom), turno (mié-jue), no consecutivo (mar+vie).

## Limitaciones v1 (fuera de alcance)

- **Feriados**: no integrados en validación de fecha de inicio (TODO en código).
- **Push móvil**: stub `ProveedorNotificacionPush`.
- **OpenAPI**: documentación en markdown, no Springdoc.

## Frontend

- Portal empleado: `/mi-vacaciones` — simular fecha, advertencias, contrapropuesta.
- RRHH: `/vacaciones` — bandeja, detalle con validación, configuración, patrón descanso.
- `/solicitudes` enlaza a detalle de vacaciones para tipo «Vacaciones».

## Tests

- Unitarios: `ValidadorFechaInicioVacacionesTest` (5 casos + no consecutivo), `ValidadorSolicitudVacacionesTest`, calculadores.
- Integración: `CircuitoVacacionesIntegracionTest` (422 fracción, simular-fecha, aprobar).
