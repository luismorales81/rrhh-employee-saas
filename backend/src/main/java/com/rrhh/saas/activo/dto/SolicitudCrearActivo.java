package com.rrhh.saas.activo.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearActivo(
        @NotBlank String nombre,
        @NotBlank String tipo,
        String numeroSerie
) {
}
