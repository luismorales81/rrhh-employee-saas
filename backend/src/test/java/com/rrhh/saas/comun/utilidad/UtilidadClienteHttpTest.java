package com.rrhh.saas.comun.utilidad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilidadClienteHttpTest {

    @Test
    void detectarSistemaOperativoDesdeUserAgent() {
        assertEquals("Android", UtilidadClienteHttp.detectarSistemaOperativo(
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36"));
        assertEquals("Windows", UtilidadClienteHttp.detectarSistemaOperativo(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"));
        assertEquals("desconocido", UtilidadClienteHttp.detectarSistemaOperativo(null));
    }
}
