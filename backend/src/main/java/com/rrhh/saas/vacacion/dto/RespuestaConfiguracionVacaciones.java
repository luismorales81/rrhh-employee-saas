package com.rrhh.saas.vacacion.dto;

public record RespuestaConfiguracionVacaciones(
        int diasMinimosFraccion,
        int anticipacionMinimaDias,
        boolean permiteSolicitudSinSaldo,
        int diasMinimosFraccionEfectivos
) {
}
