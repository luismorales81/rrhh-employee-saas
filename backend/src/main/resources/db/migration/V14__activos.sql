-- Activos y asignaciones

INSERT INTO permiso (codigo, descripcion)
SELECT 'activo.gestionar', 'Gestionar activos y asignaciones'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'activo.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'activo.ver', 'Ver activos y asignaciones'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'activo.ver');

CREATE TABLE activo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    tipo            VARCHAR(100) NOT NULL,
    numero_serie    VARCHAR(255),
    estado          VARCHAR(50) NOT NULL DEFAULT 'DISPONIBLE',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE asignacion_activo (
    id              BIGSERIAL PRIMARY KEY,
    activo_id       BIGINT NOT NULL REFERENCES activo(id),
    empleado_id     BIGINT NOT NULL REFERENCES empleado(id),
    fecha_entrega   DATE NOT NULL,
    fecha_devolucion DATE,
    notas           VARCHAR(1000),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_activo_empresa ON activo(empresa_id);
CREATE INDEX idx_asignacion_activo_empleado ON asignacion_activo(empleado_id);
CREATE INDEX idx_asignacion_activo_activo ON asignacion_activo(activo_id);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('activo.gestionar', 'activo.ver')
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'activo.ver'
WHERE r.nombre = 'Empleado' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
