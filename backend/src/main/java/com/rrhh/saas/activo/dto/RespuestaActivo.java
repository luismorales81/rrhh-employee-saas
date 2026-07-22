package com.rrhh.saas.activo.dto;

import com.rrhh.saas.activo.dominio.EstadoActivo;

public record RespuestaActivo(
        Long id,
        String nombre,
        String tipo,
        String numeroSerie,
        EstadoActivo estado
) {
}
