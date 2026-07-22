ALTER TABLE comunicacion ADD COLUMN fecha_publicacion_programada TIMESTAMP;
ALTER TABLE comunicacion ALTER COLUMN fecha_publicacion DROP NOT NULL;
CREATE INDEX idx_comunicacion_programada ON comunicacion(estado, fecha_publicacion_programada);
