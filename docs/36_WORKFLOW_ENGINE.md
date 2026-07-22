# WORKFLOW ENGINE

Version: 1.0

## Objetivo

Permitir que cada empresa configure procesos de aprobación sin desarrollo.

---

# Concepto

Un Workflow es una secuencia de pasos.

Ejemplo:

Solicitud de Vacaciones

Empleado
→ Supervisor
→ RRHH

---

# Entidades

Workflow

- Id
- CompanyId
- Name
- Description
- IsActive

WorkflowStep

- Id
- WorkflowId
- Order
- Name
- StepType

---

# Tipos de Paso

- Supervisor Directo
- RRHH
- Gerencia
- Rol específico
- Usuario específico

---

# Acciones

- Aprobar
- Rechazar
- Solicitar cambios
- Escalar

---

# Configuración

Cada tipo de solicitud puede utilizar un workflow diferente.

Ejemplo:

Vacaciones
→ Workflow Vacaciones

Licencia Médica
→ Workflow Licencias

---

# Reglas

Si un paso rechaza:

Estado final = Rechazado

Si todos aprueban:

Estado final = Aprobado

---

# Auditoría

Registrar:

- Usuario
- Acción
- Comentario
- Fecha

---

# Notificaciones

Generar automáticamente:

- Nueva aprobación pendiente
- Solicitud aprobada
- Solicitud rechazada