package com.rrhh.saas.archivo.servicio;

import com.rrhh.saas.comun.excepcion.ExcepcionNegocio;

import java.util.Map;
import java.util.Set;

public final class ValidadorArchivo {

    private static final Set<String> TIPOS_MIME_PERMITIDOS = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "application/zip",
            "application/x-zip-compressed"
    );

    private static final Map<String, byte[]> FIRMAS_MAGICAS = Map.of(
            "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46},
            "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
    );

    private static final int LONGITUD_MAXIMA_NOMBRE = 255;

    private ValidadorArchivo() {
    }

    public static void validarTipoMimePermitido(String tipoMime) {
        if (tipoMime == null || tipoMime.isBlank()) {
            throw new ExcepcionNegocio("Tipo de archivo no permitido");
        }
        String normalizado = tipoMime.toLowerCase().split(";")[0].trim();
        if (!TIPOS_MIME_PERMITIDOS.contains(normalizado)) {
            throw new ExcepcionNegocio("Tipo de archivo no permitido: " + normalizado);
        }
    }

    public static void validarContenidoContraTipoMime(String tipoMime, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            return;
        }
        String normalizado = tipoMime.toLowerCase().split(";")[0].trim();
        byte[] firma = FIRMAS_MAGICAS.get(normalizado);
        if (firma == null) {
            return;
        }
        if (contenido.length < firma.length) {
            throw new ExcepcionNegocio("El contenido no coincide con el tipo declarado");
        }
        for (int i = 0; i < firma.length; i++) {
            if (contenido[i] != firma[i]) {
                throw new ExcepcionNegocio("El contenido no coincide con el tipo declarado");
            }
        }
    }

    public static String sanitizarNombreArchivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "archivo";
        }
        String limpio = nombre
                .replaceAll("[\\r\\n\"\\\\]", "_")
                .replaceAll("[^a-zA-Z0-9._\\- áéíóúÁÉÍÓÚñÑ]", "_")
                .trim();
        if (limpio.isEmpty()) {
            return "archivo";
        }
        if (limpio.length() > LONGITUD_MAXIMA_NOMBRE) {
            return limpio.substring(0, LONGITUD_MAXIMA_NOMBRE);
        }
        return limpio;
    }

    public static void validarLongitudBase64(String datosBase64, long tamanoMaximoBytes) {
        if (datosBase64 == null) {
            return;
        }
        String datos = datosBase64;
        if (datos.contains(",")) {
            datos = datos.substring(datos.indexOf(',') + 1);
        }
        long longitudMaximaBase64 = (tamanoMaximoBytes * 4) / 3 + 4;
        if (datos.length() > longitudMaximaBase64) {
            throw new ExcepcionNegocio("La imagen de firma supera el tamaño máximo permitido");
        }
    }
}
