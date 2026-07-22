
# FLUJOS

## Solicitud Vacaciones (LCT)

Empleado
-> Simula fecha inicio (`POST /api/vacaciones/simular-fecha`)
-> Solicita con validación completa (`POST /api/vacaciones/solicitudes`)
-> Estado workflow: `EN_REVISION`
-> RRHH abre detalle en `/vacaciones` (validación automática)
-> RRHH: Aprobar / Contrapropuesta / Rechazar (`PATCH /api/vacaciones/solicitudes/{id}/...`)
-> Si contrapropuesta: estado `CAMBIOS_SOLICITADOS`
-> Empleado acepta (`POST .../aceptar-contrapropuesta`) → vuelve a `EN_REVISION`
-> Al aprobar: `ServicioMotorWorkflow` + confirmación de días en saldo anual
-> Notificaciones in-app al empleado y aprobadores

Detalle: [VACACIONES_LCT.md](VACACIONES_LCT.md)

## Documento con firma digital

RRHH publica documento (PDF + RequiereFirma)
-> Sistema notifica al empleado (push + in-app)
-> Empleado abre documento en la app
-> Empleado revisa contenido
-> Empleado firma digitalmente desde la app
-> Sistema genera PDF firmado + evidencia
-> Sistema registra auditoría (hash, certificado, fecha, dispositivo)
-> RRHH consulta estado de firma

## Onboarding

RRHH crea empleado
-> Invitación a la app
-> Empleado completa perfil
-> Sistema asigna paquete documental obligatorio:
   - Contrato de trabajo
   - Reglamento interno
   - Acuerdo de confidencialidad
   - Designación de beneficiarios (si aplica)
-> Empleado firma digitalmente cada documento desde la app
-> Onboarding completo cuando todos los obligatorios están FIRMADO

## Recibo de sueldo (Ley 27.802 / firma electrónica simple)

RRHH sube PDF (`POST /api/archivos`) y publica recibo con `archivoId` + hash SHA-256 inmutable  
**Carga masiva:** ZIP de PDFs nombrados por CUIL/legajo/DNI → `POST /api/recibos/carga-masiva` (tras `analizar`)
-> Sistema notifica al empleado
-> Empleado abre PDF (`POST /visualizar` → evento `VISTO`, estado `VISTO`)
-> Empleado confirma recepción conforme o en disconformidad (`POST /confirmar-recepcion`)
-> Sistema registra evento append-only en `evento_firma_recibo` (IP, UA, hash)
-> Estado final: `FIRMADO` o `DISCONFORMIDAD`
-> RRHH puede exportar expediente probatorio (`GET /expediente`)

Detalle: [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md)  
Cumplimiento: [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md)

## Entrega de activo

Supervisor/RRHH registra entrega
-> Sistema genera acta de entrega (PDF)
-> Empleado firma digitalmente desde la app
-> Activo queda asignado con trazabilidad

## Devolución de activo

Supervisor/RRHH registra devolución
-> Sistema genera acta de devolución (PDF)
-> Empleado firma digitalmente desde la app
-> Activo queda disponible con trazabilidad
