# BUSINESS RULES

Version: 1.0

## Objetivo

Este documento define las reglas funcionales de negocio de la plataforma.

Todas las decisiones técnicas deben respetar estas reglas.

En caso de conflicto entre una decisión técnica y una regla funcional, prevalece la regla funcional.

---

# Filosofía del Producto

La plataforma debe adaptarse a cada empresa.

La empresa no debe adaptarse a la plataforma.

Por lo tanto:

- Las políticas deben ser configurables.
- Los flujos deben ser configurables.
- Los permisos deben ser configurables.
- Las notificaciones deben ser configurables.

---

# Multiempresa

La plataforma es SaaS Multi Tenant.

Cada empresa opera de manera aislada.

Ningún usuario puede acceder a información de otra empresa.

Toda entidad debe pertenecer a una empresa.

Todas las consultas deben filtrar por CompanyId.

---

# Estructura Organizacional

Toda empresa puede definir su estructura.

Estructura mínima:

Empresa
 └─ Área
      └─ Puesto

Estructura recomendada:

Empresa
 └─ Gerencia
      └─ Área
           └─ Sector
                └─ Puesto

La estructura debe ser configurable.

No debe estar codificada.

---

# Empleados

Un empleado puede encontrarse en los siguientes estados:

- Activo
- Licencia
- Suspendido
- Desvinculado
- Pendiente de ingreso

Los estados deben quedar auditados.

---

# Legajo Digital

Todo empleado posee un legajo digital único.

El legajo centraliza:

- Datos personales
- Datos laborales
- Historial laboral
- Documentación
- Solicitudes
- Vacaciones
- Ausencias
- Activos asignados
- Historial de auditoría

El legajo nunca se elimina.

---

# Eliminación lógica (soft delete)

Regla transversal del sistema:

- **No hay borrado físico** de entidades de negocio en operación normal.
- Todo `DELETE` en la API marca `is_deleted = true` y `deleted_at` (ver `EntidadAuditable`).
- Los listados y búsquedas excluyen registros con `is_deleted = true`.
- La **auditoría** y el **legajo** no se eliminan nunca.

## Desactivación de empleado

`DELETE /api/empleados/{id}` **no** soft-deletea al empleado:

1. Pasa a estado `DESVINCULADO` con fecha de baja.
2. Genera tareas de **offboarding** desde plantillas de la empresa.
3. El legajo sigue visible para RRHH hasta completar el offboarding.

## Novedades de liquidación

Ciclo de vida por **estado** (no confundir con soft delete):

| Estado | Significado |
|--------|-------------|
| `BORRADOR` | Alta manual; editable y eliminable (soft delete) |
| `CONFIRMADA` | Lote confirmado por RRHH; listo para exportar |
| `EXPORTADA` | Incluida en CSV exportado al sistema de nómina |

La plataforma **no liquida sueldos**; solo registra y exporta novedades según este flujo.

---

# Disponibilidad Laboral

La disponibilidad laboral representa todas las ausencias de un empleado.

Incluye:

- Vacaciones
- Licencias
- Permisos
- Ausencias justificadas
- Ausencias injustificadas
- Llegadas tarde

Todo debe quedar registrado.

---

# Solicitudes

Toda gestión iniciada por un empleado es una solicitud.

Ejemplos:

- Vacaciones
- Licencia médica
- Licencia especial
- Permiso personal
- Adelanto salarial
- Cambio de turno

Las solicitudes son configurables.

---

# Tipos de Solicitud

Cada empresa puede crear tipos propios.

Ejemplo:

Solicitud:
- Home Office
- Capacitación
- Permiso de estudio
- Día de cumpleaños

No requiere desarrollo.

---

# Flujos de Aprobación

No existen flujos fijos.

Cada solicitud puede definir:

- Sin aprobación
- Un aprobador
- Múltiples aprobadores

Ejemplo:

Vacaciones

Empleado
→ Supervisor
→ RRHH

Licencia médica

Empleado
→ RRHH

Adelanto salarial

Empleado
→ Supervisor
→ Administración

---

# Vacaciones

Las vacaciones combinan **defaults legales argentinos (LCT / Ley 27.802)** con **configuración por empresa** (política activa + `configuracion_vacaciones`). El piso legal está en código; el techo y overrides CCT se configuran.

Documento detallado: [VACACIONES_LCT.md](VACACIONES_LCT.md)

## Defaults legales (piso en código)

| Regla | Valor | Fuente |
|-------|-------|--------|
| Fracción mínima por solicitud | 7 días corridos | Ley 27.802 |
| Anticipación mínima | 45 días (advertencia, no bloqueo) | Configurable por empresa |
| Fecha de inicio válida | Día siguiente al bloque de descanso semanal | Art. 151 LCT |
| Antigüedad para días anuales | Al 31/12 del año de goce | Art. 150 LCT |
| Días LCT estándar (política demo) | 0-5→14, 5-10→21, 10-20→28, 20+→35 | Art. 150 LCT |

## Configuración por empresa (`configuracion_vacaciones`)

