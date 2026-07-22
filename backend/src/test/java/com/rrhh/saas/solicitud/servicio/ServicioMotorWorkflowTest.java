package com.rrhh.saas.solicitud.servicio;

import com.rrhh.saas.solicitud.dominio.AccionPaso;
import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.solicitud.dominio.TipoPaso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicioMotorWorkflowTest {

    @Test
    void enumsWorkflowDefinidos() {
        assertEquals(5, TipoPaso.values().length);
        assertEquals(4, AccionPaso.values().length);
        assertEquals(EstadoSolicitud.EN_REVISION, EstadoSolicitud.valueOf("EN_REVISION"));
    }
}
