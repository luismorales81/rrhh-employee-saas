package com.rrhh.saas.certificado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ConfiguracionAsyncCertificados {

    @Bean(name = "ejecutorExtraccionCertificados")
    public Executor ejecutorExtraccionCertificados() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(2);
        ejecutor.setMaxPoolSize(5);
        ejecutor.setQueueCapacity(50);
        ejecutor.setThreadNamePrefix("extraccion-cert-");
        ejecutor.initialize();
        return ejecutor;
    }
}
