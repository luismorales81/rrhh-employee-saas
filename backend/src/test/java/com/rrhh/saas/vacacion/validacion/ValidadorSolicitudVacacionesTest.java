package com.rrhh.saas.vacacion.validacion;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import com.rrhh.saas.vacacion.dominio.ConfiguracionVacaciones;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorSolicitudVacacionesTest {

    private ConfiguracionVacaciones configPorDefecto() {
        ConfiguracionVacaciones config = new ConfiguracionVacaciones();
        config.setDiasMinimosFraccion(7);
        config.setAnticipacionMinimaDias(45);
        return config;
    }

    @Test
    void rechazaFraccionMenorA7Dias() {
        var solicitud = new ValidadorSolicitudVacaciones.DatosSolicitud(
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 8));
        var resultado = ValidadorSolicitudVacaciones.validar(
                solicitud, BigDecimal.TEN, configPorDefecto(), List.of(),
                LocalDate.of(2026, 6, 1), null);
        assertFalse(resultado.esValida());
        assertTrue(resultado.errores().stream().anyMatch(e -> e.contains("7")));
    }

    @Test
    void rechazaSolapamiento() {
        var solicitud = new ValidadorSolicitudVacaciones.DatosSolicitud(
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 14));
        var existente = new ValidadorSolicitudVacaciones.PeriodoSolicitudExistente(
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 20),
                EstadoSolicitud.EN_REVISION, null);
        var resultado = ValidadorSolicitudVacaciones.validar(
                solicitud, BigDecimal.valueOf(20), configPorDefecto(),
                List.of(existente), LocalDate.of(2026, 6, 1), null);
        assertFalse(resultado.esValida());
        assertTrue(resultado.errores().stream().anyMatch(e -> e.contains("superpone")));
    }

    @Test
    void rechazaExcesoSaldo() {
        var solicitud = new ValidadorSolicitudVacaciones.DatosSolicitud(
                LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 14));
        var resultado = ValidadorSolicitudVacaciones.validar(
                solicitud, BigDecimal.valueOf(5), configPorDefecto(), List.of(),
                LocalDate.of(2026, 6, 1), null);
        assertFalse(resultado.esValida());
        assertTrue(resultado.errores().stream().anyMatch(e -> e.contains("Saldo insuficiente")));
    }

    @Test
    void anticipacionInsuficienteEsAdvertenciaNoError() {
        var solicitud = new ValidadorSolicitudVacaciones.DatosSolicitud(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 10));
        var resultado = ValidadorSolicitudVacaciones.validar(
                solicitud, BigDecimal.valueOf(20), configPorDefecto(), List.of(),
                LocalDate.of(2026, 6, 1), null);
        assertTrue(resultado.esValida());
        assertTrue(resultado.advertencias().stream().anyMatch(a -> a.contains("anticipación")));
    }
}
