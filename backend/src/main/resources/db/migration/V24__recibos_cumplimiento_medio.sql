-- CP-06 a CP-11: versionado, notificación y republicación de recibos

ALTER TABLE recibo ADD COLUMN version INT NOT NULL DEFAULT 1;
ALTER TABLE recibo ADD COLUMN recibo_anterior_id BIGINT REFERENCES recibo(id);
ALTER TABLE recibo ADD COLUMN vigente BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE recibo ADD COLUMN notificado_en TIMESTAMP;
ALTER TABLE recibo ADD COLUMN canal_notificacion VARCHAR(50);
ALTER TABLE recibo ADD COLUMN motivo_republicacion VARCHAR(1000);

CREATE INDEX idx_recibo_vigente ON recibo(empresa_id, vigente, is_deleted);
