# POLICY ENGINE

Version: 1.0

## Objetivo

Permitir definir reglas de negocio sin modificar código.

---

# Concepto

Una política es:

SI ocurre X
ENTONCES hacer Y

---

# Estructura

Policy

- Id
- CompanyId
- Name
- Description
- IsActive

PolicyRule

- Id
- PolicyId
- Condition
- Action

---

# Ejemplos

Vacaciones

SI antigüedad >= 5 años
ENTONCES 21 días

---

Home Office

SI área = Sistemas
ENTONCES máximo 3 días semanales

---

Licencias

SI puesto = Gerente
ENTONCES agregar aprobación adicional

---

# Condiciones

- Antigüedad
- Área
- Puesto
- Sucursal
- Tipo de empleado
- Rol

---

# Acciones

- Aprobar automáticamente
- Requerir aprobación
- Asignar beneficio
- Limitar cantidad

---

# Futuro

Preparar motor visual de reglas.