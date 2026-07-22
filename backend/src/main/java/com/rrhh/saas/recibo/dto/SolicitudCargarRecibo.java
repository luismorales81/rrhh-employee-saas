package com.rrhh.saas.recibo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SolicitudCargarRecibo(
        @NotNull Long empleadoId,
        @NotBlank String periodo,
        @NotBlank String titulo,
        @NotNull Long archivoId,
        BigDecimal montoNeto,
        BigDecimal montoBruto
) {
}
