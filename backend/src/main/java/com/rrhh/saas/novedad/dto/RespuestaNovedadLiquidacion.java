package com.rrhh.saas.novedad.dto;

import com.rrhh.saas.novedad.dominio.TipoNovedadLiquidacion;
import com.rrhh.saas.novedad.dominio.EstadoNovedadLiquidacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RespuestaNovedadLiquidacion(
        Long id,
        Long empleadoId,
        String nombreEmpleado,
        String periodo,
        TipoNovedadLiquidacion tipo,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal monto,
        LocalDate fecha,
        EstadoNovedadLiquidacion estado
) {
}
