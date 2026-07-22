package com.rrhh.saas.comun.utilidad;

import jakarta.servlet.http.HttpServletRequest;

public final class UtilidadClienteHttp {

    private UtilidadClienteHttp() {
    }

    public static String obtenerDireccionIp(HttpServletRequest peticion) {
        if (peticion == null) {
            return null;
        }
        String encabezado = peticion.getHeader("X-Forwarded-For");
        if (encabezado != null && !encabezado.isBlank()) {
            return encabezado.split(",")[0].trim();
        }
        return peticion.getRemoteAddr();
    }

    public static String obtenerUserAgent(HttpServletRequest peticion) {
        if (peticion == null) {
            return "desconocido";
        }
        String agente = peticion.getHeader("User-Agent");
        return agente != null && !agente.isBlank() ? agente : "desconocido";
    }

    public static String detectarSistemaOperativo(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "desconocido";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "otro";
    }
}
