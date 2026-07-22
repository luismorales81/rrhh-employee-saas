-- Certificados médicos digitales — medicina laboral

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.cargar', 'Cargar certificados médicos'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.cargar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.ver_completo', 'Ver certificado médico completo incluyendo diagnóstico'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.ver_completo');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.ver_resumen', 'Ver resumen de certificado médico sin diagnóstico'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.ver_resumen');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.ver_propio', 'Ver certificados médicos propios'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.ver_propio');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.validar', 'Validar o corregir extracción de certificado médico'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.validar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.rechazar', 'Rechazar certificado médico'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.rechazar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'certificado.configurar', 'Configurar auditoría médica de certificados'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'certificado.configurar');

INSERT INTO permiso (codigo, descripcion)
SELECT 'reporte.ausentismo.ver', 'Ver reportes de ausentismo por certificados'
WHERE NOT EXISTS (SELECT 1 FROM permiso WHERE codigo = 'reporte.ausentismo.ver');

CREATE TABLE configuracion_auditoria_medica (
    id                              BIGSERIAL PRIMARY KEY,
    empresa_id                      BIGINT NOT NULL UNIQUE REFERENCES empresa(id),
    dias_reposo_alerta_leve         INT NOT NULL DEFAULT 5,
    max_licencias_cortas_periodo    INT NOT NULL DEFAULT 3,
    periodo_evaluacion_dias         INT NOT NULL DEFAULT 90,
    requiere_revision_manual_siempre BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by                      BIGINT,
    updated_by                      BIGINT,
    is_deleted                      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at                      TIMESTAMP
);

CREATE TABLE certificado (
    id                      BIGSERIAL PRIMARY KEY,
    empleado_id             BIGINT NOT NULL REFERENCES empleado(id),
    empresa_id              BIGINT NOT NULL REFERENCES empresa(id),
    tipo_examen             VARCHAR(50) NOT NULL,
    archivo_id              BIGINT REFERENCES archivo_almacenado(id),
    archivo_url             VARCHAR(500) NOT NULL,
    archivo_hash_sha256     VARCHAR(64) NOT NULL,
    origen_carga            VARCHAR(50) NOT NULL,
    cargado_por_usuario_id  BIGINT NOT NULL,
    estado                  VARCHAR(50) NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE INDEX idx_certificado_empresa ON certificado(empresa_id);
CREATE INDEX idx_certificado_empleado ON certificado(empleado_id);
CREATE INDEX idx_certificado_estado ON certificado(estado);
CREATE INDEX idx_certificado_empresa_empleado ON certificado(empresa_id, empleado_id);

CREATE TABLE datos_extraidos_certificado (
    id                      BIGSERIAL PRIMARY KEY,
    certificado_id          BIGINT NOT NULL UNIQUE REFERENCES certificado(id),
    diagnostico             VARCHAR(2000),
    reposo_dias             INT,
    medico_nombre           VARCHAR(255),
    medico_matricula        VARCHAR(100),
    fecha_emision           DATE,
    confianza_extraccion    REAL,
    corregido_manualmente   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              BIGINT,
    updated_by              BIGINT,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMP
);

CREATE TABLE evento_certificado (
    id                  BIGSERIAL PRIMARY KEY,
    certificado_id      BIGINT NOT NULL REFERENCES certificado(id),
    empresa_id          BIGINT NOT NULL,
    estado_anterior     VARCHAR(50),
    estado_nuevo        VARCHAR(50) NOT NULL,
    actor_usuario_id    BIGINT NOT NULL,
    direccion_ip        VARCHAR(100),
    user_agent          VARCHAR(500),
    timestamp_utc       TIMESTAMP NOT NULL DEFAULT NOW(),
    observaciones       VARCHAR(2000)
);

CREATE INDEX idx_evento_certificado_certificado ON evento_certificado(certificado_id);
CREATE INDEX idx_evento_certificado_timestamp ON evento_certificado(timestamp_utc);

-- Asignar permisos a roles existentes
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'certificado.cargar', 'certificado.ver_resumen', 'certificado.configurar', 'reporte.ausentismo.ver'
)
WHERE r.nombre IN ('Administrador', 'RRHH') AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('certificado.ver_propio', 'certificado.cargar')
WHERE r.nombre = 'Empleado' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN ('certificado.ver_resumen', 'reporte.ausentismo.ver')
WHERE r.nombre = 'Supervisor' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

-- Roles médicos por empresa (si aún no existen)
INSERT INTO rol (empresa_id, nombre, descripcion, created_at, updated_at, is_deleted)
SELECT e.id, 'MedicoLaboral', 'Médico laboral: carga y visualización completa de certificados',
       NOW(), NOW(), FALSE
FROM empresa e
WHERE e.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol r WHERE r.empresa_id = e.id AND r.nombre = 'MedicoLaboral' AND r.is_deleted = FALSE
  );

INSERT INTO rol (empresa_id, nombre, descripcion, created_at, updated_at, is_deleted)
SELECT e.id, 'AuditorMedico', 'Auditor médico: validación y rechazo de certificados',
       NOW(), NOW(), FALSE
FROM empresa e
WHERE e.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol r WHERE r.empresa_id = e.id AND r.nombre = 'AuditorMedico' AND r.is_deleted = FALSE
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'certificado.cargar', 'certificado.ver_completo', 'certificado.ver_propio', 'archivo.subir', 'archivo.descargar'
)
WHERE r.nombre = 'MedicoLaboral' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM rol r
JOIN permiso p ON p.codigo IN (
    'certificado.ver_completo', 'certificado.validar', 'certificado.rechazar',
    'certificado.configurar', 'reporte.ausentismo.ver', 'archivo.descargar'
)
WHERE r.nombre = 'AuditorMedico' AND r.is_deleted = FALSE AND p.is_deleted = FALSE
  AND NOT EXISTS (
    SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id
  );
