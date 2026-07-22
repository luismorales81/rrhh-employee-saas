-- Vacaciones LCT: patrón descanso, configuración, saldo anual, auditoría append-only

-- Permisos
INSERT INTO permiso (codigo, descripcion)
SELECT 'vacacion.configurar', 'Configurar reglas de vacaciones de la empresa'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'vacacion.configurar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'vacacion.patron.gestionar', 'Gestionar patrón de descanso semanal de empleados'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'vacacion.patron.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'vacacion.aprobar', 'Aprobar, rechazar o contraproponer solicitudes de vacaciones'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'vacacion.aprobar');

-- Configuración por empresa (piso legal fracción >= 7 en servicio)
CREATE TABLE configuracion_vacaciones (
    id                          BIGSERIAL PRIMARY KEY,
    empresa_id                  BIGINT NOT NULL UNIQUE REFERENCES empresa(id),
    dias_minimos_fraccion       INT NOT NULL DEFAULT 7,
    anticipacion_minima_dias    INT NOT NULL DEFAULT 45,
    permite_solicitud_sin_saldo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by                  BIGINT,
    updated_by                  BIGINT,
    is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP
);

-- Patrón de descanso semanal (puede haber varios por empleado con vigencia)
CREATE TABLE patron_descanso (
    id              BIGSERIAL PRIMARY KEY,
    empleado_id     BIGINT NOT NULL REFERENCES empleado(id),
    vigencia_desde  DATE NOT NULL,
    vigencia_hasta  DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_patron_descanso_empleado ON patron_descanso(empleado_id);
CREATE INDEX idx_patron_descanso_vigencia ON patron_descanso(empleado_id, vigencia_desde);

CREATE TABLE patron_descanso_dia (
    id              BIGSERIAL PRIMARY KEY,
    patron_id       BIGINT NOT NULL REFERENCES patron_descanso(id) ON DELETE CASCADE,
    dia_semana      VARCHAR(20) NOT NULL
);

CREATE INDEX idx_patron_descanso_dia_patron ON patron_descanso_dia(patron_id);

-- Recrear saldo_vacaciones con soporte anual (migración de datos existentes)
CREATE TABLE saldo_vacaciones_nuevo (
    id                  BIGSERIAL PRIMARY KEY,
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    anio                INT NOT NULL,
    antiguedad_anios    INT NOT NULL DEFAULT 0,
    dias_totales        DECIMAL(10, 2) NOT NULL DEFAULT 0,
    dias_tomados        DECIMAL(10, 2) NOT NULL DEFAULT 0,
    dias_pendientes     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    dias_disponibles    DECIMAL(10, 2) NOT NULL DEFAULT 0,
    ajuste_manual       DECIMAL(10, 2) NOT NULL DEFAULT 0,
    ajuste_manual_motivo VARCHAR(500),
    politica_id         BIGINT REFERENCES politica_vacaciones(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    CONSTRAINT uq_saldo_vacaciones_empleado_anio UNIQUE (empleado_id, anio)
);

INSERT INTO saldo_vacaciones_nuevo (
    empleado_id, anio, antiguedad_anios, dias_totales, dias_tomados,
    dias_pendientes, dias_disponibles, ajuste_manual, politica_id,
    created_at, updated_at, created_by, updated_by, is_deleted, deleted_at
)
SELECT
    empleado_id,
    CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS INTEGER),
    0,
    dias_disponibles + dias_pendientes + dias_usados,
    dias_usados,
    dias_pendientes,
    dias_disponibles,
    0,
    politica_id,
    created_at,
    updated_at,
    created_by,
    updated_by,
    is_deleted,
    deleted_at
FROM saldo_vacaciones;

DROP TABLE saldo_vacaciones;
ALTER TABLE saldo_vacaciones_nuevo RENAME TO saldo_vacaciones;
CREATE INDEX idx_saldo_vacaciones_empleado ON saldo_vacaciones(empleado_id);
CREATE INDEX idx_saldo_vacaciones_anio ON saldo_vacaciones(empleado_id, anio);

-- Ampliar solicitud_vacaciones
ALTER TABLE solicitud_vacaciones ADD COLUMN saldo_vacaciones_id BIGINT REFERENCES saldo_vacaciones(id);
ALTER TABLE solicitud_vacaciones ADD COLUMN fecha_inicio_contrapropuesta DATE;
ALTER TABLE solicitud_vacaciones ADD COLUMN fecha_fin_contrapropuesta DATE;
ALTER TABLE solicitud_vacaciones ADD COLUMN motivo_contrapropuesta VARCHAR(2000);
ALTER TABLE solicitud_vacaciones ADD COLUMN fecha_inicio_aprobada DATE;
ALTER TABLE solicitud_vacaciones ADD COLUMN fecha_fin_aprobada DATE;
ALTER TABLE solicitud_vacaciones ADD COLUMN motivo_rechazo VARCHAR(2000);

-- Eventos append-only de solicitudes de vacaciones
CREATE TABLE evento_solicitud_vacaciones (
    id                      BIGSERIAL PRIMARY KEY,
    solicitud_vacaciones_id BIGINT NOT NULL REFERENCES solicitud_vacaciones(id),
    solicitud_id            BIGINT NOT NULL REFERENCES solicitud(id),
    empresa_id              BIGINT NOT NULL REFERENCES empresa(id),
    usuario_id              BIGINT NOT NULL REFERENCES usuario(id),
    tipo_evento             VARCHAR(50) NOT NULL,
    estado_anterior         VARCHAR(50),
    estado_nuevo            VARCHAR(50),
    timestamp_evento        TIMESTAMP NOT NULL DEFAULT NOW(),
    direccion_ip            VARCHAR(100),
    user_agent              VARCHAR(500),
    comentario              VARCHAR(2000)
);

CREATE INDEX idx_evento_sol_vac_sol_vac ON evento_solicitud_vacaciones(solicitud_vacaciones_id);
CREATE INDEX idx_evento_sol_vac_empresa ON evento_solicitud_vacaciones(empresa_id);
