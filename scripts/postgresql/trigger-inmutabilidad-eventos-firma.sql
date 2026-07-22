-- CP-02: aplicar en PostgreSQL de producción (no ejecutado por Flyway en H2)
-- psql -d rrhh_saas -f scripts/postgresql/trigger-inmutabilidad-eventos-firma.sql

CREATE OR REPLACE FUNCTION bloquear_modificacion_evento_firma_recibo()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Los eventos de firma de recibo son inmutables';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_evento_firma_recibo_inmutable ON evento_firma_recibo;

CREATE TRIGGER trg_evento_firma_recibo_inmutable
    BEFORE UPDATE OR DELETE ON evento_firma_recibo
    FOR EACH ROW
    EXECUTE PROCEDURE bloquear_modificacion_evento_firma_recibo();
