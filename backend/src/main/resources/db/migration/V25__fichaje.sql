-- Módulo de fichaje móvil con zonas geográficas

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.zona.ver', 'Ver zonas de fichaje'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.zona.ver');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.zona.gestionar', 'Gestionar zonas de fichaje'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.zona.gestionar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.configurar', 'Configurar fichaje de empleados'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.configurar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.ver', 'Ver marcas de fichaje'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.ver');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.revisar', 'Revisar fichajes pendientes'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.revisar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.registrar', 'Registrar fichaje desde móvil'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.registrar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'fichaje.mis.ver', 'Ver propias marcas de fichaje'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'fichaje.mis.ver');

CREATE TABLE politica_fichaje_empresa (
    id                      BIGSERIAL PRIMARY KEY,
    empresa_id              BIGINT NOT NULL UNIQUE REFERENCES empresa(id),
    texto_transparencia     TEXT NOT NULL,
    version                 INTEGER NOT NULL DEFAULT 1,
    permitir_fichaje_sin_gps BOOLEAN NOT NULL DEFAULT FALSE,
    precision_maxima_metros INTEGER NOT NULL DEFAULT 100,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE TABLE zona_fichaje (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL REFERENCES empresa(id),
    nombre          VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(1000),
    coordenadas     TEXT NOT NULL,
    alcance         VARCHAR(50) NOT NULL DEFAULT 'GLOBAL',
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE asignacion_zona_fichaje (
    id              BIGSERIAL PRIMARY KEY,
    zona_fichaje_id BIGINT NOT NULL REFERENCES zona_fichaje(id),
    empleado_id     BIGINT REFERENCES empleado(id),
    gerencia_id     BIGINT REFERENCES gerencia(id),
    area_id         BIGINT REFERENCES area(id),
    sector_id       BIGINT REFERENCES sector(id),
    tipo            VARCHAR(50) NOT NULL DEFAULT 'INCLUYE',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE TABLE configuracion_fichaje_empleado (
    id                              BIGSERIAL PRIMARY KEY,
    empleado_id                     BIGINT NOT NULL UNIQUE REFERENCES empleado(id),
    fichaje_movil_habilitado        BOOLEAN NOT NULL DEFAULT FALSE,
    restringir_solo_zonas_asignadas BOOLEAN NOT NULL DEFAULT FALSE,
    consentimiento_aceptado_at      TIMESTAMP,
    version_politica_aceptada       INTEGER,
    dispositivo_registrado_id       VARCHAR(255),
    created_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by                      BIGINT,
    updated_by                      BIGINT,
    is_deleted                      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at                      TIMESTAMP
);

CREATE TABLE marca_fichaje (
    id                      BIGSERIAL PRIMARY KEY,
    empresa_id              BIGINT NOT NULL REFERENCES empresa(id),
    empleado_id             BIGINT NOT NULL REFERENCES empleado(id),
    tipo                    VARCHAR(50) NOT NULL,
    fecha_hora_servidor     TIMESTAMP NOT NULL,
    fecha_hora_dispositivo  TIMESTAMP,
    latitud                 DOUBLE PRECISION,
    longitud                DOUBLE PRECISION,
    precision_metros        DOUBLE PRECISION,
    id_dispositivo          VARCHAR(255),
    modelo_dispositivo      VARCHAR(255),
    sistema_operativo       VARCHAR(255),
    version_app             VARCHAR(50),
    direccion_ip            VARCHAR(100),
    user_agent              VARCHAR(500),
    zona_fichaje_id         BIGINT REFERENCES zona_fichaje(id),
    estado                  VARCHAR(50) NOT NULL,
    motivo_estado           VARCHAR(100),
    revisado_por_usuario_id BIGINT REFERENCES usuario(id),
    revisado_at             TIMESTAMP,
    nota_revision           VARCHAR(1000),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE INDEX idx_zona_fichaje_empresa ON zona_fichaje(empresa_id);
CREATE INDEX idx_marca_fichaje_empresa_fecha ON marca_fichaje(empresa_id, fecha_hora_servidor);
CREATE INDEX idx_marca_fichaje_empleado_fecha ON marca_fichaje(empleado_id, fecha_hora_servidor);
CREATE INDEX idx_marca_fichaje_estado ON marca_fichaje(estado);
CREATE INDEX idx_asignacion_zona_fichaje ON asignacion_zona_fichaje(zona_fichaje_id);

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'fichaje.zona.ver', 'fichaje.zona.gestionar', 'fichaje.configurar',
    'fichaje.ver', 'fichaje.revisar'
)
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo = 'fichaje.ver'
WHERE r.nombre = 'Supervisor' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('fichaje.registrar', 'fichaje.mis.ver')
WHERE r.nombre = 'Empleado' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
