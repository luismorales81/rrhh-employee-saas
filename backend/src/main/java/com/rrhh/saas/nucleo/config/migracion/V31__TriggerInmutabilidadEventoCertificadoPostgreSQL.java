package com.rrhh.saas.nucleo.config.migracion;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Trigger append-only en PostgreSQL para eventos de certificados médicos.
 * En H2 (tests) no ejecuta DDL; la inmutabilidad queda en el servicio.
 */
public class V31__TriggerInmutabilidadEventoCertificadoPostgreSQL extends BaseJavaMigration {

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("31");
    }

    @Override
    public String getDescription() {
        return "trigger inmutabilidad evento certificado postgresql";
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection conexion = context.getConnection();
        if (!esPostgreSql(conexion)) {
            return;
        }

        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE OR REPLACE FUNCTION bloquear_modificacion_evento_certificado()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        RAISE EXCEPTION 'Los eventos de certificado son inmutables';
                    END;
                    $$ LANGUAGE plpgsql;
                    """);

            sentencia.execute("DROP TRIGGER IF EXISTS trg_evento_certificado_inmutable ON evento_certificado");

            sentencia.execute("""
                    CREATE TRIGGER trg_evento_certificado_inmutable
                        BEFORE UPDATE OR DELETE ON evento_certificado
                        FOR EACH ROW
                        EXECUTE PROCEDURE bloquear_modificacion_evento_certificado();
                    """);
        }
    }

    private boolean esPostgreSql(Connection conexion) throws SQLException {
        String producto = conexion.getMetaData().getDatabaseProductName();
        return producto != null && producto.toLowerCase().contains("postgresql");
    }
}
