package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RespuestaSolicitudVacaciones(
        Long solicitudId,
        Long solicitudVacacionesId,
        Long empleadoId,
        String empleadoNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal diasSolicitados,
        EstadoSolicitud estado,
        String motivo,
        LocalDate fechaInicioContrapropuesta,
        LocalDate fechaFinContrapropuesta,
        String motivoContrapropuesta,
        LocalDate fechaInicioAprobada,
        LocalDate fechaFinAprobada,
        List<String> advertencias
) {
    public RespuestaSolicitudVacaciones(
            Long solicitudId,
            Long solicitudVacacionesId,
            Long empleadoId,
            String empleadoNombre,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal diasSolicitados,
            EstadoSolicitud estado,
            String motivo) {
        this(solicitudId, solicitudVacacionesId, empleadoId, empleadoNombre,
                fechaInicio, fechaFin, diasSolicitados, estado, motivo,
                null, null, null, null, null, List.of());
    }
}
