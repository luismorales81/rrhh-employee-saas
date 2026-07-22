package com.rrhh.saas.nucleo.dto;

import java.util.List;

public record RespuestaUsuario(
        Long id,
        Long empresaId,
        String email,
        boolean activo,
        List<String> roles
) {
}
