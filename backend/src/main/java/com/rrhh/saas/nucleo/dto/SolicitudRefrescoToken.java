package com.rrhh.saas.nucleo.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRefrescoToken(
        @NotBlank String tokenRefresco
) {
}
