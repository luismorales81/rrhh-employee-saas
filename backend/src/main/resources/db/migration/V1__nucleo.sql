-- Núcleo: empresas, usuarios, roles, permisos, auditoría

CREATE TABLE empresa (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL,
    razon_social    VARCHAR(255),
    cuit            VARCHAR(20),
    email           VARCHAR(255),
    telefono        VARCHAR(50),
    estado          VARCHAR(50) NOT NULL DEFAULT 'ACTIVA',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE permiso (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE rol (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT REFERENCES empresa(id),
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE rol_permiso (
    rol_id          BIGINT NOT NULL REFERENCES rol(id),
    permiso_id      BIGINT NOT NULL REFERENCES permiso(id),
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT REFERENCES empresa(id),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    es_super_admin  BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_login    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE usuario_rol (
    usuario_id      BIGINT NOT NULL REFERENCES usuario(id),
    rol_id          BIGINT NOT NULL REFERENCES rol(id),
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE registro_auditoria (
    id                  BIGSERIAL PRIMARY KEY,
    empresa_id          BIGINT,
    usuario_id          BIGINT,
    accion              VARCHAR(100) NOT NULL,
    nombre_entidad      VARCHAR(100) NOT NULL,
    entidad_id          BIGINT,
    valores_anteriores  TEXT,
    valores_nuevos      TEXT,
    ip_address          VARCHAR(45),
    user_agent          VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE token_refresco (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES usuario(id),
    token           VARCHAR(500) NOT NULL UNIQUE,
    expira_en       TIMESTAMP NOT NULL,
    revocado        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuario_empresa ON usuario(empresa_id);
CREATE INDEX idx_rol_empresa ON rol(empresa_id);
CREATE INDEX idx_auditoria_empresa ON registro_auditoria(empresa_id);

-- Permisos base del sistema
INSERT INTO permiso (codigo, descripcion) VALUES
    ('empresa.crear', 'Crear empresas (SuperAdmin)'),
    ('empresa.ver', 'Ver empresas'),
    ('usuario.crear', 'Crear usuarios'),
    ('usuario.ver', 'Ver usuarios'),
    ('rol.crear', 'Crear roles'),
    ('rol.ver', 'Ver roles'),
    ('permiso.ver', 'Ver permisos'),
    ('empleado.crear', 'Crear empleados'),
    ('empleado.actualizar', 'Actualizar empleados'),
    ('empleado.ver', 'Ver empleados'),
    ('organizacion.gestionar', 'Gestionar estructura organizacional'),
    ('workflow.configurar', 'Configurar flujos de trabajo'),
    ('workflow.ver', 'Ver flujos de trabajo'),
    ('solicitud.crear', 'Crear solicitudes'),
    ('solicitud.aprobar', 'Aprobar solicitudes'),
    ('solicitud.rechazar', 'Rechazar solicitudes'),
    ('solicitud.cancelar', 'Cancelar solicitudes'),
    ('solicitud.ver', 'Ver solicitudes'),
    ('solicitud.solicitar_cambios', 'Solicitar cambios en solicitudes'),
    ('solicitud.escalar', 'Escalar solicitudes'),
    ('tipo_solicitud.gestionar', 'Gestionar tipos de solicitud');
