package com.rrhh.saas.ausentismo.dto;

public record RespuestaTipoAusencia(
        Long id,
        String nombre,
        String descripcion,
        boolean requiereCertificado
) {
}
