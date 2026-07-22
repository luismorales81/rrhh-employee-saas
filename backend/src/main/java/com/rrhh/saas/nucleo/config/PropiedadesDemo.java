package com.rrhh.saas.nucleo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo")
public class PropiedadesDemo {

    private boolean cargarDatos = true;
    private String urlPdfContrato = "http://localhost:4200/demo/contrato-ejemplo.pdf";
    private String urlPdfRecibo = "http://localhost:4200/demo/recibo-ejemplo.pdf";

    public boolean isCargarDatos() {
        return cargarDatos;
    }

    public void setCargarDatos(boolean cargarDatos) {
        this.cargarDatos = cargarDatos;
    }

    public String getUrlPdfContrato() {
        return urlPdfContrato;
    }

    public void setUrlPdfContrato(String urlPdfContrato) {
        this.urlPdfContrato = urlPdfContrato;
    }

    public String getUrlPdfRecibo() {
        return urlPdfRecibo;
    }

    public void setUrlPdfRecibo(String urlPdfRecibo) {
        this.urlPdfRecibo = urlPdfRecibo;
    }
}
