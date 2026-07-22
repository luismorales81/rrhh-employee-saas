package com.rrhh.saas.fichaje.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRechazarMarcaFichaje(
        @NotBlank String notaRevision
) {
}
