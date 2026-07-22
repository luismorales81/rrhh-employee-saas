-- CP-05: marcar recibos históricos sin archivo almacenado verificable

ALTER TABLE recibo ADD COLUMN IF NOT EXISTS requiere_republicacion BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE recibo
SET requiere_republicacion = TRUE
WHERE archivo_id IS NULL
   OR hash_pdf IS NULL
   OR TRIM(hash_pdf) = '';
