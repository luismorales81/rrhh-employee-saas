package com.rrhh.saas.nucleo.config;

import com.rrhh.saas.nucleo.config.migracion.V23__TriggerInmutabilidadEventoFirmaPostgreSQL;
import com.rrhh.saas.nucleo.config.migracion.V27__TriggerInmutabilidadEventoSolicitudVacacionesPostgreSQL;
import com.rrhh.saas.nucleo.config.migracion.V31__TriggerInmutabilidadEventoCertificadoPostgreSQL;
import com.rrhh.saas.nucleo.config.migracion.V35__CertificadosVertexAiPostgreSQL;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionMigracionesFlyway {

    @Bean
    public JavaMigration migracionTriggerInmutabilidadEventoFirmaPostgreSQL() {
        return new V23__TriggerInmutabilidadEventoFirmaPostgreSQL();
    }

    @Bean
    public JavaMigration migracionTriggerInmutabilidadEventoSolicitudVacacionesPostgreSQL() {
        return new V27__TriggerInmutabilidadEventoSolicitudVacacionesPostgreSQL();
    }

    @Bean
    public JavaMigration migracionTriggerInmutabilidadEventoCertificadoPostgreSQL() {
        return new V31__TriggerInmutabilidadEventoCertificadoPostgreSQL();
    }

    @Bean
    public JavaMigration migracionCertificadosVertexAiPostgreSQL() {
        return new V35__CertificadosVertexAiPostgreSQL();
    }
}
