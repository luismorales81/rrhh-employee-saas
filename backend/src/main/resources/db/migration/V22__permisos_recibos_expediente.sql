-- CP-03: permiso para exportar expediente probatorio

INSERT INTO permiso (codigo, descripcion)
SELECT 'recibo.exportar_expediente', 'Exportar expediente probatorio de recibo'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'recibo.exportar_expediente');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'recibo.exportar_expediente'
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
