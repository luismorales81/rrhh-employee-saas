-- Documentos: categorías, catálogo, asignaciones y firmas

CREATE TABLE categoria_documento (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    UNIQUE (empresa_id, nombre)
);

CREATE TABLE documento (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    categoria_id        BIGINT NOT NULL REFERENCES categoria_documento(id),
    titulo              VARCHAR(255) NOT NULL,
    descripcion         VARCHAR(1000),
    version             VARCHAR(50) NOT NULL DEFAULT '1.0',
    url_archivo         VARCHAR(500) NOT NULL,
    hash_archivo        VARCHAR(128),
    requiere_firma      BOOLEAN NOT NULL DEFAULT FALSE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP
);

CREATE TABLE asignacion_documento (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    documento_id        BIGINT NOT NULL REFERENCES documento(id),
    estado              VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_asignacion    TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_lectura       TIMESTAMP,
    fecha_vencimiento   DATE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    UNIQUE (empleado_id, documento_id)
);

CREATE TABLE firma_documento (
    id                      BIGSERIAL PRIMARY KEY,
    asignacion_id           BIGINT NOT NULL REFERENCES asignacion_documento(id),
    empleado_id             BIGINT NOT NULL REFERENCES empleado(id),
    fecha_firma             TIMESTAMP NOT NULL DEFAULT NOW(),
    hash_firma              VARCHAR(128) NOT NULL,
    url_archivo_firmado     VARCHAR(500),
    direccion_ip            VARCHAR(45),
    dispositivo             VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE INDEX idx_documento_empresa ON documento(empresa_id);
CREATE INDEX idx_asignacion_documento_empresa ON asignacion_documento(empresa_id);
CREATE INDEX idx_asignacion_documento_empleado ON asignacion_documento(empleado_id);
CREATE INDEX idx_firma_documento_asignacion ON firma_documento(asignacion_id);

INSERT INTO permiso (codigo, descripcion) VALUES
    ('documento.categoria.gestionar', 'Gestionar categorías de documentos'),
    ('documento.categoria.ver', 'Ver categorías de documentos'),
    ('documento.gestionar', 'Gestionar documentos del catálogo'),
    ('documento.ver', 'Ver catálogo de documentos'),
    ('documento.asignar', 'Asignar documentos a empleados'),
    ('documento.asignacion.ver', 'Ver asignaciones de documentos'),
    ('documento.mis.ver', 'Ver documentos asignados al empleado'),
    ('documento.confirmar_lectura', 'Confirmar lectura de documento'),
    ('documento.firmar', 'Firmar documento asignado');
