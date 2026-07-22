-- Vertex AI directo: reintentos, versionado de extracciones, config GCP por tenant

ALTER TABLE certificado ADD COLUMN IF NOT EXISTS intentos_extraccion INT NOT NULL DEFAULT 0;
ALTER TABLE certificado ADD COLUMN IF NOT EXISTS ultimo_error_extraccion VARCHAR(500);
ALTER TABLE certificado ADD COLUMN IF NOT EXISTS ultimo_intento_extraccion TIMESTAMP;

ALTER TABLE datos_extraidos_certificado ADD COLUMN IF NOT EXISTS numero_version INT NOT NULL DEFAULT 1;
ALTER TABLE datos_extraidos_certificado ADD COLUMN IF NOT EXISTS es_vigente BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS proyecto_gcp VARCHAR(100);
ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS region_vertex VARCHAR(50);
ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS credenciales_servicio_cifrada TEXT;
ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS max_intentos_extraccion INT NOT NULL DEFAULT 3;
