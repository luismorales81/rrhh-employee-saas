-- Ausentismo: tipos y registros

CREATE TABLE tipo_ausencia (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    requiere_certificado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    UNIQUE (empresa_id, nombre)
);

CREATE TABLE ausencia (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    tipo_ausencia_id    BIGINT NOT NULL REFERENCES tipo_ausencia(id),
    fecha               DATE NOT NULL,
    motivo              VARCHAR(1000),
    justificada         BOOLEAN NOT NULL DEFAULT FALSE,
    url_certificado     VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_ausencia_empresa ON ausencia(empresa_id);
CREATE INDEX idx_ausencia_empleado ON ausencia(empleado_id);
CREATE INDEX idx_ausencia_fecha ON ausencia(fecha);

INSERT INTO permiso (codigo, descripcion) VALUES
    ('ausencia.tipo.gestionar', 'Gestionar tipos de ausencia'),
    ('ausencia.tipo.ver', 'Ver tipos de ausencia'),
    ('ausencia.registrar', 'Registrar ausencias'),
    ('ausencia.ver', 'Ver ausencias'),
    ('ausencia.actualizar', 'Actualizar ausencias');
