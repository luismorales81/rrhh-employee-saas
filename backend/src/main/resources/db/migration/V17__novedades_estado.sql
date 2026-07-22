-- Estados de novedades de liquidación (BORRADOR → CONFIRMADA → EXPORTADA)

ALTER TABLE novedad_liquidacion ADD COLUMN estado VARCHAR(50) NOT NULL DEFAULT 'BORRADOR';

CREATE INDEX idx_novedad_liquidacion_estado ON novedad_liquidacion(estado);
