-- Clave lógica de objeto (independiente del proveedor: disco, MinIO, R2)

ALTER TABLE archivo_almacenado RENAME COLUMN ruta_interna TO clave_almacenamiento;
