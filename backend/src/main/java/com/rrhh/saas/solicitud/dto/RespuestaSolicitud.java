package com.rrhh.saas.solicitud.dto;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RespuestaSolicitud(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        Long tipoSolicitudId,
        String tipoSolicitudNombre,
        EstadoSolicitud estado,
        String motivo,
        Long instanciaFlujoId,
        Long aprobadorActualId,
        LocalDate fechaInicioVacaciones,
        LocalDate fechaFinVacaciones,
        Long solicitudVacacionesId,
        LocalDateTime createdAt
) {
}
