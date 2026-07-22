package com.rrhh.saas.vacacion.validacion;

import com.rrhh.saas.vacacion.dominio.DiaSemana;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorFechaInicioVacacionesTest {

    @Test
    void descansoSabadoDomingo_inicioValidoEsLunes() {
        List<DiaSemana> patron = List.of(DiaSemana.SABADO, DiaSemana.DOMINGO);
        LocalDate lunes = LocalDate.of(2026, 6, 1);
        assertEquals(DayOfWeek.MONDAY, lunes.getDayOfWeek());

        ValidacionFechaInicio resultado = ValidadorFechaInicioVacaciones.validar(lunes, patron);
        assertTrue(resultado.esValida());
    }

    @Test
    void descansoMiercolesJueves_inicioValidoEsViernes() {
        List<DiaSemana> patron = List.of(DiaSemana.MIERCOLES, DiaSemana.JUEVES);
        LocalDate viernes = LocalDate.of(2026, 6, 5);
        assertEquals(DayOfWeek.FRIDAY, viernes.getDayOfWeek());

        assertTrue(ValidadorFechaInicioVacaciones.validar(viernes, patron).esValida());
        assertFalse(ValidadorFechaInicioVacaciones.validar(LocalDate.of(2026, 6, 4), patron).esValida());
    }

    @Test
    void soloDomingo_inicioValidoEsLunes() {
        List<DiaSemana> patron = List.of(DiaSemana.DOMINGO);
        LocalDate lunes = LocalDate.of(2026, 6, 8);
        assertTrue(ValidadorFechaInicioVacaciones.validar(lunes, patron).esValida());
    }

    @Test
    void patronVigenteSegunFecha_usaDiasDelPatronCargado() {
        List<DiaSemana> patronAntiguo = List.of(DiaSemana.SABADO, DiaSemana.DOMINGO);
        List<DiaSemana> patronNuevo = List.of(DiaSemana.MIERCOLES, DiaSemana.JUEVES);

        LocalDate viernes = LocalDate.of(2026, 7, 3);
        assertTrue(ValidadorFechaInicioVacaciones.validar(viernes, patronNuevo).esValida());
        assertFalse(ValidadorFechaInicioVacaciones.validar(viernes, patronAntiguo).esValida());
    }

    @Test
    void fechaInvalida_devuelveFechaSugerida() {
        List<DiaSemana> patron = List.of(DiaSemana.SABADO, DiaSemana.DOMINGO);
        LocalDate martes = LocalDate.of(2026, 6, 2);

        ValidacionFechaInicio resultado = ValidadorFechaInicioVacaciones.validar(martes, patron);
        assertFalse(resultado.esValida());
        assertEquals(LocalDate.of(2026, 6, 8), resultado.fechaSugerida());
        assertEquals(DayOfWeek.MONDAY, resultado.fechaSugerida().getDayOfWeek());
    }

    @Test
    void descansoNoConsecutivo_martesYViernes() {
        List<DiaSemana> patron = List.of(DiaSemana.MARTES, DiaSemana.VIERNES);
        assertTrue(ValidadorFechaInicioVacaciones.validar(
                LocalDate.of(2026, 6, 3), patron).esValida());
        assertTrue(ValidadorFechaInicioVacaciones.validar(
                LocalDate.of(2026, 6, 6), patron).esValida());
        assertFalse(ValidadorFechaInicioVacaciones.validar(
                LocalDate.of(2026, 6, 1), patron).esValida());
    }
}
