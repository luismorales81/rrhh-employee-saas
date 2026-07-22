package com.rrhh.saas.comunicacion.servicio;

import com.rrhh.saas.comunicacion.dominio.TipoDestinatarioComunicacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioComunicacionTest {

    @Test
    void tiposDestinatarioComunicacionDefinidos() {
        assertEquals(5, TipoDestinatarioComunicacion.values().length);
        assertTrue(ServicioComunicacion.EventoComunicacionPublicada.class.isRecord());
    }
}
