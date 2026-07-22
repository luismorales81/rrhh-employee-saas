package com.rrhh.saas.nucleo.dto;

import java.util.List;

public record RespuestaAutenticacion(
        String tokenAcceso,
        String tokenRefresco,
        Long usuarioId,
        Long empresaId,
        String empresaNombre,
        String email,
        boolean esSuperAdmin,
        List<String> permisos
) {
}
