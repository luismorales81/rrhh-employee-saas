package com.rrhh.saas.nucleo.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ConfiguracionCors {

    private final Environment entorno;
    private final PropiedadesCors propiedadesCors;

    public ConfiguracionCors(Environment entorno, PropiedadesCors propiedadesCors) {
        this.entorno = entorno;
        this.propiedadesCors = propiedadesCors;
    }

    @Bean
    public CorsConfigurationSource fuenteConfiguracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(resolverOrigenes());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Empresa-Id",
                "X-Modo-Soporte",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        configuracion.setExposedHeaders(List.of("Authorization"));
        configuracion.setAllowCredentials(true);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    private List<String> resolverOrigenes() {
        if (entorno.acceptsProfiles(Profiles.of("prod"))) {
            String configurados = propiedadesCors.getOrigenes();
            if (configurados == null || configurados.isBlank()) {
                throw new IllegalStateException(
                        "En perfil prod debe definirse app.cors.origenes / CORS_ORIGENES");
            }
            return Arrays.stream(configurados.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*",
                "http://10.*:*",
                "capacitor://localhost",
                "ionic://localhost",
                "http://localhost",
                "https://localhost"
        );
    }
}
