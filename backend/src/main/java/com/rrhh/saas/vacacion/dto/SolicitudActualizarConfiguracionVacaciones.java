package com.rrhh.saas.vacacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SolicitudActualizarConfiguracionVacaciones(
        @Min(7) int diasMinimosFraccion,
        @Min(0) int anticipacionMinimaDias,
        boolean permiteSolicitudSinSaldo
) {
}
