package com.rrhh.saas.recibo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recibos.verificacion-integridad")
public class PropiedadesVerificacionIntegridadRecibos {

    private boolean habilitada = true;

    public boolean isHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }
}
