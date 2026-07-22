-- Flujos de trabajo y solicitudes

CREATE TABLE flujo_trabajo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE paso_flujo_trabajo (
    id              BIGSERIAL PRIMARY KEY,
    flujo_trabajo_id BIGINT NOT NULL REFERENCES flujo_trabajo(id),
    orden           INT NOT NULL,
    nombre          VARCHAR(255) NOT NULL,
    tipo_paso       VARCHAR(50) NOT NULL,
    rol_id          BIGINT REFERENCES rol(id),
    usuario_id      BIGINT REFERENCES usuario(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    UNIQUE (flujo_trabajo_id, orden)
);

CREATE TABLE tipo_solicitud (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    nombre              VARCHAR(255) NOT NULL,
    descripcion         VARCHAR(500),
    flujo_trabajo_id    BIGINT REFERENCES flujo_trabajo(id),
    requiere_aprobacion BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_adjunto    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE TABLE solicitud (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    tipo_solicitud_id   BIGINT NOT NULL REFERENCES tipo_solicitud(id),
    estado              VARCHAR(50) NOT NULL DEFAULT 'BORRADOR',
    motivo              VARCHAR(1000),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE TABLE instancia_flujo_trabajo (
    id                      BIGSERIAL PRIMARY KEY,
    empresa_id              BIGINT NOT NULL REFERENCES empresa(id),
    solicitud_id            BIGINT NOT NULL UNIQUE REFERENCES solicitud(id),
    flujo_trabajo_id        BIGINT NOT NULL REFERENCES flujo_trabajo(id),
    paso_actual_orden       INT NOT NULL DEFAULT 1,
    aprobador_actual_id     BIGINT REFERENCES usuario(id),
    estado                  VARCHAR(50) NOT NULL DEFAULT 'EN_PROGRESO',
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE accion_paso_flujo (
    id                          BIGSERIAL PRIMARY KEY,
    instancia_flujo_trabajo_id  BIGINT NOT NULL REFERENCES instancia_flujo_trabajo(id),
    paso_flujo_trabajo_id       BIGINT NOT NULL REFERENCES paso_flujo_trabajo(id),
    usuario_id                  BIGINT NOT NULL REFERENCES usuario(id),
    accion                      VARCHAR(50) NOT NULL,
    comentarios                 VARCHAR(1000),
    fecha_accion                TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE aprobacion_solicitud (
    id                      BIGSERIAL PRIMARY KEY,
    solicitud_id            BIGINT NOT NULL REFERENCES solicitud(id),
    paso_flujo_trabajo_id    BIGINT REFERENCES paso_flujo_trabajo(id),
    aprobador_id            BIGINT NOT NULL REFERENCES usuario(id),
    estado                  VARCHAR(50) NOT NULL,
    comentarios             VARCHAR(1000),
    fecha_aprobacion        TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_solicitud_empresa ON solicitud(empresa_id);
CREATE INDEX idx_solicitud_empleado ON solicitud(empleado_id);
CREATE INDEX idx_instancia_aprobador ON instancia_flujo_trabajo(aprobador_actual_id);
