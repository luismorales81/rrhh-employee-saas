
# SEGURIDAD

## Obligatorio

- HTTPS
- JWT (secreto ≥ 32 bytes reales; sin relleno)
- Refresh Tokens revocables
- Rate limiting en `/api/auth/login` y `/api/auth/refrescar`
- Ownership/ACL en descarga de archivos (`GET /api/archivos/{id}/contenido`)
- CORS estricto en perfil `prod` (`CORS_ORIGENES` / `app.cors.origenes`)
- Headers HTTP: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`
- Logout de clientes debe llamar `POST /api/auth/cerrar-sesion` (web y app móvil)
- Auditoría completa
- Logs inmutables
- Backup diario
- Multi Tenant

## Modelo de amenazas (mínimo)

| Amenaza | Control |
|---------|---------|
| Fuerza bruta / credential stuffing | Rate limit in-memory por IP+email (login) e IP (refresco); respuesta 429 |
| Enumeración de cuentas | Mensaje único `Credenciales inválidas` (401) en login |
| IDOR horizontal de archivos | `ServicioControlAccesoArchivo`: empleado solo propios; RRHH con permisos amplios del tenant |
| Cross-tenant | `empresaId` en consultas; SuperAdmin solo con `X-Empresa-Id` |
| Robo de sesión (XSS → localStorage) | Mitigación parcial (headers, sin `innerHTML` en comunicaciones); deuda: cookies HttpOnly |
| CORS abusivo en prod | Orígenes explícitos por variable de entorno; fallar si faltan |
| Credenciales demo en build | Ayudas demo solo si `environment.produccion === false` |

### Superficie pública

- `OPTIONS /**`
- `/api/auth/**` (login/refresco públicos; cerrar-sesión y `/yo` autenticados)
- Swagger/OpenAPI **solo** fuera de perfil `prod`

### Deuda de seguridad (backlog)

- 2FA y recuperación de contraseña
- Migración de sesión a cookies HttpOnly + SameSite
- Secure Storage / biometría en Capacitor
- Separar clave AES de secretos de tenant del secreto JWT
- Attestation / binding de dispositivo en fichaje
- CSP estricto en frontends

## Auditoría

Registrar:
- Usuario
- Acción
- Fecha
- Entidad
- Valores previos
- Valores nuevos

Los registros de auditoría **nunca se eliminan** (ni lógica ni físicamente).

## Eliminación lógica

- Las entidades de negocio usan soft delete (`is_deleted`, `deleted_at`).
- Un `DELETE` HTTP no borra filas de la base de datos.
- Los datos eliminados lógicamente quedan disponibles para trazabilidad y cumplimiento.
- Ver detalle en `docs/16_DATABASE_DESIGN.md`.

## Firma digital (documentos)

Requisitos obligatorios para **documentos** con canvas/certificado:
- Integración con proveedor de firma digital conforme normativa argentina (Ley 25.506)
- Hash SHA-256 del documento original antes de firmar
- Almacenamiento del PDF firmado y evidencia de firma
- Registro inmutable: firmante, fecha/hora, documento, hash, certificado, IP, dispositivo
- No permitir modificar documento después de firmado (solo nueva versión)
- Revocación: nueva versión del documento, nunca sobrescribir firmado
- HTTPS obligatorio en todo el flujo de firma
- Autenticación previa (JWT) + validación de identidad antes de firmar

## Firma electrónica simple (recibos de sueldo)

Conforme **Ley 27.802** (Modernización Laboral) y **Ley 25.506**:

- Recibos: confirmación autenticada (JWT) + clic de conformidad o disconformidad.
- Tabla `evento_firma_recibo` append-only: IP, user-agent, SO, hash PDF en cada acto.
- `recibo.hash_pdf` inmutable desde la publicación; verificación SHA-256 al confirmar.
- Almacenamiento de objetos vía `PuertoAlmacenamientoArchivos` (disco, MinIO, Cloudflare R2). Ver [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md) y [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md).
- Acceso a archivos **solo** por API autenticada; cifrado en reposo delegado al proveedor S3.
- Retención 10 años: política en [RETENCION_RECIBOS.md](RETENCION_RECIBOS.md); job `TareaVerificacionIntegridadRecibos` (diario 03:00).
- Inmutabilidad: guardas en servicio + trigger PostgreSQL (Flyway V23 / `scripts/postgresql/`).
- Expediente probatorio: `GET /api/recibos/{id}/expediente`.
- En producción: perfil `prod`, backups con `scripts/backup-produccion.bat`, checklist `scripts/checklist-produccion-recibos.bat`.
