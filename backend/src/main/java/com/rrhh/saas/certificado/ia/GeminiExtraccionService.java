package com.rrhh.saas.certificado.ia;

/**
 * Único punto del backend que conoce Gemini/Vertex AI.
 */
public interface GeminiExtraccionService {

    ResultadoExtraccionGemini extraer(byte[] archivo, String tipoMime, ConfiguracionVertexTenant config);
}
