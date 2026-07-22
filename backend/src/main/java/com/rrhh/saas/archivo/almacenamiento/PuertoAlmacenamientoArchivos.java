package com.rrhh.saas.archivo.almacenamiento;

import java.io.InputStream;

/**
 * Puerto de almacenamiento de objetos (disco, MinIO, Cloudflare R2).
 * La clave sigue el formato empresa/{empresaId}/archivos/{uuid}{ext}.
 */
public interface PuertoAlmacenamientoArchivos {

    void guardar(String clave, InputStream contenido, long tamano, String tipoMime);

    InputStream leer(String clave);

    boolean existe(String clave);
}
