package com.rrhh.saas.certificado.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.certificados")
public class PropiedadesCertificados {

    private final Ia ia = new Ia();
    private final Reintento reintento = new Reintento();

    public Ia getIa() {
        return ia;
    }

    public Reintento getReintento() {
        return reintento;
    }

    public static class Ia {
        private boolean simulada = false;

        public boolean isSimulada() {
            return simulada;
        }

        public void setSimulada(boolean simulada) {
            this.simulada = simulada;
        }
    }

    public static class Reintento {
        private String cron = "0 */5 * * * *";
        private int minutosColgado = 10;

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public int getMinutosColgado() {
            return minutosColgado;
        }

        public void setMinutosColgado(int minutosColgado) {
            this.minutosColgado = minutosColgado;
        }
    }
}
