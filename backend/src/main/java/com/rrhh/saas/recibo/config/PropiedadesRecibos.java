package com.rrhh.saas.recibo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recibos")
public class PropiedadesRecibos {

    private int diasAlertaPendiente = 7;
    private boolean requiereContrasenaFirma = false;
    private String urlMisRecibos = "http://localhost:4200/mis-recibos";

    public int getDiasAlertaPendiente() {
        return diasAlertaPendiente;
    }

    public void setDiasAlertaPendiente(int diasAlertaPendiente) {
        this.diasAlertaPendiente = diasAlertaPendiente;
    }

    public boolean isRequiereContrasenaFirma() {
        return requiereContrasenaFirma;
    }

    public void setRequiereContrasenaFirma(boolean requiereContrasenaFirma) {
        this.requiereContrasenaFirma = requiereContrasenaFirma;
    }

    public String getUrlMisRecibos() {
        return urlMisRecibos;
    }

    public void setUrlMisRecibos(String urlMisRecibos) {
        this.urlMisRecibos = urlMisRecibos;
    }
}
