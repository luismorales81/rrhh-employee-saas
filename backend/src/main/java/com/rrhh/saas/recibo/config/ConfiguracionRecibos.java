package com.rrhh.saas.recibo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PropiedadesVerificacionIntegridadRecibos.class, PropiedadesRecibos.class})
public class ConfiguracionRecibos {
}
