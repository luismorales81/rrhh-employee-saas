package com.rrhh.saas.fichaje.dto;

import java.time.LocalDate;

public record RespuestaResumenFichajeEmpleado(
        Long empleadoId,
        String empleadoNombre,
        LocalDate fecha,
        int cantidadEntradas,
        int cantidadSalidas,
        int cantidadPendientes,
        String horasEstimadas
) {
}
