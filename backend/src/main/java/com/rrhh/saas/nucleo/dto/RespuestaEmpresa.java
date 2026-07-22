package com.rrhh.saas.nucleo.dto;

public record RespuestaEmpresa(
        Long id,
        String nombre,
        String razonSocial,
        String cuit,
        String email,
        String telefono,
        String estado
) {
}
