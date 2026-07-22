package com.rrhh.saas.certificado.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRechazarCertificado(
        @NotBlank String observaciones
) {
}
