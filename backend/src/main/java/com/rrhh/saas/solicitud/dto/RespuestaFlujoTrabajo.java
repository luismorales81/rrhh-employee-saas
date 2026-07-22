package com.rrhh.saas.solicitud.dto;

import com.rrhh.saas.solicitud.dominio.TipoPaso;

import java.util.List;

public record RespuestaFlujoTrabajo(
        Long id,
        String nombre,
        String descripcion,
        boolean activo,
        List<RespuestaPasoFlujo> pasos
) {
    public record RespuestaPasoFlujo(
            Long id,
            int orden,
            String nombre,
            TipoPaso tipoPaso,
            Long rolId,
            Long usuarioId
    ) {
    }
}