- `dias_minimos_fraccion` — mínimo 7 (validado en servicio)
- `anticipacion_minima_dias` — genera advertencia si no se cumple
- `permite_solicitud_sin_saldo` — override CCT (default: false)

## Políticas de vacaciones

Una empresa puede tener múltiples políticas. Solo una activa determina días totales según antigüedad.

## Saldo anual

- Un registro por `(empleado, año)` en `saldo_vacaciones`
- `dias_disponibles = dias_totales - dias_tomados - dias_pendientes + ajuste_manual`
- Reserva al solicitar; confirmación al aprobar; liberación al cancelar/rechazar

## Patrón de descanso

Por empleado, con vigencia. Sin patrón registrado → default sábado-domingo.

## Validación

- Validadores puros en `vacacion.validacion` (sin acceso a BD)
- Errores bloqueantes → HTTP 422 con lista de errores
- Advertencias (anticipación) → se persisten la solicitud igual

## Estado de solicitud

El estado vive en `solicitud` (workflow genérico): `EN_REVISION`, `APROBADO`, `CAMBIOS_SOLICITADOS`, `RECHAZADO`, etc. No hay enum paralelo en `solicitud_vacaciones`.

## Permisos nuevos

- `vacacion.configurar`
- `vacacion.patron.gestionar`
- `vacacion.aprobar`

---

# Calendario Laboral

Cada empresa define:

- Feriados
- Días no laborables
- Jornadas especiales

Las vacaciones deben respetar este calendario.

---

# Ausentismo

Toda ausencia debe registrar:

- Fecha
- Tipo
- Motivo
- Justificada
- Certificado adjunto

Debe existir historial permanente.

---

# Gestión Documental

El sistema administra:

- Contratos
- Reglamentos
- Certificados
- Recibos
- Sanciones
- Políticas
- Formularios

Todo documento posee:

- Versión
- Historial
- Auditoría

---

# Firma Electrónica

Los documentos pueden requerir:

- Lectura
- Confirmación
- Firma

Cada acción queda auditada.

---

# Confirmaciones

Existe un modelo único de confirmación.

Puede aplicarse a:

- Recibos
- Documentos
- Comunicaciones
- Activos
- Políticas

Acciones posibles:

- Recibido
- Leído
- Firmado

## Recibos — reglas de firma electrónica

- Estados: `PENDIENTE` → `VISTO` → `FIRMADO` | `DISCONFORMIDAD` (sin retroceso).
- `evento_firma_recibo` es append-only (sin UPDATE/DELETE).
- `hash_pdf` del recibo es inmutable tras publicación.
- `clave_almacenamiento` del archivo vinculado es inmutable tras publicación del recibo.
- Disconformidad requiere comentario (mín. 10 caracteres).
- Almacenamiento por clave lógica (`clave_almacenamiento`); proveedor configurable (disco / S3). Ver [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md).
- Conservación 10 años vía bucket versionado + backups — ver [RETENCION_RECIBOS.md](RETENCION_RECIBOS.md) (CP-01).
- Inmutabilidad legal: no eliminar recibos firmados; trigger PostgreSQL en eventos (CP-02).
- Carga masiva HU-010 vía `POST /api/recibos/carga-masiva` (CP-04).
- `archivoId` obligatorio al publicar; recibos legacy marcados con `requiere_republicacion` (CP-05).
- Ver checklist completo: [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md).

---

# Comunicaciones

Las comunicaciones pueden enviarse a:

- Toda la empresa
- Un área
- Un sector
- Un puesto
- Un empleado

Opcionalmente:

- Requieren lectura
- Requieren confirmación

---

# Onboarding

El onboarding es configurable.

Posibles tareas:

- Completar perfil
- Subir documentación
- Firmar contrato
- Firmar reglamento
- Capacitación obligatoria
- Asignación de activos

---

# Offboarding

El offboarding es configurable.

Posibles tareas:

- Devolución de activos
- Firma documentación final
- Cierre de accesos
- Cierre de legajo

---

# Gestión de Activos

Un activo puede ser:

- Notebook
- Celular
- Uniforme
- Herramienta
- Vehículo

Toda entrega debe quedar registrada.

Toda devolución debe quedar registrada.

---

# Novedades para Liquidación

La plataforma registra:

- Horas extras
- Bonificaciones
- Comisiones
- Ausencias
- Licencias

La plataforma no liquida salarios en el MVP.

La plataforma exporta novedades.

---

# Auditoría

Toda acción crítica genera auditoría.

Registrar:

- Usuario
- Fecha
- Hora
- Acción
- Entidad
- Valor anterior
- Valor nuevo

La auditoría nunca se elimina.

---

# Notificaciones

Eventos mínimos:

- Solicitud creada
- Solicitud aprobada
- Solicitud rechazada
- Documento asignado
- Documento firmado
- Comunicación publicada

Canales:

- In-App
- Push
- Email

---

# IA

La IA no toma decisiones.

La IA asiste.

Ejemplos:

- Resumen diario para RRHH
- Alertas de documentos pendientes
- Alertas de vacaciones próximas
- Resúmenes ejecutivos

La IA nunca reemplaza aprobaciones humanas.
