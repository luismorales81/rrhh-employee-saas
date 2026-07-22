package com.rrhh.saas.solicitud.dto;

import com.rrhh.saas.solicitud.dominio.TipoPaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SolicitudCrearFlujoTrabajo(
        @NotBlank String nombre,
        String descripcion,
        List<SolicitudPasoFlujo> pasos
) {
    public record SolicitudPasoFlujo(
            @NotNull Integer orden,
            @NotBlank String nombre,
            @NotNull TipoPaso tipoPaso,
            Long rolId,
            Long usuarioId
    ) {
    }
}
