package com.rrhh.saas.novedad.dto;

import com.rrhh.saas.novedad.dominio.TipoNovedadLiquidacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudCrearNovedadLiquidacion(
        @NotNull Long empleadoId,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String periodo,
        @NotNull TipoNovedadLiquidacion tipo,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal monto,
        @NotNull LocalDate fecha
) {
}
