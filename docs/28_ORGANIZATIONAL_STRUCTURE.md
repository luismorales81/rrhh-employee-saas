# ORGANIZATIONAL STRUCTURE

Version: 1.0

## Objetivo

Permitir que cada empresa modele su estructura organizacional.

La estructura organizacional impacta en:

- Solicitudes
- Aprobaciones
- Comunicaciones
- Reportes
- Onboarding
- Permisos

---

# Jerarquía

La plataforma soporta:

Empresa
→ Gerencia
→ Área
→ Sector
→ Puesto

Todos los niveles son opcionales.

---

# Gerencia

Ejemplos:

- Administración
- Comercial
- Operaciones
- Tecnología

Campos:

- Nombre
- Descripción
- Responsable

---

# Área

Ejemplos:

- Recursos Humanos
- Ventas
- Compras
- Finanzas

Campos:

- Nombre
- Gerencia

---

# Sector

Ejemplos:

- Ventas Corporativas
- Ventas Minoristas
- Reclutamiento

Campos:

- Nombre
- Área

---

# Puesto

Ejemplos:

- Analista
- Supervisor
- Coordinador
- Gerente

Campos:

- Nombre
- Nivel
- Descripción

---

# Relaciones

Cada empleado puede tener:

- Gerencia
- Área
- Sector
- Puesto
- Supervisor

---

# Supervisor

Un empleado puede supervisar múltiples empleados.

Modelo:

Employee
→ SupervisorId

---

# Organigrama

El sistema debe generar automáticamente:

- Organigrama visual
- Organigrama exportable

---

# Comunicaciones

Las comunicaciones pueden dirigirse a:

- Empresa completa
- Gerencia
- Área
- Sector
- Puesto

---

# Solicitudes

Los aprobadores pueden definirse por:

- Supervisor directo
- Responsable de área
- RRHH
- Gerencia

---

# Reportes

Permitir filtros por:

- Gerencia
- Área
- Sector
- Puesto

---

# Futuro

Preparar soporte para:

- Empresas multinivel
- Holdings
- Empresas con múltiples sucursales