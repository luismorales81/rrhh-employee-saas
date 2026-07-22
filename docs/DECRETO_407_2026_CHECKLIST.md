# Checklist reglamentario — Decreto 407/2026 y normativa asociada

**Versión:** 1.0  
**Última actualización:** julio 2026  
**Relacionado:** [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md) (CP-14), [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md)

> **Nota:** Este checklist es una guía operativa para validación jurídica antes de go-live. Debe ser revisado y firmado por asesoría legal del empleador. No constituye dictamen jurídico.

---

## Marco normativo de referencia

| Norma | Tema |
|-------|------|
| Ley 27.802 | Constancia digital de entrega de recibos |
| Ley 25.506 | Firma electrónica simple |
| Art. 140 LCT | Conservación de recibos (10 años) |
| Decreto 407/2026 | Reglamentación de documentación laboral digital |

---

## Checklist de cumplimiento reglamentario

### 1. Puesta a disposición del recibo

| # | Requisito | Implementación en sistema | Estado |
|---|-----------|---------------------------|--------|
| R-01 | El trabajador debe poder acceder al recibo en soporte digital | Portal `/mis-recibos`, app móvil | Cumple |
| R-02 | Constancia de que el recibo fue puesto a disposición | Notificación in-app + email opcional (`notificado_en`, CP-07) | Cumple |
| R-03 | Canal alternativo para quien no tenga acceso digital | Ver [ACCESO_ALTERNATIVO_RECIBOS.md](ACCESO_ALTERNATIVO_RECIBOS.md) | Procedimiento documentado |
| R-04 | No discriminación por falta de dispositivo propio | Kiosco / RRHH asistido documentado | Procedimiento documentado |

### 2. Firma y conformidad

| # | Requisito | Implementación | Estado |
|---|-----------|----------------|--------|
| R-05 | Registro de conformidad o disconformidad | `confirmar-recepcion`, eventos append-only | Cumple |
| R-06 | Consentimiento informado al firmar | Texto legal CP-10 (web + móvil) | Cumple |
| R-07 | Disconformidad con motivo registrado | Comentario obligatorio (mín. 10 caracteres) | Cumple |
| R-08 | Comprobante para el trabajador | `GET /comprobante` JSON (CP-11) | Cumple |

### 3. Integridad y conservación

| # | Requisito | Implementación | Estado |
|---|-----------|----------------|--------|
| R-09 | PDF inmutable con hash SHA-256 | `hash_pdf`, verificación al firmar | Cumple |
| R-10 | Cadena de custodia probatoria | Eventos + expediente ZIP (CP-03) | Cumple |
| R-11 | Conservación 10 años | Política [RETENCION_RECIBOS.md](RETENCION_RECIBOS.md), backups | Operativo en prod |
| R-12 | Inmutabilidad post-firma | Servicio + trigger PostgreSQL (CP-02) | Cumple |

### 4. Operación RRHH

| # | Requisito | Implementación | Estado |
|---|-----------|----------------|--------|
| R-13 | Carga individual y masiva | `/recibos`, carga masiva ZIP (CP-04) | Cumple |
| R-14 | Republicación con historial | Versionado CP-09 | Cumple |
| R-15 | Seguimiento de pendientes | Alertas CP-08 | Cumple |
| R-16 | Legajo del trabajador | Sección recibos en legajo (CP-06) | Cumple |

### 5. Plazos y canales (validar con asesoría)

| # | Punto a confirmar con legal | Estado sistema |
|---|----------------------------|----------------|
| R-17 | ¿Plazo máximo para que el trabajador firme? | Configurable `dias-alerta-pendiente` (default 7 días alerta RRHH) |
| R-18 | ¿Canal email obligatorio además de in-app? | Email opcional (`app.correo.habilitado`) |
| R-19 | ¿Re-autenticación obligatoria al firmar? | Opcional (`requiere-contrasena-firma`) |
| R-20 | ¿Firma digital certificada (PKI) exigida? | Fuera de alcance — firma electrónica simple |

---

## Acciones previas al go-live

1. [ ] Revisión del texto del Decreto 407/2026 y modificatorias vigentes a la fecha de despliegue.
2. [ ] Aprobación del texto legal de firma por asesoría laboral.
3. [ ] Aprobación del [procedimiento de acceso alternativo](ACCESO_ALTERNATIVO_RECIBOS.md).
4. [ ] Configurar SMTP en producción si el reglamento o la política interna exigen email.
5. [ ] Firmar acta de conformidad del responsable de RRHH (plantilla abajo).
6. [ ] Capacitar a RRHH en republicación, expediente y canal alternativo.

---

## Plantilla — Acta de conformidad RRHH

```
Empresa: _________________________
Fecha: ___________________________

Declaro que la empresa ha implementado el procedimiento de entrega digital
de recibos de sueldo conforme Ley 27.802, incluyendo:
- Acceso digital para trabajadores con cuenta en el sistema
- Procedimiento de acceso alternativo para trabajadores sin acceso digital
- Conservación y backups según política de retención de 10 años

Responsable RRHH: _________________________  Firma: _________________________
Asesoría legal (opcional): _________________  Firma: _________________________
```

---

## Referencias

- [ACCESO_ALTERNATIVO_RECIBOS.md](ACCESO_ALTERNATIVO_RECIBOS.md)
- [RETENCION_RECIBOS.md](RETENCION_RECIBOS.md)
- [RUNBOOK_RESTAURACION_ALMACENAMIENTO.md](RUNBOOK_RESTAURACION_ALMACENAMIENTO.md)
