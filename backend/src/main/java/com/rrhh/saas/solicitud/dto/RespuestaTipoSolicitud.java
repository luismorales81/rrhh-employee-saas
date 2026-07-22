package com.rrhh.saas.solicitud.dto;

public record RespuestaTipoSolicitud(
        Long id,
        String nombre,
        String descripcion,
        Long flujoTrabajoId,
        boolean requiereAprobacion,
        boolean requiereAdjunto
) {
}
