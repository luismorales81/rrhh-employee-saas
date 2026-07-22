package com.rrhh.saas.documento.dto;

public record RespuestaDocumento(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        String titulo,
        String descripcion,
        String version,
        String urlArchivo,
        String hashArchivo,
        boolean requiereFirma,
        boolean activo
) {
}
