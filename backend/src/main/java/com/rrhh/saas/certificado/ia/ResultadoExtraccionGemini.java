package com.rrhh.saas.certificado.ia;

import java.time.LocalDate;

/**
 * Resultado estructurado de la extracción Gemini/Vertex.
 */
public record ResultadoExtraccionGemini(
        String diagnostico,
        Integer reposoDias,
        String medicoNombre,
        String medicoMatricula,
        LocalDate fechaEmision,
        Float confianzaExtraccion,
        String error
) {
    public boolean tieneErrorDocumento() {
        return error != null && !error.isBlank();
    }

    public static ResultadoExtraccionGemini errorDocumento(String codigoError) {
        return new ResultadoExtraccionGemini(null, null, null, null, null, null, codigoError);
    }
}
