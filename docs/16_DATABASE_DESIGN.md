# DATABASE DESIGN

Version: 1.0

## Objetivo

Definir el modelo de datos conceptual de la plataforma.

Este documento es la referencia oficial para:

- Entidades
- Relaciones
- Multi Tenant
- Auditoría
- Escalabilidad

---

# Principios

## Multi Tenant

Todas las entidades de negocio deben contener:

CompanyId

Excepto:

- SuperAdmin
- Configuración global
- Catálogos globales

---

# Entidades Base

## Company

Representa una empresa cliente.

Campos:

- Id
- Name
- LegalName
- TaxId
- Email
- Phone
- Status
- CreatedAt
- UpdatedAt

---

## User

Representa una cuenta de acceso.

Campos:

- Id
- CompanyId
- Email
- PasswordHash
- IsActive
- LastLoginAt
- CreatedAt

---

## Role

Campos:

- Id
- CompanyId
- Name
- Description

Ejemplos:

- RRHH
- Supervisor
- Gerencia
- Empleado

---

## Permission

Campos:

- Id
- Code
- Description

---

## RolePermission

Relación:

Role
↔ Permission

---

# Estructura Organizacional

## Department

Área.

Ejemplos:

- Administración
- Comercial
- Producción

Campos:

- Id
- CompanyId
- Name
- Description

---

## Team

Sector.

Ejemplos:

- Ventas
- Marketing
- Compras

Campos:

- Id
- CompanyId
- DepartmentId
- Name

---

## Position

Puesto.

Ejemplos:

- Administrativo
- Analista
- Supervisor

Campos:

- Id
- CompanyId
- Name
- Description

---

# Empleados

## Employee

Entidad principal.

Campos:

- Id
- CompanyId
- UserId
- EmployeeNumber
- FirstName
- LastName
- DNI
- CUIL
- BirthDate
- HireDate
- TerminationDate
- Phone
- PersonalEmail
- WorkEmail
- Status
- SupervisorId
- DepartmentId
- TeamId
- PositionId

---

## EmployeeAddress

Campos:

- Id
- EmployeeId
- Address
- City
- Province
- Country
- PostalCode

---

## EmployeeEmergencyContact

Campos:

- Id
- EmployeeId
- FullName
- Relationship
- Phone

---

# Legajo

## EmployeeFile

Legajo digital.

Campos:

- Id
- EmployeeId
- CreatedAt

---

# Recibos de sueldo

## Tabla `archivo_almacenado`

Campos relevantes: `clave_almacenamiento` (clave lógica S3, ej. `empresa/1/archivos/{uuid}.pdf`), `hash_sha256`, `tipo_mime`, `tamano`.

Ver [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md).

## Tabla `recibo`

Campos relevantes: `archivo_id`, `hash_pdf`, `requiere_republicacion`, `monto_neto`, `monto_bruto`, `publicado_por_id`, `visto_por_empleado_en`, `estado` (`PENDIENTE`|`VISTO`|`FIRMADO`|`DISCONFORMIDAD`).

## Tabla `evento_firma_recibo` (append-only)

`tipo`, `timestamp_evento`, `direccion_ip`, `user_agent`, `sistema_operativo`, `hash_pdf_en_firma`, `comentario`.

En **PostgreSQL producción**: trigger `trg_evento_firma_recibo_inmutable` (Flyway V23) impide UPDATE/DELETE.

Ver [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md).  
Cumplimiento: [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md).

---

# Documentos

## DocumentCategory

Ejemplos:

- Contrato
- Reglamento
- Recibo
- Certificado

Campos:

- Id
- CompanyId
- Name

---

## Document

Campos:

- Id
- CompanyId
- CategoryId
- Name
- Description
- Version
- FileUrl
- IsActive

---

## EmployeeDocument

Documento asignado.

Campos:

- Id
- EmployeeId
- DocumentId
- AssignedAt

---

## DocumentAcknowledgement

Confirmación.

Campos:

- Id
- EmployeeId
- DocumentId
- Status

Estados:

- Received
- Read
- Signed

---

# Flujos de Trabajo

## Workflow

Definición configurable de aprobaciones por empresa.

Campos:

- Id
- CompanyId
- Name
- Description
- IsActive

---

## WorkflowStep

Paso dentro de un flujo.

Campos:

