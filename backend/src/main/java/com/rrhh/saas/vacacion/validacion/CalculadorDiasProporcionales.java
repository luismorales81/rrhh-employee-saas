package com.rrhh.saas.vacacion.validacion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Días proporcionales de vacaciones para empleados que no completaron el año calendario.
 * Fórmula: diasAnuales * mesesTrabajados / 12 (meses completos o proporción por días).
 */
public final class CalculadorDiasProporcionales {

    private CalculadorDiasProporcionales() {
    }

    public static int calcularDiasProporcionales(
            LocalDate fechaIngreso, int anioGoce, int diasAnualesCompletos) {

        if (fechaIngreso == null || diasAnualesCompletos <= 0) {
            return 0;
        }

        LocalDate inicioAnio = LocalDate.of(anioGoce, 1, 1);
        LocalDate finAnio = LocalDate.of(anioGoce, 12, 31);

        if (fechaIngreso.isAfter(finAnio)) {
            return 0;
        }

        LocalDate inicioComputo = fechaIngreso.isBefore(inicioAnio) ? inicioAnio : fechaIngreso;
        long diasTrabajados = ChronoUnit.DAYS.between(inicioComputo, finAnio) + 1;
        long diasEnAnio = ChronoUnit.DAYS.between(inicioAnio, finAnio) + 1;

        return (int) Math.round((double) diasAnualesCompletos * diasTrabajados / diasEnAnio);
    }
}
