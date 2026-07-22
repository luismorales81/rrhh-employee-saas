package com.rrhh.saas.nucleo.dto;

import java.util.List;

public record RespuestaPerfilUsuario(
        Long usuarioId,
        Long empresaId,
        String email,
        boolean esSuperAdmin,
        List<String> permisos
) {
}
