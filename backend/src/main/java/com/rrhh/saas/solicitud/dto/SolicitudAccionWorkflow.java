package com.rrhh.saas.solicitud.dto;

import com.rrhh.saas.solicitud.dominio.AccionPaso;
import jakarta.validation.constraints.NotNull;

public record SolicitudAccionWorkflow(
        @NotNull AccionPaso accion,
        String comentarios
) {
}
