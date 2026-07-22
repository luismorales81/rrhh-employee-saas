package com.rrhh.saas.vacacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudSimularFechaVacaciones(
        @NotNull LocalDate fechaInicioDeseada,
        @Min(1) int diasDeseados
) {
}
