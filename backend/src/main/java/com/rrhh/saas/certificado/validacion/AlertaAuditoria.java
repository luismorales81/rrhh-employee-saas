package com.rrhh.saas.certificado.validacion;

/**
 * Resultado de evaluar alertas de auditoría sobre un certificado extraído.
 */
public record AlertaAuditoria(
        boolean disparaAlerta,
        boolean requiereRevisionPrioritaria,
        boolean datosIncompletos,
        String motivo
) {
    public static AlertaAuditoria sinAlerta() {
        return new AlertaAuditoria(false, false, false, null);
    }

    public static AlertaAuditoria conAlerta(String motivo) {
        return new AlertaAuditoria(true, false, false, motivo);
    }

    public static AlertaAuditoria revisionPrioritaria(String motivo) {
        return new AlertaAuditoria(true, true, false, motivo);
    }

    public static AlertaAuditoria aclaracionRequerida(String motivo) {
        return new AlertaAuditoria(false, false, true, motivo);
    }
}
