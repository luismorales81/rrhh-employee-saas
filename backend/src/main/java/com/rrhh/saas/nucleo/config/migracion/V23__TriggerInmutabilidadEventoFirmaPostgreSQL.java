package com.rrhh.saas.nucleo.config.migracion;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * CP-02 producción: trigger append-only en PostgreSQL (Flyway V23).
 * En H2 (tests) no ejecuta DDL; la inmutabilidad queda en el servicio.
 */
public class V23__TriggerInmutabilidadEventoFirmaPostgreSQL extends BaseJavaMigration {

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("23");
    }

    @Override
    public String getDescription() {
        return "trigger inmutabilidad evento firma postgresql";
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection conexion = context.getConnection();
        if (!esPostgreSql(conexion)) {
            return;
        }

        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE OR REPLACE FUNCTION bloquear_modificacion_evento_firma_recibo()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        RAISE EXCEPTION 'Los eventos de firma de recibo son inmutables';
                    END;
                    $$ LANGUAGE plpgsql;
                    """);

            sentencia.execute("DROP TRIGGER IF EXISTS trg_evento_firma_recibo_inmutable ON evento_firma_recibo");

            sentencia.execute("""
                    CREATE TRIGGER trg_evento_firma_recibo_inmutable
                        BEFORE UPDATE OR DELETE ON evento_firma_recibo
                        FOR EACH ROW
                        EXECUTE PROCEDURE bloquear_modificacion_evento_firma_recibo();
                    """);
        }
    }

    private boolean esPostgreSql(Connection conexion) throws SQLException {
        String producto = conexion.getMetaData().getDatabaseProductName();
        return producto != null && producto.toLowerCase().contains("postgresql");
    }
}
