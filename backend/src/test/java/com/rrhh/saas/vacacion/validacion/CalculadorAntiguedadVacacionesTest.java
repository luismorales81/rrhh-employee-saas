package com.rrhh.saas.vacacion.validacion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorAntiguedadVacacionesTest {

    @Test
    void antiguedadAl31DiciembreDelAnioDeGoce() {
        LocalDate ingreso = LocalDate.of(2020, 3, 15);
        assertEquals(5, CalculadorAntiguedadVacaciones.calcularAniosAntiguedad(ingreso, 2025));
        assertEquals(4, CalculadorAntiguedadVacaciones.calcularAniosAntiguedad(ingreso, 2024));
    }

    @Test
    void ingresoPosteriorAlCierre_retornaCero() {
        LocalDate ingreso = LocalDate.of(2026, 6, 1);
        assertEquals(0, CalculadorAntiguedadVacaciones.calcularAniosAntiguedad(ingreso, 2025));
    }
}
