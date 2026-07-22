package com.rrhh.saas.nucleo.config.migracion;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ajustes de esquema específicos de PostgreSQL para versionado de extracciones.
 * En H2 (tests) la unicidad de extracción vigente queda en la capa de servicio.
 */
public class V35__CertificadosVertexAiPostgreSQL extends BaseJavaMigration {

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("35");
    }

    @Override
    public String getDescription() {
        return "certificados vertex ai constraints postgresql";
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection conexion = context.getConnection();
        if (!esPostgreSql(conexion)) {
            return;
        }

        try (Statement sentencia = conexion.createStatement()) {
            eliminarUniqueCertificadoIdSiExiste(conexion, sentencia);
            sentencia.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_datos_extraidos_certificado_version
                        ON datos_extraidos_certificado(certificado_id, numero_version)
                    """);
            sentencia.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS uq_datos_extraidos_vigente
                        ON datos_extraidos_certificado(certificado_id)
                        WHERE es_vigente = TRUE AND is_deleted = FALSE
                    """);
        }
    }

    private void eliminarUniqueCertificadoIdSiExiste(Connection conexion, Statement sentencia)
            throws SQLException {
        sentencia.execute("""
                ALTER TABLE datos_extraidos_certificado
                    DROP CONSTRAINT IF EXISTS datos_extraidos_certificado_certificado_id_key
                """);

        String sqlBuscarRestricciones = """
                SELECT c.conname
                FROM pg_constraint c
                JOIN pg_class t ON c.conrelid = t.oid
                WHERE t.relname = 'datos_extraidos_certificado'
                  AND c.contype = 'u'
                  AND array_length(c.conkey, 1) = 1
                  AND EXISTS (
                    SELECT 1
                    FROM pg_attribute a
                    WHERE a.attrelid = t.oid
                      AND a.attnum = c.conkey[1]
                      AND a.attname = 'certificado_id'
                  )
                """;
        try (ResultSet rs = sentencia.executeQuery(sqlBuscarRestricciones)) {
            while (rs.next()) {
                String nombreRestriccion = rs.getString(1);
                sentencia.execute(
                        "ALTER TABLE datos_extraidos_certificado DROP CONSTRAINT IF EXISTS "
                                + nombreRestriccion);
            }
        }

        String nombreIndice = null;
        try (ResultSet rs = conexion.getMetaData().getIndexInfo(
                null, null, "datos_extraidos_certificado", true, false)) {
            while (rs.next()) {
                if (rs.getBoolean("NON_UNIQUE")) {
                    continue;
                }
                String columna = rs.getString("COLUMN_NAME");
                if ("certificado_id".equalsIgnoreCase(columna)) {
                    nombreIndice = rs.getString("INDEX_NAME");
                    break;
                }
            }
        }
        if (nombreIndice != null
                && !nombreIndice.equalsIgnoreCase("datos_extraidos_certificado_certificado_id_key")) {
            sentencia.execute("DROP INDEX IF EXISTS " + nombreIndice);
        }
    }

    private boolean esPostgreSql(Connection conexion) throws SQLException {
        String producto = conexion.getMetaData().getDatabaseProductName();
        return producto != null && producto.toLowerCase().contains("postgresql");
    }
}
