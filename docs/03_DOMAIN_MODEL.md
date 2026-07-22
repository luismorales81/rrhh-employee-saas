
# MODELO DE DOMINIO

## Company
- Id
- Name
- TaxId
- Status
- CreatedAt

## User
- Id
- CompanyId
- Email
- PasswordHash
- Role

## Employee
- Id
- CompanyId
- UserId
- FirstName
- LastName
- DNI
- CUIL
- BirthDate
- HireDate
- Position
- Department
- SupervisorId

## RequestType
- Id
- Name
- RequiresApproval
- RequiresAttachment

## Request
- Id
- EmployeeId
- RequestTypeId
- Status
- Reason
- CreatedAt

## VacationBalance
- EmployeeId
- AvailableDays
- PendingDays
- UsedDays

## Document
- Id
- CompanyId
- EmployeeId (destinatario; null si es plantilla/publicación masiva)
- Type (CONTRATO | ANEXO | REGLAMENTO | CONFIDENCIALIDAD | BENEFICIARIOS | POLITICA | RECIBO | ACTA_ACTIVO | OTRO)
- Title
- FileUrl
- FileHash (SHA-256 del PDF original)
- Version
- RequiresSignature (bool)
- SignatureStatus (PENDIENTE | FIRMADO | RECHAZADO | VENCIDO)
- PublishedAt
- ExpiresAt (opcional)

## EventoFirmaRecibo (recibos de sueldo)

Entidad de trazabilidad append-only vinculada a `Recibo`:

- TipoEvento: `VISTO` | `CONFIRMADO` | `DISCONFORMIDAD`
- Evidencia: IP, user-agent, sistema operativo, `hashPdfEnFirma`
- Ver [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md)
- Pendiente cumplimiento: [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md)

## DocumentSignature
- Id
- DocumentId
- EmployeeId
- SignedAt
- SignatureProvider (proveedor de firma digital)
- CertificateId (referencia al certificado del firmante)
- SignatureHash (hash de la firma aplicada)
- SignedFileUrl (PDF firmado almacenado)
- IpAddress
- DeviceId
- Geolocation (opcional, según consentimiento)
- EvidenceUrl (evidencia legal / acta de firma)

## Communication
- Id
- Title
- Message
- PublishDate

## Asset
- Id
- Name
- Category
- SerialNumber

## AssetAssignment
- Id
- AssetId
- EmployeeId
- AssignedAt
- ReturnedAt
- DeliveryDocumentId (Document tipo ACTA_ACTIVO)
- ReturnDocumentId (Document tipo ACTA_ACTIVO)

## AuditLog
- Id
- Entity
- Action
- UserId
- Timestamp
