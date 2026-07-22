package com.rrhh.saas.documento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudCrearDocumento(
        @NotNull Long categoriaId,
        @NotBlank @Size(max = 255) String titulo,
        @Size(max = 1000) String descripcion,
        @Size(max = 50) String version,
        @NotBlank @Size(max = 500) String urlArchivo,
        @Size(max = 128) String hashArchivo,
        boolean requiereFirma
) {
}
