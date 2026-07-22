package com.rrhh.saas.archivo.config;

import com.rrhh.saas.archivo.servicio.ServicioMigracionAlmacenamiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("migracion")
public class ConfiguracionMigracionAlmacenamiento {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionMigracionAlmacenamiento.class);

    @Bean
    CommandLineRunner ejecutorMigracionAlmacenamiento(ServicioMigracionAlmacenamiento servicioMigracion) {
        return args -> {
            boolean ejecutar = false;
            for (String arg : args) {
                if ("--migrar-almacenamiento=true".equals(arg)) {
                    ejecutar = true;
                    break;
                }
            }
            if (!ejecutar) {
                log.info("Perfil migracion activo. Para migrar archivos a S3 use: --migrar-almacenamiento=true");
                return;
            }
            log.info("Iniciando migración de almacenamiento disco -> S3...");
            ServicioMigracionAlmacenamiento.ResultadoMigracion resultado = servicioMigracion.ejecutar();
            log.info("Migración finalizada: total={} exitos={} omitidos={} fallos={}",
                    resultado.total(), resultado.exitos(), resultado.omitidos(), resultado.fallos());
        };
    }
}
