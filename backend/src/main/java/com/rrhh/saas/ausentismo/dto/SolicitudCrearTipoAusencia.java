package com.rrhh.saas.ausentismo.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearTipoAusencia(
        @NotBlank String nombre,
        String descripcion,
        boolean requiereCertificado
) {
}
