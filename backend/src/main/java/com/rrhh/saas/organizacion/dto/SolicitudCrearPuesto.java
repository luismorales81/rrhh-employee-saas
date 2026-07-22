package com.rrhh.saas.organizacion.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearPuesto(
        @NotBlank String nombre,
        String nivel,
        String descripcion
) {
}
