package com.rrhh.saas.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitudCrearCategoriaDocumento(
        @NotBlank @Size(max = 255) String nombre,
        @Size(max = 500) String descripcion
) {
}
