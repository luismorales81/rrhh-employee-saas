package com.rrhh.saas.nucleo.seguridad;

import java.util.List;

/**
 * Permisos de consola de plataforma (provisionamiento de tenants).
 * No incluye operación RRHH diaria; eso requiere modo soporte.
 */
public final class CatalogoPermisosSuperAdmin {

    private CatalogoPermisosSuperAdmin() {
    }

    public static List<String> todos() {
        return List.of(
                "empresa.crear", "empresa.ver", "empresa.editar", "empresa.desactivar",
                "usuario.crear", "usuario.ver",
                "rol.ver", "permiso.ver"
        );
    }
}
