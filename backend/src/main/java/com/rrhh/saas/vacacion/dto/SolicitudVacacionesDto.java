package com.rrhh.saas.vacacion.dto;

import com.rrhh.saas.solicitud.dominio.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudVacacionesDto(
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        String motivo
) {
}
