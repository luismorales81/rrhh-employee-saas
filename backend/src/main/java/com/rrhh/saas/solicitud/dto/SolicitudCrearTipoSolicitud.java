package com.rrhh.saas.solicitud.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCrearTipoSolicitud(
        @NotBlank String nombre,
        String descripcion,
        Long flujoTrabajoId,
        boolean requiereAprobacion,
        boolean requiereAdjunto
) {
}
