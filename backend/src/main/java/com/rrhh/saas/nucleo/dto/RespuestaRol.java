package com.rrhh.saas.nucleo.dto;

import java.util.List;

public record RespuestaRol(
        Long id,
        String nombre,
        String descripcion,
        List<String> permisos
) {
}
