-- Onboarding: plantillas y tareas por empleado

INSERT INTO permiso (codigo, descripcion)
SELECT 'onboarding.gestionar', 'Gestionar plantillas de onboarding'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'onboarding.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'onboarding.ver', 'Ver tareas de onboarding'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'onboarding.ver');

CREATE TABLE plantilla_tarea_onboarding (
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

CREATE TABLE tarea_onboarding_empleado (
    id                  BIGSERIAL PRIMARY KEY,
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    plantilla_id        BIGINT NOT NULL REFERENCES plantilla_tarea_onboarding(id),
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

CREATE INDEX idx_plantilla_onboarding_empresa ON plantilla_tarea_onboarding(empresa_id);
CREATE INDEX idx_tarea_onboarding_empleado ON tarea_onboarding_empleado(empleado_id);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('onboarding.gestionar', 'onboarding.ver')
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'onboarding.ver'
WHERE r.nombre = 'Empleado' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
