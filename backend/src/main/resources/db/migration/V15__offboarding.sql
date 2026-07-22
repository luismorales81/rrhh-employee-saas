-- Offboarding: plantillas y tareas por empleado

INSERT INTO permiso (codigo, descripcion)
SELECT 'offboarding.gestionar', 'Gestionar plantillas de offboarding'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'offboarding.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'offboarding.ver', 'Ver tareas de offboarding'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'offboarding.ver');

ALTER TABLE legajo_empleado ADD COLUMN estado_offboarding VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE legajo_empleado ADD COLUMN fecha_completado_offboarding TIMESTAMP;

CREATE TABLE plantilla_tarea_offboarding (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    orden           INT NOT NULL DEFAULT 0,
    obligatoria     BOOLEAN NOT NULL DEFAULT TRUE,
    tipo            VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE tarea_offboarding_empleado (
    id                  BIGSERIAL PRIMARY KEY,
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    plantilla_id        BIGINT NOT NULL REFERENCES plantilla_tarea_offboarding(id),
    completada          BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_completado    TIMESTAMP,
    notas               VARCHAR(1000),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_plantilla_offboarding_empresa ON plantilla_tarea_offboarding(empresa_id);
CREATE INDEX idx_tarea_offboarding_empleado ON tarea_offboarding_empleado(empleado_id);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('offboarding.gestionar', 'offboarding.ver')
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
