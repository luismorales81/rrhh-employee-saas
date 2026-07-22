# Cumplimiento legal de recibos — trabajo pendiente

**Versión:** 1.0  
**Última actualización:** julio 2026  
**Relacionado:** [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md) (lo ya implementado)

Este documento lista **pasos independientes** para cerrar brechas frente a la **Ley 27.802**, **Ley 25.506**, **Art. 140 LCT** y la reglamentación asociada. Cada paso puede abordarse en una pestaña o sesión de trabajo aparte.

---

## Resumen ejecutivo

| Situación | Detalle |
|-----------|---------|
| **Núcleo funcional** | Implementado (publicar PDF, visualizar, confirmar/disconformidad, eventos append-only, hash SHA-256) |
| **Cumplimiento defendible** | **Completo en código** — CP-01 a CP-14; validación jurídica y firma de acta pendiente en cada cliente |
| **Uso demo / piloto** | Aceptable con las limitaciones documentadas aquí |
| **Uso producción** | Requiere cerrar al menos los pasos **CP-01 a CP-05** (prioridad alta) |

---

## Índice de pasos

| ID | Paso | Prioridad | Estado |
|----|------|-----------|--------|
| [CP-01](#cp-01-política-de-retención-10-años) | Política de retención 10 años | Alta | **Implementado** ([RETENCION_RECIBOS.md](RETENCION_RECIBOS.md), job verificación) |
| [CP-02](#cp-02-inmutabilidad-legal-de-recibos-y-eventos) | Inmutabilidad legal (no borrar) | Alta | **Implementado** (servicio + script PostgreSQL) |
| [CP-03](#cp-03-expediente-probatorio-exportable) | Expediente probatorio exportable | Alta | **Implementado** |
| [CP-04](#cp-04-carga-masiva-de-recibos-hu-010) | Carga masiva (HU-010) | Alta | **Implementado** |
| [CP-05](#cp-05-eliminar-modo-legacy-urlarchivo) | Eliminar modo legacy `urlArchivo` | Alta | **Implementado** |
| [CP-06](#cp-06-integración-con-legajo-del-empleado) | Integración con legajo | Media | **Implementado** |
| [CP-07](#cp-07-notificación-de-puesta-a-disposición) | Notificación de puesta a disposición | Media | **Implementado** (email opcional + in-app) |
| [CP-08](#cp-08-alertas-y-seguimiento-rrhh) | Alertas y seguimiento RRHH | Media | **Implementado** |
| [CP-09](#cp-09-republicación-y-versionado-de-pdf) | Republicación y versionado de PDF | Media | **Implementado** |
| [CP-10](#cp-10-texto-legal-y-re-autenticación-al-firmar) | Texto legal y re-autenticación | Media | **Implementado** (texto legal; contraseña configurable) |
| [CP-11](#cp-11-comprobante-de-firma-para-el-empleado) | Comprobante de firma para empleado | Media | **Implementado** (JSON) |
| [CP-12](#cp-12-almacenamiento-cloud-con-versionado) | Almacenamiento cloud (R2) | Alta | **Implementado** ([ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md)) |
| [CP-13](#cp-13-tests-de-integración-del-flujo-legal) | Tests de integración flujo legal | Baja | **Implementado** |
| [CP-14](#cp-14-revisión-reglamentaria-y-acceso-alternativo) | Revisión Decreto 407/2026 y acceso alternativo | Baja | **Implementado** ([DECRETO_407_2026_CHECKLIST.md](DECRETO_407_2026_CHECKLIST.md)) |

---

## Matriz de cumplimiento (actualizada)

| Obligación | Norma | Estado | Paso que lo cierra |
|------------|-------|--------|-------------------|
| Constancia digital de entrega | 27.802 / Art. 140 LCT | **Cumple** | — |
| Firma electrónica simple | Ley 25.506 | **Cumple** | CP-10 refuerza |
| Disconformidad registrada | LCT | **Cumple** | — |
| Fin del doble ejemplar (como proceso) | 27.802 | **Parcial** | CP-04, CP-07 (operación real) |
| Cadena de custodia probatoria | 25.506 / carga empleador | **Parcial** | CP-03, CP-11 |
| Conservación 10 años | Art. 140 LCT | **No cumple** | CP-01, CP-02, CP-12 |
| Prueba ante inspección / juicio | Buenas prácticas | **Parcial** | CP-03, CP-06 |
| Reglamentación vigente | Decreto 407/2026 | **Checklist documentado** | CP-14 |
| Firma digital certificada (PKI) | Ley 25.506 | **Fuera de alcance** | Módulo documentos |

---

## Pasos detallados

### CP-01: Política de retención 10 años

**Prioridad:** Alta  
**Marco legal:** Art. 140 LCT (conservación de recibos durante diez años)

#### Estado actual

- PDFs en disco local (`ServicioAlmacenamiento`, tabla `archivo_almacenado`).
- No hay política documentada de backups, restauración ni prueba de recuperación.
- No hay job de verificación de integridad periódica (hash vs. archivo en disco).

#### Objetivo

Garantizar que recibos y archivos vinculados permanezcan disponibles e íntegros durante **10 años** desde su publicación.

#### Criterios de aceptación

- [ ] Documento de política de retención (qué se guarda, dónde, cuánto tiempo, quién es responsable).
- [ ] Backups automáticos del almacenamiento de archivos + base de datos con retención ≥ 10 años.
- [ ] Procedimiento de restauración probado al menos una vez (runbook).
- [ ] Job o script de verificación periódica: `hash_pdf` del recibo = hash del archivo en almacenamiento.
- [ ] Registro en logs/auditoría cuando falle una verificación de integridad.

#### Áreas afectadas

- Infraestructura / DevOps
- `backend` — job de verificación (opcional)
- `docs/08_AUDIT_SECURITY.md`

#### Dependencias

- Ninguna (puede iniciarse en paralelo con CP-02).

---

### CP-02: Inmutabilidad legal de recibos y eventos

**Prioridad:** Alta  
**Marco legal:** Art. 140 LCT; cadena de custodia (Ley 25.506 — carga de prueba del empleador)

#### Estado actual

- `evento_firma_recibo`: append-only por diseño de servicio (sin endpoints DELETE/UPDATE).
- `recibo` extiende `EntidadAuditable` → **soft delete posible** (`is_deleted`).
- `archivo_almacenado` también admite eliminación lógica.
- No hay triggers ni restricciones en base de datos que impidan borrado.

#### Objetivo

Impossibilitar (o bloquear en aplicación) la modificación o eliminación de recibos publicados, sus eventos de firma y los archivos PDF vinculados una vez firmados o en disconformidad.

#### Criterios de aceptación

- [ ] Servicio: rechazar soft delete de `recibo` en estados `FIRMADO`, `DISCONFORMIDAD` y de cualquier recibo con eventos `CONFIRMADO`/`DISCONFORMIDAD`.
- [ ] Servicio: rechazar modificación de `hash_pdf`, `archivo_id`, `empleado_id`, `periodo` tras publicación.
- [ ] Servicio: no permitir eliminar `archivo_almacenado` referenciado por un recibo activo.
- [ ] (Opcional recomendado) Migración Flyway: trigger o constraint que impida DELETE físico en `evento_firma_recibo`.
- [ ] Tests unitarios/integración que validen los rechazos.
- [ ] Actualizar `11_BUSINESS_RULES.md` y `16_DATABASE_DESIGN.md`.

#### Áreas afectadas

- `backend` — `ServicioRecibo`, `ServicioAlmacenamiento`, migración SQL
- Tests

#### Dependencias

- Ninguna.

---

### CP-03: Expediente probatorio exportable

**Prioridad:** Alta  
**Marco legal:** Ley 25.506 (firma electrónica simple — prueba a cargo del empleador)

#### Estado actual

- RRHH puede ver eventos en UI (botón **Eventos** en `/recibos`).
- No hay exportación estructurada (ZIP, PDF de acta, JSON firmado) para inspección laboral o juicio.

#### Objetivo

Permitir a RRHH (y eventualmente auditor autorizado) descargar un **expediente** por recibo con toda la evidencia reunida.

#### Criterios de aceptación

- [ ] Endpoint `GET /api/recibos/{id}/expediente` (o similar) que devuelva ZIP con:
  - PDF original
  - `manifest.json`: hash SHA-256, metadatos del recibo, datos del empleado (nombre, CUIL/DNI, legajo)
  - Línea de tiempo de `evento_firma_recibo` (timestamps UTC, IP, user-agent, SO, tipo, comentario)
  - Identidad de quien publicó (`publicado_por_id` resuelto a nombre/email)
- [ ] UI RRHH: botón **Exportar expediente** en detalle/listado de recibos.
- [ ] Permiso dedicado (ej. `recibos:exportar_expediente`) en matriz de permisos.
- [ ] Documentar formato en `RECIBOS_FIRMA_ELECTRONICA.md`.

#### Áreas afectadas

- `backend` — nuevo servicio/controlador
- `frontend` — `pagina-recibos` RRHH
- `docs/04_PERMISSION_MATRIX.md`

#### Dependencias

- Ninguna (mejora con CP-06 para incluir referencia a legajo).

---

### CP-04: Carga masiva de recibos (HU-010)

**Prioridad:** Alta  
**Marco legal:** No exigido explícitamente; requisito **operativo** para liquidaciones reales

#### Estado actual

- Solo carga **individual** desde `/recibos` (un empleado, un PDF, un período).
- HU-010 listada en `02_USER_STORIES.md` sin implementar.

#### Objetivo

Publicar recibos de un período para muchos empleados en una sola operación (típico: un PDF por empleado o ZIP con convención de nombres).

#### Criterios de aceptación

- [ ] Endpoint `POST /api/recibos/carga-masiva` (multipart: ZIP de PDFs o CSV + carpeta).
- [ ] Convención de emparejamiento archivo ↔ empleado (por CUIL, legajo o nombre en nombre de archivo).
- [ ] Informe de resultado: cargados, omitidos, errores por fila.
- [ ] UI RRHH: asistente de carga masiva con preview y confirmación.
- [ ] Cada recibo creado con `archivoId` + `hash_pdf` (sin atajos legacy).
- [ ] Tests con ZIP de ejemplo en demo.

#### Áreas afectadas

- `backend` — `ServicioRecibo`, controlador
- `frontend` — nueva pantalla o modal en `/recibos`
- `docs/07_WORKFLOWS.md`, `DEMO_GUIA.md`

#### Dependencias

- Recomendable completar **CP-05** antes o en el mismo paso (exigir `archivoId`).

---

### CP-05: Eliminar modo legacy `urlArchivo`

**Prioridad:** Alta  
**Marco legal:** Integridad y trazabilidad (requisito implícito para firma con validez probatoria)

#### Estado actual

- `SolicitudCargarRecibo` aún permite publicar con `urlArchivo` sin `archivoId`/`hash_pdf`.
- Esos recibos **no son firmables** con verificación de integridad completa.

#### Objetivo

Que todo recibo nuevo pase obligatoriamente por almacenamiento con hash; migrar o republicar datos legacy.

#### Criterios de aceptación

- [ ] Validación backend: `archivoId` obligatorio en `POST /api/recibos` (rechazar 400 si falta).
- [ ] Eliminar o deprecar campo `urlArchivo` en API pública y frontend RRHH.
- [ ] Migración de datos: identificar recibos sin `archivo_id` y marcarlos o republicarlos.
- [ ] Actualizar seed demo y documentación.
- [ ] Mensaje claro en UI si queda algún recibo histórico sin hash.

#### Áreas afectadas

- `backend` — DTO, `ServicioRecibo`, posible migración V19
- `frontend` — formulario carga recibos
- `docs/03_DOMAIN_MODEL.md`, `16_DATABASE_DESIGN.md`

#### Dependencias

- Ninguna.

---

### CP-06: Integración con legajo del empleado

**Prioridad:** Media  
**Marco legal:** Art. 140 LCT (conservación en legajo del trabajador)

#### Estado actual

- Recibos viven en módulo `/recibos` y `/mis-recibos`.
- El legajo (`/empleados/{id}/legajo`) no lista recibos ni enlaza al PDF firmado.

#### Objetivo

Que el legajo digital muestre el historial de recibos del empleado y permita acceder al PDF y estado de firma; opcionalmente copiar referencia al export de legajo.

#### Criterios de aceptación

- [ ] Pestaña o sección **Recibos** en legajo RRHH con listado por período y estado.
- [ ] Enlace al expediente (CP-03) desde legajo.
- [ ] (Opcional) Inclusión de resumen de recibos en export PDF/JSON del legajo si existe esa función.
- [ ] Permisos: solo RRHH con acceso a legajo.

#### Áreas afectadas

- `backend` — endpoint listar por empleado (puede reutilizar existente)
- `frontend` — componente legajo
- `docs/39_EMPLOYEE_SELF_SERVICE.md`

#### Dependencias

- CP-03 recomendado para export desde legajo.

---

### CP-07: Notificación de puesta a disposición

**Prioridad:** Media  
**Marco legal:** 27.802 — constancia de que el recibo fue puesto a disposición del trabajador

#### Estado actual

- Notificación **in-app** al publicar recibo.
- Sin email, push ni registro de “notificación enviada” como evento probatorio.

#### Objetivo

Dejar constancia de que el empleado fue informado de la disponibilidad del recibo por al menos un canal adicional (email) y registrar el envío.

#### Criterios de aceptación

- [ ] Al publicar recibo: email al empleado con enlace a `/mis-recibos` (o deep link móvil).
- [ ] Evento o campo de auditoría: `notificado_en`, canal, destinatario.
- [ ] (Opcional) Reenvío manual desde RRHH.
- [ ] Configuración por empresa: activar/desactivar email (SMTP ya configurado en proyecto).

#### Áreas afectadas

- `backend` — `ServicioRecibo`, servicio de notificaciones/email
- `frontend` — indicador “notificado” en listado RRHH
- `docs/17_EVENT_CATALOG.md`

#### Dependencias

- Infra: SMTP o proveedor de email en producción.

---

### CP-08: Alertas y seguimiento RRHH

**Prioridad:** Media  
**Marco legal:** Buena práctica operativa; posibles plazos en reglamentación (CP-14)

#### Estado actual

- Contador de pendientes en panel empleado (`recibos sin firmar`).
- RRHH ve estados en listado pero sin alertas por antigüedad ni dashboard agregado.

#### Objetivo

Visibilidad para RRHH de recibos sin firmar, disconformidades recientes y vencimientos si la reglamentación fija plazos.

#### Criterios de aceptación

- [ ] Widget en panel RRHH: recibos pendientes de firma por período.
- [ ] Filtros: `PENDIENTE`/`VISTO` por más de N días (N configurable).
- [ ] Destacar `DISCONFORMIDAD` con comentario visible.
- [ ] (Opcional) Notificación RRHH cuando un empleado firma en disconformidad.

#### Áreas afectadas

- `backend` — consultas agregadas
- `frontend` — panel RRHH, `/recibos`
- App móvil RRHH (opcional)

#### Dependencias

- CP-14 si se definen plazos legales obligatorios.

---

### CP-09: Republicación y versionado de PDF

**Prioridad:** Media  
**Marco legal:** Corrección de errores sin perder historial probatorio

#### Estado actual

- `hash_pdf` es inmutable; no hay flujo para “republicar” un recibo erróneo.
- RRHH no puede corregir un PDF ya publicado sin intervención manual en BD.

#### Objetivo

Permitir anular un recibo **solo en estado PENDIENTE/VISTO** (sin firma) y republicar; si ya fue firmado, crear **nueva versión** conservando la anterior y sus eventos.

#### Criterios de aceptación

- [ ] Flujo `republicar`: nuevo `archivo_id` + nuevo `hash_pdf`, recibo hijo o campo `version` / `recibo_anterior_id`.
- [ ] Eventos y PDF de versiones anteriores permanecen consultables (solo lectura).
- [ ] UI RRHH: acción **Republicar** con motivo obligatorio auditado.
- [ ] Empleado solo ve la versión vigente para firmar; historial visible tras firma.

#### Áreas afectadas

- `backend` — modelo, migración, `ServicioRecibo`
- `frontend` — acciones RRHH
- `docs/03_DOMAIN_MODEL.md`

#### Dependencias

- CP-02 (reglas de inmutabilidad deben coexistir con versionado).

---

### CP-10: Texto legal y re-autenticación al firmar

**Prioridad:** Media  
**Marco legal:** Ley 25.506 (consentimiento informado); refuerzo de firma electrónica simple

#### Estado actual

- Checkbox genérico en `/mis-recibos` con mención breve a Ley 27.802.
- Firma con JWT de sesión activa, sin segundo factor ni reingreso de contraseña.

#### Objetivo

Texto legal completo y, opcionalmente, step-up authentication en el acto de firma.

#### Criterios de aceptación

- [ ] Leyenda normativa visible antes de confirmar (efectos de firma electrónica simple, derecho a disconformidad, conservación).
- [ ] Checkbox obligatorio con texto aprobado por asesoría legal (placeholder hasta revisión jurídica).
- [ ] (Opcional) `POST /confirmar-recepcion` exige contraseña actual o OTP.
- [ ] Misma experiencia en app móvil (`pagina-detalle-recibo`).
- [ ] i18n: textos en español (Argentina).

#### Áreas afectadas

- `frontend` — portal empleado
- `app-movil` — detalle recibo
- `backend` — validación opcional de contraseña

#### Dependencias

- Revisión jurídica del texto (externa al código).

---

### CP-11: Comprobante de firma para el empleado

**Prioridad:** Media  
**Marco legal:** Transparencia y prueba para el trabajador

#### Estado actual

- Tras firmar, el estado cambia a `FIRMADO` o `DISCONFORMIDAD`.
- El empleado puede volver a descargar el PDF del recibo, pero no un **comprobante de la constancia de firma**.

#### Objetivo

Generar documento descargable que acredite la confirmación o disconformidad con fecha, hash y resumen de eventos.

#### Criterios de aceptación

- [ ] `GET /api/recibos/{id}/comprobante` → PDF o JSON descargable solo para el empleado titular (o RRHH).
- [ ] Contenido: período, montos, tipo de confirmación, timestamp, hash del PDF, ID del evento.
- [ ] Botón **Descargar comprobante** en `/mis-recibos` y app móvil tras firma.
- [ ] No sustituye al expediente completo (CP-03); es resumen para el trabajador.

#### Áreas afectadas

- `backend` — generación PDF (puede usar librería existente del proyecto)
- `frontend`, `app-movil`

#### Dependencias

- Ninguna.

---

### CP-12: Almacenamiento cloud con versionado

**Prioridad:** Alta (producción)  
**Estado:** **Implementado** — detalle en [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md)

#### Entregables

- [x] `PuertoAlmacenamientoArchivos` + `AdaptadorAlmacenamientoDisco` + `AdaptadorAlmacenamientoS3`
- [x] Migración V19: `clave_almacenamiento`
- [x] Perfiles `minio` y `migracion`
- [x] MinIO en `docker-compose.yml` con versionado de bucket
- [x] `ServicioMigracionAlmacenamiento` (disco → S3/R2)
- [ ] Política de retención 10 años en bucket R2 (operativa, ver CP-01)

#### Sub-pasos AS-01 … AS-08

Ver [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md#sub-pasos-de-implementación-as-01--as-08).

#### Dependencias

- CP-01 (política de retención y backups en producción).

---

### CP-13: Tests de integración del flujo legal

**Prioridad:** Baja  
**Marco legal:** Calidad y regresión del cumplimiento funcional

#### Estado actual

- Tests unitarios básicos (`ServicioReciboTest`, `UtilidadClienteHttpTest`).
- Sin test end-to-end: publicar → visualizar → firmar → verificar eventos → hash mismatch 409.

#### Objetivo

Suite de integración que valide el flujo legal completo y casos borde.

#### Criterios de aceptación

- [ ] `@SpringBootTest` o MockMvc: flujo feliz completo.
- [ ] Casos: disconformidad sin comentario (400), hash alterado (409), doble firma (409), empleado ajeno (403).
- [ ] (Opcional) Test E2E frontend con Playwright/Cypress en un recibo demo.

#### Áreas afectadas

- `backend/src/test`
- CI pipeline

#### Dependencias

- Estable tras CP-02 y CP-05.

---

### CP-14: Revisión reglamentaria y acceso alternativo

**Prioridad:** Baja (pero obligatoria antes de go-live legal)  
**Marco legal:** Decreto 407/2026; principio de no discriminación de trabajadores sin acceso digital

#### Estado actual

- Decreto citado en documentación sin checklist punto por punto.
- No hay procedimiento para trabajadores sin smartphone/PC.

#### Objetivo

Validar requisitos reglamentarios vigentes y documentar canal alternativo organizativo.

#### Criterios de aceptación

- [ ] Revisión del texto del Decreto 407/2026 (y normas que lo modifiquen) → checklist en este doc o anexo.
- [ ] Procedimiento documentado: kiosco en planta, RRHH asistido, o constancia en soporte físico firmada — fuera o dentro del sistema.
- [ ] Ajustar CP-07 y CP-08 si el reglamento exige plazos o canales específicos.
- [ ] Firma del responsable legal / RRHH de conformidad con el procedimiento.

#### Áreas afectadas

- Documentación
- Posible configuración por empresa

#### Dependencias

- Ninguna técnica; puede hacerse en paralelo.

---

## Orden sugerido de implementación

Para minimizar retrabajo:

```
CP-05 → CP-02 → AS-01…AS-08 (almacenamiento) → CP-04 → CP-03 → CP-01
         ↓
CP-06, CP-07, CP-08, CP-09, CP-10, CP-11 (paralelo entre sí)
         ↓
CP-13 → CP-14
```

---

## Historias de usuario — estado

| ID | Historia | Estado |
|----|----------|--------|
| HU-010 | Cargar recibos masivamente | **Implementada** (CP-04) |
| HU-011 | Ver recibos | **Implementada** |
| HU-012 | Descargar recibos | **Implementada** (PDF; comprobante → CP-11) |
| HU-013 | Firmar recibo (conformidad de recepción) | **Implementada** (refuerzo → CP-10) |

---

## Cómo usar este documento en Cursor

1. Abrir **una pestaña de chat por paso** (ej. “Implementar CP-02 inmutabilidad recibos”).
2. Indicar el ID del paso (`CP-0X`) para cargar contexto desde este archivo.
3. Al completar un paso: marcar criterios de aceptación, actualizar la columna **Estado** en el índice y reflejar cambios en [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md).

---

## Referencias

- [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md) — diseño implementado
- [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md) — almacenamiento disco / MinIO / R2
- [07_WORKFLOWS.md](07_WORKFLOWS.md) — flujo de negocio
- [08_AUDIT_SECURITY.md](08_AUDIT_SECURITY.md) — seguridad y auditoría
- [11_BUSINESS_RULES.md](11_BUSINESS_RULES.md) — reglas de negocio
- [16_DATABASE_DESIGN.md](16_DATABASE_DESIGN.md) — esquema
- [DEMO_GUIA.md](DEMO_GUIA.md) — guía de demostración
