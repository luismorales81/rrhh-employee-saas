package com.rrhh.saas.comun.seguridad;

import com.rrhh.saas.nucleo.seguridad.UsuarioAutenticado;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UtilidadContextoSeguridad {

    private UtilidadContextoSeguridad() {
    }

    public static boolean esSuperAdminPlataforma() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario.isEsSuperAdmin();
        }
        return false;
    }
}
