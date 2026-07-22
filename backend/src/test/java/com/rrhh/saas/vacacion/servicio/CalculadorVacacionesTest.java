package com.rrhh.saas.vacacion.servicio;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorVacacionesTest {

    @Test
    void calculaDiasInclusiveEntreFechas() {
        assertEquals(5, CalculadorVacaciones.calcularDiasEntreFechas(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5)));
    }

    @Test
    void unSoloDiaCuentaComoUno() {
        assertEquals(1, CalculadorVacaciones.calcularDiasEntreFechas(
                LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 10)));
    }
}
