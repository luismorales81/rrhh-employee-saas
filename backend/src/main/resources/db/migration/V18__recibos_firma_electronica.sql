-- Firma electrónica de recibos: hash inmutable, estados ampliados y eventos append-only

ALTER TABLE recibo ADD COLUMN archivo_id BIGINT REFERENCES archivo_almacenado(id);
ALTER TABLE recibo ADD COLUMN hash_pdf VARCHAR(64);
ALTER TABLE recibo ADD COLUMN monto_neto DECIMAL(15, 2);
ALTER TABLE recibo ADD COLUMN monto_bruto DECIMAL(15, 2);
ALTER TABLE recibo ADD COLUMN publicado_por_id BIGINT REFERENCES usuario(id);
ALTER TABLE recibo ADD COLUMN visto_por_empleado_en TIMESTAMP;

UPDATE recibo SET estado = 'PENDIENTE' WHERE estado = 'PUBLICADO';

CREATE TABLE evento_firma_recibo (
    id                  BIGSERIAL PRIMARY KEY,
    recibo_id           BIGINT NOT NULL REFERENCES recibo(id),
    empleado_id         BIGINT NOT NULL REFERENCES empleado(id),
    empresa_id          BIGINT NOT NULL REFERENCES empresa(id),
    usuario_id          BIGINT NOT NULL REFERENCES usuario(id),
    tipo                VARCHAR(50) NOT NULL,
    timestamp_evento    TIMESTAMP NOT NULL DEFAULT NOW(),
    direccion_ip        VARCHAR(100),
    user_agent          VARCHAR(500),
    sistema_operativo   VARCHAR(100),
    hash_pdf_en_firma   VARCHAR(64) NOT NULL,
    comentario          VARCHAR(2000)
);

CREATE INDEX idx_evento_firma_recibo_recibo ON evento_firma_recibo(recibo_id);
CREATE INDEX idx_evento_firma_recibo_empresa ON evento_firma_recibo(empresa_id);
