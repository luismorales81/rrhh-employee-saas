-- Asignar permisos LCT de vacaciones a roles existentes (empresas creadas antes de V26)

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'vacacion.configurar', 'vacacion.patron.gestionar', 'vacacion.aprobar'
)
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
