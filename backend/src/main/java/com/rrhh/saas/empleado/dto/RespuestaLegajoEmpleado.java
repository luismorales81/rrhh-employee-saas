package com.rrhh.saas.empleado.dto;

import com.rrhh.saas.empleado.dominio.EstadoEmpleado;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RespuestaLegajoEmpleado(
        Long legajoId,
        Long empleadoId,
        String nombre,
        String apellido,
        String numeroEmpleado,
        String dni,
        LocalDate fechaIngreso,
        EstadoEmpleado estado,
        String estadoOnboarding,
        LocalDateTime fechaCompletadoOnboarding,
        String estadoOffboarding,
        LocalDateTime fechaCompletadoOffboarding,
        long documentosPendientes,
        long solicitudesAbiertas
) {
}
