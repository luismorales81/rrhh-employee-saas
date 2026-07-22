INSERT INTO permiso (codigo, descripcion)
SELECT 'ausencia.solicitar', 'Reportar ausencia propia'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ausencia.solicitar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'ausencia.mis.ver', 'Ver ausencias propias'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'ausencia.mis.ver');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('ausencia.solicitar', 'ausencia.mis.ver')
WHERE r.nombre = 'Empleado' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
