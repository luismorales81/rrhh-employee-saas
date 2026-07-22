package com.rrhh.saas.activo.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SolicitudAsignarActivo(
        @NotNull Long empleadoId,
        @NotNull LocalDate fechaEntrega,
        String notas
) {
}
