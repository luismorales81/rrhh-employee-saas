-- RRHH puede configurar flujos de trabajo y tipos de solicitud

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'workflow.configurar' AND p.is_deleted = FALSE
WHERE r.nombre = 'RRHH'
  AND r.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp
    WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'tipo_solicitud.gestionar' AND p.is_deleted = FALSE
WHERE r.nombre = 'RRHH'
  AND r.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp
    WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