- Id
- WorkflowId
- Order
- Name
- StepType
- RoleId (opcional, para Rol específico)
- UserId (opcional, para Usuario específico)

Tipos de paso (StepType):

- SupervisorDirecto
- RRHH
- Gerencia
- RolEspecifico
- UsuarioEspecifico

---

## WorkflowInstance

Instancia activa de un flujo vinculada a una solicitud.

Campos:

- Id
- CompanyId
- RequestId
- WorkflowId
- CurrentStepOrder
- Status

Estados:

- EnProgreso
- Completado
- Rechazado
- Cancelado

---

## WorkflowStepAction

Historial de acciones sobre pasos (auditoría del motor).

Campos:

- Id
- WorkflowInstanceId
- WorkflowStepId
- UserId
- Action
- Comments
- ActionAt

Acciones:

- Aprobar
- Rechazar
- SolicitarCambios
- Escalar

---

# Solicitudes

## RequestType

Campos:

- Id
- CompanyId
- Name
- Description
- WorkflowId (nullable; null = sin aprobación)
- RequiresApproval
- RequiresAttachment

Ejemplos:

- Vacaciones
- Licencia
- Permiso

---

## Request

Campos:

- Id
- CompanyId
- EmployeeId
- RequestTypeId
- Status
- Reason
- CreatedAt

Estados:

- Borrador
- EnRevision
- Aprobado
- Rechazado
- Cancelado
- CambiosSolicitados

---

## RequestApproval

Registro de aprobación por paso (complementa WorkflowStepAction).

Campos:

- Id
- RequestId
- WorkflowStepId
- ApproverId
- Status
- Comments
- ApprovedAt

---

# Vacaciones

## politica_vacaciones / rango_politica_vacaciones

Sin cambios respecto a V4. Rangos por antigüedad definen `dias_otorgados`.

## configuracion_vacaciones (V26)

| Campo | Tipo | Default |
|-------|------|---------|
| empresa_id | BIGINT UNIQUE | — |
| dias_minimos_fraccion | INT | 7 |
| anticipacion_minima_dias | INT | 45 |
| permite_solicitud_sin_saldo | BOOLEAN | false |

## patron_descanso / patron_descanso_dia (V26)

| Tabla | Campos clave |
|-------|----------------|
| patron_descanso | empleado_id, vigencia_desde, vigencia_hasta (nullable) |
| patron_descanso_dia | patron_id, dia_semana (LUNES…DOMINGO) |

## saldo_vacaciones (V26 — anual)

| Campo | Descripción |
|-------|-------------|
| empleado_id + anio | UNIQUE |
| antiguedad_anios | Al 31/12 del año de goce |
| dias_totales | Según política activa |
| dias_tomados | Aprobados y consumidos |
| dias_pendientes | Reservados en solicitudes abiertas |
| dias_disponibles | Desnormalizado |
| ajuste_manual / ajuste_manual_motivo | Ajuste RRHH con motivo obligatorio |

## solicitud_vacaciones (V26 ampliada)

Campos nuevos: `saldo_vacaciones_id`, fechas y motivo de contrapropuesta, fechas aprobadas, `motivo_rechazo`.

Estado en tabla `solicitud` (workflow), no en solicitud_vacaciones.

## evento_solicitud_vacaciones (V26 — append-only)

Auditoría: tipo_evento, estado_anterior/nuevo, usuario, timestamp, IP, user_agent, comentario. Trigger de inmutabilidad en PostgreSQL (V27).

---

# Ausentismo

## AbsenceType

Campos:

- Id
- CompanyId
- Name

Ejemplos:

- Enfermedad
- Llegada tarde
- Falta injustificada

---

## Absence

Campos:

- Id
- EmployeeId
- AbsenceTypeId
- Date
- Reason
- IsJustified

---

# Calendario

## Calendar

Campos:

- Id
- CompanyId
- Name

---

## CalendarDay

Campos:

- Id
- CalendarId
- Date
- Type

Tipos:

- WorkingDay
- Holiday
- NonWorkingDay

---

# Comunicaciones

## Communication

Campos:

- Id
- CompanyId
- Title
- Content
- PublishedAt

---

## CommunicationRecipient

Campos:

- Id
- CommunicationId
- EmployeeId

---

## CommunicationAcknowledgement

Campos:

- Id
- CommunicationId
- EmployeeId
- ReadAt

---

# Activos

## AssetCategory

Campos:

