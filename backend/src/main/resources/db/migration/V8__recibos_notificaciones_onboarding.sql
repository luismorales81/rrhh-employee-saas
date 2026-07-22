-- Recibos, notificaciones y onboarding documental

INSERT INTO permiso (codigo, descripcion) VALUES
    ('recibo.cargar', 'Cargar recibos de sueldo'),
    ('recibo.ver', 'Ver recibos de la empresa'),
    ('recibo.mis.ver', 'Ver mis recibos'),
    ('recibo.firmar', 'Firmar conformidad de recibo'),
    ('notificacion.ver', 'Ver notificaciones');

ALTER TABLE legajo_empleado ADD COLUMN estado_onboarding VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE legajo_empleado ADD COLUMN fecha_completado_onboarding TIMESTAMP;

CREATE TABLE recibo (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id     BIGINT NOT NULL REFERENCES empleado(id),
    periodo         VARCHAR(20) NOT NULL,
    titulo          VARCHAR(255) NOT NULL,
    url_archivo     VARCHAR(500),
    estado          VARCHAR(50) NOT NULL DEFAULT 'PUBLICADO',
    fecha_publicacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_firma     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE notificacion (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT REFERENCES empresa(id),
    usuario_id      BIGINT NOT NULL REFERENCES usuario(id),
    titulo          VARCHAR(255) NOT NULL,
    mensaje         VARCHAR(1000) NOT NULL,
    tipo            VARCHAR(50) NOT NULL,
    referencia_id   BIGINT,
    leida           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_recibo_empresa ON recibo(empresa_id);
CREATE INDEX idx_recibo_empleado ON recibo(empleado_id);
CREATE INDEX idx_notificacion_usuario ON notificacion(usuario_id);
