package com.rrhh.saas.activo.dto;

import java.time.LocalDate;

public record RespuestaAsignacionActivo(
        Long id,
        Long activoId,
        String nombreActivo,
        Long empleadoId,
        String nombreEmpleado,
        LocalDate fechaEntrega,
        LocalDate fechaDevolucion,
        String notas
) {
}
