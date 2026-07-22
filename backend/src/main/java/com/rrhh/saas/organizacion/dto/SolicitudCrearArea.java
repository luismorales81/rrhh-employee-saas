package com.rrhh.saas.organizacion.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearArea(
        @NotBlank String nombre,
        String descripcion,
        Long gerenciaId
) {
}
