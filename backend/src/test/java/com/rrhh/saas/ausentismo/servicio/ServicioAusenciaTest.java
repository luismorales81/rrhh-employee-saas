package com.rrhh.saas.ausentismo.servicio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioAusenciaTest {

    @Test
    void moduloAusentismoDisponible() {
        assertTrue(ServicioAusencia.EventoAusenciaRegistrada.class.isRecord());
    }
}
