-- Configuración de IA (Gemini) por tenant para extracción de certificados médicos



ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS clave_api_gemini_cifrada VARCHAR(1000);

ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS modelo_gemini VARCHAR(100);

ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS url_webhook_n8n VARCHAR(500);

ALTER TABLE configuracion_auditoria_medica ADD COLUMN IF NOT EXISTS token_servicio_webhook VARCHAR(128);


