package com.rrhh.saas.comun.tenant;

public final class ContextoTenant {

    private static final ThreadLocal<Long> empresaId = new ThreadLocal<>();
    private static final ThreadLocal<Long> usuarioId = new ThreadLocal<>();

    private ContextoTenant() {
    }

    public static void establecerEmpresaId(Long id) {
        empresaId.set(id);
    }

    public static Long obtenerEmpresaId() {
        return empresaId.get();
    }

    public static void establecerUsuarioId(Long id) {
        usuarioId.set(id);
    }

    public static Long obtenerUsuarioId() {
        return usuarioId.get();
    }

    public static void limpiar() {
        empresaId.remove();
        usuarioId.remove();
    }
}
