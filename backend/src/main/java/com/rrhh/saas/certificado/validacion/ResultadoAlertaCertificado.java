package com.rrhh.saas.certificado.validacion;

import java.util.List;

/**
 * Resultado de evaluar reglas de auditoría médica sobre un certificado extraído.
 */
public record ResultadoAlertaCertificado(
        boolean disparaAlerta,
        boolean requiereAclaracion,
        boolean revisionPrioritaria,
        List<String> motivos
) {
    public static ResultadoAlertaCertificado sinAlertas() {
        return new ResultadoAlertaCertificado(false, false, false, List.of());
    }

    public static ResultadoAlertaCertificado aclaracion(String motivo) {
        return new ResultadoAlertaCertificado(false, true, false, List.of(motivo));
    }
}
