package com.rrhh.saas.vacacion.dominio;

import java.time.DayOfWeek;

/**
 * Días de la semana para patrón de descanso (Art. 151 LCT).
 */
public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    public DayOfWeek aDayOfWeek() {
        return switch (this) {
            case LUNES -> DayOfWeek.MONDAY;
            case MARTES -> DayOfWeek.TUESDAY;
            case MIERCOLES -> DayOfWeek.WEDNESDAY;
            case JUEVES -> DayOfWeek.THURSDAY;
            case VIERNES -> DayOfWeek.FRIDAY;
            case SABADO -> DayOfWeek.SATURDAY;
            case DOMINGO -> DayOfWeek.SUNDAY;
        };
    }

    public static DiaSemana desdeDayOfWeek(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> LUNES;
            case TUESDAY -> MARTES;
            case WEDNESDAY -> MIERCOLES;
            case THURSDAY -> JUEVES;
            case FRIDAY -> VIERNES;
            case SATURDAY -> SABADO;
            case SUNDAY -> DOMINGO;
        };
    }

    /** Ordinal 0=Lunes … 6=Domingo (ISO-8601). */
    public int ordinalIso() {
        return aDayOfWeek().getValue() - 1;
    }
}
