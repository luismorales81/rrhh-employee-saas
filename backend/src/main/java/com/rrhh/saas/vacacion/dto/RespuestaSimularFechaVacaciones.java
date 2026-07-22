package com.rrhh.saas.vacacion.dto;

import java.time.LocalDate;

public record RespuestaSimularFechaVacaciones(
        boolean esValida,
        LocalDate fechaInicioDeseada,
        LocalDate fechaFinCalculada,
        LocalDate fechaSugerida,
        String motivo
) {
}
