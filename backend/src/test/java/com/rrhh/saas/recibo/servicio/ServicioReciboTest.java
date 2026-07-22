package com.rrhh.saas.recibo.servicio;

import com.rrhh.saas.recibo.dominio.EstadoRecibo;
import com.rrhh.saas.recibo.dominio.TipoConfirmacionRecibo;
import com.rrhh.saas.recibo.dominio.TipoEventoFirmaRecibo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ServicioReciboTest {

    @Test
    void estadosReciboIncluyenFlujoLegal() {
        assertEquals(4, EstadoRecibo.values().length);
        assertEquals(EstadoRecibo.PENDIENTE, EstadoRecibo.valueOf("PENDIENTE"));
        assertEquals(EstadoRecibo.DISCONFORMIDAD, EstadoRecibo.valueOf("DISCONFORMIDAD"));
    }

    @Test
    void tiposEventoFirmaSonAppendOnly() {
        assertEquals(3, TipoEventoFirmaRecibo.values().length);
        assertNotEquals(TipoEventoFirmaRecibo.CONFIRMADO, TipoEventoFirmaRecibo.DISCONFORMIDAD);
    }

    @Test
    void tiposConfirmacionMapeanAEventos() {
        assertEquals(TipoConfirmacionRecibo.CONFIRMADO.name(), TipoEventoFirmaRecibo.CONFIRMADO.name());
        assertEquals(TipoConfirmacionRecibo.DISCONFORMIDAD.name(), TipoEventoFirmaRecibo.DISCONFORMIDAD.name());
    }
}
