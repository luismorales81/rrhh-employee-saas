package com.rrhh.saas.recibo.tarea;

import com.rrhh.saas.recibo.servicio.ServicioVerificacionIntegridadRecibos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareaVerificacionIntegridadRecibos {

    private static final Logger log = LoggerFactory.getLogger(TareaVerificacionIntegridadRecibos.class);

    private final ServicioVerificacionIntegridadRecibos servicioVerificacion;

    public TareaVerificacionIntegridadRecibos(ServicioVerificacionIntegridadRecibos servicioVerificacion) {
        this.servicioVerificacion = servicioVerificacion;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void verificarIntegridad() {
        try {
            var resultado = servicioVerificacion.verificarTodos();
            log.info("Verificación de integridad de recibos: {} verificados, {} fallidos",
                    resultado.verificados(), resultado.fallidos());
        } catch (Exception e) {
            log.warn("Error en verificación de integridad de recibos: {}", e.getMessage());
        }
    }
}
