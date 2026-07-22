package com.rrhh.saas.solicitud.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudActualizarTipoSolicitud(
        @NotBlank String nombre,
        String descripcion,
        Long flujoTrabajoId,
        Boolean requiereAprobacion,
        Boolean requiereAdjunto
) {
}
