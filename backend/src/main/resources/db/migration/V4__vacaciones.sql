-- Vacaciones: políticas, saldos y solicitudes

CREATE TABLE politica_vacaciones (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    activa          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE rango_politica_vacaciones (
    id              BIGSERIAL PRIMARY KEY,
    politica_id     BIGINT NOT NULL REFERENCES politica_vacaciones(id),
    anios_minimos   INT NOT NULL,
    anios_maximos   INT,
    dias_otorgados  INT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE saldo_vacaciones (
    id                  BIGSERIAL PRIMARY KEY,
    empleado_id         BIGINT NOT NULL UNIQUE REFERENCES empleado(id),
    dias_disponibles    DECIMAL(10, 2) NOT NULL DEFAULT 0,
    dias_pendientes     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    dias_usados         DECIMAL(10, 2) NOT NULL DEFAULT 0,
    politica_id         BIGINT REFERENCES politica_vacaciones(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE TABLE solicitud_vacaciones (
    id                  BIGSERIAL PRIMARY KEY,
    solicitud_id        BIGINT NOT NULL UNIQUE REFERENCES solicitud(id),
    fecha_inicio        DATE NOT NULL,
    fecha_fin           DATE NOT NULL,
    dias_solicitados    DECIMAL(10, 2) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_politica_vacaciones_empresa ON politica_vacaciones(empresa_id);
CREATE INDEX idx_saldo_vacaciones_empleado ON saldo_vacaciones(empleado_id);

-- Permisos de vacaciones
INSERT INTO permiso (codigo, descripcion) VALUES
    ('vacacion.politica.gestionar', 'Gestionar políticas de vacaciones'),
    ('vacacion.politica.ver', 'Ver políticas de vacaciones'),
    ('vacacion.solicitar', 'Solicitar vacaciones'),
    ('vacacion.ver', 'Ver solicitudes de vacaciones'),
    ('vacacion.saldo.ver', 'Ver saldo de vacaciones'),
    ('vacacion.saldo.gestionar', 'Gestionar saldos de vacaciones');
