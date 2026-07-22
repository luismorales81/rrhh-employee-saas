package com.rrhh.saas.solicitud.dto;

import jakarta.validation.constraints.NotNull;

public record SolicitudCrearSolicitud(
        @NotNull Long tipoSolicitudId,
        String motivo
) {
}
