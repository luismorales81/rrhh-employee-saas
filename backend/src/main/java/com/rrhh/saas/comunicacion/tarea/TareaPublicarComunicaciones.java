package com.rrhh.saas.comunicacion.tarea;

import com.rrhh.saas.comunicacion.servicio.ServicioComunicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareaPublicarComunicaciones {

    private static final Logger log = LoggerFactory.getLogger(TareaPublicarComunicaciones.class);

    private final ServicioComunicacion servicioComunicacion;

    public TareaPublicarComunicaciones(ServicioComunicacion servicioComunicacion) {
        this.servicioComunicacion = servicioComunicacion;
    }

    @Scheduled(fixedRate = 60000)
    public void publicarPendientes() {
        try {
            servicioComunicacion.publicarPendientes();
        } catch (Exception e) {
            log.warn("Error al publicar comunicaciones programadas: {}", e.getMessage());
        }
    }
}
