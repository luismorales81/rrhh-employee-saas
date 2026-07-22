package com.rrhh.saas.organizacion.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearGerencia(
        @NotBlank String nombre,
        String descripcion
) {
}
