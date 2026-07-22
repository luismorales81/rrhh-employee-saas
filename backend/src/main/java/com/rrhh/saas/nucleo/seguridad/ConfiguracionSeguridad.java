package com.rrhh.saas.nucleo.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    private final FiltroJwt filtroJwt;
    private final CorsConfigurationSource fuenteConfiguracionCors;
    private final PuntoEntradaAutenticacion puntoEntradaAutenticacion;
    private final ManejadorAccesoDenegadoHttp manejadorAccesoDenegadoHttp;
    private final Environment entorno;

    public ConfiguracionSeguridad(FiltroJwt filtroJwt,
                                  CorsConfigurationSource fuenteConfiguracionCors,
                                  PuntoEntradaAutenticacion puntoEntradaAutenticacion,
                                  ManejadorAccesoDenegadoHttp manejadorAccesoDenegadoHttp,
                                  Environment entorno) {
        this.filtroJwt = filtroJwt;
        this.fuenteConfiguracionCors = fuenteConfiguracionCors;
        this.puntoEntradaAutenticacion = puntoEntradaAutenticacion;
        this.manejadorAccesoDenegadoHttp = manejadorAccesoDenegadoHttp;
        this.entorno = entorno;
    }

    @Bean
    public SecurityFilterChain cadenaFiltrosSeguridad(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(fuenteConfiguracionCors))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    var reglas = auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/auth/**").permitAll();
                    if (!entorno.acceptsProfiles(Profiles.of("prod"))) {
                        reglas.requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll();
                    }
                    reglas.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(puntoEntradaAutenticacion)
                        .accessDeniedHandler(manejadorAccesoDenegadoHttp)
                )
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
