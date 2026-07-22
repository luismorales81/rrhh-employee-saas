# EVENT CATALOG

Version: 1.1

## Objetivo

Definir todos los eventos de negocio de la plataforma.

Los eventos representan hechos ocurridos dentro del sistema.

Los eventos permiten:

- Auditoría
- Notificaciones
- Automatizaciones
- IA
- Integraciones futuras

---

# Convenciones

Formato:

Entidad + Acción

Ejemplos:

EmployeeCreated
VacationApproved
DocumentSigned

---

# EMPLOYEES

## EmployeeCreated

Se dispara cuando se crea un empleado.

Payload:

- EmployeeId
- CompanyId

---

## EmployeeUpdated

Se dispara cuando se modifica información.

---

## EmployeeActivated

---

## EmployeeSuspended

---

## EmployeeTerminated

---

## EmployeeRehired

---

# USERS

## UserInvited

---

## UserActivated

---

## UserDeactivated

---

# REQUESTS

## RequestCreated

Empleado crea solicitud.

Payload:

- RequestId
- CompanyId
- EmployeeId
- RequestTypeId

---

## RequestSubmitted

Solicitud enviada a flujo de aprobación.

---

## RequestApproved

Solicitud aprobada en todos los pasos.

---

## RequestRejected

Solicitud rechazada en algún paso.

---

## RequestCancelled

Solicitud cancelada por el empleado o administrador.

---

# WORKFLOWS

## WorkflowStarted

Se inicia una instancia de flujo para una solicitud.

Payload:

- WorkflowInstanceId
- RequestId
- WorkflowId
- CompanyId

---

## WorkflowStepAssigned

Un aprobador recibe un paso pendiente.

Payload:

- WorkflowInstanceId
- WorkflowStepId
- ApproverId
- CompanyId

Usos:

- Notificación de aprobación pendiente

---

## WorkflowApproved

Paso aprobado; puede avanzar al siguiente paso o completar el flujo.

Payload:

- WorkflowInstanceId
- WorkflowStepId
- UserId
- Comments

---

## WorkflowRejected

Paso rechazado; la solicitud queda en estado final Rechazado.

Payload:

- WorkflowInstanceId
- WorkflowStepId
- UserId
- Comments

---

## WorkflowEscalated

Paso escalado a otro aprobador o nivel.

Payload:

- WorkflowInstanceId
- WorkflowStepId
- UserId
- Comments

---

## WorkflowChangesRequested

Se solicitan cambios al empleado antes de continuar el flujo.

Payload:

- WorkflowInstanceId
- WorkflowStepId
- UserId
- Comments

---

# VACATIONS

## VacationRequested

---

## VacationApproved

---

## VacationRejected

---

## VacationCancelled

---

## VacationBalanceUpdated

---

# PERMISOS GRANULARES (referencia)

## Workflow

workflow.configure

workflow.view

---

## Requests

request.create

request.approve

request.reject

request.cancel

request.view

request.request_changes

request.escalate

---

## Vacations

vacation.request

vacation.approve

vacation.view

vacation.balance.view

---

# CERTIFICADOS MEDICOS

## CertificadoCargado

Se dispara cuando se sube un certificado medico.

Payload:

- CertificadoId
- EmpleadoId
- EmpresaId
- TipoExamen
- OrigenCarga

---

## CertificadoExtraido

Se dispara cuando Vertex AI (Gemini) devuelve datos estructurados al backend.

Payload:

- CertificadoId
- ConfianzaExtraccion

---

## CertificadoEnRevision

Se dispara al pasar a revision manual (fase 1 siempre).

---

## CertificadoValidado

Se dispara cuando el auditor medico confirma o corrige la extraccion.

---

## CertificadoRechazado

Se dispara cuando el auditor medico rechaza el certificado.

---

## CertificadoRequiereAclaracion

Se dispara ante datos incompletos o documento no reconocido.
