# AUDIT LOG SPECIFICATION

Version: 1.0

## Objetivo

Registrar todas las acciones críticas realizadas dentro de la plataforma.

La auditoría debe garantizar:

- Trazabilidad
- Seguridad
- Cumplimiento
- Investigación de incidentes

---

# Principios

## Inmutabilidad

Los registros de auditoría nunca pueden modificarse.

Nunca pueden eliminarse.

---

## Trazabilidad

Toda acción debe responder:

- Quién
- Qué
- Cuándo
- Dónde
- Sobre qué entidad

---

# Acciones Auditables

## Empleados

- Alta
- Modificación
- Baja
- Reingreso

---

## Solicitudes

- Creación
- Aprobación
- Rechazo
- Cancelación

---

## Vacaciones

- Creación
- Modificación
- Aprobación

---

## Documentos

- Creación
- Asignación
- Lectura
- Firma

---

## Comunicaciones

- Publicación
- Lectura

---

## Activos

- Entrega
- Devolución

---

# Estructura

AuditLog

- Id
- CompanyId
- UserId
- Action
- EntityName
- EntityId
- PreviousValues
- NewValues
- IpAddress
- UserAgent
- CreatedAt

---

# Búsqueda

Debe permitir filtrar por:

- Usuario
- Fecha
- Entidad
- Acción

---

# Exportación

Formatos:

- Excel
- CSV
- PDF

---

# Retención

Los registros de **auditoría** nunca se eliminan ni se modifican.

---

# Soft delete en entidades de negocio

Las entidades operativas (empleados, flujos, activos, novedades, etc.) usan **eliminación lógica** (`is_deleted`, `deleted_at`) en lugar de borrado físico en la mayoría de los casos.

| Concepto | Comportamiento |
|----------|----------------|
| Tabla `auditoria` / log de eventos | Inmutable: sin UPDATE ni DELETE |
| Entidades de negocio | Soft delete vía API `DELETE` o servicios internos |
| Desactivar empleado | No es soft delete: pasa a `DESVINCULADO` y conserva legajo |
| Novedades liquidación | Solo `BORRADOR` es editable/eliminable; `CONFIRMADA` y `EXPORTADA` son de solo lectura |

Cada acción de soft delete o cambio de estado relevante debe generar un registro de auditoría (`ELIMINAR`, `ACTUALIZAR`, `CONFIRMAR`, etc.) para mantener trazabilidad aunque el dato deje de listarse en consultas normales.

Ver también: [16_DATABASE_DESIGN.md](16_DATABASE_DESIGN.md#soft-delete-eliminación-lógica), [08_AUDIT_SECURITY.md](08_AUDIT_SECURITY.md).