-- Estructura organizacional y empleados

CREATE TABLE gerencia (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    responsable_id  BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE area (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    gerencia_id     BIGINT REFERENCES gerencia(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE sector (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    area_id         BIGINT REFERENCES area(id),
    nombre          VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE puesto (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    nivel           VARCHAR(100),
    descripcion     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE empleado (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    usuario_id          BIGINT REFERENCES usuario(id),
    numero_empleado     VARCHAR(50),
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    dni                 VARCHAR(20),
    cuil                VARCHAR(20),
    fecha_nacimiento    DATE,
    fecha_ingreso       DATE,
    fecha_baja          DATE,
    telefono            VARCHAR(50),
    email_personal      VARCHAR(255),
    email_laboral       VARCHAR(255),
    estado              VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    supervisor_id       BIGINT REFERENCES empleado(id),
    gerencia_id         BIGINT REFERENCES gerencia(id),
    area_id             BIGINT REFERENCES area(id),
    sector_id           BIGINT REFERENCES sector(id),
    puesto_id           BIGINT REFERENCES puesto(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE TABLE legajo_empleado (
    id              BIGSERIAL PRIMARY KEY,
    empleado_id     BIGINT NOT NULL UNIQUE REFERENCES empleado(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

ALTER TABLE gerencia ADD CONSTRAINT fk_gerencia_responsable
    FOREIGN KEY (responsable_id) REFERENCES empleado(id);

CREATE INDEX idx_empleado_empresa ON empleado(empresa_id);
CREATE INDEX idx_empleado_supervisor ON empleado(supervisor_id);
