package com.rrhh.saas.ausentismo.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudJustificarAusencia(
        @NotBlank String urlCertificado
) {
}
