package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RespuestaEventoCalendarioVacaciones(
        Long solicitudId,
        Long empleadoId,
        String nombreEmpleado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoSolicitud estado,
        BigDecimal diasSolicitados
) {
}
