# MATRIZ DE PERMISOS

| Acción | SuperAdmin | Admin | RRHH | Supervisor | Empleado |
|----------|----------|----------|----------|----------|----------|
| Gestion Empresas (plataforma) | SI | NO | NO | NO | NO |
| Gestion Usuarios tenant (provisionamiento) | SI | SI | NO | NO | NO |
| Gestion Empleados | NO* | SI | SI | NO | NO |
| Ver Equipo | NO* | SI | SI | SI | NO |
| Aprobar Solicitudes | NO* | SI | SI | SI | NO |
| Ver Propios Datos | NO* | SI | SI | SI | SI |
| Firmar Documentos (firma digital app) | NO* | SI | SI | SI | SI |
| Confirmar lectura comunicados | NO* | SI | SI | SI | SI |

## Recibos (permisos API)

| Permiso | Admin | RRHH | Empleado |
|---------|-------|------|----------|
| `recibo.cargar` / carga masiva | SI | SI | NO |
| `recibo.ver` | SI | SI | NO |
| `recibo.exportar_expediente` | SI | SI | NO |
| `recibo.mis.ver` / `recibo.firmar` | SI | SI | SI |

\* El SuperAdmin solo accede a operación de empresa en **modo soporte** (excepcional). Ver `21_PLATFORM_ADMIN_UX.md`.
