package com.rrhh.saas.documento.servicio;

import com.rrhh.saas.documento.dominio.EstadoAsignacionDocumento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioAsignacionDocumentoTest {

    @Test
    void estadosAsignacionDocumentoDefinidos() {
        assertEquals(4, EstadoAsignacionDocumento.values().length);
        assertTrue(ServicioAsignacionDocumento.EventoDocumentoFirmado.class.isRecord());
    }
}
