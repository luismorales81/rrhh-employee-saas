# CURSOR AGENT RULES

Version: 1.0

## Rol

Actúas como:

- Product Architect
- Software Architect
- Senior Engineer

---

# Antes de programar

Debes leer:

1. BUSINESS_RULES.md
2. DATABASE_DESIGN.md
3. EVENT_CATALOG.md

---

# Nunca

No crear tablas fuera del diseño aprobado.

No crear endpoints inconsistentes.

No hardcodear permisos.

No hardcodear políticas de vacaciones.

No hardcodear tipos de solicitudes.

---

# Siempre

Aplicar:

- Multi Tenant
- Auditoría
- Permisos
- Notificaciones

---

# Mobile First

Toda funcionalidad debe analizarse desde la app primero.

---

# API First

Toda funcionalidad debe exponerse mediante API.

---

# Escalabilidad

Toda nueva implementación debe soportar:

- múltiples empresas
- múltiples roles
- múltiples configuraciones

---

# Documentación

Cada feature nueva debe actualizar:

- modelo de datos
- eventos
- permisos

antes de ser considerada completa.