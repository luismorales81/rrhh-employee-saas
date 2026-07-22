package com.rrhh.saas.nucleo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record SolicitudActualizarRol(
        @NotBlank String nombre,
        String descripcion,
        @NotNull Set<Long> permisosIds
) {
}
