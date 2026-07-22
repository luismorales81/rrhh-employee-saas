# Política de retención de recibos de sueldo

**Versión:** 1.0  
**Marco legal:** Art. 140 LCT (conservación durante diez años)

## Alcance

Se conservan durante **10 años** desde la fecha de publicación:

- Metadatos del recibo en base de datos (`recibo`, `evento_firma_recibo`, auditoría).
- Archivos PDF en almacenamiento de objetos (`archivo_almacenado` + blob en disco o bucket S3/MinIO/R2).
- Expedientes probatorios exportados (copia de respaldo generada por RRHH, opcional).

## Ubicación

| Componente | Desarrollo | Producción recomendada |
|------------|------------|------------------------|
| Base de datos | PostgreSQL local / Docker | PostgreSQL gestionado con backups diarios |
| Archivos PDF | `./data/archivos` (disco) | Bucket S3/MinIO/R2 con versionado y lifecycle ≥ 10 años |

## Responsabilidades

- **Cliente / empresa:** conservar acceso a la plataforma y cumplir con backups acordados en el contrato.
- **Operaciones / DevOps:** ejecutar backups automáticos, probar restauración, monitorear el job de verificación de integridad.
- **RRHH:** publicar recibos solo vía `archivoId` (hash SHA-256); exportar expediente ante inspección.

## Verificación de integridad

El backend ejecuta diariamente (03:00) `TareaVerificacionIntegridadRecibos`, que compara `hash_pdf` del recibo con el hash recalculado del archivo. Los fallos se registran en auditoría (`RECIBO_INTEGRIDAD_FALLIDA`).

Configuración: `app.recibos.verificacion-integridad.habilitada` en `application.yml`.

## Backups mínimos

1. **Base de datos:** dump diario con retención ≥ 10 años (o política legal equivalente archivada en frío).
2. **Almacenamiento de archivos:** réplica o versionado del bucket/carpeta de blobs.
3. **Prueba de restauración:** al menos una vez por año (ver [RUNBOOK_RESTAURACION_ALMACENAMIENTO.md](RUNBOOK_RESTAURACION_ALMACENAMIENTO.md)).

## Scripts operativos

| Script | Uso |
|--------|-----|
| `scripts/backup-produccion.bat` / `.sh` | Volcado PostgreSQL + copia de archivos locales |
| `scripts/checklist-produccion-recibos.bat` | Verifica trigger V23 y recuerda backups |
| `scripts/postgresql/trigger-inmutabilidad-eventos-firma.sql` | Manual si Flyway no corrió |

Al desplegar: `--spring.profiles.active=prod` (ver `application-prod.yml`).

- [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md)
- [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md)
- [08_AUDIT_SECURITY.md](08_AUDIT_SECURITY.md)
