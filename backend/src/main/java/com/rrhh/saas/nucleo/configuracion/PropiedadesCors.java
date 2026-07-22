package com.rrhh.saas.nucleo.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cors")
public class PropiedadesCors {

    /**
     * Orígenes permitidos en producción, separados por coma (ej. https://app.ejemplo.com).
     */
    private String origenes = "";

    public String getOrigenes() {
        return origenes;
    }

    public void setOrigenes(String origenes) {
        this.origenes = origenes;
    }
}
