package com.rrhh.saas.nucleo.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class PropiedadesJwt {

    private String secreto;
    private long expiracionMs;
    private long refrescoExpiracionMs;

    public String getSecreto() {
        return secreto;
    }

    public void setSecreto(String secreto) {
        this.secreto = secreto;
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    public void setExpiracionMs(long expiracionMs) {
        this.expiracionMs = expiracionMs;
    }

    public long getRefrescoExpiracionMs() {
        return refrescoExpiracionMs;
    }

    public void setRefrescoExpiracionMs(long refrescoExpiracionMs) {
        this.refrescoExpiracionMs = refrescoExpiracionMs;
    }
}
