package com.rrhh.saas.vacacion.validacion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Antigüedad según Art. 150 LCT: calculada al 31/12 del año de goce de vacaciones.
 */
public final class CalculadorAntiguedadVacaciones {

    private CalculadorAntiguedadVacaciones() {
    }

    public static int calcularAniosAntiguedad(LocalDate fechaIngreso, int anioGoce) {
        if (fechaIngreso == null) {
            return 0;
        }
        LocalDate cierreAnio = LocalDate.of(anioGoce, 12, 31);
        if (fechaIngreso.isAfter(cierreAnio)) {
            return 0;
        }
        return (int) ChronoUnit.YEARS.between(fechaIngreso, cierreAnio);
    }
}
