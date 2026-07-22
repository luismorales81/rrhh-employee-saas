package com.rrhh.saas.vacacion.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRechazarVacaciones(
        @NotBlank String motivoRechazo
) {
}