- Id
- CompanyId
- Name

---

## Asset

Campos:

- Id
- CompanyId
- CategoryId
- Name
- SerialNumber

---

## AssetAssignment

Campos:

- Id
- AssetId
- EmployeeId
- AssignedAt
- ReturnedAt

---

# Onboarding

## OnboardingTemplate

Campos:

- Id
- CompanyId
- Name

---

## OnboardingTask

Campos:

- Id
- TemplateId
- Name
- Order

---

## EmployeeOnboarding

Campos:

- Id
- EmployeeId
- TemplateId
- Status

---

# Offboarding

## OffboardingTemplate

Campos:

- Id
- CompanyId
- Name

---

## EmployeeOffboarding

Campos:

- Id
- EmployeeId
- Status
- StartedAt
- FinishedAt

---

# Liquidación

## PayrollEventType

Campos:

- Id
- CompanyId
- Name

---

## PayrollEvent

Campos:

- Id
- EmployeeId
- EventTypeId
- Date
- Amount
- Notes

---

# Notificaciones

## Notification

Campos:

- Id
- CompanyId
- Title
- Message
- Type

---

## UserNotification

Campos:

- Id
- UserId
- NotificationId
- ReadAt

---

# Auditoría

## AuditLog

Campos:

- Id
- CompanyId
- UserId
- EntityName
- EntityId
- Action
- OldValues
- NewValues
- CreatedAt

---

# Convención Obligatoria

Todas las tablas deben incluir:

- CreatedAt
- UpdatedAt

Y opcionalmente:

- CreatedBy
- UpdatedBy

---

# Soft Delete (eliminación lógica)

**Regla general:** en este sistema las entidades de negocio **no se eliminan físicamente** de la base de datos. Toda operación de “eliminar” o “desactivar” en la API es una **eliminación lógica** (soft delete).

## Campos estándar

Todas las entidades que extienden `EntidadAuditable` incluyen:

| Campo (BD) | Campo (Java) | Descripción |
|------------|--------------|-------------|
| `is_deleted` | `deleted` | `false` por defecto; pasa a `true` al eliminar |
| `deleted_at` | `deletedAt` | Marca de tiempo de la eliminación lógica |
| `created_at` | `createdAt` | Alta del registro |
| `updated_at` | `updatedAt` | Última modificación |
| `created_by` | `createdBy` | Usuario que creó (opcional) |
| `updated_by` | `updatedBy` | Usuario que modificó (opcional) |

## Comportamiento en código

- Los repositorios filtran con `...AndDeletedFalse` o `is_deleted = false` en consultas.
- Los endpoints `DELETE` de la API marcan `is_deleted = true`; **no ejecutan** `DELETE FROM` en SQL.
- Los registros eliminados **no aparecen** en listados ni búsquedas normales, pero permanecen en BD para auditoría e integridad referencial.

## Excepciones y casos especiales

| Caso | Comportamiento |
|------|----------------|
| **Empleado desactivado** | Estado `DESVINCULADO` + fecha de baja. **No** se marca `is_deleted` en empleado: el legajo y el offboarding siguen accesibles para RRHH. |
| **Comunicación cancelada** | Estado `CANCELADA` (no es soft delete; la fila sigue activa con otro estado). |
| **Flujo de trabajo — pasos** | Al editar un flujo, los pasos viejos se soft-deletean y se reemplazan por pasos nuevos. El `orden` del paso eliminado se negativiza para no violar la restricción única `(flujo_trabajo_id, orden)`. |
| **Auditoría** | La tabla de auditoría **nunca** se elimina (ni lógica ni físicamente). |
| **Legajo** | El legajo del empleado **nunca** se elimina. |

## Entidades con eliminación lógica (implementado)

Empleados (solo en flujos legacy si aplica), usuarios, roles, flujos de trabajo, tipos de solicitud, documentos, comunicaciones archivadas, activos, novedades de liquidación, plantillas de onboarding/offboarding, archivos almacenados, y el resto de entidades `EntidadAuditable`.

## Implicaciones para integraciones

- No asumir que un `DELETE` HTTP borra datos de forma irreversible.
- Para “dar de baja” un empleado usar `DELETE /api/empleados/{id}` → queda `DESVINCULADO`, no borrado.
- Exportaciones y reportes deben filtrar `is_deleted = false` salvo módulos de auditoría explícitos.
