package com.rrhh.saas.vacacion.validacion;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadorDiasProporcionalesTest {

    @Test
    void anoCompleto_retornaDiasAnuales() {
        LocalDate ingreso = LocalDate.of(2020, 1, 1);
        assertEquals(14, CalculadorDiasProporcionales.calcularDiasProporcionales(ingreso, 2026, 14));
    }

    @Test
    void ingresoMidYear_retornaProporcional() {
        LocalDate ingreso = LocalDate.of(2026, 7, 1);
        int dias = CalculadorDiasProporcionales.calcularDiasProporcionales(ingreso, 2026, 14);
        assertTrue(dias > 0 && dias < 14);
        assertEquals(7, dias);
    }
}
