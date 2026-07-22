# Certificados Médicos Digitales — Medicina Laboral

## Objetivo

Permitir carga de certificados médicos (foto/PDF), extracción estructurada vía **Vertex AI (Gemini)** directamente desde el backend y validación humana auditada antes de impactar legajo y reportes de ausentismo.

**Principio:** la IA solo sugiere; la validación final es humana y queda en `evento_certificado` (append-only).

## Adaptaciones al codebase

- IDs `Long` (no UUID), igual que el resto del SaaS.
- Soft delete y auditoría vía `EntidadAuditable`.
- Multi-tenant con `ContextoTenant`.
- Storage: MinIO/S3/disco vía `ServicioAlmacenamiento` (`archivo_id` + `archivo_url` + hash).

## Migraciones

| Versión | Contenido |
|---------|-----------|
| V30 | Tablas, permisos, roles `MedicoLaboral` / `AuditorMedico` |
| V31 | Trigger PostgreSQL inmutabilidad `evento_certificado` |
| V32 | Marcador de cumplimiento (H2/tests) |
| V34 | Reintentos extracción, versionado `datos_extraidos_certificado`, campos Vertex por tenant |
| V35 | Índices únicos PostgreSQL para versionado vigente |

## Permisos RBAC

| Código | Uso |
|--------|-----|
| `certificado.cargar` | Empleado / médico laboral |
| `certificado.ver_completo` | Médico / auditor (incluye diagnóstico) |
| `certificado.ver_resumen` | Admin / RRHH (sin diagnóstico) |
| `certificado.ver_propio` | Empleado (completo solo propio) |
| `certificado.validar` / `certificado.rechazar` | Auditor médico |
| `certificado.configurar` | Config auditoría médica y Vertex AI |
| `reporte.ausentismo.ver` | Reportes |

DTOs separados: `CertificadoCompletoDTO` vs `CertificadoResumenDTO` (sin filtro condicional sobre la misma entidad).

## Pipeline (Vertex AI directo)

1. `POST /api/certificados` → estado `CARGADO` → `EN_PROCESAMIENTO` + extracción async (`ServicioProcesamientoCertificado`)
2. `GeminiExtraccionService` llama Vertex AI con credenciales del tenant
3. Backend versiona `DatosExtraidosCertificado` (`numero_version`, `es_vigente`) → `EXTRAIDO` → evalúa alertas → `EN_REVISION` o `REQUIERE_ACLARACION`
4. Auditor: `PATCH .../validar` o `.../rechazar`
5. Reprocesamiento manual: `PATCH /api/certificados/{id}/reprocesar` (permisos `certificado.configurar` o `certificado.validar`)
6. Job `@Scheduled` reintenta certificados colgados en `EN_PROCESAMIENTO`

Si la IA no está configurada para el tenant, el certificado pasa directo a `EN_REVISION`.

## Bandeja de revisión

`GET /api/certificados/bandeja-revision?incluirRequiereAclaracion=false`

- **Permisos:** `certificado.ver_completo`, `certificado.ver_resumen` o `certificado.validar`
- **Estados incluidos:** siempre `EN_REVISION`; opcionalmente `REQUIERE_ACLARACION` si `incluirRequiereAclaracion=true`
- **Orden:** certificados con revisión prioritaria primero; luego por fecha de carga descendente
- **Respuesta:** lista de `CertificadoResumenDTO` o `CertificadoCompletoDTO` según rol (mismo criterio que listado por empleado)

En la web Angular (`/certificados-medicos`), la pestaña **Bandeja de revisión** consume este endpoint y permite filtrar por texto, incluir requiere aclaración y abrir el detalle para validar/rechazar/reprocesar.

## App móvil empleado

Ruta: `/tabs/ausentismo` (`pagina-ausentismo.component.ts`).

Flujo de carga (dos pasos, igual que la web):

1. `POST /api/archivos` — subida del PDF/imagen
2. `POST /api/certificados` — `{ archivoId, tipoExamen: 'AUSENTISMO', origenCarga: 'EMPLEADO' }`

Casos de uso:

- **Reportar ausencia justificada:** sube certificado → registra ausencia con `urlCertificado` (referencia al archivo del certificado)
- **Justificar ausencia existente:** sube certificado → `PUT /api/ausentismo/mis-ausencias/{id}/justificar`
- **Cargar certificado suelto:** solo pasos 1 y 2; aparece en **Mis certificados médicos** con estado de revisión

Permisos: `certificado.cargar`, `archivo.subir`, `certificado.ver_propio` (listado propio).

## Configuración por tenant (panel admin)

`PUT /api/certificados/configuracion-auditoria` con permiso `certificado.configurar`:

| Campo | Descripción |
|-------|-------------|
| `proyectoGcp` | ID del proyecto GCP |
| `regionVertex` | Región Vertex (ej. `us-central1`) |
| `modeloGemini` | Modelo del catálogo permitido |
| `credencialesServicioJson` | JSON de cuenta de servicio (solo escritura; se cifra en BD) |
| `maxIntentosExtraccion` | Máximo de reintentos automáticos (default 3) |

La respuesta expone `iaConfigurada`, `credencialesConfiguradas` (boolean) y **nunca** el JSON completo de credenciales.

## Variables globales (`application.yml`)

```yaml
app.certificados:
  ia:
    simulada: ${CERTIFICADOS_IA_SIMULADA:false}   # true en tests / local sin GCP
  reintento:
    cron: ${CERTIFICADOS_REINTENTO_CRON:0 */5 * * * *}
    minutos-colgado: ${CERTIFICADOS_REINTENTO_MINUTOS:10}
```

## Funciones puras

- `EvaluadorAlertaCertificado.evaluar`
- `ValidadorTransicionEstadoCertificado.validarTransicionEstado`
- `CalculadorAusentismoEmpleado.calcular`
- `ParseadorRespuestaGemini.parsear`

## Pendiente legal (no bloquea desarrollo)

- Consentimiento específico para IA externa sobre datos de salud
- Transferencia internacional Ley 25.326 (Vertex AI)
- DPA con Google si corresponde
