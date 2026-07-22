package com.rrhh.saas.vacacion.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record SolicitudContrapropuestaVacaciones(
        @NotBlank String motivo,
        LocalDate fechaInicioContrapropuesta,
        LocalDate fechaFinContrapropuesta
) {
}
