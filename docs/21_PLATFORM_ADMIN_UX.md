# CONSOLA DE PLATAFORMA (SuperAdmin)

Version: 1.1

## Objetivo

Definir la experiencia del **SuperAdmin** de plataforma, separada de la operación RRHH diaria de cada empresa.

---

## Principio

El SuperAdmin **administra la plataforma** (tenants). No gestiona solicitudes, vacaciones ni documentos en operación normal.

La operación diaria corresponde a:

- Administrador Empresa
- RRHH
- Supervisor
- Empleado

---

## Consola de plataforma

**Ruta base:** `/plataforma`

**Menú:**

- Inicio (métricas de empresas)
- Empresas

**Rutas de empresas:**

| Ruta | Descripción |
|------|-------------|
| `/plataforma/empresas` | Listado y alta de empresas |
| `/plataforma/empresas/:id` | Ficha: editar, desactivar, accesos |
| `/plataforma/empresas/:id/usuarios` | Usuarios del tenant |

**Acciones por empresa:**

| Acción | Descripción |
|--------|-------------|
| Abrir ficha | Ver y editar datos del tenant |
| Usuarios | Crear y gestionar cuentas del tenant (primer administrador) |
| Gestionar empleados | Activa modo soporte y abre `/empleados` en la consola RRHH |
| Desactivar | Soft-delete: estado `INACTIVA` y deja de listarse |

---

## Modo soporte

Uso exclusivo para diagnóstico o soporte técnico.

- Se activa explícitamente desde el listado o la ficha de una empresa (acción **Gestionar empleados**)
- Muestra banner visible en la consola operativa
- Habilita permisos operativos de tenant en backend (`X-Modo-Soporte: true`)
- Debe registrarse en auditoría (futuro)

Al salir del modo soporte, el SuperAdmin vuelve a `/plataforma/empresas`.

---

## Permisos de plataforma

Solo en operación normal (sin modo soporte):

- `empresa.crear`, `empresa.ver`, `empresa.editar`, `empresa.desactivar`
- `usuario.crear`, `usuario.ver`
- `rol.ver`, `permiso.ver`

---

## Flujo de alta de empresa

1. SuperAdmin crea empresa
2. Se abre la ficha de la empresa creada
3. SuperAdmin asigna primer usuario Administrador desde **Usuarios**
4. El administrador de la empresa opera RRHH con su propia cuenta
5. Si hace falta soporte operativo, SuperAdmin usa **Gestionar empleados** (modo soporte)

---

## Relación con otros documentos

- `04_PERMISSION_MATRIX.md` — matriz de roles
- `20_WEB_UX_SPECIFICATION.md` — consola operativa de empresa (RRHH)
- `DEMO_GUIA.md` — credenciales de demostración
