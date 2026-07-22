-- Comunicaciones: publicación y confirmación de lectura

CREATE TABLE comunicacion (
    id                      BIGSERIAL PRIMARY KEY,
    empresa_id              BIGINT NOT NULL REFERENCES empresa(id),
    titulo                  VARCHAR(255) NOT NULL,
    mensaje                 TEXT NOT NULL,
    tipo_destinatario       VARCHAR(30) NOT NULL,
    referencia_destinatario_id BIGINT,
    requiere_confirmacion   BOOLEAN NOT NULL DEFAULT FALSE,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PUBLICADA',
    fecha_publicacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE TABLE destinatario_comunicacion (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    comunicacion_id     BIGINT NOT NULL REFERENCES comunicacion(id),
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    fecha_lectura       TIMESTAMP,
    fecha_confirmacion  TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    UNIQUE (comunicacion_id, empleado_id)
);

CREATE INDEX idx_comunicacion_empresa ON comunicacion(empresa_id);
CREATE INDEX idx_destinatario_comunicacion_empleado ON destinatario_comunicacion(empleado_id);
CREATE INDEX idx_destinatario_comunicacion_comunicacion ON destinatario_comunicacion(comunicacion_id);

INSERT INTO permiso (codigo, descripcion) VALUES
    ('comunicacion.publicar', 'Publicar comunicaciones'),
    ('comunicacion.ver', 'Ver comunicaciones publicadas'),
    ('comunicacion.destinatarios.ver', 'Ver destinatarios y estado de lectura'),
    ('comunicacion.mis.ver', 'Ver comunicaciones asignadas al empleado'),
    ('comunicacion.confirmar_lectura', 'Confirmar lectura de comunicación');
