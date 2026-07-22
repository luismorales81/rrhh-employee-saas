package com.rrhh.saas.vacacion.dto;

import java.math.BigDecimal;

public record RespuestaSaldoVacaciones(
        Long empleadoId,
        String empleadoNombre,
        Integer anio,
        Integer antiguedadAnios,
        BigDecimal diasTotales,
        BigDecimal diasDisponibles,
        BigDecimal diasPendientes,
        BigDecimal diasTomados,
        Long politicaId,
        String politicaNombre
) {
}
