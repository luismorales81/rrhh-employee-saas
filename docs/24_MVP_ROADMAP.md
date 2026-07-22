# MVP ROADMAP

Version: 1.0

---

# MVP

Objetivo:

Tener un producto vendible en menos de 90 días.

---

## Módulos

### Core

- Empresas
- Usuarios
- Roles
- Permisos

---

### Empleados

- Legajo digital
- Datos laborales
- Datos personales

---

### Solicitudes

- Motor configurable

---

### Vacaciones

- Políticas configurables
- Saldo
- Solicitudes

---

### Ausentismo

- Registro
- Justificación

---

### Documentos

- Asignación
- Lectura
- Firma

---

### Comunicaciones

- Publicación
- Confirmación de lectura

---

### App

- Perfil
- Solicitudes
- Vacaciones
- Documentos
- Comunicaciones

---

# V1

Implementado (migraciones V13–V18):

- **Onboarding** — checklist por empleado, portal `/mi-panel` y legajo RRHH
- **Offboarding** — checklist al desactivar empleado (`DESVINCULADO`), tareas en legajo
- **Activos** — inventario, asignación/devolución, página `/activos`
- **Novedades para liquidación** — CRUD, confirmación por período, export CSV (`/novedades`)
- **Recibos — firma electrónica simple** — publicación con PDF + hash, visualizar, confirmar/disconformidad, `evento_firma_recibo` (Ley 27.802 / 25.506)

Guía demo: [DEMO_GUIA.md](DEMO_GUIA.md).  
Diseño recibos: [RECIBOS_FIRMA_ELECTRONICA.md](RECIBOS_FIRMA_ELECTRONICA.md).

---

# V1.1 — Cumplimiento legal recibos (implementado)

Pasos CP-01 a CP-05 cerrados en código. Detalle: [RECIBOS_CUMPLIMIENTO_PENDIENTE.md](RECIBOS_CUMPLIMIENTO_PENDIENTE.md).

| Paso | Entregable |
|------|------------|
| CP-05 | `archivoId` obligatorio; flag `requiere_republicacion` para legacy |
| CP-02 | Guardas en `ServicioRecibo` / `ServicioAlmacenamiento`; trigger en `scripts/postgresql/` |
| CP-03 | `GET /api/recibos/{id}/expediente` + botón en `/recibos` |
| CP-04 | Carga masiva ZIP + asistente UI |
| CP-01 | [RETENCION_RECIBOS.md](RETENCION_RECIBOS.md), job diario de integridad |

**Ya implementado antes:** CP-12 almacenamiento objetos — [ALMACENAMIENTO_OBJETOS.md](ALMACENAMIENTO_OBJETOS.md).

---

# V2

- IA
- Capacitación
- Encuestas

---

# V3

- Evaluaciones
- Plan de carrera
- Analytics