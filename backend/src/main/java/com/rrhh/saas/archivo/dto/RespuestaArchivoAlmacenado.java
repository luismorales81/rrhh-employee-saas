package com.rrhh.saas.archivo.dto;

public record RespuestaArchivoAlmacenado(
        Long id,
        String nombreOriginal,
        String tipoMime,
        long tamano,
        String hashSha256,
        String urlContenido
) {
}
