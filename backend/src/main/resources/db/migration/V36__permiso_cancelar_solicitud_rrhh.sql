-- RRHH y Administrador pueden cancelar solicitudes (p. ej. pedidos de vacaciones incompletos)

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'solicitud.cancelar' AND p.is_deleted = FALSE
WHERE r.nombre IN ('Administrador', 'RRHH')
  AND r.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp
    WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
