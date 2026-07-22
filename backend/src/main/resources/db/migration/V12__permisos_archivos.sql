INSERT INTO permiso (codigo, descripcion)
SELECT 'archivo.subir', 'Subir archivos'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'archivo.subir');

INSERT INTO permiso (codigo, descripcion)
SELECT 'archivo.descargar', 'Descargar archivos'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'archivo.descargar');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('archivo.subir', 'archivo.descargar')
WHERE r.nombre IN ('Administrador', 'RRHH', 'Empleado') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
