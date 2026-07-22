package com.rrhh.saas.vacacion.servicio;

import com.rrhh.saas.empleado.dominio.Empleado;
import com.rrhh.saas.vacacion.dominio.PoliticaVacaciones;
import com.rrhh.saas.vacacion.dominio.RangoPoliticaVacaciones;
import com.rrhh.saas.vacacion.validacion.CalculadorAntiguedadVacaciones;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public final class CalculadorVacaciones {

    private CalculadorVacaciones() {
    }

    /**
     * @deprecated Usar {@link CalculadorAntiguedadVacaciones#calcularAniosAntiguedad(LocalDate, int)}
     */
    @Deprecated
    public static long calcularAniosAntiguedad(Empleado empleado) {
        int anio = LocalDate.now().getYear();
        return CalculadorAntiguedadVacaciones.calcularAniosAntiguedad(empleado.getFechaIngreso(), anio);
    }

    public static int calcularDiasOtorgados(PoliticaVacaciones politica, long aniosAntiguedad) {
        return politica.getRangos().stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> r.aplicaParaAnios(aniosAntiguedad))
                .max(Comparator.comparingInt(RangoPoliticaVacaciones::getAniosMinimos))
                .map(RangoPoliticaVacaciones::getDiasOtorgados)
                .orElse(0);
    }

    public static int calcularDiasEntreFechas(LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(inicio, fin) + 1;
    }
}
