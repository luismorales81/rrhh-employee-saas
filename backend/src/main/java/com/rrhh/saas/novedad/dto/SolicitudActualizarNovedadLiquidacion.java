package com.rrhh.saas.novedad.dto;

import com.rrhh.saas.novedad.dominio.TipoNovedadLiquidacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudActualizarNovedadLiquidacion(
        TipoNovedadLiquidacion tipo,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal monto,
        LocalDate fecha
) {
}
