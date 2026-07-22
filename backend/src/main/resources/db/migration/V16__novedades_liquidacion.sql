-- Novedades para liquidación

INSERT INTO permiso (codigo, descripcion)
SELECT 'novedad.gestionar', 'Gestionar novedades de liquidación'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'novedad.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'novedad.ver', 'Ver novedades de liquidación'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'novedad.ver');

CREATE TABLE novedad_liquidacion (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id     BIGINT NOT NULL REFERENCES empleado(id),
    periodo         VARCHAR(7) NOT NULL,
    tipo            VARCHAR(50) NOT NULL,
    descripcion     VARCHAR(500),
    cantidad        DECIMAL(12, 2),
    monto           DECIMAL(12, 2),
    fecha           DATE NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_novedad_liquidacion_empresa ON novedad_liquidacion(empresa_id);
CREATE INDEX idx_novedad_liquidacion_empleado ON novedad_liquidacion(empleado_id);
CREATE INDEX idx_novedad_liquidacion_periodo ON novedad_liquidacion(periodo);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('novedad.gestionar', 'novedad.ver')
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
